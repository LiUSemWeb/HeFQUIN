package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm.rewritingRule.impl;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm.rewritingRule.ConvertTPAddToBindJoinWithVALUES;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm.rewritingRule.IdentifyPhysicalOperatorOfTPAdd;

public class RuleConvertTPAddBindJoinWithFILTERToVALUES extends ConvertTPAddToBindJoinWithVALUES {

    @Override
    public Boolean canBeAppliedTo(PhysicalPlan pp) {
        return new IdentifyPhysicalOperatorOfTPAdd(pp).matchTPAddBJFilter();
    }

}
