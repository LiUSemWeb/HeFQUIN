package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.cardinality;

import static java.lang.Math.max;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.base.utils.CompletableFutureUtils;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanProperty;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanWithNullaryRoot;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;
import se.liu.ida.hefquin.engine.queryproc.impl.cardinality.RequestBasedCardinalityEstimator;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.CardinalityEstimation;
import se.liu.ida.hefquin.federation.access.FederationAccessManager;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * This class implements an approach to cardinality estimation that is based
 * on the following two ideas: First, for leaf nodes in the given plans, which
 * are request operators, the implementation issues cardinality requests to
 * the respective federation members. Second, for joins, the implementation
 * uses the join cardinality estimation approach from the Lusail paper, which
 * is based on variable-specific cardinality estimates. To functionality to
 * obtain the variable-specific cardinality estimates is implemented separately
 * (see {@link VarSpecificCardinalityEstimation}.
 */
public class CardinalityEstimationImpl implements CardinalityEstimation
{
	protected final Map<PhysicalPlan, CompletableFuture<Integer>> cache = new HashMap<>();

	protected final RequestBasedCardinalityEstimator cardEstimator;
    protected final VarSpecificCardinalityEstimation vsCardEstimator;

    // The visibility of this constructor is at the package level (i.e.,
    // not public) such that it can be used in the unit tests, but not by
    // the configuration framework of HeFQUIN.
    CardinalityEstimationImpl( final FederationAccessManager fedAccessMgr ) {
        cardEstimator = new RequestBasedCardinalityEstimator(fedAccessMgr);
        vsCardEstimator = new VarSpecificCardinalityEstimationImpl(this);
    }

    public CardinalityEstimationImpl( final QueryProcContext ctxt ) {
        this( ctxt.getFederationAccessMgr() );
    }

    @Override
    public final CompletableFuture<Integer> initiateCardinalityEstimation( final PhysicalPlan plan ) {
        synchronized (cache) {
            // If we already have a CompletableFuture for the
        	// given plan in the cache, return that one.
            final CompletableFuture<Integer> cachedFuture = cache.get(plan);
            if ( cachedFuture != null ) {
                return cachedFuture;
            }

            cardEstimator.addCardinalitiesForRequests(plan);

            // If we don't have a cache hit, create a CompletableFuture
            // that will produce the cardinality estimate for the given
            // plan, ...
            final CompletableFuture<Integer> futRslt = _initiateCardinalityEstimation(plan);

            // ... extend it into a CompletableFuture that will update the
            // cache once the future result has been produced, ...
            final CompletableFuture<Integer> future = futRslt.thenApply( c -> {
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

    public CompletableFuture<Integer> _initiateCardinalityEstimation( final PhysicalPlan plan ) {
        final Supplier<Integer> worker;
        if ( plan instanceof PhysicalPlanWithNullaryRoot )
            worker = new WorkerForRequestOps(plan);
		else
			worker = new WorkerForSubquery(plan);

        return CompletableFuture.supplyAsync(worker);
    }

	protected class WorkerForRequestOps implements Supplier<Integer>
	{
		protected final int extractedCardinality;

		public WorkerForRequestOps( final PhysicalPlan plan ) {
			final QueryPlanningInfo qpInfo = plan.getQueryPlanningInfo();
			final QueryPlanProperty prop = qpInfo.getProperty( QueryPlanProperty.CARDINALITY );
			assert prop != null;
			extractedCardinality = prop.getValue();
		}

		@Override
		public Integer get() {
			return extractedCardinality;
		}
	}

	protected class WorkerForSubquery implements Supplier<Integer>
	{
		protected final PhysicalPlan plan;

		public WorkerForSubquery( final PhysicalPlan plan) {
			this.plan = plan;
		}

		@Override
		public Integer get() {
//			The cardinality of a subquery is the maximum cardinality
//			of VarSpecificCardinalityEstimation for all (certain) variables
			final Set<Var> allCertainVars = plan.getExpectedVariables().getCertainVariables();

			return getEstimateBasedOnAllCertainVars( new ArrayList<>(allCertainVars) );
		}

		protected Integer getEstimateBasedOnAllCertainVars( final List<Var> vars ) {
			@SuppressWarnings("unchecked")
			final CompletableFuture<Integer>[] futures = new CompletableFuture[vars.size()];

			for ( int i = 0; i < vars.size(); ++i ) {
				final Var v = vars.get(i);
				futures[i] = vsCardEstimator.initiateCardinalityEstimation(plan, v);
			}

			final Integer[] cardinalities;
			try {
				cardinalities = CompletableFutureUtils.getAll(futures, Integer.class);
			}
			catch ( final CompletableFutureUtils.GetAllException ex ) {
				if ( ex.getCause() != null && ex.getCause() instanceof InterruptedException ) {
					throw new RuntimeException("Unexpected interruption when getting a variable-specific cardinality estimate.", ex.getCause() );
				}
				else {
					throw new RuntimeException("Getting a variable-specific cardinality estimate caused an exception.", ex.getCause() );
				}
			}

			int cardinality = 0;
			for ( int i = 0; i < vars.size(); ++i ) {
				int intValue = cardinalities[i].intValue();
				cardinality = max( intValue < 0? Integer.MAX_VALUE: intValue, cardinality );
			}

			return cardinality;
		}
	}
}
