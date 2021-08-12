package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils;

import java.util.concurrent.CompletableFuture;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;

public interface CardinalityEstimation
{
	CompletableFuture<Integer> initiateCardinalityEstimation( PhysicalPlan plan ) throws CardinalityEstimationException;
}
