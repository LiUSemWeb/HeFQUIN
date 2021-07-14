package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizationException;

public interface CostFunctionsForPhysicalPlans {

    CostOfPhysicalPlan determineCostOfPhysicalPlan( final PhysicalPlan pp ) throws QueryOptimizationException;

    int determineTotalNumberOfRequests( PhysicalPlan pp ) throws QueryOptimizationException;

    int determineTotalShippedRDFTermsForRequests( PhysicalPlan pp ) throws QueryOptimizationException;

    int determineTotalShippedVarsForRequests( PhysicalPlan pp ) throws QueryOptimizationException;

    int determineTotalShippedRDFTermsForResponses( PhysicalPlan pp ) throws QueryOptimizationException;

    int determineTotalShippedVarsForResponses( PhysicalPlan pp ) throws QueryOptimizationException;

    int determineTotalIntermediateResultsSize( PhysicalPlan pp ) throws QueryOptimizationException;

}
