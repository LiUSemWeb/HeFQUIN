package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;

import java.util.HashSet;
import java.util.Set;

public class ApplicableRules {

    RuleInstances rules = new RuleInstances();
    Set<RewritingRule> rulesForPlan = new HashSet<>();

    public ApplicableRules( final PhysicalPlan plan ){
        rules.addRuleInstances();
        findApplicableRules( plan );
    }

    public void findApplicableRules( final PhysicalPlan plan ){
        rulesForPlan.addAll( findApplicableRulesForRootOp(plan) );

        final int numChildren = plan.numberOfSubPlans();
        for ( int i = 0; i < numChildren; ++i ) {
            findApplicableRules( plan.getSubPlan(i) );
        }
    }

    public Set<RewritingRule> findApplicableRulesForRootOp( final PhysicalPlan plan ){
        Set<RewritingRule> rulesForRoot = new HashSet<>();

        for (RewritingRule rule : rules.ruleApplications) {
            if ( rule.canBeAppliedTo( plan ) ){
                rulesForRoot.add(rule);
            }
        }
        return rulesForRoot;
    }

}
