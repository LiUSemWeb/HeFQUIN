package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.impl;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.ChangeOrderOfTPAddBJFILTERAndIndexNLJ;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.IdentifyPhysicalOperatorOfTwoTPAdd;

public class RuleChangeOrderOfTPAddBJFILTERAndIndexNLJ extends ChangeOrderOfTPAddBJFILTERAndIndexNLJ {
    double priority;

    public RuleChangeOrderOfTPAddBJFILTERAndIndexNLJ( double priority ){
        this.priority = priority;
    }

    @Override
    public double getPriority() {
        return priority;
    }

    @Override
    public boolean canBeAppliedTo( final PhysicalPlan pp ) {
        return new IdentifyPhysicalOperatorOfTwoTPAdd(pp).matchTPAddBJFILTERAndTPAddIndexNLJ();
    }

}
