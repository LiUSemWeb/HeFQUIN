package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.federation.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.engine.query.Query;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

public abstract class ExecOpGenericIndexNestedLoopsJoinWithSolMapsRequests<QueryType extends Query,
                                                                           MemberType extends FederationMember,
                                                                           ReqType extends DataRetrievalRequest>
                   extends ExecOpGenericIndexNestedLoopsJoin<QueryType,MemberType>
{
	public ExecOpGenericIndexNestedLoopsJoinWithSolMapsRequests( final QueryType query, final MemberType fm ) {
		super( query, fm );
	}

	@Override
	protected Iterable<? extends SolutionMapping> fetchSolutionMappings(
			final SolutionMapping sm,
			final ExecutionContext execCxt ) throws ExecOpExecutionException
	{
		final ReqType req = createRequest(sm);
		final SolMapsResponse resp = performRequest( req, execCxt.getFederationAccessMgr() );
		return resp.getSolutionMappings();
	}

	protected abstract ReqType createRequest( final SolutionMapping sm );

	protected abstract SolMapsResponse performRequest( final ReqType req,
	                                                   final FederationAccessManager fedAccessMgr )
			throws ExecOpExecutionException;
}
