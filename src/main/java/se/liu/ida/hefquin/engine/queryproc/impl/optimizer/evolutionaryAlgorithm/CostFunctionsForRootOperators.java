package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizationException;

public interface CostFunctionsForRootOperators {

    int getNumberOfRequests( final PhysicalPlan pp ) throws QueryOptimizationException;

    int getShippedRDFTermsForRequests( final PhysicalPlan pp ) throws QueryOptimizationException;

    int getShippedRDFVarsForRequests( final PhysicalPlan pp ) throws QueryOptimizationException;

    int getShippedRDFTermsForResponses( final PhysicalPlan pp ) throws QueryOptimizationException;

    int getShippedRDFVarsForResponses( final PhysicalPlan pp ) throws QueryOptimizationException;

    int getIntermediateResultsSize( final PhysicalPlan pp ) throws QueryOptimizationException;

}
