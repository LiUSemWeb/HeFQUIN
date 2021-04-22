package se.liu.ida.hefquin.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.data.SolutionMapping;
import se.liu.ida.hefquin.data.Triple;
import se.liu.ida.hefquin.data.jenaimpl.TriplesToSolMapsConverter;
import se.liu.ida.hefquin.federation.BRTPFServer;
import se.liu.ida.hefquin.federation.FederationAccessManager;
import se.liu.ida.hefquin.federation.access.BindingsRestrictedTriplePatternRequest;
import se.liu.ida.hefquin.federation.access.TriplesResponse;
import se.liu.ida.hefquin.query.TriplePattern;
import se.liu.ida.hefquin.federation.access.impl.req.BindingsRestrictedTriplePatternRequestImpl;


import java.util.Iterator;
import java.util.Set;

public class ExecOpBindJoinBRTPF extends
        ExecOpGenericBindJoinWithTriplesRequests<TriplePattern, BRTPFServer, BindingsRestrictedTriplePatternRequest>
{
    public ExecOpBindJoinBRTPF( final TriplePattern query, final BRTPFServer fm ) {
        super( query, fm );
    }

    @Override
    protected BindingsRestrictedTriplePatternRequest createRequest( final Set<SolutionMapping> solMaps) {
        return new BindingsRestrictedTriplePatternRequestImpl(query, solMaps);
    }

    @Override
    protected TriplesResponse performRequest( final BindingsRestrictedTriplePatternRequest req, final FederationAccessManager fedAccessMgr ) {
        return fedAccessMgr.performRequest(req, fm);
    }

    @Override
    protected Iterator<? extends SolutionMapping> convert(final Iterator<? extends Triple> itTriples, final BindingsRestrictedTriplePatternRequest req ) {
        return TriplesToSolMapsConverter.convert( itTriples, req.getTriplePattern());
    }
}
