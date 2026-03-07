package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.rewriting;

import java.util.Set;

import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;

public interface RewritingRule
{
    double getPriority();

    /**
     * Returns all possible applications of this rule for the given plan. Each
     * of the returned {@link RuleApplication} objects will return the given
     * plan when calling {@link RuleApplication#getPlan()} and it will return
     * this rule when calling {@link RuleApplication#getRule()}.
     */
    Set<RuleApplication> determineAllPossibleApplications( PhysicalPlan plan );

}
