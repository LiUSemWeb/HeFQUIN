package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting;

import java.util.HashSet;
import java.util.Set;

import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.rules.*;

public class RuleInstances {
    public Set<RewritingRule> ruleInstances;

    public RuleInstances() {
        this.ruleInstances = addRuleInstances();
    }

    protected Set<RewritingRule> addRuleInstances() {
        final Set<RewritingRule> ruleInstances = new HashSet<>();

        // Group 1. Convert TPAdd to binary join (category: C)
        ruleInstances.add( new RuleConvertTPAddToHashJoin(0.15) );
        ruleInstances.add( new RuleConvertTPAddToSymmetricHashJoin(0.15) );
        ruleInstances.add( new RuleConvertTPAddToNaiveNLJ(0.15) );

        // Group 2. Conversion of physical algorithms of TPAdd (category: C)
        // Convert other types of physical algorithm to IndexNLJ, BindJoin, Bind join with FILTER, Bind join with UNION, Bind join with VALUES, respectively
        ruleInstances.add( new RuleConvertTPAddToIndexNLJ(0.2) );
        ruleInstances.add( new RuleConvertTPAddToBindJoin(0.2) );
        ruleInstances.add( new RuleConvertTPAddToBJFILTER(0.2) );
        ruleInstances.add( new RuleConvertTPAddToBJUNION(0.2) );
        ruleInstances.add( new RuleConvertTPAddToBJVALUES(0.2) );

        // Group 3. Order tweaking of two unary operator (category: B),
        // Equation (22)
        ruleInstances.add( new RuleChangeOrderOfTwoTPAdd(0.25) );
        ruleInstances.add( new RuleChangeOrderOfTPAddAndBGPAdd(0.25) );
        ruleInstances.add( new RuleChangeOrderOfTwoBGPAdd(0.25) );
        ruleInstances.add( new RuleChangeOrderOfBGPAddAndTPAdd(0.25) );

        // Group 4. Merge two operators into one
        // 4.1 Merge a TPAdd and a BGP request (with the same fm) into one request (category: A), B' = B U {tp}
        // Equation (13)
        ruleInstances.add( new RuleMergeTPAddAndBGPReqIntoOneRequest(0.3) );
        // 4.2 Merge a tpAdd and a graph pattern request (with the same fm: SPARQL endpoint) into one request (category: A),  (P AND tp)
        // Equation (20)
        ruleInstances.add( new RuleMergeTPAddAndGraphPatternReqIntoOneRequest(0.3) );
        // 4.3 Merge a tpAdd and a bgpAdd (with the same fm) into one bgpAdd (category: A), B' = B U {tp}
        // Equation (14)
        ruleInstances.add( new RuleMergeTPAddAndBGPAddIntoBGPAdd(0.3) );

        // TODO: more rules to be added
        return ruleInstances;
    }

}
