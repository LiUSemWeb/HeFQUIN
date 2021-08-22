package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;

public interface RewritingRule {

    boolean canBeAppliedTo( final PhysicalPlan plan );

    PhysicalPlan applyTo( final PhysicalPlan plan );

    double getPriority();

}
