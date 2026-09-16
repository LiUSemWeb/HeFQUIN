package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.jena.sparql.core.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.Query;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutableOperatorStats;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.NullaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecutableOperatorStatsImpl;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContextExt;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.federation.access.DataRetrievalResponse;

public abstract class BaseForExecOpParallelBindJoinWithSingleFedMember<
                                       QueryType extends Query,
                                       MemberType extends FederationMember,
                                       ReqType extends DataRetrievalRequest,
                                       RespType extends DataRetrievalResponse<?> >
       extends BaseForExecOpParallelBindJoin<QueryType, ReqType, RespType>
{
	private static final Logger log = LoggerFactory.getLogger( BaseForExecOpParallelBindJoinWithSingleFedMember.class );

	protected final MemberType fm;

	// statistics
	protected ExecutableOperatorStats statsOfFullRetrievalReqOp = null;

	public BaseForExecOpParallelBindJoinWithSingleFedMember(
			final QueryType query,
			final Set<Var> varsInQuery,
			final MemberType fm,
			final ExpectedVariables inputVars,
			final boolean useOuterJoinSemantics,
			final boolean mayReduce,
			final int batchSize,
			final boolean collectExceptions,
			final QueryPlanningInfo qpInfo ) {
		super( query, varsInQuery, inputVars, useOuterJoinSemantics, mayReduce, batchSize, collectExceptions, qpInfo );

		assert fm != null;
		this.fm = fm;

		log.info( "Initialized (parallel) bind-join operator for endpoint with service URI {}, batchSize={}, outerJoinSemantics={}, joinVars={}, allJoinVarsCertain={}",
		          fm.getServiceURI(),
		          batchSize,
		          useOuterJoinSemantics,
		          varsInQuery,
		          allJoinVarsAreCertain );
	}

	/**
	 * Issues the given bind-join request.
	 * The {@link CompletableFuture}s obtained for issuing the request is
	 * connected to a {@link MyResponseProcessor} that will handle the
	 * processing of the response (joining the solution mappings from
	 * the response with the solution mappings covered by the
	 * corresponding batch.
	 */
	@Override
	protected void initiateProcessingOfRequest( final ReqType req,
	                                            final List<SolutionMapping> solMapsCoveredByBatch,
	                                            final IntermediateResultElementSink sink,
	                                            final QueryProcContextExt ctx )
			throws ExecOpExecutionException
	{
		// Issue the request via the federation access manager.
		final CompletableFuture<RespType> f;
		try {
			f = ctx.getFederationAccessMgr().issueRequest(req, fm);
		}
		catch ( final Exception e ) {
			// Not strictly necessary, but doesn't hurt either.
			final String msg = "Issuing a request during the execution of " +
					"a bind join at the federation member with the service" +
					"URI " + fm.getServiceURI() + " caused an exception " +
					"(type: " + e.getClass().getName() + ") with the " +
					"following message: " + e.getMessage();
			throw new ExecOpExecutionException(msg, e, this);
		}

		numberOfRequestsIssued++;

		// Create a response processor that shall handle the response
		// obtained via the bind-join request (namely, joining it with
		// the solution mappings covered by the current batch).
		final MyResponseProcessor respProc = new MyResponseProcessor( solMapsCoveredByBatch,
		                                                              sink,
		                                                              fm );

		// Attach the response processor to the future for the request and
		// remember the future so that we can wait for its completion later.
		futures.add( f.thenAccept(respProc) );
	}

	@Override
	protected ExecOpExecutionException createExceptionIfWaitingFailed( final InterruptedException e ) {
		final String msg = "Waiting for the requests of this bind " +
				"join at the federation member with service URI " +
				fm.getServiceURI() + " was interrupted with the " +
				"following message: " + e.getMessage();
		return new ExecOpExecutionException(msg, e, this);
	}

	@Override
	protected ExecOpExecutionException createExceptionIfWaitingFailed( final ExecutionException e ) {
		final String msg = "Processing the requests of this bind " +
				"join at the federation member with service URI " +
				fm.getServiceURI() + " caused an exception with " +
				"the following message: " + e.getMessage();
		return new ExecOpExecutionException(msg, e, this);
	}


	// ------- functionality for full-retrieval mode ------

	@Override
	protected void obtainFullResult( final IntermediateResultElementSink sink,
	                                 final QueryProcContextExt ctx )
			throws ExecOpExecutionException
	{
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

		statsOfFullRetrievalReqOp = reqOp.getStats();
	}

	/**
	 * Implementations of this function should create an executable operator
	 * that can perform a request to retrieve all solution mappings for the
	 * query of this operator (i.e., not a bind-join request).
	 *
	 * The operator created by this function should throw exceptions instead
	 * of collecting them.
	 */
	protected abstract NullaryExecutableOp createExecutableReqOpForAll();


	// ------- functionality for Stats ------

	@Override
	public void resetStats() {
		super.resetStats();
		statsOfFullRetrievalReqOp = null;
	}

	@Override
	protected ExecutableOperatorStatsImpl createStats() {
		final ExecutableOperatorStatsImpl s = super.createStats();

		if ( statsOfFullRetrievalReqOp != null) {
			s.put( "statsOfFullRetrievalReqOp",  statsOfFullRetrievalReqOp );
		}

		return s;
	}
}
