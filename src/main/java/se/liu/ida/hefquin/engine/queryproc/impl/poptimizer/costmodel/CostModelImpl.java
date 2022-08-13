package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.costmodel;

import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.CardinalityEstimation;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.CostModel;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class CostModelImpl implements CostModel
{
    protected final CostDimension[] dimensions;
	protected final Map<PhysicalPlan, CompletableFuture<Double>> cache = new HashMap<>();

    public CostModelImpl( final CostDimension[] dimensions ) {
        assert dimensions.length > 0;
        this.dimensions = dimensions;
    }

    public CostModelImpl( final CardinalityEstimation cardEstimation ) {
    	this( getDefaultDimensions(cardEstimation) );
    }

    public static CostDimension[] getDefaultDimensions( final CardinalityEstimation cardEstimation ) {
        if ( dfltDimensions == null ) {
            dfltDimensions = new CostDimension[] {
                new CostDimension( 1.0, new CFRBasedCostFunctionForPlan(new CFRNumberOfRequests(cardEstimation)) ),
                new CostDimension( 1.0, new CFRBasedCostFunctionForPlan(new CFRNumberOfTermsShippedInRequests(cardEstimation)) ),
                new CostDimension( 1.0, new CFRBasedCostFunctionForPlan(new CFRNumberOfVarsShippedInRequests(cardEstimation)) ),
                new CostDimension( 1.0, new CFRBasedCostFunctionForPlan(new CFRNumberOfTermsShippedInResponses(cardEstimation)) ),
                new CostDimension( 1.0, new CFRBasedCostFunctionForPlan(new CFRNumberOfVarsShippedInResponses(cardEstimation)) ),
                new CostDimension( 1.0, new CFRBasedCostFunctionForPlan(new CFRNumberOfProcessedSolMaps(cardEstimation)) )
            };
    	}

        return dfltDimensions;
    }

	private static CostDimension[] dfltDimensions = null;


	@Override
    public CompletableFuture<Double> initiateCostEstimation( final PhysicalPlan plan )
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
    {
        CompletableFuture<Double> f = CompletableFuture.completedFuture( Double.valueOf(0) );
        for ( int i = 0; i < dimensions.length; ++i ) {
            final CostFunctionForPlan costFct = dimensions[i].costFct;
            final double weight = dimensions[i].weight;
            f = f.thenCombine( costFct.initiateCostEstimation(plan),
                               (aggregate,costValue) -> aggregate + weight * (costValue < 0 ? Integer.MAX_VALUE: costValue) );
        }

        return f;
    }

}
