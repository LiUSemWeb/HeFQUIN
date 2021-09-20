package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.rules;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.query.BGP;
import se.liu.ida.hefquin.engine.queryplan.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RuleApplication;

public class RuleMergeTPAddAndBGPAddIntoBGPAdd extends AbstractRewritingRuleImpl{

    public RuleMergeTPAddAndBGPAddIntoBGPAdd( final double priority ) {
        super(priority);
    }

    @Override
    protected boolean canBeAppliedTo( final PhysicalPlan plan ) {
        final PhysicalOperator rootOp = plan.getRootOperator();

        if( IdentifyLogicalOp.matchTPAdd(rootOp) ) {
            final LogicalOpTPAdd rootLop = (LogicalOpTPAdd) ((PhysicalOperatorForLogicalOperator) rootOp).getLogicalOperator();
            final FederationMember fm = rootLop.getFederationMember();

            final PhysicalOperator subRootOp = plan.getSubPlan(0).getRootOperator();
            return IdentifyLogicalOp.isBGPAddWithFm( subRootOp, fm );
        }
        return false;
    }

    @Override
    protected RuleApplication createRuleApplication(PhysicalPlan[] pathToTargetPlan) {
        return new AbstractRuleApplicationImpl(pathToTargetPlan, this) {
            @Override
            protected PhysicalPlan rewritePlan( final PhysicalPlan plan ) {
                final LogicalOpTPAdd rootOp = (LogicalOpTPAdd) ((PhysicalOperatorForLogicalOperator) plan.getRootOperator()).getLogicalOperator();

                final PhysicalOperator subRootOp =  plan.getSubPlan(0).getRootOperator();
                final LogicalOpBGPAdd subRootLop = (LogicalOpBGPAdd) ((PhysicalOperatorForLogicalOperator) subRootOp).getLogicalOperator();

                final BGP newBGP = GraphPatternConstructor.createNewBGP( rootOp, subRootLop);
                final FederationMember fm = rootOp.getFederationMember();

                final LogicalOpBGPAdd logicalBGPAdd = new LogicalOpBGPAdd( fm, newBGP );

                return PhysicalPlanFactory.createPlanBasedOnTypeOfGivenPhysicalOp( subRootOp, logicalBGPAdd, plan.getSubPlan(0).getSubPlan(0) );
            }
        };
    }

}
