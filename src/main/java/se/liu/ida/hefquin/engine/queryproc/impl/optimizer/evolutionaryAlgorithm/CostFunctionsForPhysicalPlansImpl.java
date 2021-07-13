package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils.CardinalityEstimation;

public class CostFunctionsForPhysicalPlansImpl implements CostFunctionsForPhysicalPlans{
    protected final CostFunctionsForRootOperators costFunctionForRoot;

    public CostFunctionsForPhysicalPlansImpl(CardinalityEstimation cardEstimate) {
        this.costFunctionForRoot = new CostFunctionsForRootOperatorsImpl(cardEstimate);
    }

    public CostOfPhysicalPlan getCostOfPhysicalPlan( final PhysicalPlan pp ) throws QueryOptimizationException {

        final int numberOfRequests = getTotalNumberOfRequests(pp);
        final int shippedRDFTermsForRequests = getTotalShippedRDFTermsForRequests(pp);
        final int shippedRDFVarsForRequests = getTotalShippedRDFVarsForRequests(pp);
        final int shippedRDFTermsForResponses = getTotalShippedRDFTermsForResponses(pp);
        final int shippedRDFVarsForResponses = getTotalShippedVarsForResponses(pp);
        final int getIntermediateResultsSize = getTotalIntermediateResultsSize(pp);

        return new CostOfPhysicalPlanImpl( numberOfRequests, shippedRDFTermsForRequests , shippedRDFVarsForRequests, shippedRDFTermsForResponses, shippedRDFVarsForResponses, getIntermediateResultsSize);
    }

    public int getTotalNumberOfRequests( PhysicalPlan pp ) throws QueryOptimizationException {
        if ( pp.numberOfSubPlans() == 0 ){
            return costFunctionForRoot.getNumberOfRequests( pp );
        }

        int totalNumberOfRequests = 0;
        for ( int i = 0; i < pp.numberOfSubPlans(); i++ ){
            final int costOfRootOp = costFunctionForRoot.getNumberOfRequests( pp.getSubPlan(i) );
            totalNumberOfRequests += costOfRootOp ;
            getTotalNumberOfRequests( pp.getSubPlan(i) );
        }

        return totalNumberOfRequests;
    }

    public int getTotalShippedRDFTermsForRequests( PhysicalPlan pp ) throws QueryOptimizationException {
        int totalShippedRDFTermsForRequests = 0;
        if ( pp.numberOfSubPlans() == 0 ){
            totalShippedRDFTermsForRequests += costFunctionForRoot.getShippedRDFTermsForRequests( pp );
        }

        for ( int i = 0; i < pp.numberOfSubPlans(); i++ ){
            final int costOfRootOp = costFunctionForRoot.getShippedRDFTermsForRequests( pp.getSubPlan(i) );
            totalShippedRDFTermsForRequests += costOfRootOp ;
            getTotalShippedRDFTermsForRequests( pp.getSubPlan(i) );
        }

        return totalShippedRDFTermsForRequests;
    }

    public int getTotalShippedRDFVarsForRequests( PhysicalPlan pp ) throws QueryOptimizationException {
        int totalShippedRDFVarsForRequests = 0;
        if ( pp.numberOfSubPlans() == 0 ){
            totalShippedRDFVarsForRequests += costFunctionForRoot.getShippedRDFVarsForRequests( pp );
        }

        for ( int i = 0; i < pp.numberOfSubPlans(); i++ ){
            final int costOfRootOp = costFunctionForRoot.getShippedRDFVarsForRequests( pp.getSubPlan(i) );
            totalShippedRDFVarsForRequests += costOfRootOp ;
            getTotalShippedRDFVarsForRequests( pp.getSubPlan(i) );
        }

        return totalShippedRDFVarsForRequests;
    }

    public int getTotalShippedRDFTermsForResponses( PhysicalPlan pp ) throws QueryOptimizationException {
        int totalShippedRDFTermsForResponses = 0;
        if ( pp.numberOfSubPlans() == 0 ){
            totalShippedRDFTermsForResponses += costFunctionForRoot.getShippedRDFTermsForResponses( pp );
        }

        for ( int i = 0; i < pp.numberOfSubPlans(); i++ ){
            final int costOfRootOp = costFunctionForRoot.getShippedRDFTermsForResponses( pp.getSubPlan(i) );
            totalShippedRDFTermsForResponses += costOfRootOp ;
            getTotalShippedRDFTermsForResponses( pp.getSubPlan(i) );
        }

        return totalShippedRDFTermsForResponses;
    }

    public int getTotalShippedVarsForResponses( PhysicalPlan pp ) throws QueryOptimizationException {
        int totalShippedVarsForResponses = 0;
        if ( pp.numberOfSubPlans() == 0 ){
            totalShippedVarsForResponses += costFunctionForRoot.getShippedRDFVarsForResponses( pp );
        }

        for ( int i = 0; i < pp.numberOfSubPlans(); i++ ){
            final int costOfRootOp = costFunctionForRoot.getShippedRDFVarsForResponses( pp.getSubPlan(i) );
            totalShippedVarsForResponses += costOfRootOp ;
            getTotalShippedVarsForResponses( pp.getSubPlan(i) );
        }

        return totalShippedVarsForResponses;
    }

    public int getTotalIntermediateResultsSize( PhysicalPlan pp ) throws QueryOptimizationException {
        int totalIntermediateResultsSize = 0;
        if ( pp.numberOfSubPlans() == 0 ){
            totalIntermediateResultsSize += costFunctionForRoot.getIntermediateResultsSize( pp );
        }

        for ( int i = 0; i < pp.numberOfSubPlans(); i++ ){
            final int costOfRootOp = costFunctionForRoot.getIntermediateResultsSize( pp.getSubPlan(i) );
            totalIntermediateResultsSize += costOfRootOp ;
            getTotalIntermediateResultsSize( pp.getSubPlan(i) );
        }
        return totalIntermediateResultsSize;
    }

}
