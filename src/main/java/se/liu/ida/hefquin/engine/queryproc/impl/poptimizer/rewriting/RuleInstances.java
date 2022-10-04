package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.rewriting;

import java.util.HashSet;
import java.util.Set;

import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.rewriting.rules.*;

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

        // Group 1.2. Conversion of physical algorithms of TPAdd (category: C)
        // Convert other types of physical algorithm to IndexNLJ, BindJoin, Bind join with FILTER, Bind join with UNION, Bind join with VALUES, respectively
        ruleInstances.add( new RuleConvertTPAddToIndexNLJ(0.2) );
        ruleInstances.add( new RuleConvertTPAddToBindJoin(0.2) );
        ruleInstances.add( new RuleConvertTPAddToBJFILTER(0.2) );
        ruleInstances.add( new RuleConvertTPAddToBJUNION(0.2) );
        ruleInstances.add( new RuleConvertTPAddToBJVALUES(0.2) );

        // Group 1.3. Order tweaking of two unary operator (category: B),
        // Equation (22)
        ruleInstances.add( new RuleChangeOrderOfTwoTPAdd(0.25) );
        ruleInstances.add( new RuleChangeOrderOfTPAddAndBGPAdd(0.25) );
        ruleInstances.add( new RuleChangeOrderOfTwoBGPAdd(0.25) );
        ruleInstances.add( new RuleChangeOrderOfBGPAddAndTPAdd(0.25) );

        // Group 1.4. Merge two operators into one
        // Merge a TPAdd and a BGP request (with the same fm) into one request (category: A), B' = B U {tp}
        // Equation (13)
        ruleInstances.add( new RuleMergeTPAddOfBGPReqIntoOneRequest(0.3) );
        //ruleInstances.add( new RuleMergeTPAddOfTPReqIntoOneRequest(0.3) );
        // 4.2 Merge a tpAdd and a graph pattern request (with the same fm: SPARQL endpoint) into one request (category: A),  (P AND tp)
        // Equation (20)
        ruleInstances.add( new RuleMergeTPAddOfGraphPatternReqIntoOneRequest(0.3) );
        // 4.3 Merge a tpAdd and a bgpAdd (with the same fm) into one bgpAdd (category: A), B' = B U {tp}
        // Equation (14)
        ruleInstances.add( new RuleMergeTPAddOfBGPAddIntoBGPAdd(0.3) );

        // rewriting rules for bgpAdd
        // Group 2.1. Convert BGPAdd to binary join (category: D)
        ruleInstances.add( new RuleConvertBGPAddToHashJoin(0.15) );
        ruleInstances.add( new RuleConvertBGPAddToSymmetricHashJoin(0.15) );
        ruleInstances.add( new RuleConvertGBPAddToNaiveNLJ(0.15) );

        // Group 2.2. Conversion of physical algorithms of bgpAdd (category: C)
        ruleInstances.add( new RuleConvertBGPAddToIndexNLJ(0.2) );
        ruleInstances.add( new RuleConvertBGPAddToBJFILTER(0.2) );
        ruleInstances.add( new RuleConvertBGPAddToBJUNION(0.2) );
        ruleInstances.add( new RuleConvertBGPAddToBJVALUES(0.2) );

        // Group 2.3. Merge two operators into one
        // Merge a bgpAdd and a BGP request (with the same fm) into one request (category: A), B' = B1 U B2
        // Equation (6)
        ruleInstances.add( new RuleMergeBGPAddOfBGPReqIntoOneBGPReq(0.3) );
        // Merge a bgpAdd and a triple pattern request (with the same fm) into one BGP request (category: A), B' = B U {tp}
        // Equation (12)
        ruleInstances.add( new RuleMergeBGPAddOfTPReqIntoOneBGPReq(0.3) );
        // Merge a bgpAdd and a graph pattern request (with the same fm: SPARQL endpoint) into one request (category: A),  (P AND B)
        // Equation (21)
        ruleInstances.add( new RuleMergeBGPAddOfGraphPatternReqIntoOneRequest(0.3) );
        // Merge two bgpAdd (with the same fm) into one bgpAdd (category: A), B' = B1 U B2
        // The physical operator depends on which bgpAdd?
        // Equation (7)
        ruleInstances.add( new RuleMergeTwoBGPAddIntoOneBGPAdd(0.3) );

        // Group 2.4: divide one bgpAdd to multiple operators (category: E)
        ruleInstances.add( new RuleDivideBGPAddToMultiTPAdd(0.1) );

        // Rewriting rules for join:
        // Group 3.1. order tweaking of subPlans of join  (category: B)
        ruleInstances.add( new RuleChangeOrderOfTwoSubPlansOfJOIN(0.25) );
        ruleInstances.add( new RuleDistributeJOINOverUNION(0.25) );
        // apply associative property of join (3.1.3)
        ruleInstances.add( new RuleChangeOrderOfThreeSubPlansOfJOIN(0.25) );

        // Group 3.2. Merge join of two subPlans into one (category: A)
        // (3.2.1)
        ruleInstances.add( new RuleMergeJoinOfOneTPReqIntoTPAdd(0.3) );
        // (3.2.2)
        ruleInstances.add( new RuleMergeJoinOfOneBGPReqIntoBGPAdd(0.3) );
        ruleInstances.add( new RuleMergeJoinOfTwoBGPReqIntoOneReq(0.3) );
        ruleInstances.add( new RuleMergeJoinOfTwoPatternReqIntoOneReq(0.3) );
        // fm should support tp request, also support BGP request
        ruleInstances.add( new RuleMergeJoinOfTwoTPReqIntoOneBGPReq(0.3) );
        ruleInstances.add( new RuleMergeJoinOfTPReqAndBGPReqIntoOneBGPReq(0.3) );
        // if one of the sub plans of join is a request, then it can be rewritten as tpAdd and bgpAdd with one subPlan of the other subPlan
        // A shortcut of the combination of rule (3.1.3) and (3.2.1)
        ruleInstances.add( new RuleChangeOrderAndMergeJoinOfTPReqIntoTPAdd(0.3) );
        // A shortcut of the combination of rule (3.1.3) and (3.2.2)
        ruleInstances.add( new RuleChangeOrderAndMergeJoinOfBGPReqIntoBGPAdd(0.3) );

        // Rewriting rules of union:
        // Group 4.1. order tweaking of subPlans of union (category: B)
        ruleInstances.add( new RuleChangeOrderOfTwoSubPlansOfUNION(0.25) );
        ruleInstances.add( new RuleChangeOrderOfThreeSubPlansOfUNION(0.25) );

        //Group 4.2. Merge union of two subPlans into one (category: A)
        ruleInstances.add( new RuleMergeUnionOfTwoIdenticalSubPlansIntoOne(0.3) );
        ruleInstances.add( new RuleMergeUNIONOfTwoPatternReqIntoOneReq(0.3) );

        // Rewriting rules of BGP Request
        // Group 5.1 fm support BGP request, also support tp request (category: E)
        ruleInstances.add( new RuleDivideBGPReqIntoJoinOfTPReqs(0.1) );
        ruleInstances.add( new RuleDivideBGPReqIntoMultiTPAdds(0.1) );
        //ruleInstances.add( new RuleDivideBGPReqIntoBGPAddOfReq(0.1) );

        // Rewriting rules of Nary operator
        // Group 6.1. Merge multiway join(union) of multiple subPlans into one (category: A)
        ruleInstances.add( new RuleMergeMultiwayJoinOfMultiIdenticalSubPlansIntoOne(0.3) );
        ruleInstances.add( new RuleMergeMultiwayUnionOfMultiIdenticalSubPlansIntoOne(0.3) );
        // Removes a child join of a multiway join by merging it directly into the multiway join
        ruleInstances.add( new RuleMergeChildJoinIntoMultiwayJoin(0.3) );
        ruleInstances.add( new RuleMergeChildUnionIntoMultiwayUnion(0.3) );

        // Group 6.2. Divide multiway join(union) (category: E)
        ruleInstances.add( new RuleDivideMultiwayJoinToJoinOfSubPlans(0.1) );
        ruleInstances.add( new RuleDivideMultiwayUnionToUnionOfSubPlans(0.1) );

        // TODO: add corresponding unit tests
        return ruleInstances;
    }

}