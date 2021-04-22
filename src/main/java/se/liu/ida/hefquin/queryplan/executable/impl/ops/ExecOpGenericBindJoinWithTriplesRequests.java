package se.liu.ida.hefquin.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.data.SolutionMapping;
import se.liu.ida.hefquin.data.Triple;
import se.liu.ida.hefquin.federation.FederationAccessManager;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.federation.access.TriplesResponse;
import se.liu.ida.hefquin.query.Query;
import se.liu.ida.hefquin.queryproc.ExecutionContext;

import java.util.Iterator;
import java.util.Set;

public abstract class ExecOpGenericBindJoinWithTriplesRequests<QueryType extends Query,
        MemberType extends FederationMember,
        ReqType extends DataRetrievalRequest>
        extends ExecOpGenericBindJoin<QueryType,MemberType>
{
    public ExecOpGenericBindJoinWithTriplesRequests(final QueryType query, final MemberType fm ) {
        super( query, fm );
    }

    @Override
    public Iterator<? extends SolutionMapping> fetchSolutionMappings( final Set<SolutionMapping> solMaps,
                                                                      final ExecutionContext execCxt )
    {
        final ReqType req = createRequest(solMaps);
        final TriplesResponse resp = performRequest( req, execCxt.getFederationAccessMgr() );
        return convert( resp.getIterator(), req );
    }

    protected abstract ReqType createRequest(final Set<SolutionMapping> solMaps);

    protected abstract TriplesResponse performRequest( final ReqType req, final FederationAccessManager fedAccessMgr );

    protected abstract Iterator<? extends SolutionMapping> convert( final Iterator<? extends Triple> itTriples, final ReqType req );
}