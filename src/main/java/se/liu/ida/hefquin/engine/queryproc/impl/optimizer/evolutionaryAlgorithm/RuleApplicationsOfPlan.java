package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RewritingRule;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RuleApplication;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RuleInstances;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RuleApplicationsOfPlan {
    protected final RuleInstances ruleIns;
    protected final Map<PhysicalPlan, Set<RuleApplication>> cache = new HashMap<>();

    public RuleApplicationsOfPlan( final RuleInstances ruleIns ) {
        this.ruleIns = ruleIns;
    }

    public Set<RuleApplication> determineRuleApplications( final PhysicalPlan plan )
    {
        if ( cache.containsKey(plan) ) {
            return cache.get(plan);
        }

        final Set<RuleApplication> ruleApps = new HashSet<>();
        for ( final RewritingRule rewritingRule : ruleIns.ruleInstances ) {
            ruleApps.addAll( rewritingRule.determineAllPossibleApplications( plan ) );
        }
        cache.put(plan, ruleApps);
        return ruleApps;
    }

}
