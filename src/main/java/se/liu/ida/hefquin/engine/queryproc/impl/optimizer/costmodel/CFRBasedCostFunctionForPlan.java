package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.costmodel;

import java.util.concurrent.CompletableFuture;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;

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
	public CompletableFuture<Integer> initiateCostEstimation( final PhysicalPlan plan ) {
		final CompletableFuture<Integer> futureForRoot = costFctForRoot.initiateCostEstimation(plan);
		if ( plan.numberOfSubPlans() == 0 ) {
			return futureForRoot;
		}

		CompletableFuture<Integer> f = futureForRoot;
		for ( int i = 0; i < plan.numberOfSubPlans(); i++ ) {
			final PhysicalPlan subPlan = plan.getSubPlan(i);
			f = f.thenCombine( initiateCostEstimation(subPlan),
			                   (total,valueForSubPlan) -> (total < 0 ? Integer.MAX_VALUE : total) + (valueForSubPlan < 0 ? Integer.MAX_VALUE: valueForSubPlan) );
		}

		return f;
	}

}
