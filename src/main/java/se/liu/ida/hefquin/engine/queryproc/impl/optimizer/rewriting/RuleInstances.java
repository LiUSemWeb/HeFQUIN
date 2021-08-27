package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting;

import java.util.HashSet;
import java.util.Set;

import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.rules.*;

public class RuleInstances {
    Set<RewritingRule> ruleApplications = new HashSet<>();

    public void addRuleInstances() {
        // 1. Convert TPAdd to binary join (category: C)
        // Convert TPAdd to Hash Join
        ruleApplications.add( new RuleConvertTPAddIndexNLJToHashJoin(0.15) );
        ruleApplications.add( new RuleConvertTPAddBJFILTERToHashJoin(0.15) );
        ruleApplications.add( new RuleConvertTPAddBJUNIONToHashJoin(0.15) );
        ruleApplications.add( new RuleConvertTPAddBJVALUESToHashJoin(0.15) );
        ruleApplications.add( new RuleConvertTPAddBindJoinToHashJoin(0.15) );

        // Convert TPAdd to Symmetric Hash Join
        ruleApplications.add( new RuleConvertTPAddIndexNLJToSymmetricHashJoin(0.15) );
        ruleApplications.add( new RuleConvertTPAddBJFILTERToSymmetricHashJoin(0.15) );
        ruleApplications.add( new RuleConvertTPAddBJUNIONToSymmetricHashJoin(0.15) );
        ruleApplications.add( new RuleConvertTPAddBJVALUESToSymmetricHashJoin(0.15) );
        ruleApplications.add( new RuleConvertTPAddBJToSymmetricHashJoin(0.15) );

        // Convert TPAdd to NaiveNLJ
        ruleApplications.add( new RuleConvertTPAddIndexNLJToNaiveNLJ(0.15) );
        ruleApplications.add( new RuleConvertTPAddBJFILTERToNaiveNLJ(0.15) );
        ruleApplications.add( new RuleConvertTPAddBJUNIONToNaiveNLJ(0.15) );
        ruleApplications.add( new RuleConvertTPAddBJVALUESToNaiveNLJ(0.15) );
        ruleApplications.add( new RuleConvertTPAddBindJoinToNaiveNLJ(0.15) );

        // 2. Conversion of physical algorithms of TPAdd (category: C)
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

        // 3. Order tweaking of two TPAdd (category: B),
        // Equation (22)
        ruleApplications.add( new RuleChangeOrderOfTwoTPAddIndexNLJ(0.25) );
        ruleApplications.add( new RuleChangeOrderOfTwoTPAddBindJoin(0.25) );
        ruleApplications.add( new RuleChangeOrderOfTwoTPAddBJFILTER(0.25) );
        ruleApplications.add( new RuleChangeOrderOfTwoTPAddBJUNION(0.25) );
        ruleApplications.add( new RuleChangeOrderOfTwoTPAddBJVALUES(0.25) );

        ruleApplications.add( new RuleChangeOrderOfTPAddBindJoinAndTPAddIndexNLJ(0.25) );
        ruleApplications.add( new RuleChangeOrderOfTPAddBJFILTERAndIndexNLJ(0.25) );
        ruleApplications.add( new RuleChangeOrderOfTPAddBJUNIONAndTPAddIndexNLJ(0.25) );
        ruleApplications.add( new RuleChangeOrderOfTPAddBJVALUESAndTPAddIndexNLJ(0.25) );

        ruleApplications.add( new RuleChangeOrderOfTPAddIndexNLJAndTPAddBindJoin(0.25) );
        ruleApplications.add( new RuleChangeOrderOfTPAddIndexNLJAndTPAddBJFILTER(0.25) );
        ruleApplications.add( new RuleChangeOrderOfTPAddIndexNLJAndTPAddBJUNION(0.25) );
        ruleApplications.add( new RuleChangeOrderOfTPAddIndexNLJAndTPAddBJVALUES(0.25) );

        // 4. Order tweaking of TPAdd and BGPAdd (category: B)
        // Equation (23)
        ruleApplications.add( new RuleChangeOrderOfTPAddIndexNLJAndBGPAddIndexNLJ(0.25) );
        // TODO: more rules to be added (different variations of physical algorithm)

        // 5. Merge a TPAdd and a BGP request (with the same fm) into one request (category: A), B' = B U {tp}
        // Equation (13)
        ruleApplications.add( new RuleMergeTPAddIndexNLJAndBGPReqIntoOneRequest(0.3) );
        ruleApplications.add( new RuleMergeTPAddBJFILTERAndBGPReqIntoOneRequest(0.3) );
        ruleApplications.add( new RuleMergeTPAddBJUNIONAndBGPReqIntoOneRequest(0.3) );
        ruleApplications.add( new RuleMergeTPAddBJVALUESAndBGPReqIntoOneRequest(0.3) );

        // 6. Merge a tpAdd and a bgpAdd (with the same fm) into one bgpAdd (category: A), B' = B U {tp}
        // Equation (14)
        ruleApplications.add( new RuleMergeTPAddIndexNLJAndBGPAddIndexNLJIntoBGPAdd(0.3) );
        ruleApplications.add( new RuleMergeTPAddBJFILTERAndBGPAddIndexNLJIntoBGPAdd(0.3) );
        ruleApplications.add( new RuleMergeTPAddBJUNIONAndBGPAddIndexNLJIntoBGPAdd(0.3) );
        ruleApplications.add( new RuleMergeTPAddBJVALUESAndBGPAddIndexNLJIntoBGPAdd(0.3) );
        // TODO: more rules to be added (different physical algorithms for bgpAdd)

        // 7. Merge a tpAdd and a graph pattern request (with the same fm: SPARQL endpoint) into one request (category: A),  (P AND tp)
        // Equation (20)
        ruleApplications.add( new RuleMergeTPAddIndexNLJAndGraphPatternReqIntoOneRequest(0.3) );
        ruleApplications.add( new RuleMergeTPAddBJFILTERAndGraphPatternReqIntoOneRequest(0.3) );
        ruleApplications.add( new RuleMergeTPAddBJUNIONAndGraphPatternReqIntoOneRequest(0.3) );
        ruleApplications.add( new RuleMergeTPAddBJVALUESAndGraphPatternReqIntoOneRequest(0.3) );

        // TODO: more rules to be added
    }

}
