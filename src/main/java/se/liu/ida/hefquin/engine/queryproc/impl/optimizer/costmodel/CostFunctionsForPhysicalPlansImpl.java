package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.costmodel;

import java.util.concurrent.CompletableFuture;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.CardinalityEstimation;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.CostEstimationException;

public class CostFunctionsForPhysicalPlansImpl implements CostFunctionsForPhysicalPlans
{
    protected final CostFunctionsForRootOperators costFunctionForRoot;

    public CostFunctionsForPhysicalPlansImpl( final CardinalityEstimation cardEstimate ) {
        this.costFunctionForRoot = new CostFunctionsForRootOperatorsImpl(cardEstimate);
    }

    @Override
    public CompletableFuture<CostOfPhysicalPlan> initiateCostEstimation( final PhysicalPlan pp ) throws CostEstimationException {

        final int numberOfRequests = determineTotalNumberOfRequests(pp);
        final int shippedRDFTermsForRequests = determineTotalShippedRDFTermsForRequests(pp);
        final int shippedRDFVarsForRequests = determineTotalShippedVarsForRequests(pp);
        final int shippedRDFTermsForResponses = determineTotalShippedRDFTermsForResponses(pp);
        final int shippedRDFVarsForResponses = determineTotalShippedVarsForResponses(pp);
        final int getIntermediateResultsSize = determineTotalIntermediateResultsSize(pp);

        final CostOfPhysicalPlan result = new CostOfPhysicalPlanImpl( numberOfRequests, shippedRDFTermsForRequests , shippedRDFVarsForRequests, shippedRDFTermsForResponses, shippedRDFVarsForResponses, getIntermediateResultsSize);
        return CompletableFuture.completedFuture(result);
    }

    protected int determineTotalNumberOfRequests( final PhysicalPlan pp ) throws CostEstimationException {
        int totalNumberOfRequests = costFunctionForRoot.determineNumberOfRequests( pp );
        if ( pp.numberOfSubPlans() == 0 ){
            return totalNumberOfRequests;
        }
        for ( int i = 0; i < pp.numberOfSubPlans(); i++ ){
            totalNumberOfRequests += determineTotalNumberOfRequests( pp.getSubPlan(i) );
        }

        return totalNumberOfRequests;
    }

    protected int determineTotalShippedRDFTermsForRequests( final PhysicalPlan pp ) throws CostEstimationException {
        int totalShippedRDFTermsForRequests = costFunctionForRoot.determineShippedRDFTermsForRequests( pp );

        if ( pp.numberOfSubPlans() == 0 ){
            return totalShippedRDFTermsForRequests;
        }
        for ( int i = 0; i < pp.numberOfSubPlans(); i++ ){
            totalShippedRDFTermsForRequests += determineTotalShippedRDFTermsForRequests( pp.getSubPlan(i) );
        }

        return totalShippedRDFTermsForRequests;
    }

    protected int determineTotalShippedVarsForRequests( final PhysicalPlan pp ) throws CostEstimationException {
        int totalShippedVarsForRequests = costFunctionForRoot.determineShippedVarsForRequests( pp );

        if ( pp.numberOfSubPlans() == 0 ){
            return totalShippedVarsForRequests;
        }
        for ( int i = 0; i < pp.numberOfSubPlans(); i++ ){
            totalShippedVarsForRequests += determineTotalShippedVarsForRequests( pp.getSubPlan(i) );
        }

        return totalShippedVarsForRequests;
    }

    protected int determineTotalShippedRDFTermsForResponses( final PhysicalPlan pp ) throws CostEstimationException {
        int totalShippedRDFTermsForResponses = costFunctionForRoot.determineShippedRDFTermsForResponses( pp );

        if ( pp.numberOfSubPlans() == 0 ){
            return totalShippedRDFTermsForResponses;
        }
        for ( int i = 0; i < pp.numberOfSubPlans(); i++ ){
            totalShippedRDFTermsForResponses += determineTotalShippedRDFTermsForResponses( pp.getSubPlan(i) );
        }

        return totalShippedRDFTermsForResponses;
    }

    protected int determineTotalShippedVarsForResponses( final PhysicalPlan pp ) throws CostEstimationException {
        int totalShippedVarsForResponses = costFunctionForRoot.determineShippedVarsForResponses( pp );

        if ( pp.numberOfSubPlans() == 0 ){
            return totalShippedVarsForResponses;
        }
        for ( int i = 0; i < pp.numberOfSubPlans(); i++ ){
            totalShippedVarsForResponses += determineTotalShippedVarsForResponses( pp.getSubPlan(i) );
        }

        return totalShippedVarsForResponses;
    }

    protected int determineTotalIntermediateResultsSize( final PhysicalPlan pp ) throws CostEstimationException {
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
