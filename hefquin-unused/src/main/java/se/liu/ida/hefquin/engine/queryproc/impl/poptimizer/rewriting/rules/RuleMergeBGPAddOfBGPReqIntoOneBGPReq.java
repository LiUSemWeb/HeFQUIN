package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.rewriting.rules;

import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.federation.FederationMember;

public class RuleMergeBGPAddOfBGPReqIntoOneBGPReq extends GenericRuleMergeBGPAddOfReqIntoOneBGPReq{

    public RuleMergeBGPAddOfBGPReqIntoOneBGPReq( final double priority ) {
        super(priority);
    }

    @Override
    protected boolean canBeAppliedTo( final PhysicalPlan plan ) {
        final PhysicalOperator rootOp = plan.getRootOperator();
        final LogicalOperator rootLop = ((PhysicalOperatorForLogicalOperator) rootOp).getLogicalOperator();

        if( rootLop instanceof LogicalOpBGPAdd ) {
            final FederationMember fm = ((LogicalOpBGPAdd)rootLop).getFederationMember();

            final PhysicalOperator subRootOp = plan.getSubPlan(0).getRootOperator();
            return IdentifyTypeOfRequestUsedForReq.isBGPRequestWithFm( subRootOp, fm );
        }
        return false;
    }

}
