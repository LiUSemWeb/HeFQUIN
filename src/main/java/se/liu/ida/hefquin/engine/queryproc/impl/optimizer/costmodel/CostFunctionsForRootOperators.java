package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.costmodel;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizationException;

public interface CostFunctionsForRootOperators {

    int determineNumberOfRequests( final PhysicalPlan pp ) throws QueryOptimizationException;

    int determineShippedRDFTermsForRequests( final PhysicalPlan pp ) throws QueryOptimizationException;

    int determineShippedVarsForRequests( final PhysicalPlan pp ) throws QueryOptimizationException;

    int determineShippedRDFTermsForResponses( final PhysicalPlan pp ) throws QueryOptimizationException;

    int determineShippedVarsForResponses( final PhysicalPlan pp ) throws QueryOptimizationException;

    int determineIntermediateResultsSize( final PhysicalPlan pp ) throws QueryOptimizationException;

}
