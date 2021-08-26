package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.rules;

import se.liu.ida.hefquin.engine.queryplan.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;

public class RuleChangeOrderOfTPAddIndexNLJAndBGPAddIndexNLJ extends GenericRuleChangeOrderOfTwoUnaryOp{

    public RuleChangeOrderOfTPAddIndexNLJAndBGPAddIndexNLJ( final double priority ) {
        super(priority);
    }

    @Override
    protected boolean canBeAppliedTo( final PhysicalPlan plan ) {
        final PhysicalOperator rootOp = plan.getRootOperator();
        if( IdentifyPhysicalOpUsedForTPAdd.isIndexNLJ(rootOp) ) {
            final PhysicalOperator subRootOp = plan.getSubPlan(0).getRootOperator();
            return IdentifyPhysicalOpUsedForBGPAdd.isIndexNLJ(subRootOp);
        }
        return false;
    }

}
