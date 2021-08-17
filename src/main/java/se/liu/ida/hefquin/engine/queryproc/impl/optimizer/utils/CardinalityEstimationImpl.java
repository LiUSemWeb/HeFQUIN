package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils;

import static java.lang.Math.max;
import static java.lang.Math.min;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.engine.federation.access.CardinalityResponse;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessException;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.access.utils.FederationAccessUtils;
import se.liu.ida.hefquin.engine.federation.access.utils.RequestMemberPair;
import se.liu.ida.hefquin.engine.queryplan.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.*;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.utils.ExpectedVariablesUtils;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;
import se.liu.ida.hefquin.engine.utils.CompletableFutureUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class CardinalityEstimationImpl implements CardinalityEstimation
{
	protected final Map<PhysicalPlan, CompletableFuture<Integer>> cache = new HashMap<>();

	protected final FederationAccessManager fedAccessMgr;
    protected final VarSpecificCardinalityEstimation vsCardEstimator;

    public CardinalityEstimationImpl( final FederationAccessManager fedAccessMgr ) {
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

        if ( rootOp instanceof LogicalOpUnion ) {
            final CompletableFuture<Integer> future1 = initiateCardinalityEstimation( plan.getSubPlan(0) );
            final CompletableFuture<Integer> future2 = initiateCardinalityEstimation( plan.getSubPlan(1) );
            return future1.thenCombine( future2, (c1,c2) -> c1 + c2 );
        }

        final Supplier<Integer> worker;
        if ( rootOp instanceof LogicalOpRequest ) {
            worker = new WorkerForRequestOps( (LogicalOpRequest<?, ?>) rootOp);
        }
        else if ( rootOp instanceof LogicalOpJoin ) {
            worker = new WorkerForJoins( plan.getSubPlan(0), plan.getSubPlan(1) );
        }
        else if ( rootOp instanceof LogicalOpTPAdd ) {
            final PhysicalPlan reqTP = CardinalityEstimationHelper.formRequestBasedOnTPofTPAdd( (LogicalOpTPAdd) rootOp );
            worker = new WorkerForJoins( plan.getSubPlan(0), reqTP );
        }
        else if ( rootOp instanceof LogicalOpBGPAdd ) {
            final PhysicalPlan reqBGP = CardinalityEstimationHelper.formRequestBasedOnBGPofBGPAdd( (LogicalOpBGPAdd) rootOp );
            worker = new WorkerForJoins( plan.getSubPlan(0), reqBGP );
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
			final RequestMemberPair rm = new RequestMemberPair(reqOp);
			final CardinalityResponse[] resps;
			try {
				resps = FederationAccessUtils.performCardinalityRequests(fedAccessMgr, rm);
			}
			catch ( final FederationAccessException e ) {
				throw new RuntimeException("Issuing a cardinality request caused an exception.", e);
			}

			return Integer.valueOf( resps[0].getCardinality() );
		}
	}


	protected class WorkerForJoins implements Supplier<Integer>
	{
		protected final PhysicalPlan plan1;
		protected final PhysicalPlan plan2;

		public WorkerForJoins( final PhysicalPlan plan1,
		                       final PhysicalPlan plan2 ) {
			this.plan1 = plan1;
			this.plan2 = plan2;
		}

		@Override
		public Integer get() {
			final Set<Var> certainJoinVars = ExpectedVariablesUtils.intersectionOfCertainVariables( plan1.getExpectedVariables(), plan2.getExpectedVariables() );
			if ( ! certainJoinVars.isEmpty() ) {
				return getEstimateBasedOnJoinVars( new ArrayList<>(certainJoinVars) );
			}

			final Set<Var> possibleJoinVars = ExpectedVariablesUtils.unionOfAllVariables( plan1.getExpectedVariables(), plan2.getExpectedVariables() );
			possibleJoinVars.removeAll(certainJoinVars);
			if ( ! possibleJoinVars.isEmpty() ) {
				return getEstimateBasedOnJoinVars( new ArrayList<>(possibleJoinVars) );
			}

			final Set<Var> allCertainVars = ExpectedVariablesUtils.unionOfCertainVariables( plan1.getExpectedVariables(), plan2.getExpectedVariables() );
			return getEstimateBasedOnAllCertainVars( new ArrayList<>(allCertainVars) );
		}

		protected Integer getEstimateBasedOnJoinVars( final List<Var> joinVars ) {
			final CompletableFuture<?>[] futures1 = new CompletableFuture[joinVars.size()];
			final CompletableFuture<?>[] futures2 = new CompletableFuture[joinVars.size()];
			for ( int i = 0; i < joinVars.size(); ++i ) {
				futures1[i] = vsCardEstimator.initiateCardinalityEstimation(plan1, joinVars.get(i));
				futures2[i] = vsCardEstimator.initiateCardinalityEstimation(plan2, joinVars.get(i));
			}

			final Integer[] cardinalities1;
			try {
				cardinalities1 = (Integer[]) CompletableFutureUtils.getAll(futures1);
			}
			catch ( final CompletableFutureUtils.GetAllException ex ) {
				for ( int i = 0; i < joinVars.size(); ++i ) {
					futures2[i].cancel(true);
				}
				if ( ex.getCause() != null && ex.getCause() instanceof InterruptedException ) {
					throw new RuntimeException("Unexpected interruption when getting a variable-specific cardinality estimate.", ex.getCause() );
				}
				else {
					throw new RuntimeException("Getting a variable-specific cardinality estimate caused an exception.", ex.getCause() );
				}
			}

			final Integer[] cardinalities2;
			try {
				cardinalities2 = (Integer[]) CompletableFutureUtils.getAll(futures2);
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
			for ( int i = 0; i < joinVars.size(); ++i ) {
				final int c = min( cardinalities1[i].intValue(), cardinalities2[i].intValue() );
				cardinality = max(c, cardinality);
			}

			return Integer.valueOf(cardinality);
		}

		protected Integer getEstimateBasedOnAllCertainVars( final List<Var> vars ) {
			// Note that this method is used only if the sets
			// of all variables in the two plans are disjoint.
			final CompletableFuture<?>[] futures = new CompletableFuture[vars.size()];
			for ( int i = 0; i < vars.size(); ++i ) {
				final Var v = vars.get(i);
				if ( plan1.getExpectedVariables().getCertainVariables().contains(v) ) {
					futures[i] = vsCardEstimator.initiateCardinalityEstimation(plan1, v);
				}
				else {
					futures[i] = vsCardEstimator.initiateCardinalityEstimation(plan2, v);
				}
			}

			final Integer[] cardinalities;
			try {
				cardinalities = (Integer[]) CompletableFutureUtils.getAll(futures);
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
				cardinality = max( cardinalities[i].intValue(), cardinality );
			}

			return Integer.valueOf(cardinality);
		}
	}

}
