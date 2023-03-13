package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.costmodel;

import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpBinaryUnion;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpMultiwayUnion;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpSymmetricHashJoin;

import java.util.HashSet;
import java.util.Set;
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
public class CFRBasedParallelismCostFunctionForPlan extends CFRBasedCostFunctionForPlan
{
	protected CFRBasedParallelismCostFunctionForPlan(final CostFunctionForRootOp costFctForRoot ) {
		super(costFctForRoot);
	}

	@Override
	public CompletableFuture<Integer> aggregateValueForAllSubPlans( final Set<PhysicalPlan> visitedPlans, final CompletableFuture<Integer> futureForRoot, final PhysicalPlan plan ) {
		final PhysicalOperator pop = plan.getRootOperator();
		if ( pop instanceof PhysicalOpBinaryUnion || pop instanceof PhysicalOpMultiwayUnion || pop instanceof PhysicalOpSymmetricHashJoin ){
			CompletableFuture<Integer> cardForSubPlan = CompletableFuture.completedFuture( 0 );
			CompletableFuture<Integer> f = futureForRoot;
			for ( int i = 0; i < plan.numberOfSubPlans(); i++ ) {
				final PhysicalPlan subPlan = plan.getSubPlan(i);
				cardForSubPlan = cardForSubPlan.thenCombine( initiateCostEstimation( visitedPlans, subPlan), (m, v) -> Math.max(m, v));
			}
			return f.thenCombine(cardForSubPlan, ( total, valueForSubPlan) -> (total + valueForSubPlan) < 0 ? Integer.MAX_VALUE : (total + valueForSubPlan) );
		}
		else
			return super.aggregateValueForAllSubPlans(new HashSet<>(), futureForRoot, plan);
	}

}
