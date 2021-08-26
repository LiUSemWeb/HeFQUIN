package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.rules;

import se.liu.ida.hefquin.engine.queryplan.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;

public class RuleChangeOrderOfTPAddBJFILTERAndIndexNLJ extends GenericRuleChangeOrderOfTwoTPAdd{

    public RuleChangeOrderOfTPAddBJFILTERAndIndexNLJ( final double priority ) {
        super(priority);
    }

    @Override
    protected boolean canBeAppliedTo( final PhysicalPlan plan ) {
        final PhysicalOperator rootOp = plan.getRootOperator();
        final PhysicalOperator subRootOp = plan.getSubPlan(0).getRootOperator();
        return IdentifyPhysicalOpUsedForTPAdd.isBindJoinFILTER(rootOp) && IdentifyPhysicalOpUsedForTPAdd.isIndexNLJ(subRootOp);
    }

}
