package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics.formula;

import org.apache.jena.graph.Node;
import se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics.utils.JoinAnalyzer;
import se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics.utils.QueryAnalyzer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Estimate selectivity of a subplan by considering
 * - the number of new unbound variables
 * - the position of new unbound variables (sub, pred, or obj)
 * - number of joins contained in this subplan
 * - types of joins contained in this subplan (star join, chain join, unusual join)
 * To get unbound variables, the set of bindings in selectedPlans needs to be considered.
 *
 * This implementation is based on the formula (7) that is proposed in paper
 * "Heuristics-based Query Reordering for Federated Queries in SPARQL 1.1 and SPARQL-LD"
 */

public class JoinAwareWeightedUnboundVariableCount {
    protected static final double W_S = 1; // weight for subject variables
    protected static final double W_P = 0.1; // weight for predicate variables
    protected static final double W_O = 0.8; // weight for object variables

    protected static final double J_Tc = 0.6; // weight for chain join
    protected static final double J_Ts = 0.5; // weight for star join
    protected static final double J_Tu = 1; // weight for unusual join

    public static double estimate( final List<QueryAnalyzer> selectedPlans, final QueryAnalyzer subPlan ) {
        // Add bound variables of all selected plans to a set
        final Set<Node> boundVariables = new HashSet<>();

        // This part can be optimized if all formulas need to consider bound variables:
        // adding to this set directly when adding a new subPlan to 'selectedPlans'
        for ( final QueryAnalyzer plan : selectedPlans ) {
            boundVariables.addAll( plan.getSubs() );
            boundVariables.addAll( plan.getPreds() );
            boundVariables.addAll( plan.getObjs() );
        }

        return calculateCost( boundVariables, subPlan );
    }

    // Formula (7) in paper "Heuristics-based Query Reordering for Federated Queries in SPARQL 1.1 and SPARQL-LD"
    private static double calculateCost( final Set<Node> boundVariables, final QueryAnalyzer subPlan ) {
        return weightedUnboundVarsCount(boundVariables, subPlan)
                / weightedJoinsCountInNextPlan( subPlan );
    }

    /**
     * Calculate the weighed sum of unbound variables based on different positions (subs, preds and objs)
     * @param boundVariables All bound variables (i.e., variables that are bound through any sub-plan of the selectedPlans)
     * @param plan The next candidate plan
     */
    protected static double weightedUnboundVarsCount( final Set<Node> boundVariables, final QueryAnalyzer plan ) {
        // Calculate the number of (unique) unbound subjects, predicates and objects
        final int totalSubs = countUnboundVars( plan.getSubs(), boundVariables);
        final int totalObjs = countUnboundVars( plan.getObjs(), boundVariables);
        final int totalPreds = countUnboundVars( plan.getPreds(), boundVariables);

        return totalSubs * W_S + totalPreds * W_P + totalObjs * W_O;
    }

    /**
     * For a given list of variables, count the number of unique variables that are not in the set of 'boundVariables'
     */
    protected static int countUnboundVars( final List<Node> vars, final Set<Node> boundVariables ) {
        final Set<Node> uniqueVars = new HashSet<>(vars);
        uniqueVars.removeAll(boundVariables);

        return uniqueVars.size();
    }

    /**
     * Calculate the weighed sum of different types of joins
     */
    protected static double weightedJoinsCountInNextPlan( final QueryAnalyzer plan ) {
        return 1
                + ( JoinAnalyzer.countNumOfJoinsWithSameSub(plan) + JoinAnalyzer.countNumOfJoinsWithSameObj(plan)) * J_Ts
                + JoinAnalyzer.countNumOfChainJoins(plan) * J_Tc
                + JoinAnalyzer.countNumOfUnusualJoins(plan) * J_Tu;
    }

}