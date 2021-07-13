package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizationException;

public interface CostFunctionsForPhysicalPlans {

    int getTotalNumberOfRequests( PhysicalPlan pp ) throws QueryOptimizationException;

    int getTotalShippedRDFTermsForRequests( PhysicalPlan pp ) throws QueryOptimizationException;

    int getTotalShippedRDFVarsForRequests( PhysicalPlan pp ) throws QueryOptimizationException;

    int getTotalShippedRDFTermsForResponses( PhysicalPlan pp ) throws QueryOptimizationException;

    int getTotalShippedVarsForResponses( PhysicalPlan pp ) throws QueryOptimizationException;

    int getTotalIntermediateResultsSize( PhysicalPlan pp ) throws QueryOptimizationException;

}
