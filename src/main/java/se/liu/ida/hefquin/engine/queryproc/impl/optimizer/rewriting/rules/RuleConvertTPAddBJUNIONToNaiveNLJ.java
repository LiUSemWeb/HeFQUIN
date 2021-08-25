package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.rules;

import se.liu.ida.hefquin.engine.queryplan.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;

public class RuleConvertTPAddBJUNIONToNaiveNLJ extends GenericRuleConvertTPAddToNaiveNLJ{

    public RuleConvertTPAddBJUNIONToNaiveNLJ( final double priority ) {
        super(priority);
    }

    @Override
    public boolean canBeAppliedTo( final PhysicalPlan plan ) {
        final PhysicalOperator rootOp = plan.getRootOperator();
        return IdentifyPhysicalOpUsedForTPAdd.isBindJoinUNION(rootOp);
    }

}
