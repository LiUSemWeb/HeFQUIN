package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils.CardinalityEstimation;

public class CostFunctionsForPhysicalPlansImpl implements CostFunctionsForPhysicalPlans{
    protected final CostFunctionsForRootOperators costFunctionForRoot;

    public CostFunctionsForPhysicalPlansImpl( final CardinalityEstimation cardEstimate ) {
        this.costFunctionForRoot = new CostFunctionsForRootOperatorsImpl(cardEstimate);
    }

    @Override
    public CostOfPhysicalPlan getCostOfPhysicalPlan( final PhysicalPlan pp ) throws QueryOptimizationException {

        final int numberOfRequests = determineTotalNumberOfRequests(pp);
        final int shippedRDFTermsForRequests = determineTotalShippedRDFTermsForRequests(pp);
        final int shippedRDFVarsForRequests = determineTotalShippedVarsForRequests(pp);
        final int shippedRDFTermsForResponses = determineTotalShippedRDFTermsForResponses(pp);
        final int shippedRDFVarsForResponses = determineTotalShippedVarsForResponses(pp);
        final int getIntermediateResultsSize = determineTotalIntermediateResultsSize(pp);

        return new CostOfPhysicalPlanImpl( numberOfRequests, shippedRDFTermsForRequests , shippedRDFVarsForRequests, shippedRDFTermsForResponses, shippedRDFVarsForResponses, getIntermediateResultsSize);
    }

    @Override
    public int determineTotalNumberOfRequests( final PhysicalPlan pp ) throws QueryOptimizationException {
        if ( pp.numberOfSubPlans() == 0 ){
            return costFunctionForRoot.determineNumberOfRequests( pp );
        }

        int totalNumberOfRequests = 0;
        for ( int i = 0; i < pp.numberOfSubPlans(); i++ ){
            final int costOfRootOp = costFunctionForRoot.determineNumberOfRequests( pp.getSubPlan(i) );
            totalNumberOfRequests += costOfRootOp ;
            determineTotalNumberOfRequests( pp.getSubPlan(i) );
        }

        return totalNumberOfRequests;
    }

    @Override
    public int determineTotalShippedRDFTermsForRequests( final PhysicalPlan pp ) throws QueryOptimizationException {
        if ( pp.numberOfSubPlans() == 0 ){
            return costFunctionForRoot.determineShippedRDFTermsForRequests( pp );
        }

        int totalShippedRDFTermsForRequests = 0;
        for ( int i = 0; i < pp.numberOfSubPlans(); i++ ){
            final int costOfRootOp = costFunctionForRoot.determineShippedRDFTermsForRequests( pp.getSubPlan(i) );
            totalShippedRDFTermsForRequests += costOfRootOp ;
            determineTotalShippedRDFTermsForRequests( pp.getSubPlan(i) );
        }

        return totalShippedRDFTermsForRequests;
    }

    @Override
    public int determineTotalShippedVarsForRequests( final PhysicalPlan pp ) throws QueryOptimizationException {
        int totalShippedRDFVarsForRequests = 0;
        if ( pp.numberOfSubPlans() == 0 ){
            totalShippedRDFVarsForRequests += costFunctionForRoot.determineShippedVarsForRequests( pp );
        }

        for ( int i = 0; i < pp.numberOfSubPlans(); i++ ){
            final int costOfRootOp = costFunctionForRoot.determineShippedVarsForRequests( pp.getSubPlan(i) );
            totalShippedRDFVarsForRequests += costOfRootOp ;
            determineTotalShippedVarsForRequests( pp.getSubPlan(i) );
        }

        return totalShippedRDFVarsForRequests;
    }

    @Override
    public int determineTotalShippedRDFTermsForResponses( final PhysicalPlan pp ) throws QueryOptimizationException {
        int totalShippedRDFTermsForResponses = 0;
        if ( pp.numberOfSubPlans() == 0 ){
            totalShippedRDFTermsForResponses += costFunctionForRoot.determineShippedRDFTermsForResponses( pp );
        }

        for ( int i = 0; i < pp.numberOfSubPlans(); i++ ){
            final int costOfRootOp = costFunctionForRoot.determineShippedRDFTermsForResponses( pp.getSubPlan(i) );
            totalShippedRDFTermsForResponses += costOfRootOp ;
            determineTotalShippedRDFTermsForResponses( pp.getSubPlan(i) );
        }

        return totalShippedRDFTermsForResponses;
    }

    @Override
    public int determineTotalShippedVarsForResponses( final PhysicalPlan pp ) throws QueryOptimizationException {
        int totalShippedVarsForResponses = 0;
        if ( pp.numberOfSubPlans() == 0 ){
            totalShippedVarsForResponses += costFunctionForRoot.determineShippedVarsForResponses( pp );
        }

        for ( int i = 0; i < pp.numberOfSubPlans(); i++ ){
            final int costOfRootOp = costFunctionForRoot.determineShippedVarsForResponses( pp.getSubPlan(i) );
            totalShippedVarsForResponses += costOfRootOp ;
            determineTotalShippedVarsForResponses( pp.getSubPlan(i) );
        }

        return totalShippedVarsForResponses;
    }

    @Override
    public int determineTotalIntermediateResultsSize( final PhysicalPlan pp ) throws QueryOptimizationException {
        int totalIntermediateResultsSize = 0;
        if ( pp.numberOfSubPlans() == 0 ){
            totalIntermediateResultsSize += costFunctionForRoot.determineIntermediateResultsSize( pp );
        }

        for ( int i = 0; i < pp.numberOfSubPlans(); i++ ){
            final int costOfRootOp = costFunctionForRoot.determineIntermediateResultsSize( pp.getSubPlan(i) );
            totalIntermediateResultsSize += costOfRootOp ;
            determineTotalIntermediateResultsSize( pp.getSubPlan(i) );
        }
        return totalIntermediateResultsSize;
    }

}
