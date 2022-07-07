package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.cardinality;

import static java.lang.Math.max;
import static java.lang.Math.min;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.TPFServer;
import se.liu.ida.hefquin.engine.federation.access.BRTPFRequest;
import se.liu.ida.hefquin.engine.federation.access.BindingsRestrictedTriplePatternRequest;
import se.liu.ida.hefquin.engine.federation.access.CardinalityResponse;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessException;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.access.TPFRequest;
import se.liu.ida.hefquin.engine.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.engine.federation.access.impl.req.BRTPFRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.req.TPFRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.utils.FederationAccessUtils;
import se.liu.ida.hefquin.engine.federation.access.utils.RequestMemberPair;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.*;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.utils.ExpectedVariablesUtils;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.CardinalityEstimation;
import se.liu.ida.hefquin.engine.utils.CompletableFutureUtils;

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
            return future1.thenCombine( future2, (c1,c2) -> ((c1 < 0? Integer.MAX_VALUE:c1) + (c2 < 0? Integer.MAX_VALUE:c2)) < 0? Integer.MAX_VALUE:(c1 < 0? Integer.MAX_VALUE:c1) + (c2 < 0? Integer.MAX_VALUE:c2) );
        }

        final Supplier<Integer> worker;
        if ( rootOp instanceof LogicalOpRequest ) {
            worker = new WorkerForRequestOps( (LogicalOpRequest<?, ?>) rootOp);
        }
        else if ( rootOp instanceof LogicalOpJoin ) {
            worker = new WorkerForJoins( plan.getSubPlan(0), plan.getSubPlan(1) );
        }
        else if ( rootOp instanceof LogicalOpTPAdd ) {
            final PhysicalPlan reqTP = PhysicalPlanFactory.extractRequestAsPlan( (LogicalOpTPAdd) rootOp );
            worker = new WorkerForJoins( plan.getSubPlan(0), reqTP );
        }
        else if ( rootOp instanceof LogicalOpBGPAdd ) {
            final PhysicalPlan reqBGP = PhysicalPlanFactory.extractRequestAsPlan( (LogicalOpBGPAdd) rootOp );
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
			final FederationMember fm = reqOp.getFederationMember();
			DataRetrievalRequest req = reqOp.getRequest();
			if ( fm instanceof TPFServer ) {
				req = ensureTPFRequest( (TriplePatternRequest) req );
			}
			else if ( fm instanceof BRTPFServer && req instanceof TriplePatternRequest ) {
				req = ensureTPFRequest( (TriplePatternRequest) req );
			}
			else if ( fm instanceof BRTPFServer && req instanceof BindingsRestrictedTriplePatternRequest ) {
				req = ensureBRTPFRequest( (BindingsRestrictedTriplePatternRequest) req );
			}

			final RequestMemberPair rm = new RequestMemberPair(req, fm);
			final CardinalityResponse[] resps;
			try {
				resps = FederationAccessUtils.performCardinalityRequests(fedAccessMgr, rm);
			}
			catch ( final FederationAccessException e ) {
				throw new RuntimeException("Issuing a cardinality request caused an exception.", e);
			}

			int intValue = Integer.valueOf( resps[0].getCardinality() );
			if( intValue < 0 ) intValue = Integer.MAX_VALUE;
			return intValue;
		}

		protected TPFRequest ensureTPFRequest( final TriplePatternRequest req ) {
			if ( req instanceof TPFRequest ) {
				return (TPFRequest) req;
			}
			else {
				return new TPFRequestImpl( req.getQueryPattern() );
			}
		}

		protected BRTPFRequest ensureBRTPFRequest( final BindingsRestrictedTriplePatternRequest req ) {
			if ( req instanceof BRTPFRequest ) {
				return (BRTPFRequest) req;
			}
			else {
				return new BRTPFRequestImpl( req.getTriplePattern(), req.getSolutionMappings() );
			}
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
			@SuppressWarnings("unchecked")
			final CompletableFuture<Integer>[] futures1 = new CompletableFuture[joinVars.size()];
			@SuppressWarnings("unchecked")
			final CompletableFuture<Integer>[] futures2 = new CompletableFuture[joinVars.size()];

			for ( int i = 0; i < joinVars.size(); ++i ) {
				futures1[i] = vsCardEstimator.initiateCardinalityEstimation(plan1, joinVars.get(i));
				futures2[i] = vsCardEstimator.initiateCardinalityEstimation(plan2, joinVars.get(i));
			}

			final Integer[] cardinalities1;
			try {
				cardinalities1 = CompletableFutureUtils.getAll(futures1, Integer.class);
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
				cardinalities2 = CompletableFutureUtils.getAll(futures2, Integer.class);
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
				int intValue1 = cardinalities1[i].intValue();
				int intValue2 = cardinalities2[i].intValue();
				if( intValue1 < 0 ) intValue1 = Integer.MAX_VALUE;
				if( intValue2 < 0 ) intValue2 = Integer.MAX_VALUE;

				final int c = min( intValue1, intValue2 );
				cardinality = max(c, cardinality);
			}

			return Integer.valueOf(cardinality);
		}

		protected Integer getEstimateBasedOnAllCertainVars( final List<Var> vars ) {
			// Note that this method is used only if the sets
			// of all variables in the two plans are disjoint.
			@SuppressWarnings("unchecked")
			final CompletableFuture<Integer>[] futures = new CompletableFuture[vars.size()];

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
				if( intValue < 0 ) intValue = Integer.MAX_VALUE;
				cardinality = max( intValue, cardinality );
			}

			return Integer.valueOf(cardinality);
		}
	}

}
