package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.rewriting.rules;

import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.SPARQLEndpoint;

public class RuleMergeJoinOfTPReqAndBGPReqIntoOneBGPReq extends GenericRuleMergeJoinOfTwoReqIntoOneBGPReq{

    public RuleMergeJoinOfTPReqAndBGPReqIntoOneBGPReq( final double priority ) {
        super(priority);
    }

    @Override
    protected boolean canBeAppliedTo( final PhysicalPlan plan ) {
        final PhysicalOperator rootOp = plan.getRootOperator();
        if ( IdentifyLogicalOp.isJoin(rootOp) ) {
            final PhysicalOperator subPlanOp1 = plan.getSubPlan(0).getRootOperator();
            final LogicalOperator lop1 =  ((PhysicalOperatorForLogicalOperator)subPlanOp1).getLogicalOperator();

            if ( IdentifyTypeOfRequestUsedForReq.isTriplePatternRequest( lop1 ) ) {
                final FederationMember fm = ((LogicalOpRequest<?, ?>) lop1).getFederationMember();

                final PhysicalOperator subPlanOp2 = plan.getSubPlan(1).getRootOperator();
                return ( fm instanceof SPARQLEndpoint) && IdentifyTypeOfRequestUsedForReq.isBGPRequestWithFm( subPlanOp2, fm );
            }
            else if ( IdentifyTypeOfRequestUsedForReq.isBGPRequest( lop1 ) ){
                final FederationMember fm = ((LogicalOpRequest<?, ?>) lop1).getFederationMember();

                final PhysicalOperator subPlanOp2 = plan.getSubPlan(1).getRootOperator();
                return ( fm instanceof SPARQLEndpoint) && IdentifyTypeOfRequestUsedForReq.isTriplePatternRequestWithFm( subPlanOp2, fm );
            }
            else {
                return false;
            }
        }
        return false;
    }

}
