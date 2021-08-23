package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;

public interface RuleApplication {

    RewritingRule getRule();

    /** Returns the plan to which the rule is meant to be applied. */
    PhysicalPlan getPlan();

    PhysicalPlan getResultingPlan();
}
