package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm.rewritingRule.impl;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm.rewritingRule.ConvertTPAddToBindJoinWithVALUES;

public class RuleConvertTPAddIndexNLJToBingJoinVALUES extends ConvertTPAddToBindJoinWithVALUES {

    @Override
    public Boolean canBeAppliedTo(PhysicalPlan pp) {
        return null;
    }

}
