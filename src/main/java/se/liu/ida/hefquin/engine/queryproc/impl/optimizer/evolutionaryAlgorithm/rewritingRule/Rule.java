package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm.rewritingRule;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;

public interface Rule {

    Boolean canBeAppliedTo( final PhysicalPlan pp );

    PhysicalPlan applyTo( final PhysicalPlan pp );

    Double getPriority();

}
