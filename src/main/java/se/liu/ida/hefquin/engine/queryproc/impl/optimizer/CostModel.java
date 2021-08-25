package se.liu.ida.hefquin.engine.queryproc.impl.optimizer;

import java.util.concurrent.CompletableFuture;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;

public interface CostModel
{
	CompletableFuture<Double> initiateCostEstimation( PhysicalPlan p ) throws CostEstimationException;
}
