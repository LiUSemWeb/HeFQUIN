package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.http.Params;

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
       extends UnaryExecutableOpBaseWithBatching
{
	// Since this algorithm processes the input solution mappings
	// in parallel, we should use an input block size with which
	// we can leverage this parallelism. However, I am not sure
	// yet what a good value is; it probably depends on various
	// factors, including the load on the federation member and
	// the degree of parallelism in the FederationAccessManager.
	// Notice that this number is essentially the number of
	// requests issued in parallel (to the same endpoint!).
	public final static int DEFAULT_BATCH_SIZE = 5;

	protected final SPARQLGraphPattern pattern;
	protected final List<Var> paramVars;
	protected final WrappedRESTEndpoint fm;

	protected final Map<Node[], List<SolutionMapping>> cache = new HashMap<>();

	// statistics
	private long numberOfRequestsIssued = 0L;
	private long sumOfRequestExecutionTimes = 0L;
	private long sumOfResponseProcTimes = 0L;
	private long numberOfOutputMappingsProduced = 0L;
	private int numberOfDataConversionExceptions = 0;


	public ExecOpLookupJoinViaWrapperWithParamVars( final SPARQLGraphPattern pattern,
	                                                final List<Var> paramVars,
	                                                final WrappedRESTEndpoint fm,
	                                                final boolean collectExceptions,
	                                                final QueryPlanningInfo qpInfo ) {
		this( pattern, paramVars, fm,
		      DEFAULT_BATCH_SIZE,
		      collectExceptions, qpInfo );
	}

	public ExecOpLookupJoinViaWrapperWithParamVars( final SPARQLGraphPattern pattern,
	                                                final List<Var> paramVars,
	                                                final WrappedRESTEndpoint fm,
	                                                final int batchSize,
	                                                final boolean collectExceptions,
	                                                final QueryPlanningInfo qpInfo ) {
		super(batchSize, collectExceptions, qpInfo);

		assert pattern   != null;
		assert paramVars != null;
		assert fm        != null;

		assert paramVars.size() > 0;
		assert paramVars.size() == fm.getNumberOfParameters();

		this.pattern = pattern;
		this.paramVars = paramVars;
		this.fm = fm;
	}

	@Override
	protected void _processBatch( final List<SolutionMapping> input,
	                              final IntermediateResultElementSink sink,
	                              final ExecutionContext execCxt )
			throws ExecOpExecutionException
	{
		final Map<Node[], Set<SolutionMapping>> paramsForRequests = new HashMap<>();
		for ( final SolutionMapping sm : input ) {
			final Node[] paramValues = extractParamValues(sm);

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
				Set<SolutionMapping> solmaps = paramsForRequests.get(paramValues);
				if ( solmaps == null ) {
					solmaps = new HashSet<>();
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
		for ( Map.Entry<Node[], Set<SolutionMapping>> entry : paramsForRequests.entrySet() ) {
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
			final MyResponseProcessor respProc = new MyResponseProcessor( entry.getValue(), sink, this );
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
			_processBatch(input, sink, execCxt);
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
	protected Node[] extractParamValues( final SolutionMapping sm ) {
		final Binding solmap = sm.asJenaBinding();

		if ( solmap.size() < paramVars.size() ) return null;

		final Node[] result = new Node[ paramVars.size() ];

		int i = 0;
		final Iterator<RESTEndpoint.Parameter> itParamDecl = fm.getParameters().iterator();
		final Iterator<Var>                    itParamVar  = paramVars.iterator();

		while ( itParamDecl.hasNext() ) {
			final Var paramVar = itParamVar.next();
			final Node paramValueAsNode = solmap.get(paramVar);

			if ( paramValueAsNode == null ) return null;
			if ( ! paramValueAsNode.isLiteral() ) return null;

			final RESTEndpoint.Parameter paramDecl = itParamDecl.next();

			final RDFDatatype typeOfNode = paramValueAsNode.getLiteralDatatype();

			if ( ! paramDecl.getType().equals(typeOfNode) ) return null;

			result[i++] = paramValueAsNode;
		}

		return result;
	}

	/**
	 * Assumes that the number of parameter values in the given array is the
	 * same as the number of parameter declarations of the REST endpoint and
	 * that the datatypes of the values match the ones of the parameter
	 * declarations.
	 */
	protected RESTRequest createRequest( final Node[] paramValues ) {
		final Params params = Params.create();

		final Iterator<RESTEndpoint.Parameter> it = fm.getParameters().iterator();
		int i = 0;
		while ( it.hasNext() ) {
			final RESTEndpoint.Parameter paramDecl = it.next();
			final Node paramValueAsNode = paramValues[i++];
			final String paramValue = paramValueAsNode.getLiteralValue().toString();
			params.add( paramDecl.getName(), paramValue );
		}

		return new RESTRequestImpl( fm.getURL(), params );
	}


	// ---- helper classes -----

	protected class MyResponseProcessor implements Consumer<StringResponse>
	{
		protected final Set<SolutionMapping> solmaps;
		protected final IntermediateResultElementSink sink;
		protected final ExecutableOperator op;

		public MyResponseProcessor( final Set<SolutionMapping> solmaps,
		                            final IntermediateResultElementSink sink,
		                            final ExecutableOperator op ) {
			this.solmaps = solmaps;
			this.sink = sink;
			this.op = op;
		}

		@Override
		public void accept( final StringResponse response ) {
			final long time1 = System.currentTimeMillis();

			final String respData;
			try {
				respData = response.getResponseData();
			}
			catch ( final UnsupportedOperationDueToRetrievalError e ) {
				final ExecOpExecutionException ex = new ExecOpExecutionException( "Accessing the response caused an exception that indicates a data retrieval error (message: " + e.getMessage() + ").", e, op );
				recordExceptionCaughtDuringExecution( ex );
				return;
			}

			final Iterable<SolutionMapping> resutingSolMaps;
			try {
				resutingSolMaps = fm.evaluatePatternOverRDFView(pattern, respData);
			}
			catch ( final DataConversionException e ) {
				numberOfDataConversionExceptions++;
				final ExecOpExecutionException ex = new ExecOpExecutionException( "Converting the reponse of a REST request into RDF failed (message: " + e.getMessage() + ").", e, op );
				recordExceptionCaughtDuringExecution( ex );
				return;
			}


			join(resutingSolMaps, solmaps, sink);

			final long time2 = System.currentTimeMillis();

			sumOfRequestExecutionTimes += response.getRequestDuration().toMillis();
			sumOfResponseProcTimes += time2 - time1;
		}

		protected Iterable<SolutionMapping> extractSolMaps( final StringResponse response )
				throws UnsupportedOperationDueToRetrievalError {
			final String data = response.getResponseData();
			try {
				return fm.evaluatePatternOverRDFView(pattern, data);
			}
			catch ( final DataConversionException e ) {
				numberOfDataConversionExceptions++;
				throw new UnsupportedOperationDueToRetrievalError("Converting the reponse of a REST request into RDF failed.", e, null, fm);
			}
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
				numberOfOutputMappingsProduced++;
			}
		}
	}

	@Override
	public void resetStats() {
		super.resetStats();

		numberOfRequestsIssued = 0L;
		sumOfRequestExecutionTimes = 0L;
		sumOfResponseProcTimes = 0L;
		numberOfOutputMappingsProduced = 0L;
		numberOfDataConversionExceptions = 0;
	}

	@Override
	protected ExecutableOperatorStatsImpl createStats() {
		final ExecutableOperatorStatsImpl s = super.createStats();

		s.put( "numberOfRequestsIssued",   numberOfRequestsIssued );

		double avgRequestExecTime = Double.NaN;
		double avgResponseProcTimes = Double.NaN;
		if ( numberOfRequestsIssued > 0L ) {
			avgRequestExecTime = sumOfRequestExecutionTimes / numberOfRequestsIssued;
			avgResponseProcTimes = sumOfResponseProcTimes / numberOfRequestsIssued;
		}

		s.put( "avgRequestExecTime",    avgRequestExecTime );
		s.put( "avgResponseProcTimes",  avgResponseProcTimes );

		s.put( "numberOfDataConversionExceptions",  numberOfDataConversionExceptions );
		s.put( "numberOfOutputMappingsProduced",    numberOfOutputMappingsProduced );
		return s;
	}

}
