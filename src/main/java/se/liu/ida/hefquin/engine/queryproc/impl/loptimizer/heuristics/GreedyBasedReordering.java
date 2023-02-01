package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics;

import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanUtils;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithBinaryRootImpl;
import se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.HeuristicForLogicalOptimization;
import se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics.formula.JoinAwareWeightedUnboundVariableCount;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GreedyBasedReordering implements HeuristicForLogicalOptimization {

    @Override
    public LogicalPlan apply( final LogicalPlan inputPlan ) {
        final int numberOfSubPlans = inputPlan.numberOfSubPlans();
        if ( numberOfSubPlans == 0 || numberOfSubPlans == 1 ) {
            return inputPlan;
        }

        final LogicalOperator rootOp = inputPlan.getRootOperator();
        if ( rootOp instanceof LogicalOpJoin || rootOp instanceof LogicalOpMultiwayJoin) {
            return reorderSubPlans(inputPlan);
        }
        else {
            // For any other type of root operator, simply apply this heuristic recursively to all of the subplans.
            final LogicalPlan[] newSubPlans = new LogicalPlan[numberOfSubPlans];
            boolean noChanges = true; // set to false if the heuristic changes any of the subplans
            for ( int i = 0; i < numberOfSubPlans; i++ ) {
                final LogicalPlan oldSubPlan = inputPlan.getSubPlan(i);
                newSubPlans[i] = apply(oldSubPlan);
                if ( ! newSubPlans[i].equals(oldSubPlan) ) {
                    noChanges = false;
                }
            }

            if ( noChanges )
                return inputPlan;
            else
                return LogicalPlanUtils.createPlanWithSubPlans(rootOp, newSubPlans);
        }

    }

    protected LogicalPlan reorderSubPlans( final LogicalPlan inputPlan ) {
        final List<LogicalPlan> candidatePlans = new ArrayList<>();
        final List<LogicalPlan> selectedSubPlans = new ArrayList<>();

        // Find the first subQuery and put it into selectedSubPlans
        double cost = -1;
        LogicalPlan plan = null;
        for ( int i = 0; i < inputPlan.numberOfSubPlans(); i ++ ) {
            LogicalPlan lop = inputPlan.getSubPlan(i);
            candidatePlans.add(lop);
            final double selectivity = estimateSelectivity( selectedSubPlans, lop);

            if ( cost < 0 || selectivity < cost ) {
                cost = selectivity;
                plan = lop;
            }
        }
        selectedSubPlans.add(plan);
        candidatePlans.remove(plan);

        // Find the next subQuery from the remaining subPlans
        while ( !candidatePlans.isEmpty() ) {
            findNextPlan(selectedSubPlans, candidatePlans);
        }

        return constructBinaryPlan(selectedSubPlans);
    }

    protected void findNextPlan( final List<LogicalPlan> selectedSubPlans, final List<LogicalPlan> candidatePlans ) {
        double cost = -1;
        LogicalPlan plan = null;
        for (LogicalPlan lop : candidatePlans) {
            final double selectivity = estimateSelectivity(selectedSubPlans, lop);
            if (cost < 0 || selectivity < cost) {
                cost = selectivity;
                plan = lop;
            }
        }

        selectedSubPlans.add(plan);
        candidatePlans.remove(plan);
    }

    protected LogicalPlan constructBinaryPlan( final List<LogicalPlan> selectedSubPlans ) {
        if( selectedSubPlans.size() == 0 ) {
            return null;
        }
        else if( selectedSubPlans.size() == 1 ) {
            return selectedSubPlans.get(0);
        }

        final Iterator<LogicalPlan> i = selectedSubPlans.iterator();
        LogicalPlan output = LogicalPlanUtils.createPlanWithBinaryJoin( i.next(), i.next() );
        while ( i.hasNext() ) {
            output = LogicalPlanUtils.createPlanWithBinaryJoin( output, i.next() );
        }
        return output;
    }

    protected double estimateSelectivity( final List<LogicalPlan> selectedPlans, final LogicalPlan nextPossiblePlan ) {
        return JoinAwareWeightedUnboundVariableCount.estimate(selectedPlans, nextPossiblePlan);
    }

}
