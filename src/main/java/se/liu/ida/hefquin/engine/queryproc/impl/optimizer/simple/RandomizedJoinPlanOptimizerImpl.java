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
		 * The class contains a random seed object.
		 */
		protected final Random random = new Random();
		
		/**
		 * Have a list of subplans.
		 */
		protected final List<PhysicalPlan> subplans;

		/**
		 *  When initializing the algorithm, start with the given list of subplans.
		 *  @param subplans
		 */
		public RandomizedEnumerationAlgorithm( final List<PhysicalPlan> subplans ) {
			this.subplans = subplans;
			this.random = new Random(); // Generates the random seed.
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
		 * Returns one of the subplans which is picked at random.
		 */
		protected PhysicalPlan chooseFirstSubplan() throws QueryOptimizationException {
			final int indexOfRandomPlan = random.nextInt(subplans.size()); // nextInt returns an int between 0 (inclusive) and size (exclusive).

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

			int indexOfRandomPlan = this.random.nextInt(subplans.size());
			final PhysicalPlan nextPlan = PhysicalPlanFactory.createPlanWithJoin( currentPlan, subplans.get(indexOfRandomPlan));
			// Instead of creating a whole array of plans, create one.
			
			subplans.remove(indexOfRandomPlan);
			// Remove the old subplan.
			
			return nextPlan; // Return the created plan.
		}
		
	}
	
	
}
