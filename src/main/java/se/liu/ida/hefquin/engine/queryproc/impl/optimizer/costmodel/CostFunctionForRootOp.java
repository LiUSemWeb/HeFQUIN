package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.costmodel;

import java.util.concurrent.CompletableFuture;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;

/**
 * Implementations of this interface represent functions that determine
 * some form of cost value for the root node of any given query plan.
 */
public interface CostFunctionForRootOp
{
	CompletableFuture<Integer> initiateCostEstimation( PhysicalPlan plan );
}