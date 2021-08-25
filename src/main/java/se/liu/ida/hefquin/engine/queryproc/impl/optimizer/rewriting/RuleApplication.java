package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;

public interface RuleApplication
{
	/**
	 * Returns the actual rewriting rule to be applied.@return
	 */
    RewritingRule getRule();

    /**
     * Returns the (complete) original plan to which the rule is meant to
     * be applied. Note that this is really the whole original plan, not
     * just the specific subplan that is meant to be modified by the rule
     * application.
     */
    PhysicalPlan getPlan();

    /**
     * Returns the complete rewritten plan.
     */
    PhysicalPlan getResultingPlan();
}
