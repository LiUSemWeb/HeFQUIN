package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics.formula;

import org.apache.jena.graph.Node;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics.utils.Join_Analyzer;
import se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics.utils.SubQuery_Analyzer;
import se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics.utils.Var_Analyzer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JoinAwareWeightedUnboundVariableCount {
    public static final double W_S = 1; // weight for subject variables
    public static final double W_P = 0.1; // weight for predicate variables
    public static final double W_O = 0.8; // weight for object variables

    public static final double J_Tc=0.6; // weight for chain join
    public static final double J_Ts=0.5; // weight for star join
    public static final double J_Tu=1; // weight for unusual join
    public static final double J_Tn=0; // no join

    public static double estimate( final List<LogicalPlan> selectedPlans, LogicalPlan lop ) {
        Set<Node> bindings = new HashSet<>();
        for ( LogicalPlan plan : selectedPlans ) {
            final SubQuery_Analyzer analyzer = new SubQuery_Analyzer( plan );
            bindings = Var_Analyzer.addBinds( analyzer.getSubs(), analyzer.getPreds(), analyzer.getObjs() );
        }

        return calculateCost( bindings, new SubQuery_Analyzer(lop) );
    }

    private static double calculateCost( final Set<Node> bindings, final SubQuery_Analyzer subQuery ) {
        final double unboundVarsCost = getUnboundVarsCost(
                subQuery.getSubs(),
                subQuery.getPreds(),
                subQuery.getObjs(),
                bindings);
        final double joinCost = JoinsWeight(
                subQuery.getSubs(),
                subQuery.getPreds(),
                subQuery.getObjs());
        return unboundVarsCost / joinCost;
    }

    private static double getUnboundVarsCost( final List<Node> vars_s, final List<Node> vars_p,
                                              final List<Node> vars_o, final Set<Node> bindings) {
        final Set<Node> varsTotal = new HashSet<>();
        final int totalSubs = Var_Analyzer.calculateVars(vars_s, varsTotal, bindings);
        final int totalObjs = Var_Analyzer.calculateVars(vars_o, varsTotal, bindings);
        final int totalPreds = Var_Analyzer.calculateVars(vars_p, varsTotal, bindings);
        return calculateTripleWeights(totalSubs, totalPreds, totalObjs);
    }

    public static double calculateTripleWeights( final int totalSubs, final int totalPreds, final int totalObjs) {
        return totalSubs * W_S + totalPreds * W_P + totalObjs * W_O;
    }

    public static double JoinsWeight( final List<Node> vars_s, final List<Node> vars_p, final List<Node> vars_o ) {
        return 1
                + Join_Analyzer.getStarJoins(vars_s, vars_o, J_Ts)
                + Join_Analyzer.getChainJoins(vars_s, vars_o, J_Tc)
                + Join_Analyzer.getUnusualJoins(vars_s, vars_p, vars_o, J_Tu);
    }

}