package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.ExprVar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.NullaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.BaseForExecOpParallelBindJoin.MyResponseProcessor;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContextExt;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.federation.access.UnsupportedOperationDueToRetrievalError;
import se.liu.ida.hefquin.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.federation.members.SPARQLEndpoint;

public class ExecOpMultiBindJoin
       extends BaseForExecOpParallelBindJoin<SPARQLGraphPattern,
                                             SPARQLRequest,
                                             SolMapsResponse>
{
	private static final Logger log = LoggerFactory.getLogger( ExecOpMultiBindJoin.class );

	protected final Var serviceVar;
	protected final Set<FederationMember> fms;

	protected final boolean serviceVarIsInPattern;

	public ExecOpMultiBindJoin( final SPARQLGraphPattern pattern,
	                            final Var serviceVar,
	                            final Set<FederationMember> fms,
	                            final ExpectedVariables inputVars,
	                            final boolean useOuterJoinSemantics,
	                            final boolean mayReduce,
	                            final int batchSize,
	                            final boolean collectExceptions,
	                            final QueryPlanningInfo qpInfo ) {
		super( pattern,
		       pattern.getAllMentionedVariables(),
		       inputVars,
		       useOuterJoinSemantics,
		       mayReduce,
		       batchSize,
		       collectExceptions,
		       qpInfo );

		assert serviceVar != null;
		assert ! fms.isEmpty();

		this.serviceVar = serviceVar;
		this.fms = fms;

		final ExpectedVariables expVars = pattern.getExpectedVariables();
		serviceVarIsInPattern =    expVars.getCertainVariables().contains(serviceVar)
		                        || expVars.getPossibleVariables().contains(serviceVar);

		log.debug( "Initialized ExecOpMultiBindJoin for {}", serviceVar.toString() );
	}

	@Override
	protected void initiateProcessingOfRequest( final SPARQLRequest req,
	                                            final List<SolutionMapping> solMapsCoveredByBatch,
	                                            final IntermediateResultElementSink sink,
	                                            final QueryProcContextExt ctx ) {
		for ( final FederationMember fm : fms ) {
			initiateProcessingOfRequest(req, fm, solMapsCoveredByBatch, sink, ctx);
		}
	}


	protected void initiateProcessingOfRequest( final SPARQLRequest req,
	                                            final FederationMember fm,
	                                            List<SolutionMapping> solMapsCoveredByBatch,
	                                            final IntermediateResultElementSink sink,
	                                            final QueryProcContextExt ctx ) {
		// Issue the request via the federation access manager.
		final CompletableFuture<SolMapsResponse> f;
		try {
			f = ctx.getFederationAccessMgr().issueRequest(req, fm);
		}
		catch ( final Exception e ) {
			// Not strictly necessary, but doesn't hurt either.
			final String msg = "Issuing a request at the federation member " +
					"with the service URI " + fm.getServiceURI() + "during " +
					"the execution of a multi-bind-join caused an exception " +
					"(type: " + e.getClass().getName() + ") with the " +
					"following message: " + e.getMessage();
			// Record the exception only (instead of throwing it), to make
			// sure that we also get to issue the requests for the other
			// federation members.
			recordException(msg, e);
			return;
		}

		numberOfRequestsIssued++;

		// Create a response processor that shall handle the response
		// obtained via the bind-join request (namely, joining it with
		// the solution mappings covered by the current batch).
// TODO: We will probably need a different response processor here,
// one that also covers the service variable---or can we bind that
// already beforehand? We certainly need to extend the pre-processing
// in the case the the service variable is in the variables of the
// pattern!
		final MyResponseProcessor respProc = new MyResponseProcessor( solMapsCoveredByBatch,
		                                                              sink,
		                                                              fm );

		// Attach the response processor to the future for the request and
		// remember the future so that we can wait for its completion later.
		futures.add( f.thenAccept(respProc) );
	}

	@Override
	protected ExecOpExecutionException createExceptionIfWaitingFailed( final InterruptedException e ) {
		final String msg = "Waiting for the requests of this multi-bind-join" +
				"was interrupted with the following message: " + e.getMessage();
		return new ExecOpExecutionException(msg, e, this);
	}

	@Override
	protected ExecOpExecutionException createExceptionIfWaitingFailed( final ExecutionException e ) {
		final String msg = "Processing the requests of this multi-bind-join " +
				"caused an exception with the following message: " + e.getMessage();
		return new ExecOpExecutionException(msg, e, this);
	}

	@Override
	protected Iterable<SolutionMapping> extractSolMaps( final SolMapsResponse response )
			throws UnsupportedOperationDueToRetrievalError {
		return response.getResponseData();
	}

	// ------- functionality for full-retrieval mode ------

	@Override
	protected void obtainFullResult( final IntermediateResultElementSink sink,
	                                 final QueryProcContextExt ctx )
			throws ExecOpExecutionException
	{
		for ( final FederationMember fm : fms ) {
// TODO: change the following line---we need to ingest the binding
// for the service variable if that variable is in 'query'
			final SPARQLRequest req = new SPARQLRequestImpl(query, null, mayReduce);
		}
		final NullaryExecutableOp reqOp = createExecutableReqOpForAll();
		try {
			reqOp.execute(sink, ctx);
		}
		catch ( final ExecOpExecutionException e ) {
			final String msg = "Executing a request operator used by " +
					"this bind join caused an exception with the " +
					"following message: " + e.getMessage();
			throw new ExecOpExecutionException(msg, e, this);
		}
	}

	@Override
	protected NullaryExecutableOp createExecutableReqOpForAll() {
		log.debug( "Creating full-retrieval SPARQL request for endpoint with service URI {}", fm.getServiceURI() );

		
		return new ExecOpRequestSPARQL<>(req, fm, this.mayReduce, false, null);
	}


	// ------- helper functions ------

	protected void recordException( final String msg,
	                                final Exception cause ) {
		final ExecOpExecutionException e = new ExecOpExecutionException(msg, cause, this);
		recordExceptionCaughtDuringExecution(e);
	}
}
