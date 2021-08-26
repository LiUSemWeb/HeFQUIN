package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting;

import java.util.HashSet;
import java.util.Set;

import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.rules.*;

public class RuleInstances {
    Set<RewritingRule> ruleApplications = new HashSet<>();

    public void addRuleInstances() {
        // Convert TPAdd to binary join (category: C)
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

        // Conversion of physical algorithms of TPAdd (category: C)
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

        // Order tweaking of two TPAdd (category: B)
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

        // Order tweaking of TPAdd and BGPAdd (category: B)
        ruleApplications.add( new RuleChangeOrderOfTPAddIndexNLJAndBGPAddIndexNLJ(0.25) );
        // TODO: more rules to be added

        // Merge a TPAdd and a request (with the same fm) into one BGP request (category: A), B' = B U {tp}
        ruleApplications.add( new RuleMergeTPAddIndexNLJAndRequestIntoOneRequest(0.3) );
        ruleApplications.add( new RuleMergeTPAddBJFILTERAndRequestIntoOneRequest(0.3) );
        ruleApplications.add( new RuleMergeTPAddBJUNIONAndRequestIntoOneRequest(0.3) );
        ruleApplications.add( new RuleMergeTPAddBJVALUESAndRequestIntoOneRequest(0.3) );

        // Merge a tpAdd and a bgpAdd (with the same fm) into one bgpAdd (category: A), B' = B U {tp}
        ruleApplications.add( new RuleMergeTPAddIndexNLJAndBGPAddIndexNLJIntoBGPAdd(0.3) );
        ruleApplications.add( new RuleMergeTPAddBJFILTERAndBGPAddIndexNLJIntoBGPAdd(0.3) );
        // TODO: more rules to be added

    }

}
