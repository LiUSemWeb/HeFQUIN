package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.utils;

import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.*;

import java.util.*;

public class CardinalityBasedJoinPlanOptimizerUtils
{
    /**
     * Extracts all request operators from the given plan, assuming
     * that this plan is a sub-plan of a source assignment (hence,
     * assuming that this plan can only be either a single request,
     * a filter over a request, or a union with requests).
     *
     * The extracted request operators will be order in the order in
     * which they are discovered by a depth-first traversal of the
     * given plan.
     */
    public static List<LogicalOpRequest<?,?>> extractAllRequestOpsFromSourceAssignment(final PhysicalPlan plan) {
        final PhysicalOperator pop = plan.getRootOperator();
        if (pop instanceof PhysicalOpRequest) {
            return Arrays.asList( ((PhysicalOpRequest<?,?>) pop).getLogicalOperator() );
        }
        else if (pop instanceof PhysicalOpFilter
                && plan.getSubPlan(0).getRootOperator() instanceof PhysicalOpRequest) {
            return Arrays.asList( ((PhysicalOpRequest<?,?>) plan.getSubPlan(0).getRootOperator()).getLogicalOperator() );
        }
        else if ( pop instanceof PhysicalOpLocalToGlobal
                && plan.getSubPlan(0).getRootOperator() instanceof PhysicalOpRequest
        ) {
            return Arrays.asList( ((PhysicalOpRequest<?,?>) plan.getSubPlan(0).getRootOperator()).getLogicalOperator() );
        }
        else if ( pop instanceof PhysicalOpLocalToGlobal
                && plan.getSubPlan(0).getRootOperator() instanceof PhysicalOpFilter
                && plan.getSubPlan(0).getSubPlan(0).getRootOperator() instanceof PhysicalOpRequest
        ) {
            return Arrays.asList( ((PhysicalOpRequest<?,?>) plan.getSubPlan(0).getSubPlan(0).getRootOperator()).getLogicalOperator() );
        }
        else if (pop instanceof PhysicalOpBinaryUnion || pop instanceof PhysicalOpMultiwayUnion) {
            final List<LogicalOpRequest<?,?>> reqOps = new ArrayList<>();
            final int numOfSubPlans = plan.numberOfSubPlans();
            for (int i = 0; i < numOfSubPlans; i++) {
                reqOps.addAll( extractAllRequestOpsFromSourceAssignment(plan.getSubPlan(i)) );
            }
            return reqOps;
        }
        else
            throw new IllegalArgumentException("Unsupported type of subquery in source assignment (" + pop.getClass().getName() + ")");
    }

}
