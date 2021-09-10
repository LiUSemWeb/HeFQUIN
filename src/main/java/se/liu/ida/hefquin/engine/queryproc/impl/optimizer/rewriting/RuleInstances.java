package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting;

import java.util.HashSet;
import java.util.Set;

import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.rules.*;

public class RuleInstances {
    Set<RewritingRule> ruleApplications = new HashSet<>();

    public void addRuleInstances() {

        // Group 1.1. Convert TPAdd to binary join (category: D)
        ruleApplications.add( new RuleConvertTPAddToHashJoin(0.15) );
        ruleApplications.add( new RuleConvertTPAddToSymmetricHashJoin(0.15) );
        ruleApplications.add( new RuleConvertTPAddToNaiveNLJ(0.15) );

        // Group 1.2. Conversion of physical algorithms of TPAdd (category: C)
        // Convert other types of physical algorithm to IndexNLJ, BindJoin, Bind join with FILTER, Bind join with UNION, Bind join with VALUES, respectively
        ruleApplications.add( new RuleConvertTPAddToIndexNLJ(0.2) );
        ruleApplications.add( new RuleConvertTPAddToBindJoin(0.2) );
        ruleApplications.add( new RuleConvertTPAddToBJFILTER(0.2) );
        ruleApplications.add( new RuleConvertTPAddToBJUNION(0.2) );
        ruleApplications.add( new RuleConvertTPAddToBJVALUES(0.2) );

        // Group 1.3. Order tweaking of two unary operator (category: B),
        // Equation (22)
        ruleApplications.add( new RuleChangeOrderOfTwoTPAdd(0.25) );
        ruleApplications.add( new RuleChangeOrderOfTPAddAndBGPAdd(0.25) );
        ruleApplications.add( new RuleChangeOrderOfTwoBGPAdd(0.25) );
        ruleApplications.add( new RuleChangeOrderOfBGPAddAndTPAdd(0.25) );

        // Group 1.4. Merge two operators into one
        // Merge a TPAdd and a BGP request (with the same fm) into one request (category: A), B' = B U {tp}
        // Equation (13)
        ruleApplications.add( new RuleMergeTPAddAndBGPReqIntoOneRequest(0.3) );
        // Merge a tpAdd and a graph pattern request (with the same fm: SPARQL endpoint) into one request (category: A),  (P AND tp)
        // Equation (20)
        ruleApplications.add( new RuleMergeTPAddAndGraphPatternReqIntoOneRequest(0.3) );
        // Merge a tpAdd and a bgpAdd (with the same fm) into one bgpAdd (category: A), B' = B U {tp}
        // Equation (14)
        ruleApplications.add( new RuleMergeTPAddAndBGPAddIntoBGPAdd(0.3) );

        // rewriting rules for bgpAdd
        // Group 2.1. Convert BGPAdd to binary join (category: D)
        ruleApplications.add( new RuleConvertBGPAddToHashJoin(0.15) );
        ruleApplications.add( new RuleConvertBGPAddToSymmetricHashJoin(0.15) );
        ruleApplications.add( new RuleConvertGBPAddToNaiveNLJ(0.15) );

        // Group 2.2. Conversion of physical algorithms of bgpAdd (category: C)
        ruleApplications.add( new RuleConvertBGPAddToIndexNLJ(0.2) );
        ruleApplications.add( new RuleConvertBGPAddToBJFILTER(0.2) );
        ruleApplications.add( new RuleConvertBGPAddToBJUNION(0.2) );
        ruleApplications.add( new RuleConvertBGPAddToBJVALUES(0.2) );

        // Group 2.4. Merge two operators into one
        // Merge a bgpAdd and a BGP request (with the same fm) into one request (category: A), B' = B1 U B2
        // Equation (6)
        ruleApplications.add( new RuleMergeBGPAddAndBGPReqIntoOneRequest(0.3) );
        // Merge a bgpAdd and a triple pattern request (with the same fm) into one BGP request (category: A), B' = B U {tp}
        // Equation (12)
        ruleApplications.add( new RuleMergeBGPAddAndTPReqIntoOneRequest(0.3) );
        // Merge a bgpAdd and a graph pattern request (with the same fm: SPARQL endpoint) into one request (category: A),  (P AND B)
        // Equation (21)
        ruleApplications.add( new RuleMergeBGPAddAndGraphPatternReqIntoOneRequest(0.3) );
        // Merge two bgpAdd (with the same fm) into one bgpAdd (category: A), B' = B1 U B2
        // The physical operator depends on which bgpAdd?
        // Equation (7)
        ruleApplications.add( new RuleMergeTwoBGPAddIntoOneBGPAdd(0.3) );

        /*
        // Group 2.5: divide one bgpAdd to multiple operators (category: E)
        ruleApplications.add( new RuleDivideBGPAddToMultiTPAdd(0.1) );
         */
        // TODO: more rules to be added
    }

}
