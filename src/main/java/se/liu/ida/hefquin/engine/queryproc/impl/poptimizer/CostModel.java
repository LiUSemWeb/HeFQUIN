package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer;

import java.util.concurrent.CompletableFuture;

import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;

public interface CostModel
{
	CompletableFuture<Double> initiateCostEstimation( PhysicalPlan p ) throws CostEstimationException;
}
