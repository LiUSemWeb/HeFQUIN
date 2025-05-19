package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.simple;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import org.apache.jena.sparql.core.Var;

import java.util.HashMap;

import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpBinaryUnion;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpMultiwayUnion;

import java.util.List;

import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanUtils;
import se.liu.ida.hefquin.engine.queryproc.PhysicalOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.CostModel;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.utils.CostEstimationUtils;

public class CostModelBasedGreedyJoinPlanOptimizerImpl extends JoinPlanOptimizerBase
{
	protected final CostModel costModel;

	public CostModelBasedGreedyJoinPlanOptimizerImpl( final CostModel costModel ) {
		assert costModel != null;
		this.costModel= costModel;
	}

	@Override
	public EnumerationAlgorithm initializeEnumerationAlgorithm( final List<PhysicalPlan> subplans ) {
		return new GreedyEnumerationAlgorithm(subplans);
	}


	protected class GreedyEnumerationAlgorithm implements EnumerationAlgorithm
	{
		protected final List<PhysicalPlan> subplans;

		public GreedyEnumerationAlgorithm( final List<PhysicalPlan> subplans ) {
			this.subplans = subplans;
		}

		@Override
		public PhysicalPlan getResultingPlan() throws PhysicalOptimizationException {
			PhysicalPlan currentPlan = chooseFirstSubplan();

			while ( subplans.size() > 0 ){
				currentPlan = addNextBestJoin(currentPlan);
			}

			return currentPlan;
		}

		/**
		 * Compares all available subplans (see {@link #subplans}) in terms of
		 * their respective costs (as estimated by using the {@link #costModel})
		 * and returns the one with the lowest estimated cost.
		 */
		protected PhysicalPlan chooseFirstSubplan() throws PhysicalOptimizationException {
			final Double[] costs = CostEstimationUtils.getEstimates(costModel, subplans);

			int indexOfBestPlan = 0;

			for ( int i = 1; i < subplans.size(); ++i ) {
				if ( costs[indexOfBestPlan] > costs[i] ) {
					indexOfBestPlan = i;
				}
			}

			final PhysicalPlan bestPlan = subplans.get(indexOfBestPlan);
			subplans.remove(indexOfBestPlan);
			return bestPlan;
		}

		/**
		 * Creates a binary join plan with the given plan as left child and one
		 * of the remaining subplans (see {@link #subplans}) as the right child.
		 * The subplan that is picked to be the right child is the one for which
		 * the returned plan has the minimum cost among all plans that can be
		 * constructed in this way.
		 */
		protected PhysicalPlan addNextBestJoin( final PhysicalPlan currentPlan )
				throws PhysicalOptimizationException
		{
			final Map<Integer, List<PhysicalPlan>> nextPossiblePlans = createNextPossiblePlans(currentPlan);

			PhysicalPlan bestCandidate = null;
			int indexOfBestCandidate = -1;
			double costOfBestCandidate = Double.MAX_VALUE;

			for ( int indexOfSubPlan = 0; indexOfSubPlan < subplans.size(); indexOfSubPlan++ ){
				final List<PhysicalPlan> candidatePlansForSubPlan = nextPossiblePlans.get(indexOfSubPlan);
				if ( candidatePlansForSubPlan == null || candidatePlansForSubPlan.isEmpty() ){
					continue;
				}

				final Double[] costs = CostEstimationUtils.getEstimates(costModel, candidatePlansForSubPlan);

				for ( int i = 0; i < costs.length; i++ ){
					if ( costOfBestCandidate > costs[i] ) {
						bestCandidate = candidatePlansForSubPlan.get(i);
						indexOfBestCandidate = indexOfSubPlan;
						costOfBestCandidate = costs[i];
					}
				}
			}

			subplans.remove(indexOfBestCandidate);
			return bestCandidate;
		}

		/**
		 * Creates all possible binary join plans with the given plan as left
		 * child and one of the remaining subplans (see {@link #subplans}) as
		 * the right child, as well as plans with unary (gpAdd-based) joins
		 * with the given plan as the child. In the returned map, the index
		 * that each of these remaining subplans has within {@link #subplans}
		 * is mapped to the collection of possible join plans created using
		 * this subplan.
		 */
		protected Map<Integer, List<PhysicalPlan>> createNextPossiblePlans( final PhysicalPlan currentPlan ) {
			final Map<Integer, List<PhysicalPlan>> nextPossiblePlans = new HashMap<>();

			// First, consider only those subplans that share variables
			// with the currentPlan, which would then be the respective
			// join variables. We ignore the other subplans at this stage
			// because joining currentPlan with any of them would become
			// a cartesian product.
			for ( int i = 0; i < subplans.size(); i++ ) {
				final PhysicalPlan subplan = subplans.get(i);
				final Set<Var> joinVars = PhysicalPlanUtils.intersectionOfAllVariables(currentPlan, subplan);
				if ( ! joinVars.isEmpty() ) {
					nextPossiblePlans.put( i, createAllJoinPlans(currentPlan, subplan) );
				}
			}

			// If there are subplans that share variables with the currentPlan,
			// return the join plans created using these plans.
			if ( ! nextPossiblePlans.isEmpty() ) {
				return nextPossiblePlans;
			}

			// If there are no subplans that share variables with the
			// currentPlan, create join plans using all of the subplans,
			// which will all be cartesian products.
			for ( int i = 0; i < subplans.size(); i++ ) {
				final PhysicalPlan subplan = subplans.get(i);
				nextPossiblePlans.put( i, createAllJoinPlans(currentPlan, subplan) );
			}

			return nextPossiblePlans;
		}
	}

	/**
	 * Creates a list of join plans, including a (default) binary join of the
	 * given two plans as well as possible plans with unary (gpAdd-based) joins
	 * in which the first given plan is the child.
	 */
	protected List<PhysicalPlan> createAllJoinPlans( final PhysicalPlan leftOrChild,
	                                                 final PhysicalPlan rightOrTop ) {
		final List<PhysicalPlan> plans = new ArrayList<>();
		plans.add( PhysicalPlanFactory.createPlanWithJoin(leftOrChild,rightOrTop) );

		final PhysicalOperator rootOfSubPlan = rightOrTop.getRootOperator();
		if ( rootOfSubPlan instanceof PhysicalOpRequest ) {
			final PhysicalOpRequest<?,?> _rootOfSubPlan = (PhysicalOpRequest<?,?>) rootOfSubPlan;
			final List<PhysicalPlan> plansWithUnaryRoot = PhysicalPlanFactory.enumeratePlansWithUnaryOpFromReq(_rootOfSubPlan, leftOrChild);
			plans.addAll(plansWithUnaryRoot);
		}
		else if (    rootOfSubPlan instanceof PhysicalOpBinaryUnion
		          || rootOfSubPlan instanceof PhysicalOpMultiwayUnion ) {
			if ( PhysicalPlanFactory.checkUnaryOpApplicableToUnionPlan(rightOrTop) ) {
				final PhysicalPlan planWithUnariesUnderUnion = PhysicalPlanFactory.createPlanWithUnaryOpForUnionPlan(leftOrChild, rightOrTop);
				plans.add(planWithUnariesUnderUnion);
			}
		}

		return plans;
	}

}
