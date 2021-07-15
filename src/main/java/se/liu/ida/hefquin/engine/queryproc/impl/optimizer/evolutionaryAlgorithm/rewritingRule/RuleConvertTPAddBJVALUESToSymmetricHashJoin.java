package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm.rewritingRule;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;

public class RuleConvertTPAddBJVALUESToSymmetricHashJoin extends ConvertTPAddToSymmetricHashJoin{

    @Override
    public Boolean canBeAppliedTo( final PhysicalPlan pp ) {

        return new IdentifyPhysicalOperatorOfTPAdd(pp).matchTPAddBJVALUES();

    }

}
