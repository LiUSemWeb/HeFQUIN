package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;

public interface Rule {

    boolean canBeAppliedTo( final PhysicalPlan pp );

    PhysicalPlan applyTo( final PhysicalPlan pp );

    double getPriority();

}
