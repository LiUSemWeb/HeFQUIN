package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.rules;

import se.liu.ida.hefquin.engine.queryplan.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;

public class RuleConvertTPAddBJToNaiveNLJ extends GenericRuleConvertTPAddToNaiveNLJ{

    public RuleConvertTPAddBJToNaiveNLJ( final double priority ) {
        super(priority);
    }

    @Override
    protected boolean canBeAppliedTo(PhysicalPlan plan) {
        final PhysicalOperator rootOp = plan.getRootOperator();
        return IdentifyPhysicalOpUsedForTPAdd.isBindJoin(rootOp);
    }

}
