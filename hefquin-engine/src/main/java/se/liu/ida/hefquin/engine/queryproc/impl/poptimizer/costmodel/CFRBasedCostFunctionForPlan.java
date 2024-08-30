package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.costmodel;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;

/**
 * Generic implementation of {@link CostFunctionForPlan} that can be used
 * for any cost function that is defined as the sum of operator-specific
 * cost values for all operators in the plan. This implementation uses
 * recursion to first determine the cost values for subplans, and to
 * determines the operator-specific cost values the implementation
 * uses a {@link CostFunctionForRootOp}.
 */
public class CFRBasedCostFunctionForPlan implements CostFunctionForPlan
{
	protected final CostFunctionForRootOp costFctForRoot;

	protected CFRBasedCostFunctionForPlan( final CostFunctionForRootOp costFctForRoot ) {
		assert costFctForRoot != null;
		this.costFctForRoot = costFctForRoot;
	}

	@Override
	public CompletableFuture<Integer> initiateCostEstimation( final Set<PhysicalPlan> visitedPlans, final PhysicalPlan plan ) {
		if ( visitedPlans.contains( plan ) ){
			return CompletableFuture.completedFuture(0);
		}

		visitedPlans.add(plan);
		final CompletableFuture<Integer> futureForRoot = costFctForRoot.initiateCostEstimation(plan);
		if ( plan.numberOfSubPlans() == 0 ) {
			return futureForRoot;
		}
		return aggregateValueForAllSubPlans( visitedPlans, futureForRoot, plan );
	}

	public CompletableFuture<Integer> aggregateValueForAllSubPlans( final Set<PhysicalPlan> visitedPlan, final CompletableFuture<Integer> futureForRoot, final PhysicalPlan plan ) {
		CompletableFuture<Integer> f = futureForRoot;
		for ( int i = 0; i < plan.numberOfSubPlans(); i++ ) {
			final PhysicalPlan subPlan = plan.getSubPlan(i);
			f = f.thenCombine( initiateCostEstimation( visitedPlan, subPlan),
					(total,valueForSubPlan) -> (total + valueForSubPlan) < 0 ? Integer.MAX_VALUE : (total + valueForSubPlan) );
		}

		return f;
	}

}
