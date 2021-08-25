package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.rules;

import se.liu.ida.hefquin.engine.queryplan.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;

/**
 * This is a rewriting rule that convert a TPAdd(indexNLJ) operator to Hash Join.
 */
public class RuleConvertTPAddIndexNLJToHashJoin extends GenericRuleConvertTPAddToHashJoin
{
    public RuleConvertTPAddIndexNLJToHashJoin( final double priority ){
        super(priority);
    }

    @Override
    public boolean canBeAppliedTo( final PhysicalPlan plan ) {
        final PhysicalOperator rootOp = plan.getRootOperator();
        return IdentifyPhysicalOpUsedForTPAdd.isIndexNLJ(rootOp);
    }

}
