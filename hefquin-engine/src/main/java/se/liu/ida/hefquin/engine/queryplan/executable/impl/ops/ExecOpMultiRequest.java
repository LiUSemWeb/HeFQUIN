package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.impl.SolutionMappingImpl;
import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecutableOperatorStatsImpl;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContextExt;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.FederationAccessException;
import se.liu.ida.hefquin.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.federation.access.UnsupportedOperationDueToRetrievalError;

public class ExecOpMultiRequest extends NullaryExecutableOpBase
{
	private static final Logger log = LoggerFactory.getLogger( ExecOpMultiRequest.class );

	protected final SPARQLRequest req;
	protected final Var serviceVar;
	protected final Set<FederationMember> fms;
	protected final boolean serviceVarIsInPattern;

	// statistics
	protected int numberOfRequestsIssued = 0;
	// (access to any of the following three statistics must be
	//  synchronized on the first of them, requestDurationsInMS)
	private List<Long> requestDurationsInMS = new ArrayList<>();
	private List<Integer> numOfSolMapsRetrievedPerReq = new ArrayList<>();
	private long numberOfOutputMappingsProduced = 0L;

	public ExecOpMultiRequest( final SPARQLRequest req,
	                           final Var serviceVar,
	                           final Set<FederationMember> fms,
	                           final boolean collectExceptions,
	                           final QueryPlanningInfo qpInfo ) {
		super( req.getDistinctRequired(), collectExceptions, qpInfo );

		assert req != null;
		assert serviceVar != null;
		assert fms != null;

		this.req = req;
		this.serviceVar = serviceVar;
		this.fms = fms;

		final ExpectedVariables expVars = req.getQueryPattern().getExpectedVariables();
		serviceVarIsInPattern =    expVars.getCertainVariables().contains(serviceVar)
		                        || expVars.getPossibleVariables().contains(serviceVar);

		log.debug( "Initialized ExecOpMultiRequest for {}", serviceVar.toString() );
	}

	@Override
	protected void _execute( final IntermediateResultElementSink sink,
	                         final QueryProcContextExt ctx )
			throws ExecOpExecutionException {
		log.debug( "Starting to execute the multi-request operator for {}", serviceVar.toString() );

		// Create an array to collect the CompletableFuture:s for all
		// the requests that we are going to issue (which will be one
		// per federation member covered by this operator).
		final CompletableFuture<?>[] futures = new CompletableFuture[ fms.size() ];

		int i = 0;
		for ( final FederationMember fm : fms ) {
			// For every federation member covered by this operator:
			// i) issue the request of this operator via the federation
			// access manager, ...
			final CompletableFuture<SolMapsResponse> f;
			try {
				f = ctx.getFederationAccessMgr().issueRequest(req, fm);
			}
			catch ( final FederationAccessException e ) {
				throw new ExecOpExecutionException("Issuing a request caused an exception.", e, this);
			}

			numberOfRequestsIssued++;

			// ... ii) create a response processor that shall handle the
			// response obtained via the request issued in the previous
			// step, and ...
			final Consumer<SolMapsResponse> respProc;
			if ( serviceVarIsInPattern )
				respProc = new MyResponseProcessor2( fm.getServiceURI(), sink );
			else
				respProc = new MyResponseProcessor1( fm.getServiceURI(), sink );

			// ... iii) attach the response processor to the future for
			// the request and remember the future so that we can wait
			// for its completion later.
			futures[i++] = f.thenAccept(respProc);
		}

		log.debug( "All requests for the multi-request operator for {} are issued; waiting for them to complete now.", serviceVar.toString() );

		// Now we simply wait for all the futures to be completed. Once
		// they are, the execution of this operator is finished.
		final CompletableFuture<?> combinedFuture = CompletableFuture.allOf(futures);
		try {
			combinedFuture.get();
		}
		catch ( final InterruptedException e ) {
			throw new ExecOpExecutionException("Interruption of the futures that perform the requests and process the responses", e, this);
		}
		catch ( final ExecutionException e ) {
			throw new ExecOpExecutionException("The execution of the futures that perform the requests and process the responses caused an exception.", e, this);
		}

		log.debug( "Execution of the multi-request operator for {} is finished.", serviceVar.toString() );
	}

	@Override
	public void resetStats() {
		super.resetStats();

		numberOfRequestsIssued = 0;

		synchronized (requestDurationsInMS) {
			requestDurationsInMS.clear();
			numOfSolMapsRetrievedPerReq.clear();
			numberOfOutputMappingsProduced = 0L;
		}
	}

