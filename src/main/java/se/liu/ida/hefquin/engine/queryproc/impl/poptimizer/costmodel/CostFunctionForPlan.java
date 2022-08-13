package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.costmodel;

import java.util.concurrent.CompletableFuture;

import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;

/**
 * Implementations of this interface represent functions that determine
 * some form of cost value for any given (physical) query plan.
 */
public interface CostFunctionForPlan
{
	CompletableFuture<Integer> initiateCostEstimation( PhysicalPlan plan );
}
