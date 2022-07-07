package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.rules;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.access.BGPRequest;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;

public class IdentifyTypeOfRequestUsedForReq {

    public static boolean isBGPRequest( final PhysicalOperator op ) {
        final LogicalOperator lop = ((PhysicalOperatorForLogicalOperator) op).getLogicalOperator();

        return isBGPRequest(lop);
    }

    public static boolean isBGPRequest( final LogicalOperator lop ) {
        if( lop instanceof LogicalOpRequest){
            return ( (LogicalOpRequest<?, ?>) lop ).getRequest() instanceof BGPRequest;
        }
        return false;
    }

    public static boolean isTriplePatternRequest( final PhysicalOperator op ) {
        final LogicalOperator lop = ((PhysicalOperatorForLogicalOperator) op).getLogicalOperator();

        return isTriplePatternRequest(lop);
    }

    public static boolean isTriplePatternRequest( final LogicalOperator lop ) {
        if( lop instanceof LogicalOpRequest){
            return ( (LogicalOpRequest<?, ?>) lop ).getRequest() instanceof TriplePatternRequest;
        }
        return false;
    }

    public static boolean isGraphPatternRequest( final LogicalOperator lop ) {
        if( lop instanceof LogicalOpRequest){
            return ( (LogicalOpRequest<?, ?>) lop ).getRequest() instanceof SPARQLRequest;
        }
        return false;
    }

    public static boolean isBGPRequestOverSPARQLEndpoint( final PhysicalOperator op ) {
        final LogicalOperator lop = ((PhysicalOperatorForLogicalOperator) op).getLogicalOperator();

        return isBGPRequestOverSPARQLEndpoint(lop);
    }

    public static boolean isBGPRequestOverSPARQLEndpoint( final LogicalOperator lop ) {
        if ( isBGPRequest(lop) ){
            final FederationMember fm = ( (LogicalOpRequest<?, ?>)lop ).getFederationMember();
            return fm instanceof SPARQLEndpoint;
        }
        return false;
    }

    public static boolean isBGPRequestWithFm( final PhysicalOperator op, final FederationMember fm ) {
        final LogicalOperator lop = ((PhysicalOperatorForLogicalOperator) op).getLogicalOperator();

        if ( isBGPRequest(lop) ){
            return ( (LogicalOpRequest<?, ?>)lop ).getFederationMember() == fm;
        }
        return false;
    }

    public static boolean isTriplePatternRequestWithFm( final PhysicalOperator op, final FederationMember fm ) {
        final LogicalOperator lop = ((PhysicalOperatorForLogicalOperator) op).getLogicalOperator();

        if ( isTriplePatternRequest(lop) ){
            return ( (LogicalOpRequest<?, ?>)lop ).getFederationMember() == fm;
        }
        return false;
    }

    public static boolean isGraphPatternReqWithFm( final PhysicalOperator op, final FederationMember fm ) {
        final LogicalOperator lop = ((PhysicalOperatorForLogicalOperator) op).getLogicalOperator();

        if ( isGraphPatternRequest(lop) ){
            return ( (LogicalOpRequest<?, ?>)lop ).getFederationMember() == fm;
        }
        return false;
    }

    public static boolean twoGraphPatternReqWithSameFm( final PhysicalOperator subPlanOp1, final PhysicalOperator subPlanOp2 ) {
        final LogicalOperator lop = ((PhysicalOperatorForLogicalOperator)subPlanOp1).getLogicalOperator();

        if ( IdentifyTypeOfRequestUsedForReq.isGraphPatternRequest( lop ) ) {
            final FederationMember fm = ((LogicalOpRequest<?, ?>) lop).getFederationMember();

            return IdentifyTypeOfRequestUsedForReq.isGraphPatternReqWithFm( subPlanOp2, fm );
        }
        return false;
    }

}
