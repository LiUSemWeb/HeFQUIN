package se.liu.ida.hefquin.queryplan.executable.impl.ops;

import java.util.Iterator;

import se.liu.ida.hefquin.data.SolutionMapping;
import se.liu.ida.hefquin.data.Triple;
import se.liu.ida.hefquin.federation.FederationAccessManager;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.federation.access.TriplesResponse;
import se.liu.ida.hefquin.query.Query;
import se.liu.ida.hefquin.queryproc.ExecutionContext;

public abstract class ExecOpGenericIndexNestedLoopsJoinWithTriplesRequests<QueryType extends Query,
                                                                           MemberType extends FederationMember,
                                                                           ReqType extends DataRetrievalRequest>
           extends ExecOpGenericIndexNestedLoopsJoin<QueryType,MemberType>
{
	public ExecOpGenericIndexNestedLoopsJoinWithTriplesRequests( final QueryType query, final MemberType fm ) {
		super( query, fm );
	}

	@Override
	protected Iterator<? extends SolutionMapping> fetchSolutionMappings(
			final SolutionMapping sm,
			final ExecutionContext execCxt )
	{
		final ReqType req = createRequest(sm);
		final TriplesResponse resp = performRequest( req, execCxt.getFederationAccessMgr() );
		return convert( resp.getIterator(), req );
	}

	protected abstract ReqType createRequest( final SolutionMapping sm );

	protected abstract TriplesResponse performRequest( final ReqType req, final FederationAccessManager fedAccessMgr );

	protected abstract Iterator<? extends SolutionMapping> convert( final Iterator<? extends Triple> itTriples, final ReqType req );
}
