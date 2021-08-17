package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.costmodel;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.CardinalityEstimation;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.CostEstimationException;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.CostModel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class CostModelImpl implements CostModel
{
	protected static final List<Double> weights = Arrays.asList( 0.2, 0.2, 0.2, 0.2, 0.2 );

    protected final CostFunctionsForPhysicalPlans costFunctions;
	protected final Map<PhysicalPlan, CompletableFuture<Double>> cache = new HashMap<>();

    public CostModelImpl( final CardinalityEstimation cardEstimation ) {
        costFunctions = new CostFunctionsForPhysicalPlansImpl(cardEstimation);
    }

    public CompletableFuture<Double> initiateCostEstimation( final PhysicalPlan plan )
            throws CostEstimationException
    {
        synchronized (cache) {
            // If we already have a CompletableFuture for the
        	// given plan in the cache, return that one.
            final CompletableFuture<Double> cachedFuture = cache.get(plan);
            if ( cachedFuture != null ) {
                return cachedFuture;
            }

            // If we don't have a cache hit, create a CompletableFuture
            // that will produce the cost estimate for the given plan, ...
            final CompletableFuture<Double> futRslt = _initiateCostEstimation(plan);

            // ... extend it into a CompletableFuture that will update the
            // cache once the future result has been produced, ...
            final CompletableFuture<Double> future = futRslt.thenApply( c -> {
                synchronized (cache) {
                    cache.put( plan, CompletableFuture.completedFuture(c) );
                }
                return c;
            });

            // ... add the extended CompletableFuture into the cache, and ...
            cache.put(plan, future);

            // ... return it.
            return future;
        }
    }

    protected CompletableFuture<Double> _initiateCostEstimation( final PhysicalPlan plan )
            throws CostEstimationException
    {
        final CompletableFuture<CostOfPhysicalPlan> futureCost = costFunctions.initiateCostEstimation(plan);
        return futureCost.thenApply( cost -> aggregateCost(cost) );
    }

    protected Double aggregateCost( final CostOfPhysicalPlan cost ) {
        final double aggCost =
                        weights.get(0) * cost.getNumberOfRequests()
                      + weights.get(1) * cost.getShippedRDFTermsForRequests()
                      + weights.get(2) * cost.getShippedVarsForRequests()
                      + weights.get(3) * cost.getShippedRDFTermsForResponses()
                      + weights.get(4) * cost.getShippedVarsForResponses()
                      + weights.get(5) * cost.getIntermediateResultsSize();
        return Double.valueOf(aggCost);
    }

}
