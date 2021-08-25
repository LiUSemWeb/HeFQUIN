package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting;

import java.util.HashSet;
import java.util.Set;

import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.rules.RuleConvertTPAddBJFILTERToHashJoin;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.rules.RuleConvertTPAddIndexNLJToHashJoin;

public class RuleInstances {
    Set<RewritingRule> ruleApplications = new HashSet<>();

    public void addRuleInstances() {
        // Convert TPAdd to Hash Join
        ruleApplications.add( new RuleConvertTPAddIndexNLJToHashJoin(0.15) );
        ruleApplications.add( new RuleConvertTPAddBJFILTERToHashJoin(0.15) );
        // TODO: more rules to be added
    }

}
