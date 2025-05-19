package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.cardinality;

import static java.lang.Math.min;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.base.query.utils.ExpectedVariablesUtils;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.*;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanUtils;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.CardinalityEstimation;

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
			final PhysicalPlan reqTP = PhysicalPlanFactory.extractRequestAsPlan( (LogicalOpTPAdd) rootOp );
			return _initiateJoinCardinalityEstimation( plan.getSubPlan(0), reqTP, v );
		}
		else if ( rootOp instanceof LogicalOpBGPAdd ) {
			final PhysicalPlan reqBGP = PhysicalPlanFactory.extractRequestAsPlan( (LogicalOpBGPAdd) rootOp );
			return _initiateJoinCardinalityEstimation( plan.getSubPlan(0), reqBGP, v );
		}
		else if ( rootOp instanceof LogicalOpGPAdd ) {
			final PhysicalPlan reqGP = PhysicalPlanFactory.extractRequestAsPlan( (LogicalOpGPAdd) rootOp );
			return _initiateJoinCardinalityEstimation( plan.getSubPlan(0), reqGP, v );
		}
		else if ( rootOp instanceof LogicalOpLocalToGlobal || rootOp instanceof LogicalOpGlobalToLocal ){
			return _initiateCardinalityEstimation( plan.getSubPlan(0), v );
		}
		else if ( rootOp instanceof LogicalOpJoin ) {
			return _initiateJoinCardinalityEstimation( plan.getSubPlan(0), plan.getSubPlan(1), v );
		}
		else if ( rootOp instanceof LogicalOpUnion || rootOp instanceof LogicalOpMultiwayUnion ) {
			return _initiateMultiwayUnionCardinalityEstimation( plan, v);
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
//		Estimate cardinality of sub-queries
		final CompletableFuture<Integer> f1 = cardEstimator.initiateCardinalityEstimation(plan1);
		final CompletableFuture<Integer> f2 = cardEstimator.initiateCardinalityEstimation(plan2);

		final Set<Var> allJoinVars = PhysicalPlanUtils.intersectionOfAllVariables( plan1, plan2 );
		if ( allJoinVars.contains(v) ) {
			// Create a CompletableFuture that will complete once
			// both f1 and f2 have completed and, then, will combine
			// the results of f1 and f2 (i.e., c1 and c2) using the
			// min function.
			return f1.thenCombine( f2, (c1,c2) -> min( c1 , c2 ) );
		}

		final Set<Var> vars1 = ExpectedVariablesUtils.unionOfAllVariables( plan1.getExpectedVariables() );
		if ( !allJoinVars.isEmpty() && vars1.contains(v) ) {
			return f1;
		}

		final Set<Var> vars2 = ExpectedVariablesUtils.unionOfAllVariables( plan2.getExpectedVariables() );
		if ( !allJoinVars.isEmpty() && vars2.contains(v) ) {
			return f2;
		}
		// ... combine the results of f1 and f2 (i.e., c1 and c2)
		// by multiplication.
		return f1.thenCombine( f2, (c1,c2) -> ( c1 * c2 < 0 ? Integer.MAX_VALUE : c1 * c2 ) );

	}

	protected CompletableFuture<Integer> _initiateMultiwayUnionCardinalityEstimation(
			final PhysicalPlan plan,
			final Var v )
	{
		CompletableFuture<Integer> f = CompletableFuture.completedFuture(0);
		boolean containVar = false;
		for ( int i = 0; i < plan.numberOfSubPlans(); i++ ) {
			final Set<Var> vars = PhysicalPlanUtils.unionOfAllVariables( plan.getSubPlan(i) );
			if ( vars.contains(v) ) {
				final CompletableFuture<Integer> f1 = initiateCardinalityEstimation( plan.getSubPlan(i), v );
				f = f.thenCombine(f1, (c, c1) -> (c + c1) < 0 ? Integer.MAX_VALUE : (c + c1));

				containVar = true;
			}
		}

		if ( containVar )
			return f;
		else
			throw new IllegalArgumentException("The given variable is not included in the Union plan");

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
