package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.rules;

import se.liu.ida.hefquin.engine.queryplan.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;

public class RuleChangeOrderOfTwoTPAddBJFILTER extends GenericRuleChangeOrderOfTwoUnaryOp{

    public RuleChangeOrderOfTwoTPAddBJFILTER( final double priority ) {
        super(priority);
    }

    @Override
    protected boolean canBeAppliedTo( final PhysicalPlan plan ) {
        final PhysicalOperator rootOp = plan.getRootOperator();
        if( IdentifyPhysicalOpUsedForTPAdd.isBindJoinFILTER(rootOp) ) {
            final PhysicalOperator subRootOp = plan.getSubPlan(0).getRootOperator();
            return IdentifyPhysicalOpUsedForTPAdd.isBindJoinFILTER(subRootOp);
        }
        return false;
    }

}
