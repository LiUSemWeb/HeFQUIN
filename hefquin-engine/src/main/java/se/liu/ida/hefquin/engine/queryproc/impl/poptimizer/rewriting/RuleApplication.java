package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.rewriting;

import se.liu.ida.hefquin.base.utils.RandomizedSelection;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;

public interface RuleApplication extends RandomizedSelection.WeightedObject {
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
