package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.rules;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.BGPRequest;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.engine.queryplan.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpRequest;

public class IdentifyPhysicalOpUsedForReq {

    public static boolean isBGPRequest( final PhysicalOperator op ) {
        final LogicalOperator lop = ((PhysicalOperatorForLogicalOperator) op).getLogicalOperator();
        if( lop instanceof LogicalOpRequest){
            return ((LogicalOpRequest<?, ?>) lop).getRequest() instanceof BGPRequest;
        }
        return false;
    }

    public static boolean isTriplePatternRequest( final PhysicalOperator op ) {
        final LogicalOperator lop = ((PhysicalOperatorForLogicalOperator) op).getLogicalOperator();
        if( lop instanceof LogicalOpRequest){
            return ((LogicalOpRequest<?, ?>) lop).getRequest() instanceof TriplePatternRequest;
        }
        return false;
    }

    public static boolean isGraphPatternRequest( final PhysicalOperator op ) {
        final LogicalOperator lop = ((PhysicalOperatorForLogicalOperator) op).getLogicalOperator();
        if( lop instanceof LogicalOpRequest){
            return ((LogicalOpRequest<?, ?>) lop).getRequest() instanceof SPARQLRequest;
        }
        return false;
    }

    public static boolean isBGPRequestWithFm( final PhysicalOperator op, final FederationMember fm ) {
        if ( op instanceof PhysicalOpRequest) {
            final LogicalOpRequest lop = (LogicalOpRequest) ((PhysicalOperatorForLogicalOperator) op).getLogicalOperator();

            return ( lop.getFederationMember() == fm ) && ( lop.getRequest() instanceof BGPRequest);
        }
        return false;
    }

    public static boolean isTriplePatternRequestWithFm( final PhysicalOperator op, final FederationMember fm ) {
        if ( IdentifyPhysicalOpUsedForReq.isTriplePatternRequest(op) ){
            final LogicalOperator lop = ((PhysicalOperatorForLogicalOperator) op).getLogicalOperator();
            return ((LogicalOpRequest)lop).getFederationMember() == fm;
        }
        return false;
    }

    public static boolean isGraphPatternReqWithFm(final PhysicalOperator op, final FederationMember fm) {
        if ( IdentifyPhysicalOpUsedForReq.isGraphPatternRequest(op) ){
            final LogicalOperator lop = ((PhysicalOperatorForLogicalOperator) op).getLogicalOperator();
            return ((LogicalOpRequest)lop).getFederationMember() == fm;
        }
        return false;
    }

    public static boolean twoBGPRequestWithSameFm( final PhysicalOperator op1, final PhysicalOperator op2 ) {
        if ( IdentifyPhysicalOpUsedForReq.isBGPRequest( op1 ) ){
            final LogicalOpRequest lop1 = (LogicalOpRequest) ((PhysicalOperatorForLogicalOperator) op1).getLogicalOperator();
            final FederationMember fm = lop1.getFederationMember();

            return isBGPRequestWithFm( op2, fm);
        }
        return false;
    }

}
