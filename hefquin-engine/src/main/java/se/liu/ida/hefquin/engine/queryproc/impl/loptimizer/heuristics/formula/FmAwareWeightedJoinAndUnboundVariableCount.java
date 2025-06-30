package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics.formula;

import org.apache.jena.graph.Node;

import se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics.utils.JoinAnalyzer;
import se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics.utils.QueryAnalyzer;
import se.liu.ida.hefquin.federation.FederationMember;

import java.util.List;
import java.util.Set;

/**
 * Estimate selectivity of a subplan by considering
 * - 1. the number of new unbound variables
 * - 2. the position of new unbound variables (sub, pred, or obj)
 * - 3. number of joins contained in this subplan
 * - 4. types of joins contained in this subplan (star join, chain join, unusual join)
 *
 * - 5. number of joins between this subplan with all selected plans
 * - 6. type of joins between this subplan with all selected plans
 *      - star join, chain join, or unusual join
 *      - join within the same federation member or across different federation members
 *
 * This formula is an extended version of the formula that is implemented in {@link JoinAwareWeightedUnboundVariableCount}
 */

public class FmAwareWeightedJoinAndUnboundVariableCount extends JoinAwareWeightedUnboundVariableCount {

    @Override
    protected double calculateCost( final List<QueryAnalyzer> selectedPlans, final Set<Node> boundVariables, final QueryAnalyzer subPlan ) {
        return weightedUnboundVarsCount(boundVariables, subPlan)
                /
                ( weightedJoinsCountInNextPlan( subPlan )
                        * weightedJoinsCountAcrossPlans( selectedPlans, subPlan )
                );
    }

    protected double weightedJoinsCountAcrossPlans( final List<QueryAnalyzer> selectedPlans, final QueryAnalyzer nextPlan ) {
        double fmAwareStarJoinsCount = 0;
        double fmAwareChainJoinsCount = 0;
        double fmAwareUnusualJoinsCount = 0;

        for ( final QueryAnalyzer plan: selectedPlans ) {
            final Set<Node> intersection = plan.getUniqueVars();
            intersection.retainAll( nextPlan.getUniqueVars() );
            // check whether there exists intersection
            if ( intersection.isEmpty() ) {
                continue;
            }

            final double weightedFmCount = weightedFmCount( plan.getFms(), nextPlan.getFms() );

            fmAwareStarJoinsCount += ( JoinAnalyzer.countNumOfJoinsWithSameSub(plan, nextPlan) + JoinAnalyzer.countNumOfJoinsWithSameObj(plan, nextPlan) ) / weightedFmCount;
            fmAwareChainJoinsCount += JoinAnalyzer.countNumOfChainJoins(plan, nextPlan) / weightedFmCount;
            fmAwareUnusualJoinsCount += JoinAnalyzer.countNumOfUnusualJoins(plan, nextPlan) / weightedFmCount;
        }

        return 1 + fmAwareStarJoinsCount * J_Ts + fmAwareChainJoinsCount * J_Tc + fmAwareUnusualJoinsCount * J_Tu;
    }

    protected double weightedFmCount( final List<FederationMember> fms_l, final List<FederationMember> fms_r ){
        double count = 0;
        for ( final FederationMember fm_l: fms_l ) {
            for ( final FederationMember fm_r: fms_r ) {
                if ( fm_l.equals(fm_r) ) {
                    count += 1;
                }
                else
                    count += 0.1;
            }
        }
        return count;
    }

}
