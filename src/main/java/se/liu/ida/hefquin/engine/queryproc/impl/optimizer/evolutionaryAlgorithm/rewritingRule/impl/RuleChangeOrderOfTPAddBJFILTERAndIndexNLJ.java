package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm.rewritingRule.impl;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm.rewritingRule.ChangeOrderOfTPAddBJFILTERAndIndexNLJ;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm.rewritingRule.IdentifyPhysicalOperatorOfTwoTPAdd;

public class RuleChangeOrderOfTPAddBJFILTERAndIndexNLJ extends ChangeOrderOfTPAddBJFILTERAndIndexNLJ {

    @Override
    public Boolean canBeAppliedTo( final PhysicalPlan pp ) {
        return new IdentifyPhysicalOperatorOfTwoTPAdd(pp).matchTPAddBJFILTERAndTPAddIndexNLJ();
    }

}
