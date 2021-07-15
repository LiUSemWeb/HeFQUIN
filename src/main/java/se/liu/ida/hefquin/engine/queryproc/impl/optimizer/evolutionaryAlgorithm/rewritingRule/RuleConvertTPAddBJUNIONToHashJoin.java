package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm.rewritingRule;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;

public class RuleConvertTPAddBJUNIONToHashJoin extends ConvertTPAddToHashJoin{

    @Override
    public Boolean canBeAppliedTo( final PhysicalPlan pp ) {

        return new IdentifyPhysicalOperatorOfTPAdd(pp).matchTPAddBJUNION();

    }

}
