package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
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
	private AtomicLong numberOfOutputMappingsProduced = new AtomicLong(0L);
	protected int numberOfRequestsIssued = 0;
	protected List<Long> requestDurationsInMS = new Vector<>(); // Vector is thread safe
	protected List<Integer> numOfSolMapsRetrievedPerReq = new Vector<>();

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
		log.debug( "Starting to execute multi-request for {}", serviceVar.toString() );

		final CompletableFuture<?>[] futures = new CompletableFuture[ fms.size() ];

		int i = 0;
		for ( final FederationMember fm : fms ) {
			// Issue the request via the federation access manager.
			final CompletableFuture<SolMapsResponse> f;
			try {
				f = ctx.getFederationAccessMgr().issueRequest(req, fm);
			}
			catch ( final FederationAccessException e ) {
				throw new ExecOpExecutionException("Issuing a request caused an exception.", e, this);
			}

			numberOfRequestsIssued++;

			// Create a response processor that shall handle the response
			// obtained via the request.
			final Consumer<SolMapsResponse> respProc;
			if ( serviceVarIsInPattern )
				respProc = new MyResponseProcessor2( fm.getServiceURI(), sink );
			else
				respProc = new MyResponseProcessor1( fm.getServiceURI(), sink );

			// Attach the response processor to the future for the request and
			// remember the future so that we can wait for its completion later.
			futures[i++] = f.thenAccept(respProc);
		}

		// Now we wait for all the futures to be completed. Note that this
		// needs to be done outside of the previous if-block because, even
		// if we had to switch into full-retrieval mode, we may have futures
		// from before we made that switch.
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
	}

	@Override
	public void resetStats() {
		super.resetStats();
//		timeAfterResponse = 0L;
//		solMapsRetrieved = 0L;
//		numberOfOutputMappingsProduced = 0L;
	}

	protected ExecutableOperatorStatsImpl createStats() {
		final ExecutableOperatorStatsImpl s = super.createStats();
//		s.put( "requestExecTime",                Long.valueOf( timeAtExecEnd - timeAfterResponse ) );
//		s.put( "responseProcTime",               Long.valueOf( timeAfterResponse - timeAtExecStart ) );
//		s.put( "solMapsRetrieved",               Long.valueOf( solMapsRetrieved ) );
//		s.put( "numberOfOutputMappingsProduced", Long.valueOf( numberOfOutputMappingsProduced ) );
		return s;
	}

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

			numberOfOutputMappingsProduced.addAndGet(cntOut);

			log.info("Retrieved {} solution mappings from the endpoint with service URI {}, and produced {} output solution mappings from them", cntIn, serviceURI, cntOut);
		}

		protected SolutionMapping tryToExtend( final SolutionMapping sm ) {
			final BindingBuilder bb = BindingBuilder.create( sm.asJenaBinding() );
			bb.add(serviceVar, serviceURI);
			return new SolutionMappingImpl( bb.build() );
		}
	}

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
