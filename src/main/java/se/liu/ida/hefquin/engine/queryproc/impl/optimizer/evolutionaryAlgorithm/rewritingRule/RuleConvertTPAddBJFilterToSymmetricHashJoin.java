package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm.rewritingRule;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;

public class RuleConvertTPAddBJFilterToSymmetricHashJoin extends ConvertTPAddToSymmetricHashJoin{

    @Override
    public Boolean canBeAppliedTo( final PhysicalPlan pp ) {

        return new IdentifyPhysicalOperatorOfTPAdd(pp).matchTPAddBJFilter();

    }

}
