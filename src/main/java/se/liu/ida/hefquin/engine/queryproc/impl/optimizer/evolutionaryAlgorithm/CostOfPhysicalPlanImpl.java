package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils.CardinalityEstimation;

public class CostOfPhysicalPlanImpl extends CostOfRootOperator implements CostOfPhysicalPlan{
    protected int totalNumberOfRequests = 0;
    protected int totalShippedRDFTermsForRequests = 0;
    protected int totalShippedRDFVarsForRequests = 0;
    protected int totalShippedRDFTermsForResponses = 0;
    protected int totalShippedVarsForResponses = 0;
    protected int totalIntermediateResultsSize = 0;

    public CostOfPhysicalPlanImpl(CardinalityEstimation cardEstimate) {
        super(cardEstimate);
    }

    public int getTotalNumberOfRequests( PhysicalPlan pp ) throws QueryOptimizationException {
        if ( pp.numberOfSubPlans() == 0 ){
            totalNumberOfRequests += getNumberOfRequests( pp );
        }

        for ( int i = 0; i < pp.numberOfSubPlans(); i++ ){
            final int costOfRootOp = getNumberOfRequests( pp.getSubPlan(i) );
            totalNumberOfRequests += costOfRootOp ;
            getTotalNumberOfRequests( pp.getSubPlan(i) );
        }

        return totalNumberOfRequests;
    }

    public int getTotalShippedRDFTermsForRequests( PhysicalPlan pp ) throws QueryOptimizationException {
        if ( pp.numberOfSubPlans() == 0 ){
            totalShippedRDFTermsForRequests += getShippedRDFTermsForRequests( pp );
        }

        for ( int i = 0; i < pp.numberOfSubPlans(); i++ ){
            final int costOfRootOp = getShippedRDFTermsForRequests( pp.getSubPlan(i) );
            totalShippedRDFTermsForRequests += costOfRootOp ;
            getTotalShippedRDFTermsForRequests( pp.getSubPlan(i) );
        }

        return totalShippedRDFTermsForRequests;
    }

    public int getTotalShippedRDFVarsForRequests( PhysicalPlan pp ) throws QueryOptimizationException {
        if ( pp.numberOfSubPlans() == 0 ){
            totalShippedRDFVarsForRequests += getShippedRDFVarsForRequests( pp );
        }

        for ( int i = 0; i < pp.numberOfSubPlans(); i++ ){
            final int costOfRootOp = getShippedRDFVarsForRequests( pp.getSubPlan(i) );
            totalShippedRDFVarsForRequests += costOfRootOp ;
            getTotalShippedRDFVarsForRequests( pp.getSubPlan(i) );
        }

        return totalShippedRDFVarsForRequests;
    }

    public int getTotalShippedRDFTermsForResponses( PhysicalPlan pp ) throws QueryOptimizationException {
        if ( pp.numberOfSubPlans() == 0 ){
            totalShippedRDFTermsForResponses += getShippedRDFTermsForResponses( pp );
        }

        for ( int i = 0; i < pp.numberOfSubPlans(); i++ ){
            final int costOfRootOp = getShippedRDFTermsForResponses( pp.getSubPlan(i) );
            totalShippedRDFTermsForResponses += costOfRootOp ;
            getTotalShippedRDFTermsForResponses( pp.getSubPlan(i) );
        }

        return totalShippedRDFTermsForResponses;
    }

    public int getTotalShippedVarsForResponses( PhysicalPlan pp ) throws QueryOptimizationException {
        if ( pp.numberOfSubPlans() == 0 ){
            totalShippedVarsForResponses += getShippedRDFVarsForResponses( pp );
        }

        for ( int i = 0; i < pp.numberOfSubPlans(); i++ ){
            final int costOfRootOp = getShippedRDFVarsForResponses( pp.getSubPlan(i) );
            totalShippedVarsForResponses += costOfRootOp ;
            getTotalShippedVarsForResponses( pp.getSubPlan(i) );
        }

        return totalShippedVarsForResponses;
    }

    public int getTotalIntermediateResultsSize( PhysicalPlan pp ) throws QueryOptimizationException {
        if ( pp.numberOfSubPlans() == 0 ){
            totalIntermediateResultsSize += getIntermediateResultsSize( pp );
        }

        for ( int i = 0; i < pp.numberOfSubPlans(); i++ ){
            final int costOfRootOp = getIntermediateResultsSize( pp.getSubPlan(i) );
            totalIntermediateResultsSize += costOfRootOp ;
            getTotalIntermediateResultsSize( pp.getSubPlan(i) );
        }
        return totalIntermediateResultsSize;
    }

}
