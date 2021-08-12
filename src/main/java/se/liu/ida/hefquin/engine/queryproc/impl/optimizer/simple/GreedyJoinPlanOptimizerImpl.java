package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.simple;

import java.util.List;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.BinaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalPlanWithBinaryRootImpl;
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
				currentPlan = cardinalityTwoSubQueries(currentPlan);
			}

			return currentPlan;
		}

		protected PhysicalPlan chooseFirstSubplan() throws QueryOptimizationException {
			final double[] costs = CostEstimationUtils.getEstimates(costModel, subplans);

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

		protected PhysicalPlan cardinalityTwoSubQueries( final PhysicalPlan currentPlan )
				throws QueryOptimizationException
		{
			final PhysicalPlan[] nextPossiblePlans = createNextPossiblePlans(currentPlan);
			final double[] costs = CostEstimationUtils.getEstimates(costModel, nextPossiblePlans);

			int indexOfBestPlan = 0;

			for ( int i = 1; i < subplans.size(); ++i ) {
				if ( costs[indexOfBestPlan] > costs[i] ) {
					indexOfBestPlan = i;
				}
			}

			subplans.remove(indexOfBestPlan);
			return nextPossiblePlans[indexOfBestPlan];
		}

		protected PhysicalPlan[] createNextPossiblePlans( final PhysicalPlan currentPlan ) {
			final PhysicalPlan[] plans = new PhysicalPlan[ subplans.size() ];
			for ( int i = 0; i < subplans.size(); ++i ) {
				final BinaryPhysicalOp joinOp = EnumerationAlgorithm.createNewJoinOperator();
				plans[i] = new PhysicalPlanWithBinaryRootImpl( joinOp, currentPlan, subplans.get(i) );
			}
			return plans;
		}
	}

}
