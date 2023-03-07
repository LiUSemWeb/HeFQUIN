package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.costmodel;

import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpBinaryUnion;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpMultiwayUnion;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpSymmetricHashJoin;

import java.util.concurrent.CompletableFuture;

/**
 * An implementation of {@link CostFunctionForPlan} that can be used
 * for any cost function that considers parallelism of operators
 * {@link PhysicalOpMultiwayUnion}, {@link PhysicalOpBinaryUnion}
 * and {@link PhysicalOpSymmetricHashJoin}, in which operators, the maximum
 * cost values among subQueries is taken into account. For other operators in the plan,
 * the cost is defined as the sum of operator-specific cost values.
 * This implementation uses recursion to first determine the cost values for subplans,
 * and to determine the operator-specific cost values the implementation
 * uses a {@link CostFunctionForRootOp}.
 */
public class CFRBasedParallelismCostFunctionForPlan implements CostFunctionForPlan
{
	protected final CostFunctionForRootOp costFctForRoot;

	protected CFRBasedParallelismCostFunctionForPlan(final CostFunctionForRootOp costFctForRoot ) {
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
		final PhysicalOperator pop = plan.getRootOperator();
		if ( pop instanceof PhysicalOpBinaryUnion || pop instanceof PhysicalOpMultiwayUnion || pop instanceof PhysicalOpSymmetricHashJoin ){
			CompletableFuture<Integer> max = CompletableFuture.completedFuture( 0 );
			for ( int i = 0; i < plan.numberOfSubPlans(); i++ ) {
				final PhysicalPlan subPlan = plan.getSubPlan(i);
				max = max.thenCombine( initiateCostEstimation(subPlan), (m, v) -> Math.max(m, v));
			}
			return f.thenCombine(max, ( total, valueForSubPlan) -> (total < 0 ? Integer.MAX_VALUE : total) + (valueForSubPlan < 0 ? Integer.MAX_VALUE: valueForSubPlan) );
		}
		else {
			for ( int i = 0; i < plan.numberOfSubPlans(); i++ ) {
				final PhysicalPlan subPlan = plan.getSubPlan(i);
				f = f.thenCombine( initiateCostEstimation(subPlan),
						(total,valueForSubPlan) -> (total < 0 ? Integer.MAX_VALUE : total) + (valueForSubPlan < 0 ? Integer.MAX_VALUE: valueForSubPlan) );
			}
			return f;
		}
	}

}
