package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting;

import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.impl.RuleConvertTPAddIndexNLJToHashJoin;

import java.util.HashSet;
import java.util.Set;

public class RuleInstances {
    Set<RewritingRule> ruleApplications = new HashSet<>();

    public void addRuleInstances() {
        // Convert TPAdd(IndexNLJ) to Hash Join
        ruleApplications.add( new RuleConvertTPAddIndexNLJToHashJoin(0.15) );
        // TODO: more rules to be added
    }

}
