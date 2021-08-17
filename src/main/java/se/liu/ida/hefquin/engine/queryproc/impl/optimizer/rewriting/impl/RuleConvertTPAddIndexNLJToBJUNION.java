package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.impl;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.ConvertTPAddToBindJoinWithUNION;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.IdentifyPhysicalOperatorOfTPAdd;

public class RuleConvertTPAddIndexNLJToBJUNION extends ConvertTPAddToBindJoinWithUNION {
    double priority;

    public RuleConvertTPAddIndexNLJToBJUNION( double priority ){
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
