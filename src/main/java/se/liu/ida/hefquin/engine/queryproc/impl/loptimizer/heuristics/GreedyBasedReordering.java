package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics;

import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanUtils;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayJoin;
import se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.HeuristicForLogicalOptimization;
import se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics.formula.FormulaForComputingSelectivity;
import se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics.formula.JoinAwareWeightedUnboundVariableCount;
import se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics.utils.QueryAnalyzer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Implement a greedy algorithm to determine the join order of sub-plans.
 * For a given logical plan, when a sub-plan with LogicalOpMultiwayJoin or LogicalOpJoin as the root operator is found,
 * the order of sub-plans is determined by the unrestrictiveness score (so-called cost):
 * starting with the sub-plan with the smallest cost and continuing with the next sub-plan with the smallest cost.
 *
 * Any of the defined formulas can be used for calculating the cost value to find the optimal order.
 * One example of such formulas is {@link JoinAwareWeightedUnboundVariableCount}.
 */
public class GreedyBasedReordering implements HeuristicForLogicalOptimization {
    protected final FormulaForComputingSelectivity formula;

    public GreedyBasedReordering( final FormulaForComputingSelectivity formula ) {
        this.formula = formula;
    }

    @Override
    public LogicalPlan apply( final LogicalPlan inputPlan ) {
        final int numberOfSubPlans = inputPlan.numberOfSubPlans();
        if ( numberOfSubPlans == 0 ) {
            return inputPlan;
        }

        // Apply this heuristic recursively to all sub-plans.
        final LogicalPlan[] newSubPlans = new LogicalPlan[numberOfSubPlans];
        boolean noChanges = true; // set to false if the heuristic changes any of the subplans
        for ( int i = 0; i < numberOfSubPlans; i++ ) {
            final LogicalPlan oldSubPlan = inputPlan.getSubPlan(i);
            newSubPlans[i] = apply(oldSubPlan);
            if ( ! newSubPlans[i].equals(oldSubPlan) ) {
                noChanges = false;
            }
        }

        final LogicalPlan newPlan;
        final LogicalOperator rootOp = inputPlan.getRootOperator();
        if ( noChanges )
            newPlan = inputPlan;
        else {
            newPlan = LogicalPlanUtils.createPlanWithSubPlans(rootOp, newSubPlans);
        }

        if ( rootOp instanceof LogicalOpJoin || rootOp instanceof LogicalOpMultiwayJoin) {
            return reorderSubPlans( newPlan );
        }
        else {
            return newPlan;
        }
    }

    protected LogicalPlan reorderSubPlans( final LogicalPlan inputPlan ) {
        // Initialize candidatePlans
        final List<QueryAnalyzer> candidatePlans = new ArrayList<>();
        for ( int i = 0; i < inputPlan.numberOfSubPlans(); i ++ ) {
            final QueryAnalyzer subPlan = new QueryAnalyzer( inputPlan.getSubPlan(i) );
            candidatePlans.add( subPlan );
        }

        // Find the next subPlan with the lowest cost and put it into selectedSubPlans
        final List<QueryAnalyzer> selectedSubPlans = new ArrayList<>();
        while ( !candidatePlans.isEmpty() ) {
            final QueryAnalyzer bestSubPlan = findNextPlan(selectedSubPlans, candidatePlans);
            selectedSubPlans.add(bestSubPlan);
            candidatePlans.remove(bestSubPlan);
        }

        return constructBinaryPlan(selectedSubPlans);
    }

    protected QueryAnalyzer findNextPlan( final List<QueryAnalyzer> selectedSubPlans, final List<QueryAnalyzer> candidatePlans ) {
        double costOfBestSubPlan = Double.MAX_VALUE;
        QueryAnalyzer bestSubPlan = null;
        for ( final QueryAnalyzer subPlan : candidatePlans) {
            final double selectivity = formula.estimate( selectedSubPlans, subPlan );
            if ( selectivity < costOfBestSubPlan ) {
                costOfBestSubPlan = selectivity;
                bestSubPlan = subPlan;
            }
        }
        return bestSubPlan;
    }

    protected LogicalPlan constructBinaryPlan( final List<QueryAnalyzer> selectedSubPlans ) {
        final Iterator<QueryAnalyzer> it = selectedSubPlans.iterator();
        LogicalPlan output = LogicalPlanUtils.createPlanWithBinaryJoin( it.next().getPlan(), it.next().getPlan() );
        while ( it.hasNext() ) {
            output = LogicalPlanUtils.createPlanWithBinaryJoin( output, it.next().getPlan() );
        }
        return output;
    }

}
