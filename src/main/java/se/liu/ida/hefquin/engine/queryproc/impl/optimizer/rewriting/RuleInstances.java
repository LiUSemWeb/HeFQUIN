package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting;

import java.util.HashSet;
import java.util.Set;

import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.rules.*;

public class RuleInstances {
    Set<RewritingRule> ruleApplications = new HashSet<>();

    public void addRuleInstances() {

        // Group 1. Convert TPAdd to binary join (category: C)
        ruleApplications.add( new RuleConvertTPAddToHashJoin(0.15) );
        ruleApplications.add( new RuleConvertTPAddToSymmetricHashJoin(0.15) );
        ruleApplications.add( new RuleConvertTPAddToNaiveNLJ(0.15) );

        // Group 2. Conversion of physical algorithms of TPAdd (category: C)
        // For TPAdd, convert other types of physical algorithm to IndexNLJ
        ruleApplications.add( new RuleConvertTPAddBindJoinToIndexNLJ(0.2) );
        ruleApplications.add( new RuleConvertTPAddBJFILTERToIndexNLJ(0.2) );
        ruleApplications.add( new RuleConvertTPAddBJUNIONToIndexNLJ(0.2) );
        ruleApplications.add( new RuleConvertTPAddBJVALUESToIndexNLJ(0.2) );

        // For TPAdd, convert IndexNLJ to other types of physical algorithm
        ruleApplications.add( new RuleConvertTPAddIndexNLJToBindJoin(0.2) );
        ruleApplications.add( new RuleConvertTPAddIndexNLJToBJFILTER(0.2) );
        ruleApplications.add( new RuleConvertTPAddIndexNLJToBJUNION(0.2) );
        ruleApplications.add( new RuleConvertTPAddIndexNLJToBJVALUES(0.2) );
        // TODO: more rules to be added (conversion between different algorithms of bind join)

        // Group 3. Order tweaking of two unary operator (category: B),
        // Equation (22)
        ruleApplications.add( new RuleChangeOrderOfTwoTPAdd(0.25) );
        ruleApplications.add( new RuleChangeOrderOfTPAddAndBGPAdd(0.25) );
        ruleApplications.add( new RuleChangeOrderOfTwoBGPAdd(0.25) );
        ruleApplications.add( new RuleChangeOrderOfBGPAddAndTPAdd(0.25) );

        // Group 5. Merge a TPAdd and a BGP request (with the same fm) into one request (category: A), B' = B U {tp}
        // Equation (13)
        ruleApplications.add( new RuleMergeTPAddAndBGPReqIntoOneRequest(0.3) );

        // Group 6. Merge a tpAdd and a bgpAdd (with the same fm) into one bgpAdd (category: A), B' = B U {tp}
        // Equation (14)
        ruleApplications.add( new RuleMergeTPAddAndBGPAddIntoBGPAdd(0.3) );

        // Group 7. Merge a tpAdd and a graph pattern request (with the same fm: SPARQL endpoint) into one request (category: A),  (P AND tp)
        // Equation (20)
        ruleApplications.add( new RuleMergeTPAddAndGraphPatternReqIntoOneRequest(0.3) );

        // TODO: more rules to be added
    }

}
