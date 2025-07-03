package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.rewriting.rules;

import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;

public class RuleChangeOrderOfTPAddAndBGPAdd extends GenericRuleChangeOrderOfTwoUnaryOp{

    public RuleChangeOrderOfTPAddAndBGPAdd( final double priority ) {
        super(priority);
    }

    @Override
    protected boolean canBeAppliedTo( final PhysicalPlan plan ) {
        final PhysicalOperator rootOp = plan.getRootOperator();
        if( IdentifyLogicalOp.isTPAdd(rootOp) ) {
            final PhysicalOperator subRootOp = plan.getSubPlan(0).getRootOperator();
            return IdentifyLogicalOp.isBGPAdd(subRootOp);
        }
        return false;
    }

}
