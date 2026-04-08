package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.datatypes.xsd.impl.RDFDirLangString;
import org.apache.jena.datatypes.xsd.impl.RDFLangString;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutableOperator;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecutableOperatorStatsImpl;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;
import se.liu.ida.hefquin.federation.access.FederationAccessException;
import se.liu.ida.hefquin.federation.access.RESTRequest;
import se.liu.ida.hefquin.federation.access.StringResponse;
import se.liu.ida.hefquin.federation.access.UnsupportedOperationDueToRetrievalError;
import se.liu.ida.hefquin.federation.access.impl.req.RESTRequestImpl;
import se.liu.ida.hefquin.federation.members.RESTEndpoint;
import se.liu.ida.hefquin.federation.members.WrappedRESTEndpoint;
import se.liu.ida.hefquin.federation.members.WrappedRESTEndpoint.DataConversionException;

public class ExecOpLookupJoinViaWrapperWithParamVars
       extends BaseForUnaryExecOpWithCollectedInput
{
	// Since this algorithm processes the input solution mappings
	// in parallel, we should use an input block size with which
	// we can leverage this parallelism. However, I am not sure
	// yet what a good value is; it probably depends on various
	// factors, including the load on the federation member and
	// the degree of parallelism in the FederationAccessManager.
	// Notice that this number is essentially the number of
	// requests issued in parallel (to the same endpoint!).
	public final static int DEFAULT_INPUT_BLOCK_SIZE = 5;

	protected final SPARQLGraphPattern pattern;
	protected final Map<RESTEndpoint.Parameter,Var> paramVars;
	protected final WrappedRESTEndpoint fm;

	protected final ConcurrentMap<Map<String,Node>, List<SolutionMapping>> cache = new ConcurrentHashMap<>();

	// statistics
	private long numberOfRequestsIssued = 0L;
	private AtomicLong numberOfRequestsFailed = new AtomicLong(0L);
	private List<Integer> errorCodesOfFailedRequests = Collections.synchronizedList( new ArrayList<>() );
	private AtomicLong sumOfRequestExecutionTimes = new AtomicLong(0L);
	private AtomicLong sumOfResponseProcTimes = new AtomicLong(0L);
	private AtomicLong numberOfOutputMappingsProduced = new AtomicLong(0L);
	private AtomicInteger numberOfDataConversionExceptions =new AtomicInteger(0);


	public ExecOpLookupJoinViaWrapperWithParamVars( final SPARQLGraphPattern pattern,
	                                                final Map<String,Var> paramVars,
	                                                final WrappedRESTEndpoint fm,
	                                                final boolean collectExceptions,
	                                                final QueryPlanningInfo qpInfo,
	                                                final boolean mayReduce ) {
		this( pattern, paramVars, fm,
		      DEFAULT_INPUT_BLOCK_SIZE,
		      collectExceptions, qpInfo, mayReduce );
	}

	public ExecOpLookupJoinViaWrapperWithParamVars( final SPARQLGraphPattern pattern,
	                                                final Map<String,Var> paramVars,
	                                                final WrappedRESTEndpoint fm,
	                                                final int minimumInputBlockSize,
	                                                final boolean collectExceptions,
	                                                final QueryPlanningInfo qpInfo,
	                                                final boolean mayReduce ) {
		super(minimumInputBlockSize, collectExceptions, qpInfo, mayReduce);

		assert pattern   != null;
		assert paramVars != null;
		assert fm        != null;

		assert paramVars.size() > 0;
		assert paramVars.size() <= fm.getNumberOfParameters();

		this.pattern = pattern;
		this.fm = fm;

		this.paramVars = new HashMap<>();
		for ( final Map.Entry<String,Var> e : paramVars.entrySet() ) {
			final String paramVarName = e.getKey();
			final Var paramVar = e.getValue();

			final RESTEndpoint.Parameter paramDecl = fm.getParameterByName(paramVarName);
			if ( paramDecl == null )
				throw new IllegalArgumentException("Unknown parameter name (" + paramVarName + ")");

			this.paramVars.put(paramDecl, paramVar);
		}
	}

	@Override
	protected void _processCollectedInput( final List<SolutionMapping> input,
	                              final IntermediateResultElementSink sink,
	                              final ExecutionContext execCxt )
			throws ExecOpExecutionException
	{
		final Map<Map<String,Node>, List<SolutionMapping>> paramsForRequests = new HashMap<>();
		for ( final SolutionMapping sm : input ) {
			final Map<String,Node> paramValues = extractParamValues(sm);

			// If we could not extract parameter values from the
			// current solution mapping, then this solution mapping
			// needs to be ignored.
			if ( paramValues == null )
				continue;

			// If we have already seen the current parameter values for
			// an earlier batch of input solution mappings, then ...
			final List<SolutionMapping> cachedResult = cache.get(paramValues);
			if ( cachedResult != null ) {
				// ... we can join the current solution mapping with the
				// result that has been cached earlier.
				join(cachedResult, sm, sink);
			}
			else {
				// If we have not already seen the current parameter values
				// for an earlier batch, then remember them, together with
				// the current solution mapping, for further processing
				// after this for loop.
				List<SolutionMapping> solmaps = paramsForRequests.get(paramValues);
				if ( solmaps == null ) {
					solmaps = new ArrayList<>();
					paramsForRequests.put(paramValues, solmaps);
				}
				solmaps.add(sm);
			}
		}

		// If the previous loop did not end up collecting extracted
		// parameter values for further processing, we can stop here.
		if ( paramsForRequests.isEmpty() ) return;

		// For each combination of extracted parameter values that the
		// previous loop has collected for further processing, we now
		// issue a request to the REST endpoint.
		final CompletableFuture<?>[] futures = new CompletableFuture[ paramsForRequests.size() ];
		int i = 0;
		for ( Map.Entry<Map<String,Node>, List<SolutionMapping>> entry : paramsForRequests.entrySet() ) {
			// issue a request based on the current solution mapping
			final RESTRequest req = createRequest( entry.getKey() );

			final CompletableFuture<StringResponse> f;
			try {
				f = execCxt.getFederationAccessMgr().issueRequest(req, fm);
			}
			catch ( final FederationAccessException e ) {
				throw new ExecOpExecutionException("Issuing a request caused an exception.", e, this);
			}

			numberOfRequestsIssued++;

			// attach the processing of the response obtained for the request
			final MyResponseProcessor respProc = new MyResponseProcessor(
					entry.getKey(),entry.getValue(), sink, this );
			futures[i++] = f.thenAccept(respProc);
		}

		// Wait for all the futures to be completed.
		try {
			CompletableFuture.allOf(futures).get();
		}
		catch ( final InterruptedException e ) {
			throw new ExecOpExecutionException("interruption of the futures that perform the requests and process the responses", e, this);
		}
		catch ( final ExecutionException e ) {
			throw new ExecOpExecutionException("The execution of the futures that perform the requests and process the responses caused an exception.", e, this);
		}
	}

	@Override
	protected void _concludeExecution( final List<SolutionMapping> input,
	                                   final IntermediateResultElementSink sink,
	                                   final ExecutionContext execCxt )
			throws ExecOpExecutionException
	{
		if ( input != null && ! input.isEmpty() ) {
			_processCollectedInput(input, sink, execCxt);
		}
	}

	/**
	 * Returns the values that the given solution mapping has for the
	 * parameter variables, but only if the solution mapping does indeed
	 * have bindings for all parameter variables and the values bound to
	 * these variables are literals of the correct datatypes (as specified
	 * in the parameter declaration of the REST endpoint). Otherwise,
	 * {@code null} is returned.
	 *
	 * @param sm - the solution mapping from which the parameter values
	 *             are to be extracted
	 * @return the parameter values (in the form of a Jena {@link Node}
	 *         objects) or {@code null}
	 */
	protected Map<String, Node> extractParamValues( final SolutionMapping sm ) {
		final Binding solmap = sm.asJenaBinding();
		if ( solmap.size() < paramVars.size() ) return null;

		final Map<String, Node> result = new HashMap<>();

		for ( final Map.Entry<RESTEndpoint.Parameter,Var> e : paramVars.entrySet() ) {
			final RESTEndpoint.Parameter paramDecl = e.getKey();
			final Var paramVar = e.getValue();

			final Node paramValueAsNode = solmap.get(paramVar);
			if ( paramValueAsNode == null ) return null;
			if ( ! paramValueAsNode.isLiteral() ) return null;

			// Check that the datatype of the literal equals
			// the datatype expected for the parameter.
			final RDFDatatype typeOfNode = paramValueAsNode.getLiteralDatatype();
			if ( paramDecl.getType().equals(XSDDatatype.XSDstring) ) {
				// Special case: If the expected datatype is xsd:string but
				// the actual one is rdf:langString or rdf:dirLangString,
				// then the value is still accepted.
				if (    ! typeOfNode.equals(XSDDatatype.XSDstring)
				     && ! RDFLangString.isRDFLangString(typeOfNode)
				     && ! RDFDirLangString.isRDFDirLangString(typeOfNode) ) {
					return null;
				}
			}
			else if ( ! paramDecl.getType().equals(typeOfNode) ) {
				return null;
			}

			result.put( paramDecl.getName(), paramValueAsNode );
		}

		return result;
	}

	/**
	 * Assumes that the number of parameter values in the given array is the
	 * same as the number of parameter declarations of the REST endpoint and
	 * that the datatypes of the values match the ones of the parameter
	 * declarations.
	 */
	protected RESTRequest createRequest( final Map<String,Node> paramValues ) {
		final Map<String,String> params = paramValues.entrySet().stream().collect(
				HashMap::new,
				(m, e) -> m.put( e.getKey(), e.getValue().getLiteralValue().toString() ),
				HashMap::putAll );
		return new RESTRequestImpl( fm.getURLTemplate(), params );
	}


	// ---- helper classes -----

	protected class MyResponseProcessor implements Consumer<StringResponse>
	{
		protected final Map<String,Node> paramValues;
		protected final List<SolutionMapping> solmaps;
		protected final IntermediateResultElementSink sink;
		protected final ExecutableOperator op;

		public MyResponseProcessor( final Map<String,Node> paramValues,
		                            final List<SolutionMapping> solmaps,
		                            final IntermediateResultElementSink sink,
		                            final ExecutableOperator op ) {
			this.paramValues = paramValues;
			this.solmaps = solmaps;
			this.sink = sink;
			this.op = op;
		}

		@Override
		public void accept( final StringResponse response ) {
			// If the given response captures the fact that an error was
			// returned for the corresponding request, then the given input
			// solution mappings (see 'solmaps') can be dropped and, thus,
			// we can immediately stop at this point.
			if ( response.isError() ) {
				numberOfRequestsFailed.incrementAndGet();
				errorCodesOfFailedRequests.add( response.getErrorStatusCode() );
				return;
			}

			final long time1 = System.currentTimeMillis();

			final String respData;
			try {
				respData = response.getResponseData();
			}
			catch ( final UnsupportedOperationDueToRetrievalError e ) {
				// We should never end up here because we have explicitly
				// checked for a potential error before.
				throw new IllegalStateException("Unexpected exception at this point.", e);
			}

			final List<SolutionMapping> resultingSolMaps;
			try {
				resultingSolMaps = fm.evaluatePatternOverRDFView(pattern, respData);
			}
			catch ( final DataConversionException e ) {
				numberOfDataConversionExceptions.incrementAndGet();
				final ExecOpExecutionException ex = new ExecOpExecutionException( "Converting the reponse of a REST request into RDF failed (message: " + e.getMessage() + ").", e, op );
				recordExceptionCaughtDuringExecution( ex );
				return;
			}

			join(resultingSolMaps, solmaps, sink);

			final List<SolutionMapping> x = cache.putIfAbsent( paramValues,
			                                                   resultingSolMaps );
			if ( x != null )
				throw new IllegalStateException("Overwriting in the cache.");

			final long time2 = System.currentTimeMillis();

			sumOfRequestExecutionTimes.addAndGet( response.getRequestDuration().toMillis() );
			sumOfResponseProcTimes.addAndGet( time2 - time1 );
		}
	}

	/**
	 * Joins the given sets of solution mappings and sends the result to
	 * the given sink.
	 *
	 * @param solmaps1
	 * @param solmaps2
	 * @param sink
	 */
	protected void join( final Iterable<SolutionMapping> solmaps1,
	                     final Iterable<SolutionMapping> solmaps2,
	                     final IntermediateResultElementSink sink ) {
		for ( final SolutionMapping sm : solmaps1 ) {
			join( solmaps2, sm, sink );
		}
	}

	/**
	 * Joins the given set of solution mappings with a singleton set that
	 * contains the single given solution mapping, and sends the result to
	 * the given sink.
	 *
	 * @param solmaps
	 * @param sm
	 * @param sink
	 */
	protected void join( final Iterable<SolutionMapping> solmaps,
	                     final SolutionMapping sm,
	                     final IntermediateResultElementSink sink ) {
		for ( final SolutionMapping sm2 : solmaps ) {
			if ( SolutionMappingUtils.compatible(sm, sm2) ) {
				final SolutionMapping out = SolutionMappingUtils.merge(sm, sm2);
				sink.send(out);
				numberOfOutputMappingsProduced.incrementAndGet();
			}
		}
	}

	@Override
	public void resetStats() {
		super.resetStats();

		numberOfRequestsIssued = 0L;
		numberOfRequestsFailed.set(0L);
		errorCodesOfFailedRequests.clear();
		sumOfRequestExecutionTimes.set(0L);
		sumOfResponseProcTimes.set(0L);
		numberOfOutputMappingsProduced.set(0L);
		numberOfDataConversionExceptions.set(0);
	}

	@Override
	protected ExecutableOperatorStatsImpl createStats() {
		final ExecutableOperatorStatsImpl s = super.createStats();

		s.put( "numberOfRequestsIssued",   numberOfRequestsIssued );
		s.put( "numberOfRequestsFailed",   numberOfRequestsFailed );
		s.put( "errorCodesOfFailedRequests",   errorCodesOfFailedRequests );

		double avgRequestExecTime = Double.NaN;
		double avgResponseProcTimes = Double.NaN;
		if ( numberOfRequestsIssued > 0L ) {
			avgRequestExecTime = sumOfRequestExecutionTimes.get() / numberOfRequestsIssued;
			avgResponseProcTimes = sumOfResponseProcTimes.get() / numberOfRequestsIssued;
		}

		s.put( "avgRequestExecTime",    avgRequestExecTime );
		s.put( "avgResponseProcTimes",  avgResponseProcTimes );

		s.put( "numberOfDataConversionExceptions",  numberOfDataConversionExceptions );
		s.put( "numberOfOutputMappingsProduced",    numberOfOutputMappingsProduced );
		return s;
	}

}
