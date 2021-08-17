package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils;

import static java.lang.Math.min;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.engine.queryplan.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpUnion;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.utils.ExpectedVariablesUtils;

public class VarSpecificCardinalityEstimationImpl implements VarSpecificCardinalityEstimation
{
	protected final MyCache cache = new MyCache();

	protected final CardinalityEstimation cardEstimator;

	public VarSpecificCardinalityEstimationImpl( final CardinalityEstimation cardEstimator ) {
		assert cardEstimator != null;
		this.cardEstimator = cardEstimator;
    }

	@Override
	public CompletableFuture<Integer> initiateCardinalityEstimation(
			final PhysicalPlan plan,
			final Var v )
	{
		synchronized (cache) {
			// If we already have a CompletableFuture for the given
			// plan-variable pair in the cache, return that one.
			final CompletableFuture<Integer> cachedFuture = cache.get(plan, v);
			if ( cachedFuture != null ) {
				return cachedFuture;
			}

			// If we don't have a cache hit, create a CompletableFuture
			// that will produce the variable-specific cardinality estimate
			// for the given plan-variable pair, ...
			final CompletableFuture<Integer> futRslt = _initiateCardinalityEstimation(plan, v);

			// ... extend it into a CompletableFuture that will update the
			// cache once the future result has been produced, ...
			final CompletableFuture<Integer> future = futRslt.thenApply( c -> {
				synchronized (cache) {
					cache.put( plan, v, CompletableFuture.completedFuture(c) );
				}
				return c;
			});

			// ... add the extended CompletableFuture into the cache, and ... 
			cache.put(plan, v, future);

			// ... return it.
			return future;
		}
	}

	protected CompletableFuture<Integer> _initiateCardinalityEstimation(
			final PhysicalPlan plan,
			final Var v )
	{
		final LogicalOperator rootOp = ((PhysicalOperatorForLogicalOperator) plan.getRootOperator()).getLogicalOperator();

		if ( rootOp instanceof LogicalOpRequest ) {
			return cardEstimator.initiateCardinalityEstimation(plan);
		}
		else if ( rootOp instanceof LogicalOpTPAdd ) {
			final PhysicalPlan reqTP = CardinalityEstimationHelper.formRequestPlan( (LogicalOpTPAdd) rootOp );
			return _initiateJoinCardinalityEstimation( plan.getSubPlan(0), reqTP, v );
		}
		else if ( rootOp instanceof LogicalOpBGPAdd ) {
			final PhysicalPlan reqBGP = CardinalityEstimationHelper.formRequestPlan( (LogicalOpBGPAdd) rootOp );
			return _initiateJoinCardinalityEstimation( plan.getSubPlan(0), reqBGP, v );
		}
		else if ( rootOp instanceof LogicalOpJoin ) {
			return _initiateJoinCardinalityEstimation( plan.getSubPlan(0), plan.getSubPlan(1), v );
		}
		else if ( rootOp instanceof LogicalOpUnion ) {
			return _initiateUnionCardinalityEstimation( plan.getSubPlan(0), plan.getSubPlan(1), v );
		}
		else {
			throw new IllegalArgumentException("Unsupported type of root operator (" + rootOp.getClass().getName() + ").");
		}
	}

	protected CompletableFuture<Integer> _initiateJoinCardinalityEstimation(
			final PhysicalPlan plan1,
			final PhysicalPlan plan2,
			final Var v )
	{
		final CompletableFuture<Integer> f1 = initiateCardinalityEstimation(plan1, v);
		final CompletableFuture<Integer> f2 = initiateCardinalityEstimation(plan2, v);

		final Set<Var> allJoinVars = ExpectedVariablesUtils.intersectionOfAllVariables( plan1.getExpectedVariables(), plan2.getExpectedVariables() );

		if ( allJoinVars.contains(v) ) {
			// Create a CompletableFuture that will complete once
			// both f1 and f2 have completed and, then, will combine
			// the results of f1 and f2 (i.e., c1 and c2) using the
			// min function.
			return f1.thenCombine(f2, (c1,c2) -> min(c1, c2) );
		}
		else {
			// ... combine the results of f1 and f2 (i.e., c1 and c2)
			// by multiplication.
			return f1.thenCombine(f2, (c1,c2) -> c1 * c2 );
		}
	}

	protected CompletableFuture<Integer> _initiateUnionCardinalityEstimation(
			final PhysicalPlan plan1,
			final PhysicalPlan plan2,
			final Var v )
	{
		final CompletableFuture<Integer> f1 = initiateCardinalityEstimation(plan1, v);
		final CompletableFuture<Integer> f2 = initiateCardinalityEstimation(plan2, v);

		// Create a CompletableFuture that will complete once
		// both f1 and f2 have completed and, then, will combine
		// the results of f1 and f2 (i.e., c1 and c2) by adding
		// them.
		return f1.thenCombine(f2, (c1,c2) -> c1 + c2 );
	}


	protected static class MyCache
	{
		protected final Map<PhysicalPlan, Map<Var, CompletableFuture<Integer>>> map = new HashMap<>();

		public void put( final PhysicalPlan plan, final Var v, final CompletableFuture<Integer> future ) {
			Map<Var, CompletableFuture<Integer>> mapForPlan = map.get(plan);
			if ( mapForPlan == null ) {
				mapForPlan = new HashMap<>();
				map.put(plan, mapForPlan);
			}
			mapForPlan.put(v, future);
		}

		public CompletableFuture<Integer> get( final PhysicalPlan plan, final Var v ) {
			final Map<Var, CompletableFuture<Integer>> mapForPlan = map.get(plan);
			return ( mapForPlan == null ) ? null : mapForPlan.get(v);
		}
	}

}
