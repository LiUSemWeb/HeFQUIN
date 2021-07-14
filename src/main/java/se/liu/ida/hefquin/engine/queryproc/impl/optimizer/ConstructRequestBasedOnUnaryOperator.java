package se.liu.ida.hefquin.engine.queryproc.impl.optimizer;

import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.TPFServer;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.impl.req.BGPRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.req.TPFRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.req.TriplePatternRequestImpl;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalPlanWithNullaryRootImpl;

public class ConstructRequestBasedOnUnaryOperator {
    public PhysicalPlan formRequestBasedOnTPofTPAdd(final LogicalOpTPAdd lop ){
        final FederationMember fm = lop.getFederationMember();

        final DataRetrievalRequest req;
        if ( fm instanceof SPARQLEndpoint){
            req = new TriplePatternRequestImpl( lop.getTP());
        } else if ( fm instanceof TPFServer){
            req = new TPFRequestImpl(lop.getTP(), 0);
        } else if ( fm instanceof BRTPFServer){
            req = new TPFRequestImpl(lop.getTP(), 0);
        } else
            throw new IllegalArgumentException("Unsupported combination of federation member (type: " + fm.getClass().getName() );

        final LogicalOpRequest<?,?> op = new LogicalOpRequest<>( fm, req );
        final PhysicalPlan pp = new PhysicalPlanWithNullaryRootImpl( new PhysicalOpRequest(op) );

        return pp;
    }

    public PhysicalPlan formRequestBasedOnBGPofBGPAdd( final LogicalOpBGPAdd lop ){
        final FederationMember fm = lop.getFederationMember();

        final DataRetrievalRequest req;
        if ( fm.getInterface().supportsBGPRequests() ){
            req = new BGPRequestImpl( lop.getBGP());
        } else
            throw new IllegalArgumentException("Unsupported combination of federation member (type: " + fm.getClass().getName() );

        final LogicalOpRequest<?,?> op = new LogicalOpRequest<>( fm, req );
        final PhysicalPlan pp = new PhysicalPlanWithNullaryRootImpl(new PhysicalOpRequest(op));

        return pp;
    }
}
