package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.simple;

import java.util.List;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.CostModel;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils.CostEstimationUtils;

public class GreedyJoinPlanOptimizerImpl extends JoinPlanOptimizerBase
{
	protected final CostModel costModel;

	public GreedyJoinPlanOptimizerImpl( final CostModel costModel ) {
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
		public PhysicalPlan getResultingPlan() throws QueryOptimizationException {
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
		protected PhysicalPlan chooseFirstSubplan() throws QueryOptimizationException {
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
				throws QueryOptimizationException
		{
			final PhysicalPlan[] nextPossiblePlans = createNextPossiblePlans(currentPlan);
			final Double[] costs = CostEstimationUtils.getEstimates(costModel, nextPossiblePlans);

			int indexOfBestPlan = 0;

			for ( int i = 1; i < subplans.size(); ++i ) {
				if ( costs[indexOfBestPlan] > costs[i] ) {
					indexOfBestPlan = i;
				}
			}

			subplans.remove(indexOfBestPlan);
			return nextPossiblePlans[indexOfBestPlan];
		}

		/**
		 * Creates all possible binary join plans with the given plan as left
		 * child and one of the remaining subplans (see {@link #subplans}) as
		 * the right child.
		 */
		protected PhysicalPlan[] createNextPossiblePlans( final PhysicalPlan currentPlan ) {
			final PhysicalPlan[] plans = new PhysicalPlan[ subplans.size() ];
			for ( int i = 0; i < subplans.size(); ++i ) {
				plans[i] = PhysicalPlanFactory.createPlanWithJoin( currentPlan, subplans.get(i) );
			}
			return plans;
		}
	}

}
