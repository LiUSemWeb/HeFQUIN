package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.costmodel;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;

/**
 * Implementations of this interface represent functions that determine
 * some form of cost value for any given (physical) query plan.
 */
public interface CostFunctionForPlan
{
	/**
	 * A function for estimating the cost of a plan.
	 * @param visitedPlans: 
	 * Record a set of sub-plans (of plan) that have been visited during the cost estimation.
	 * This set can be used for checking if a subplan has been visited or not.
	 */
	CompletableFuture<Integer> initiateCostEstimation( final Set<PhysicalPlan> visitedPlans, final PhysicalPlan plan );
}
