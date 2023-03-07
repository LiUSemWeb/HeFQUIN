package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.simple;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import se.liu.ida.hefquin.engine.utils.Pair;
import java.util.List;

import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpRequestWithTranslation;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.PhysicalOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.CostModel;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.utils.CostEstimationUtils;

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

			Pair<Integer, Integer> indexOfBestPlan = new Pair<>(0, 0);
			double leastCost = Double.MAX_VALUE;
			for ( int indexOfSubPlan = 0; indexOfSubPlan < subplans.size(); indexOfSubPlan ++ ){
				final Double[] costs = CostEstimationUtils.getEstimates(costModel, nextPossiblePlans.get(indexOfSubPlan));

				for ( int i = 0; i < costs.length; i ++ ){
					if ( leastCost > costs[i] ) {
						leastCost = costs[i];
						indexOfBestPlan = new Pair<>(indexOfSubPlan, i);
					}
				}
			}

			int indexOfSubPlan = indexOfBestPlan.object1;
			subplans.remove( indexOfSubPlan );
			return nextPossiblePlans.get(indexOfBestPlan.object1).get(indexOfBestPlan.object2);
		}

		/**
		 * Creates all possible binary join plans with the given plan as left
		 * child and one of the remaining subplans (see {@link #subplans}) as
		 * the right child.
		 */
		protected Map<Integer, List<PhysicalPlan>> createNextPossiblePlans( final PhysicalPlan currentPlan ) {
			final Map<Integer, List<PhysicalPlan>> nextPossiblePlans = new HashMap<>();

			for ( int i = 0; i < subplans.size(); ++i ) {
				final List<PhysicalPlan> plans = new ArrayList<>();
				plans.add( PhysicalPlanFactory.createPlanWithJoin(currentPlan, subplans.get(i)) );

				if ( currentPlan.getRootOperator() instanceof PhysicalOpRequest ){
					plans.addAll( PhysicalPlanFactory.enumeratePlansWithUnaryOpFromReq( (PhysicalOpRequest<?,?>) currentPlan.getRootOperator(), subplans.get(i) ));
				}
				else if ( currentPlan.getRootOperator() instanceof PhysicalOpRequestWithTranslation ){
					plans.addAll( PhysicalPlanFactory.enumeratePlansWithUnaryOpFromReq( (PhysicalOpRequestWithTranslation<?,?>) currentPlan.getRootOperator(), subplans.get(i) ));
				}

				if ( subplans.get(i).getRootOperator() instanceof PhysicalOpRequest ) {
					plans.addAll( PhysicalPlanFactory.enumeratePlansWithUnaryOpFromReq( (PhysicalOpRequest<?,?>) subplans.get(i).getRootOperator(), currentPlan ) );
				}
				else if ( subplans.get(i).getRootOperator() instanceof PhysicalOpRequestWithTranslation ) {
					plans.addAll( PhysicalPlanFactory.enumeratePlansWithUnaryOpFromReq( (PhysicalOpRequestWithTranslation<?,?>) subplans.get(i).getRootOperator(), currentPlan ));
				}

				nextPossiblePlans.put(i, plans);
			}

			return nextPossiblePlans;
		}
	}

}
