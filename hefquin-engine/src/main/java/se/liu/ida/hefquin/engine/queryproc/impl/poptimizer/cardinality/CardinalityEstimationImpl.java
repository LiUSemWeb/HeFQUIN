package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.cardinality;

import static java.lang.Math.max;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.base.utils.CompletableFutureUtils;
import se.liu.ida.hefquin.engine.federation.access.CardinalityResponse;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessException;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.access.UnsupportedOperationDueToRetrievalError;
import se.liu.ida.hefquin.engine.federation.access.utils.FederationAccessUtils;
import se.liu.ida.hefquin.engine.queryplan.logical.BinaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.NaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.*;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.CardinalityEstimation;

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

	protected final FederationAccessManager fedAccessMgr;
    protected final VarSpecificCardinalityEstimation vsCardEstimator;

    // The visibility of this constructor is at the package level (i.e.,
    // not public) such that it can be used in the unit tests, but not by
    // the configuration framework of HeFQUIN.
    CardinalityEstimationImpl( final FederationAccessManager fedAccessMgr ) {
        assert fedAccessMgr != null;
        this.fedAccessMgr = fedAccessMgr;

        vsCardEstimator = new VarSpecificCardinalityEstimationImpl(this);
    }

    public CardinalityEstimationImpl( final QueryProcContext ctxt ) {
        this( ctxt.getFederationAccessMgr() );
    }

    @Override
    public CompletableFuture<Integer> initiateCardinalityEstimation( final PhysicalPlan plan ) {
        synchronized (cache) {
            // If we already have a CompletableFuture for the
        	// given plan in the cache, return that one.
            final CompletableFuture<Integer> cachedFuture = cache.get(plan);
            if ( cachedFuture != null ) {
                return cachedFuture;
            }

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
        final LogicalOperator rootOp = ((PhysicalOperatorForLogicalOperator) plan.getRootOperator()).getLogicalOperator();

        final Supplier<Integer> worker;
        if ( rootOp instanceof LogicalOpRequest ) {
            worker = new WorkerForRequestOps( (LogicalOpRequest<?, ?>) rootOp);
        }
		else if ( rootOp instanceof NaryLogicalOp || rootOp instanceof BinaryLogicalOp || rootOp instanceof UnaryLogicalOp ) {
			worker = new WorkerForSubquery(plan);
		}
        else {
            throw new IllegalArgumentException("The type of the root operator of the given plan is currently not supported (" + rootOp.getClass().getName() + ").");
        }

        return CompletableFuture.supplyAsync(worker);
    }

	protected class WorkerForRequestOps implements Supplier<Integer>
	{
		protected final LogicalOpRequest<?,?> reqOp;

		public WorkerForRequestOps( final LogicalOpRequest<?,?> reqOp ) { this.reqOp = reqOp; }

		@Override
		public Integer get() {
			final CardinalityResponse[] resps;
			try {
				resps = FederationAccessUtils.performCardinalityRequests(fedAccessMgr, reqOp);
			}
			catch ( final FederationAccessException e ) {
				throw new RuntimeException("Issuing a cardinality request caused an exception.", e);
			}

			final int intValue = computeEffectiveCardinality( resps[0] );
			// This value might end up with a negative value
			// when the cardinality exceed the maximum possible Integer number that can be represented
			return ( intValue < 0 ? Integer.MAX_VALUE : intValue ) ;
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

	/**
	 * TODO: Fallback behavior? Returning Integer.MAX_VALUE for now
	 *
	 * Computes the cardinality from the given {@link CardinalityResponse}.
	 *
	 * If retrieving the cardinality fails due to an {@link UnsupportedOperationDueToRetrievalError}, this method
	 * returns {@link Integer#MAX_VALUE} as a fallback.
	 *
	 * @param resp the cardinality response to extract the cardinality from
	 * @return the cardinality, or {@code Integer.MAX_VALUE} if retrieval is unsupported
	 */
	private int computeEffectiveCardinality( final CardinalityResponse resp ) {
		try {
			return resp.getCardinality();
		} catch ( UnsupportedOperationDueToRetrievalError e ) {
			return Integer.MAX_VALUE;
		}
	}
}
