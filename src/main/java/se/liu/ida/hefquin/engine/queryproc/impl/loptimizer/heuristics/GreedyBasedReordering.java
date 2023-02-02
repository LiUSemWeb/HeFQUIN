package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics;

import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanUtils;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayJoin;
import se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.HeuristicForLogicalOptimization;
import se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics.formula.JoinAwareWeightedUnboundVariableCount;
import se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics.utils.Query_Analyzer;

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

    @Override
    public LogicalPlan apply( final LogicalPlan inputPlan ) {
        final int numberOfSubPlans = inputPlan.numberOfSubPlans();
        if ( numberOfSubPlans == 0 || numberOfSubPlans == 1 ) {
            return inputPlan;
        }

        final LogicalOperator rootOp = inputPlan.getRootOperator();
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

        LogicalPlan newPlan;
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
        final List<Query_Analyzer> candidatePlans = new ArrayList<>();
        final List<Query_Analyzer> selectedSubPlans = new ArrayList<>();

        // Find the first subQuery and put it into selectedSubPlans
        double costOfBestSubPlan = Double.MAX_VALUE;
        Query_Analyzer bestSubPlan = null;
        for ( int i = 0; i < inputPlan.numberOfSubPlans(); i ++ ) {
            final Query_Analyzer subPlan = new Query_Analyzer( inputPlan.getSubPlan(i) );
            candidatePlans.add( subPlan );
            final double selectivity = estimateSelectivity( selectedSubPlans, subPlan);

            if ( selectivity < costOfBestSubPlan ) {
                costOfBestSubPlan = selectivity;
                bestSubPlan = subPlan;
            }
        }
        selectedSubPlans.add(bestSubPlan);
        candidatePlans.remove(bestSubPlan);

        // Find the next subQuery from the remaining subPlans
        while ( !candidatePlans.isEmpty() ) {
            findNextPlan(selectedSubPlans, candidatePlans);
        }

        return constructBinaryPlan(selectedSubPlans);
    }

    protected void findNextPlan( final List<Query_Analyzer> selectedSubPlans, final List<Query_Analyzer> candidatePlans ) {
        double costOfBestSubPlan = Double.MAX_VALUE;
        Query_Analyzer bestSubPlan = null;
        for ( final Query_Analyzer subPlan : candidatePlans) {
            final double selectivity = estimateSelectivity(selectedSubPlans, subPlan);
            if ( selectivity < costOfBestSubPlan ) {
                costOfBestSubPlan = selectivity;
                bestSubPlan = subPlan;
            }
        }

        selectedSubPlans.add(bestSubPlan);
        candidatePlans.remove(bestSubPlan);
    }

    protected LogicalPlan constructBinaryPlan( final List<Query_Analyzer> selectedSubPlans ) {
        final Iterator<Query_Analyzer> it = selectedSubPlans.iterator();
        LogicalPlan output = LogicalPlanUtils.createPlanWithBinaryJoin( it.next().getPlan(), it.next().getPlan() );
        while ( it.hasNext() ) {
            output = LogicalPlanUtils.createPlanWithBinaryJoin( output, it.next().getPlan() );
        }
        return output;
    }

    protected double estimateSelectivity( final List<Query_Analyzer> selectedPlans, final Query_Analyzer nextPossiblePlan ) {
        return JoinAwareWeightedUnboundVariableCount.estimate(selectedPlans, nextPossiblePlan);
    }

}
