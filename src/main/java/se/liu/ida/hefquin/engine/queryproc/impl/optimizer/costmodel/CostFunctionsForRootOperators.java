package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.costmodel;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.CostEstimationException;

public interface CostFunctionsForRootOperators {

    int determineNumberOfRequests( final PhysicalPlan pp ) throws CostEstimationException;

    int determineShippedRDFTermsForRequests( final PhysicalPlan pp ) throws CostEstimationException;

    int determineShippedVarsForRequests( final PhysicalPlan pp ) throws CostEstimationException;

    int determineShippedRDFTermsForResponses( final PhysicalPlan pp ) throws CostEstimationException;

    int determineShippedVarsForResponses( final PhysicalPlan pp ) throws CostEstimationException;

    int determineIntermediateResultsSize( final PhysicalPlan pp ) throws CostEstimationException;

}
