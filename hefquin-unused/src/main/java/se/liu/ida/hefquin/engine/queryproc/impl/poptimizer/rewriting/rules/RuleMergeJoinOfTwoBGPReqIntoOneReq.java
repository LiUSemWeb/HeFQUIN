package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.rewriting.rules;

import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.federation.FederationMember;

public class RuleMergeJoinOfTwoBGPReqIntoOneReq extends GenericRuleMergeJoinOfTwoReqIntoOneBGPReq{

    public RuleMergeJoinOfTwoBGPReqIntoOneReq( final double priority ) {
        super(priority);
    }

    @Override
    protected boolean canBeAppliedTo( final PhysicalPlan plan ) {
        final PhysicalOperator rootOp = plan.getRootOperator();
        if ( IdentifyLogicalOp.isJoin(rootOp) ) {
            final PhysicalOperator subPlanOp1 = plan.getSubPlan(0).getRootOperator();
            final LogicalOperator lop = ((PhysicalOperatorForLogicalOperator) subPlanOp1).getLogicalOperator();

            if ( IdentifyTypeOfRequestUsedForReq.isBGPRequest( lop ) ){
                final FederationMember fm = ((LogicalOpRequest<?, ?>)lop).getFederationMember();

                final PhysicalOperator subPlanOp2 = plan.getSubPlan(1).getRootOperator();
                return IdentifyTypeOfRequestUsedForReq.isBGPRequestWithFm( subPlanOp2, fm);
            }
            return false;
        }
        return false;
    }

}
