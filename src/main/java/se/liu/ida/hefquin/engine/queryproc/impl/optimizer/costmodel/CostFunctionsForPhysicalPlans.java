package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.costmodel;

import java.util.concurrent.CompletableFuture;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.CostEstimationException;

public interface CostFunctionsForPhysicalPlans
{
    CompletableFuture<CostOfPhysicalPlan> initiateCostEstimation( PhysicalPlan pp ) throws CostEstimationException;

    int determineTotalNumberOfRequests( PhysicalPlan pp ) throws CostEstimationException;

    int determineTotalShippedRDFTermsForRequests( PhysicalPlan pp ) throws CostEstimationException;

    int determineTotalShippedVarsForRequests( PhysicalPlan pp ) throws CostEstimationException;

    int determineTotalShippedRDFTermsForResponses( PhysicalPlan pp ) throws CostEstimationException;

    int determineTotalShippedVarsForResponses( PhysicalPlan pp ) throws CostEstimationException;

    int determineTotalIntermediateResultsSize( PhysicalPlan pp ) throws CostEstimationException;
}
