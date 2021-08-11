package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.impl;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.ChangeOrderOfTPAddBJFILTERAndIndexNLJ;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.IdentifyPhysicalOperatorOfTwoTPAdd;

public class RuleChangeOrderOfTPAddBJFILTERAndIndexNLJ extends ChangeOrderOfTPAddBJFILTERAndIndexNLJ {

    @Override
    public Boolean canBeAppliedTo( final PhysicalPlan pp ) {
        return new IdentifyPhysicalOperatorOfTwoTPAdd(pp).matchTPAddBJFILTERAndTPAddIndexNLJ();
    }

}
