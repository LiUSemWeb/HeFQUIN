package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils.CardinalityEstimation;

public class CostFunctionsForPhysicalPlansImpl implements CostFunctionsForPhysicalPlans{
    protected final CostFunctionsForRootOperatorsImpl costFunctionForRoot;

    public CostFunctionsForPhysicalPlansImpl(CardinalityEstimation cardEstimate) {
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

        final CostOfPhysicalPlan costOfPhysicalPlan = new CostOfPhysicalPlanImpl( numberOfRequests, shippedRDFTermsForRequests , shippedRDFVarsForRequests, shippedRDFTermsForResponses, shippedRDFVarsForResponses, getIntermediateResultsSize);

        return costOfPhysicalPlan;
    }

    @Override
    public int determineTotalNumberOfRequests( PhysicalPlan pp ) throws QueryOptimizationException {
        int totalNumberOfRequests = 0;
        if ( pp.numberOfSubPlans() == 0 ){
            totalNumberOfRequests += costFunctionForRoot.getNumberOfRequests( pp );
        }

        for ( int i = 0; i < pp.numberOfSubPlans(); i++ ){
            final int costOfRootOp = costFunctionForRoot.getNumberOfRequests( pp.getSubPlan(i) );
            totalNumberOfRequests += costOfRootOp ;
            determineTotalNumberOfRequests( pp.getSubPlan(i) );
        }

        return totalNumberOfRequests;
    }

    @Override
    public int determineTotalShippedRDFTermsForRequests( PhysicalPlan pp ) throws QueryOptimizationException {
        int totalShippedRDFTermsForRequests = 0;
        if ( pp.numberOfSubPlans() == 0 ){
            totalShippedRDFTermsForRequests += costFunctionForRoot.getShippedRDFTermsForRequests( pp );
        }

        for ( int i = 0; i < pp.numberOfSubPlans(); i++ ){
            final int costOfRootOp = costFunctionForRoot.getShippedRDFTermsForRequests( pp.getSubPlan(i) );
            totalShippedRDFTermsForRequests += costOfRootOp ;
            determineTotalShippedRDFTermsForRequests( pp.getSubPlan(i) );
        }

        return totalShippedRDFTermsForRequests;
    }

    @Override
    public int determineTotalShippedVarsForRequests( PhysicalPlan pp ) throws QueryOptimizationException {
        int totalShippedRDFVarsForRequests = 0;
        if ( pp.numberOfSubPlans() == 0 ){
            totalShippedRDFVarsForRequests += costFunctionForRoot.getShippedRDFVarsForRequests( pp );
        }

        for ( int i = 0; i < pp.numberOfSubPlans(); i++ ){
            final int costOfRootOp = costFunctionForRoot.getShippedRDFVarsForRequests( pp.getSubPlan(i) );
            totalShippedRDFVarsForRequests += costOfRootOp ;
            determineTotalShippedVarsForRequests( pp.getSubPlan(i) );
        }

        return totalShippedRDFVarsForRequests;
    }

    @Override
    public int determineTotalShippedRDFTermsForResponses( PhysicalPlan pp ) throws QueryOptimizationException {
        int totalShippedRDFTermsForResponses = 0;
        if ( pp.numberOfSubPlans() == 0 ){
            totalShippedRDFTermsForResponses += costFunctionForRoot.getShippedRDFTermsForResponses( pp );
        }

        for ( int i = 0; i < pp.numberOfSubPlans(); i++ ){
            final int costOfRootOp = costFunctionForRoot.getShippedRDFTermsForResponses( pp.getSubPlan(i) );
            totalShippedRDFTermsForResponses += costOfRootOp ;
            determineTotalShippedRDFTermsForResponses( pp.getSubPlan(i) );
        }

        return totalShippedRDFTermsForResponses;
    }

    @Override
    public int determineTotalShippedVarsForResponses( PhysicalPlan pp ) throws QueryOptimizationException {
        int totalShippedVarsForResponses = 0;
        if ( pp.numberOfSubPlans() == 0 ){
            totalShippedVarsForResponses += costFunctionForRoot.getShippedRDFVarsForResponses( pp );
        }

        for ( int i = 0; i < pp.numberOfSubPlans(); i++ ){
            final int costOfRootOp = costFunctionForRoot.getShippedRDFVarsForResponses( pp.getSubPlan(i) );
            totalShippedVarsForResponses += costOfRootOp ;
            determineTotalShippedVarsForResponses( pp.getSubPlan(i) );
        }

        return totalShippedVarsForResponses;
    }

    @Override
    public int determineTotalIntermediateResultsSize( PhysicalPlan pp ) throws QueryOptimizationException {
        int totalIntermediateResultsSize = 0;
        if ( pp.numberOfSubPlans() == 0 ){
            totalIntermediateResultsSize += costFunctionForRoot.getIntermediateResultsSize( pp );
        }

        for ( int i = 0; i < pp.numberOfSubPlans(); i++ ){
            final int costOfRootOp = costFunctionForRoot.getIntermediateResultsSize( pp.getSubPlan(i) );
            totalIntermediateResultsSize += costOfRootOp ;
            determineTotalIntermediateResultsSize( pp.getSubPlan(i) );
        }
        return totalIntermediateResultsSize;
    }

}
