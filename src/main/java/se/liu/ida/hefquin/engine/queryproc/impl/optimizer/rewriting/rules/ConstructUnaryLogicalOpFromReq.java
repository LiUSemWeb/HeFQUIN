package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.rules;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.BGPRequest;
import se.liu.ida.hefquin.engine.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.engine.query.BGP;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.queryplan.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;

public class ConstructUnaryLogicalOpFromReq {

    public static UnaryLogicalOp constructUnaryLopFromReq( final PhysicalOperator op ) {
        final LogicalOperator lop = ((PhysicalOperatorForLogicalOperator) op).getLogicalOperator();
        if ( lop instanceof BGPRequest) {
            return constructBGPAddLopFromReq((BGPRequest) lop);
        }
        else if( lop instanceof TriplePatternRequest ) {
            return constructTPAddLopFromReq( (TriplePatternRequest) lop);
        }
        else {
            return null;
        }
    }

    protected static LogicalOpBGPAdd constructBGPAddLopFromReq( final BGPRequest lop ) {
        final BGPRequest bgpReq = (BGPRequest) ((LogicalOpRequest<?, ?>) lop).getRequest();
        final BGP bgp = bgpReq.getQueryPattern();

        final FederationMember fm = ((LogicalOpRequest<?, ?>) lop).getFederationMember();

        return new LogicalOpBGPAdd( fm, bgp );
    }

    protected static LogicalOpTPAdd constructTPAddLopFromReq( final TriplePatternRequest lop ) {
        final TriplePatternRequest tpReq = (TriplePatternRequest) ((LogicalOpRequest<?, ?>) lop).getRequest();
        final TriplePattern tp = tpReq.getQueryPattern();

        final FederationMember fm = ((LogicalOpRequest<?, ?>) lop).getFederationMember();

        return new LogicalOpTPAdd( fm, tp );
    }

}
