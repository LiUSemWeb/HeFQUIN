package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.rules;

import se.liu.ida.hefquin.engine.queryplan.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RuleApplication;

public class RuleMergeMultiwayUnionOfMultiIdenticalSubPlansIntoOne extends AbstractRewritingRuleImpl{

    public RuleMergeMultiwayUnionOfMultiIdenticalSubPlansIntoOne( final double priority ) {
        super(priority);
    }

    @Override
    protected boolean canBeAppliedTo( final PhysicalPlan plan ) {
        final PhysicalOperator op = plan.getRootOperator();
        if ( IdentifyLogicalOp.matchMultiwayUnion(op) ) {
            final PhysicalPlan subPlan = plan.getSubPlan(0);
            for ( int i = 1; i < plan.numberOfSubPlans(); i ++ ) {
                if ( subPlan != plan.getSubPlan(i) ) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    protected RuleApplication createRuleApplication( final PhysicalPlan[] pathToTargetPlan ) {
        return new AbstractRuleApplicationImpl(pathToTargetPlan, this) {
            @Override
            protected PhysicalPlan rewritePlan( final PhysicalPlan plan ) {
                return plan.getSubPlan(0);
            }
        };
    }

}
