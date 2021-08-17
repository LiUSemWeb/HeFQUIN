package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.impl;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.ConvertTPAddToBindJoinWithVALUES;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.IdentifyPhysicalOperatorOfTPAdd;

public class RuleConvertTPAddIndexNLJToBJVALUES extends ConvertTPAddToBindJoinWithVALUES {
    double priority;

    public RuleConvertTPAddIndexNLJToBJVALUES( double priority ){
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
