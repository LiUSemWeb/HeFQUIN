package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;

import java.util.Set;

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
