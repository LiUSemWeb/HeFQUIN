package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils.CardinalityEstimation;

public class CostFunctionsForPhysicalPlansImpl implements CostFunctionsForPhysicalPlans{
    protected final CostFunctionsForRootOperators costFunctionForRoot;
    // protected final PhysicalPlanCostCache physicalPlanCostCache;

    public CostFunctionsForPhysicalPlansImpl( final CardinalityEstimation cardEstimate ) {
        this.costFunctionForRoot = new CostFunctionsForRootOperatorsImpl(cardEstimate);
        // this.physicalPlanCostCache = new PhysicalPlanCostCache();
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
        /*
        CostOfPhysicalPlan costOfPhysicalPlan = physicalPlanCostCache.get(pp);
        if ( costOfPhysicalPlan != null ){
            return costOfPhysicalPlan.getNumberOfRequests();
        }
        */
        int totalNumberOfRequests = costFunctionForRoot.determineNumberOfRequests( pp );
        if ( pp.numberOfSubPlans() == 0 ){
            return totalNumberOfRequests;
        }
        for ( int i = 0; i < pp.numberOfSubPlans(); i++ ){
            totalNumberOfRequests += determineTotalNumberOfRequests( pp.getSubPlan(i) );
            // totalNumberOfRequests += getCostOfPhysicalPlan( pp.getSubPlan(i) ).getNumberOfRequests();
        }
        // TODO: How to add totalNumberOfRequests of (sub)PhysicalPlans to this cache?
        // Perhaps move the cache checking and adding to method 'getCostOfPhysicalPlan'.
        // In this case, call getCostOfPhysicalPlan within for loop.

        return totalNumberOfRequests;
    }

    @Override
    public int determineTotalShippedRDFTermsForRequests( final PhysicalPlan pp ) throws QueryOptimizationException {
        int totalShippedRDFTermsForRequests = costFunctionForRoot.determineShippedRDFTermsForRequests( pp );

        if ( pp.numberOfSubPlans() == 0 ){
            return totalShippedRDFTermsForRequests;
        }
        for ( int i = 0; i < pp.numberOfSubPlans(); i++ ){
            totalShippedRDFTermsForRequests += determineTotalShippedRDFTermsForRequests( pp.getSubPlan(i) );
        }

        return totalShippedRDFTermsForRequests;
    }

    @Override
    public int determineTotalShippedVarsForRequests( final PhysicalPlan pp ) throws QueryOptimizationException {
        int totalShippedVarsForRequests = costFunctionForRoot.determineShippedVarsForRequests( pp );

        if ( pp.numberOfSubPlans() == 0 ){
            return totalShippedVarsForRequests;
        }
        for ( int i = 0; i < pp.numberOfSubPlans(); i++ ){
            totalShippedVarsForRequests += determineTotalShippedVarsForRequests( pp.getSubPlan(i) );
        }

        return totalShippedVarsForRequests;
    }

    @Override
    public int determineTotalShippedRDFTermsForResponses( final PhysicalPlan pp ) throws QueryOptimizationException {
        int totalShippedRDFTermsForResponses = costFunctionForRoot.determineShippedRDFTermsForResponses( pp );

        if ( pp.numberOfSubPlans() == 0 ){
            return totalShippedRDFTermsForResponses;
        }
        for ( int i = 0; i < pp.numberOfSubPlans(); i++ ){
            totalShippedRDFTermsForResponses += determineTotalShippedRDFTermsForResponses( pp.getSubPlan(i) );
        }

        return totalShippedRDFTermsForResponses;
    }

    @Override
    public int determineTotalShippedVarsForResponses( final PhysicalPlan pp ) throws QueryOptimizationException {
        int totalShippedVarsForResponses = costFunctionForRoot.determineShippedVarsForResponses( pp );

        if ( pp.numberOfSubPlans() == 0 ){
            return totalShippedVarsForResponses;
        }
        for ( int i = 0; i < pp.numberOfSubPlans(); i++ ){
            totalShippedVarsForResponses += determineTotalShippedVarsForResponses( pp.getSubPlan(i) );
        }

        return totalShippedVarsForResponses;
    }

    @Override
    public int determineTotalIntermediateResultsSize( final PhysicalPlan pp ) throws QueryOptimizationException {
        int totalIntermediateResultsSize = costFunctionForRoot.determineIntermediateResultsSize( pp );
        if ( pp.numberOfSubPlans() == 0 ){
            return totalIntermediateResultsSize;
        }

        for ( int i = 0; i < pp.numberOfSubPlans(); i++ ){
            totalIntermediateResultsSize += determineTotalIntermediateResultsSize( pp.getSubPlan(i) );
        }
        return totalIntermediateResultsSize;
    }

}
