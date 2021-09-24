package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.rules;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.query.BGP;
import se.liu.ida.hefquin.engine.queryplan.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalOpUtils;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RuleApplication;

public class RuleMergeTwoBGPAddIntoOneBGPAdd extends AbstractRewritingRuleImpl{

    public RuleMergeTwoBGPAddIntoOneBGPAdd( final double priority ) {
        super(priority);
    }

    @Override
    protected boolean canBeAppliedTo( final PhysicalPlan plan ) {
        final PhysicalOperator rootOp = plan.getRootOperator();
        final LogicalOperator rootLop = ((PhysicalOperatorForLogicalOperator) rootOp).getLogicalOperator();

        if( rootLop instanceof LogicalOpBGPAdd ) {
            final FederationMember fm = ((LogicalOpBGPAdd)rootLop).getFederationMember();

            final PhysicalOperator subRootOp = plan.getSubPlan(0).getRootOperator();
            return IdentifyLogicalOp.isBGPAddWithFm( subRootOp, fm );
        }
        return false;
    }

    @Override
    protected RuleApplication createRuleApplication( final PhysicalPlan[] pathToTargetPlan ) {
        return new AbstractRuleApplicationImpl(pathToTargetPlan, this) {
            @Override
            protected PhysicalPlan rewritePlan( final PhysicalPlan plan ) {
                final PhysicalOperator rootOp = plan.getRootOperator();
                final LogicalOpBGPAdd rootLop = (LogicalOpBGPAdd) ((PhysicalOperatorForLogicalOperator) rootOp).getLogicalOperator();

                final PhysicalOperator subRootOp =  plan.getSubPlan(0).getRootOperator();
                final LogicalOpBGPAdd subRootLop = (LogicalOpBGPAdd) ((PhysicalOperatorForLogicalOperator) subRootOp).getLogicalOperator();

                final BGP newBGP = LogicalOpUtils.createNewBGP(rootLop, subRootLop);
                final FederationMember fm = rootLop.getFederationMember();
                final LogicalOpBGPAdd logicalBGPAdd = new LogicalOpBGPAdd( fm, newBGP );

                return PhysicalPlanFactory.createPlanBasedOnTypeOfGivenPhysicalOp( logicalBGPAdd, rootOp, plan.getSubPlan(0).getSubPlan(0) );
            }
        };
    }

}
