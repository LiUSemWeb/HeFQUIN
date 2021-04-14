package se.liu.ida.hefquin.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.data.SolutionMapping;
import se.liu.ida.hefquin.federation.FederationAccessManager;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.query.Query;
import se.liu.ida.hefquin.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.queryproc.ExecutionContext;

import java.util.Iterator;
import java.util.Set;

public abstract class ExecOpGenericBindJoinWithSolMapsRequests<QueryType extends Query,
		MemberType extends FederationMember,
		ReqType extends DataRetrievalRequest>
		extends ExecOpGenericBindJoin<QueryType,MemberType>
{
	public ExecOpGenericBindJoinWithSolMapsRequests(final QueryType query, final MemberType fm ) {
		super( query, fm );
	}

	@Override
	protected Iterator<? extends SolutionMapping> fetchSolutionMappings(
			final Set<SolutionMapping> solMaps,
			final ExecutionContext execCxt ){
		final ReqType req = createRequest(solMaps);
		final SolMapsResponse resp = performRequest( req, execCxt.getFederationAccessMgr() );
		return resp.getIterator();
	}

	protected abstract ReqType createRequest(final Set<SolutionMapping> solMaps);
	protected abstract SolMapsResponse performRequest( final ReqType req, final FederationAccessManager fedAccessMgr );
}
