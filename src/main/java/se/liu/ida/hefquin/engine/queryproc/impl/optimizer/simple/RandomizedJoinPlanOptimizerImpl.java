package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.simple;

import java.util.List;
import java.util.Random;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizationException;

public class RandomizedJoinPlanOptimizerImpl extends JoinPlanOptimizerBase {
	@Override // Implement initializeEnumerationAlgorithm from JoinPlanOptimizerBase.
	public EnumerationAlgorithm initializeEnumerationAlgorithm( final List<PhysicalPlan> subplans ) {
		return new RandomizedEnumerationAlgorithm(subplans);
	}
	
	protected class RandomizedEnumerationAlgorithm implements EnumerationAlgorithm {
		/**
		 * Have a list of subplans.
		 */
		protected final List<PhysicalPlan> subplans;

		/**
		 *  When initializing the algorithm, start with the given list of subplans.
		 *  @param subplans
		 *  (These @ things where create automatically when I was making the comments, should I keep them in? // Simon)
		 */
		public RandomizedEnumerationAlgorithm( final List<PhysicalPlan> subplans ) {
			this.subplans = subplans;
		}
		

		/**
		 * Also taken from Greedy and left unchanged save for the different method being called within.
		 */
		@Override
		public PhysicalPlan getResultingPlan() throws QueryOptimizationException {
			PhysicalPlan currentPlan = chooseFirstSubplan();

			while ( subplans.size() > 0 ){
				currentPlan = addNextRandomJoin(currentPlan);
			}

			return currentPlan;
		}

		
		/**
		 * Creating all possible plans, which are then to be picked from at random.
		 * This method is directly copied from GreedyJoinPlanOptimizer without modification.
		 * @param currentPlan
		 * @return
		 */
		protected PhysicalPlan[] createNextPossiblePlans( final PhysicalPlan currentPlan ) {
			final PhysicalPlan[] plans = new PhysicalPlan[ subplans.size() ];
			for ( int i = 0; i < subplans.size(); ++i ) {
				plans[i] = PhysicalPlanFactory.createPlanWithJoin( currentPlan, subplans.get(i) );
			}
			return plans;
		}
		

		/**
		 * Chooses the first subplan.
		 * Instead of the best plan chosen by Greedy, this method picks a completely random plan.
		 * @return
		 * @throws QueryOptimizationException
		 */
		protected PhysicalPlan chooseFirstSubplan() throws QueryOptimizationException {
			Random random = new Random(); // Create a random seed.
			int indexOfRandomPlan = random.nextInt(subplans.size()); // nextInt returns an int between 0 (inclusive) and size (exclusive).

			final PhysicalPlan randomPlan = subplans.get(indexOfRandomPlan);
			subplans.remove(indexOfRandomPlan);
			return randomPlan;
		}
		

		/**
		 * Next random join.
		 * @param currentPlan
		 * @return
		 * @throws QueryOptimizationException
		 */
		protected PhysicalPlan addNextRandomJoin( final PhysicalPlan currentPlan )
				throws QueryOptimizationException
		{
			final PhysicalPlan[] nextPossiblePlans = createNextPossiblePlans(currentPlan);

			Random random = new Random(); // Create a random seed.
			// We're creating another random seed...
			// ...but would it be better performance-wise to store the random seed in the class?
			// This would allow us to only have to generate it once, with the downside of it taking up space.
			// I would probably opt for putting it as part of the class, because processor time is expensive and storage is cheap. // Simon

			int indexOfRandomPlan = random.nextInt(subplans.size());

			subplans.remove(indexOfRandomPlan);
			return nextPossiblePlans[indexOfRandomPlan];
		}
		
	}
	
	
}
