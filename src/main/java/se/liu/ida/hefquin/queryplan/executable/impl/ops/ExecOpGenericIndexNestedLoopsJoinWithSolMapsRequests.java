package se.liu.ida.hefquin.queryplan.executable.impl.ops;

import java.util.Iterator;

import se.liu.ida.hefquin.data.SolutionMapping;
import se.liu.ida.hefquin.federation.FederationAccessManager;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.query.Query;
import se.liu.ida.hefquin.queryproc.ExecutionContext;

public abstract class ExecOpGenericIndexNestedLoopsJoinWithSolMapsRequests<QueryType extends Query,
                                                                           MemberType extends FederationMember,
                                                                           ReqType extends DataRetrievalRequest>
                   extends ExecOpGenericIndexNestedLoopsJoin<QueryType,MemberType>
{
	public ExecOpGenericIndexNestedLoopsJoinWithSolMapsRequests( final QueryType query, final MemberType fm ) {
		super( query, fm );
	}

	@Override
	protected Iterator<SolutionMapping> fetchSolutionMappings(
			final SolutionMapping sm,
			final ExecutionContext execCxt )
	{
		final ReqType req = createRequest(sm);
		final SolMapsResponse resp = performRequest( req, execCxt.getFederationAccessMgr() );
		return resp.getIterator();
	}

	protected abstract ReqType createRequest( final SolutionMapping sm );

	protected abstract SolMapsResponse performRequest( final ReqType req, final FederationAccessManager fedAccessMgr );
}
