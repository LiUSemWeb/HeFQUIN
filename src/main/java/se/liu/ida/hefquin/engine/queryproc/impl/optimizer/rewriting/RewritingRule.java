package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;

import java.util.Set;

public interface RewritingRule {

    double getPriority();

    Set<RuleApplication> determineAllPossibleApplications( final PhysicalPlan plan );

}
