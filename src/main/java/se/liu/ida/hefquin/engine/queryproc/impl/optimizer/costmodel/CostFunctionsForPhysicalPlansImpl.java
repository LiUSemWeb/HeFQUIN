package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.costmodel;

import java.util.ArrayList;
import java.util.List;
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

    	final CompletableFuture<Integer> futureNumberOfRequests = determineTotalNumberOfRequests(pp);
    	final CompletableFuture<Integer> futureShippedRDFTermsForRequests = determineTotalShippedRDFTermsForRequests(pp);
        final CompletableFuture<Integer> futureShippedRDFVarsForRequests = determineTotalShippedVarsForRequests(pp);
        final CompletableFuture<Integer> futureShippedRDFTermsForResponses = determineTotalShippedRDFTermsForResponses(pp);
        final CompletableFuture<Integer> futureShippedRDFVarsForResponses = determineTotalShippedVarsForResponses(pp);
        final CompletableFuture<Integer> futureIntermediateResultsSize = determineTotalIntermediateResultsSize(pp);

        CompletableFuture<List<Integer>> f = CompletableFuture.completedFuture( new ArrayList<>(6) );
        f = f.thenCombine( futureNumberOfRequests,            (l,c) -> { l.add(c); return l; } );
        f = f.thenCombine( futureShippedRDFTermsForRequests,  (l,c) -> { l.add(c); return l; } );
        f = f.thenCombine( futureShippedRDFVarsForRequests,   (l,c) -> { l.add(c); return l; } );
        f = f.thenCombine( futureShippedRDFTermsForResponses, (l,c) -> { l.add(c); return l; } );
        f = f.thenCombine( futureShippedRDFVarsForResponses,  (l,c) -> { l.add(c); return l; } );
        f = f.thenCombine( futureIntermediateResultsSize,     (l,c) -> { l.add(c); return l; } );

        return f.thenApply( l -> {
            return new CostOfPhysicalPlanImpl( l.get(0), l.get(1), l.get(2),
                                               l.get(3), l.get(4), l.get(5) );
        });
    }

    protected CompletableFuture<Integer> determineTotalNumberOfRequests( final PhysicalPlan pp ) {
        final CompletableFuture<Integer> futureForRoot = costFunctionForRoot.determineNumberOfRequests( pp );
        if ( pp.numberOfSubPlans() == 0 ){
            return futureForRoot;
        }

        CompletableFuture<Integer> f = futureForRoot;
        for ( int i = 0; i < pp.numberOfSubPlans(); i++ ) {
            final PhysicalPlan subPlan = pp.getSubPlan(i);
            f = f.thenCombine( determineTotalNumberOfRequests(subPlan),
                               (total,valueForSubPlan) -> total + valueForSubPlan );
        }

        return f;
    }

    protected CompletableFuture<Integer> determineTotalShippedRDFTermsForRequests( final PhysicalPlan pp ) {
        final CompletableFuture<Integer> futureForRoot = costFunctionForRoot.determineShippedRDFTermsForRequests( pp );
        if ( pp.numberOfSubPlans() == 0 ){
            return futureForRoot;
        }

        CompletableFuture<Integer> f = futureForRoot;
        for ( int i = 0; i < pp.numberOfSubPlans(); i++ ) {
            final PhysicalPlan subPlan = pp.getSubPlan(i);
            f = f.thenCombine( determineTotalShippedRDFTermsForRequests(subPlan),
                               (total,valueForSubPlan) -> total + valueForSubPlan );
        }

        return f;
    }

    protected CompletableFuture<Integer> determineTotalShippedVarsForRequests( final PhysicalPlan pp ) {
        final CompletableFuture<Integer> futureForRoot = costFunctionForRoot.determineShippedVarsForRequests( pp );
        if ( pp.numberOfSubPlans() == 0 ){
            return futureForRoot;
        }

        CompletableFuture<Integer> f = futureForRoot;
        for ( int i = 0; i < pp.numberOfSubPlans(); i++ ) {
            final PhysicalPlan subPlan = pp.getSubPlan(i);
            f = f.thenCombine( determineTotalShippedVarsForRequests(subPlan),
                               (total,valueForSubPlan) -> total + valueForSubPlan );
        }

        return f;
    }

    protected CompletableFuture<Integer> determineTotalShippedRDFTermsForResponses( final PhysicalPlan pp ) {
        final CompletableFuture<Integer> futureForRoot = costFunctionForRoot.determineShippedRDFTermsForResponses( pp );
        if ( pp.numberOfSubPlans() == 0 ){
            return futureForRoot;
        }

        CompletableFuture<Integer> f = futureForRoot;
        for ( int i = 0; i < pp.numberOfSubPlans(); i++ ) {
            final PhysicalPlan subPlan = pp.getSubPlan(i);
            f = f.thenCombine( determineTotalShippedRDFTermsForResponses(subPlan),
                               (total,valueForSubPlan) -> total + valueForSubPlan );
        }

        return f;
    }

    protected CompletableFuture<Integer> determineTotalShippedVarsForResponses( final PhysicalPlan pp ) {
        final CompletableFuture<Integer> futureForRoot = costFunctionForRoot.determineShippedVarsForResponses( pp );
        if ( pp.numberOfSubPlans() == 0 ){
            return futureForRoot;
        }

        CompletableFuture<Integer> f = futureForRoot;
        for ( int i = 0; i < pp.numberOfSubPlans(); i++ ) {
            final PhysicalPlan subPlan = pp.getSubPlan(i);
            f = f.thenCombine( determineTotalShippedVarsForResponses(subPlan),
                               (total,valueForSubPlan) -> total + valueForSubPlan );
        }

        return f;
    }

    protected CompletableFuture<Integer> determineTotalIntermediateResultsSize( final PhysicalPlan pp ) {
        final CompletableFuture<Integer> futureForRoot = costFunctionForRoot.determineIntermediateResultsSize( pp );
        if ( pp.numberOfSubPlans() == 0 ){
            return futureForRoot;
        }

        CompletableFuture<Integer> f = futureForRoot;
        for ( int i = 0; i < pp.numberOfSubPlans(); i++ ) {
            final PhysicalPlan subPlan = pp.getSubPlan(i);
            f = f.thenCombine( determineTotalIntermediateResultsSize(subPlan),
                               (total,valueForSubPlan) -> total + valueForSubPlan );
        }

        return f;
    }

}
