package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.impl;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.ConvertTPAddToHashJoin;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.IdentifyPhysicalOperatorOfTPAdd;

/**
 * This is a rewriting rule that convert a TPAdd(indexNLJ) operator to Hash Join.
 */
public class RuleConvertTPAddIndexNLJToHashJoin extends ConvertTPAddToHashJoin {
    double priority;

    public RuleConvertTPAddIndexNLJToHashJoin( double priority ){
        this.priority = priority;
    }

    @Override
    public double getPriority() {
        return priority;
    }

    @Override
    public boolean canBeAppliedTo( final PhysicalPlan pp ) {
        return new IdentifyPhysicalOperatorOfTPAdd(pp).matchTPAddIndexNLJ();
    }

}
