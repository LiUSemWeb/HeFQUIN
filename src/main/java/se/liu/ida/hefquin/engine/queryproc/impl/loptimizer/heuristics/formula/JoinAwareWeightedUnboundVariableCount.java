package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics.formula;

import org.apache.jena.graph.Node;
import se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics.utils.Join_Analyzer;
import se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics.utils.Query_Analyzer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Estimate selectivity of a subplan by considering
 * - the number of new unbound variables
 * - the position of new unbound variables (sub, pred, or obj)
 * - number of joins contained in this subplan
 * - types of joins contained in this subplan (star join, chain join, unusal join)
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

    public static double estimate( final List<Query_Analyzer> selectedPlans, final Query_Analyzer subPlan ) {
        // Add bound variables of all selected plans to a set
        Set<Node> boundVariables = new HashSet<>();

        // This part can be optimized if all formulas need to consider bound variables:
        // adding to this set directly when adding a new subPlan to 'selectedPlans'
        for ( Query_Analyzer plan : selectedPlans ) {
            boundVariables = addBinds( plan.getSubs(), plan.getPreds(), plan.getObjs() );
        }

        return calculateCost( boundVariables, subPlan );
    }

    // Formula (7) in paper "Heuristics-based Query Reordering for Federated Queries in SPARQL 1.1 and SPARQL-LD"
    private static double calculateCost( final Set<Node> bindings, final Query_Analyzer subPlan ) {
        final double unboundVarsCost = getUnboundVarsCost(
                subPlan.getSubs(),
                subPlan.getPreds(),
                subPlan.getObjs(),
                bindings);
        final double joinCost = joinsWeight(
                subPlan.getSubs(),
                subPlan.getPreds(),
                subPlan.getObjs());
        return unboundVarsCost / joinCost;
    }

    private static double getUnboundVarsCost( final List<Node> vars_s, final List<Node> vars_p,
                                              final List<Node> vars_o, final Set<Node> boundVariables) {
        final Set<Node> varsTotal = new HashSet<>();
        // Calculate the number of (unique) unbound subjects, predicates and objects
        final int totalSubs = calculateVars(vars_s, varsTotal, boundVariables);
        final int totalObjs = calculateVars(vars_o, varsTotal, boundVariables);
        final int totalPreds = calculateVars(vars_p, varsTotal, boundVariables);

        return calculateTripleWeights(totalSubs, totalPreds, totalObjs);
    }

    /**
     * Creates a collection with all variables that have been currently calculated
     * and adds the bound variables to bindings
     * @param vars A list of variables, can be subs, preds or objects
     * @param varsTotal A list of all variables that have been currently calculated in this subquery
     * @param boundVariables All bound variables (including variables in selected plans and calculated part of this subquery)
     * @return The number of unbounded variables
     */
    private static int calculateVars( final List<Node> vars, final Set<Node> varsTotal, final Set<Node> boundVariables ) {
        varsTotal.addAll( vars );
        varsTotal.removeAll( boundVariables );

        boundVariables.addAll( varsTotal );
        return varsTotal.size();
    }

    private static Set<Node> addBinds( final List<Node> vars_s,
                                      final List<Node> vars_p,
                                      final List<Node> vars_o) {
        final Set<Node> tempBoundVars = new HashSet<>();
        tempBoundVars.addAll(vars_s);
        tempBoundVars.addAll(vars_o);
        tempBoundVars.addAll(vars_p);
        return tempBoundVars;
    }

    /**
     * Calculate the weighed sum of subs, preds and objs
     */
    private static double calculateTripleWeights( final int totalSubs, final int totalPreds, final int totalObjs) {
        return totalSubs * W_S + totalPreds * W_P + totalObjs * W_O;
    }

    /**
     * Calculate the weighed sum of different types of joins
     */
    private static double joinsWeight( final List<Node> vars_s, final List<Node> vars_p, final List<Node> vars_o ) {
        return 1
                + Join_Analyzer.getNumOfStarJoins(vars_s, vars_o) * J_Ts
                + Join_Analyzer.getNumOfChainJoins(vars_s, vars_o) * J_Tc
                + Join_Analyzer.getNumOfUnusualJoins(vars_s, vars_p, vars_o)* J_Tu;
    }

}