	protected ExecutableOperatorStatsImpl createStats() {
		final ExecutableOperatorStatsImpl s = super.createStats();

		final double avgRequestDurationInMS;
		long minRequestDurationInMS = Long.MAX_VALUE;
		long maxRequestDurationInMS = Long.MIN_VALUE;
		final double avgNumOfSolMapsRetrievedPerReq;
		int minNumOfSolMapsRetrievedPerReq = Integer.MAX_VALUE;
		int maxNumOfSolMapsRetrievedPerReq = Integer.MIN_VALUE;
		final long totalNumOfSolMapsRetrieved;
		final long outputSize;
		synchronized (requestDurationsInMS) {
			long sumRequestDuration = 0L;
			for ( final long x : requestDurationsInMS ) {
				sumRequestDuration += x;
				if ( x < minRequestDurationInMS ) minRequestDurationInMS = x;
				if ( x > maxRequestDurationInMS ) maxRequestDurationInMS = x;
			}

			long sumSolMapsRetrieved = 0L;
			for ( final int x : numOfSolMapsRetrievedPerReq ) {
				sumSolMapsRetrieved += x;
				if ( x < minNumOfSolMapsRetrievedPerReq ) minNumOfSolMapsRetrievedPerReq = x;
				if ( x > maxNumOfSolMapsRetrievedPerReq ) maxNumOfSolMapsRetrievedPerReq = x;
			}

			avgRequestDurationInMS = sumRequestDuration / requestDurationsInMS.size();
			avgNumOfSolMapsRetrievedPerReq = sumSolMapsRetrieved / numOfSolMapsRetrievedPerReq.size();
			totalNumOfSolMapsRetrieved = sumSolMapsRetrieved;
			outputSize = numberOfOutputMappingsProduced;
		}

		s.put( "numberOfRequestsIssued",          Integer.valueOf(numberOfRequestsIssued) );
		s.put( "avgRequestDurationInMS",          Double.valueOf(avgRequestDurationInMS) );
		s.put( "minRequestDurationInMS",          Long.valueOf(minRequestDurationInMS) );
		s.put( "maxRequestDurationInMS",          Long.valueOf(maxRequestDurationInMS) );
		s.put( "avgNumOfSolMapsRetrievedPerReq",  Double.valueOf(avgNumOfSolMapsRetrievedPerReq) );
		s.put( "minNumOfSolMapsRetrievedPerReq",  Integer.valueOf(minNumOfSolMapsRetrievedPerReq) );
		s.put( "maxNumOfSolMapsRetrievedPerReq",  Integer.valueOf(maxNumOfSolMapsRetrievedPerReq) );
		s.put( "totalNumOfSolMapsRetrieved",      Long.valueOf(totalNumOfSolMapsRetrieved) );
		s.put( "numberOfOutputMappingsProduced",  Long.valueOf(outputSize) );

		return s;
	}

	/**
	 * This response processor can be used for cases in which the service
	 * variable of this operator is not in the request of this operator
	 * and, thus, does not show up in the solution mappings obtained via
	 * this request.
	 */
	protected class MyResponseProcessor1 implements Consumer<SolMapsResponse> {
		protected final Node serviceURI;
		protected final IntermediateResultElementSink sink;

		public MyResponseProcessor1( final String serviceURI,
		                             final IntermediateResultElementSink sink ) {
			this( NodeFactory.createURI(serviceURI), sink );
		}

		public MyResponseProcessor1( final Node serviceURI,
		                             final IntermediateResultElementSink sink ) {
			this.serviceURI = serviceURI;
			this.sink = sink;
		}

		@Override
		public void accept( final SolMapsResponse response ) {
			log.info("Received response from endpoint with service URI {}", serviceURI);

			final Iterable<SolutionMapping> solMaps;
			try {
				solMaps = response.getResponseData();
			}
			catch ( final UnsupportedOperationDueToRetrievalError e ) {
				recordException( "Accessing the response caused an exception that indicates a data retrieval error (message: " + e.getMessage() + ").", e );
				return;
			}

			int cntIn = 0;
			int cntOut = 0;
			for ( final SolutionMapping sm : solMaps ) {
				cntIn++;
				final SolutionMapping sm2 = tryToExtend(sm);
				if ( sm2 != null ) {
					sink.send(sm2);
					cntOut++;
				}
			}

			synchronized (requestDurationsInMS) {
				requestDurationsInMS.add( response.getRequestDuration().toMillis() );
				numOfSolMapsRetrievedPerReq.add(cntIn);
				numberOfOutputMappingsProduced += cntOut;
			}

			log.info("Retrieved {} solution mappings from the endpoint with service URI {}, and produced {} output solution mappings from them", cntIn, serviceURI, cntOut);
		}

		protected SolutionMapping tryToExtend( final SolutionMapping sm ) {
			final BindingBuilder bb = BindingBuilder.create( sm.asJenaBinding() );
			bb.add(serviceVar, serviceURI);
			return new SolutionMappingImpl( bb.build() );
		}
	}

	/**
	 * This response processor can be used for cases in which the service
	 * variable of this operator is also in the request of this operator,
	 * which means that the solution mappings obtained via this request
	 * may already have a binding for this variable and, thus, need to
	 * be checked for compatibility.
	 */
	protected class MyResponseProcessor2 extends MyResponseProcessor1 {
		public MyResponseProcessor2( final String serviceURI,
		                             final IntermediateResultElementSink sink ) {
			super(serviceURI, sink);
		}

		public MyResponseProcessor2( final Node serviceURI,
		                             final IntermediateResultElementSink sink ) {
			super(serviceURI, sink);
		}

		@Override
		protected SolutionMapping tryToExtend( final SolutionMapping sm ) {
			final Node n = sm.asJenaBinding().get(serviceVar);
			if ( n == null )
				return super.tryToExtend(sm);
			else if ( n.equals(serviceURI) )
				return sm;
			else
				return null;
		}
	}

	protected void recordException( final String msg,
	                                final Exception cause ) {
		final ExecOpExecutionException e = new ExecOpExecutionException(msg, cause, this);
		recordExceptionCaughtDuringExecution(e);
	}
}
