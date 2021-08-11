package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.impl;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.ChangeOrderOfTwoTPAddIndexNLJ;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.IdentifyPhysicalOperatorOfTwoTPAdd;

public class RuleChangeOrderOfTwoTPAddIndexNLJ extends ChangeOrderOfTwoTPAddIndexNLJ {

    @Override
    public Boolean canBeAppliedTo( final PhysicalPlan pp ) {
        return new IdentifyPhysicalOperatorOfTwoTPAdd(pp).matchTwoTPAddIndexNLJ();
    }

}
