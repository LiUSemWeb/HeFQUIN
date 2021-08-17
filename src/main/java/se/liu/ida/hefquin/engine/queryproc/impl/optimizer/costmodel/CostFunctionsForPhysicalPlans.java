package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.costmodel;

import java.util.concurrent.CompletableFuture;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.CostEstimationException;

public interface CostFunctionsForPhysicalPlans
{
    CompletableFuture<CostOfPhysicalPlan> initiateCostEstimation( PhysicalPlan pp ) throws CostEstimationException;
}
