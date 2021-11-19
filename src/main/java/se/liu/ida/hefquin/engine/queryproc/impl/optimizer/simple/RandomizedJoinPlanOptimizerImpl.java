package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.simple;

import java.util.List;
import java.util.Random;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizationException;

public class RandomizedJoinPlanOptimizerImpl implements JoinPlanOptimizer {
	
	/**
	 * The class contains a random seed object.
	 * This cannot be marked final (compilation error if so).
	 */
	protected Random random = new Random();
	
	/**
	 * Have a list of subplans.
	 */
	protected final List<PhysicalPlan> subplans;
	
	/**
	 *  When initializing the algorithm, start with the given list of subplans.
	 *  @param subplans
	 */
	public RandomizedJoinPlanOptimizerImpl( final List<PhysicalPlan> subplans ) {
		this.subplans = subplans;
		this.random = new Random(); // Generates the random seed.
	}
	

	@Override
	public PhysicalPlan determineJoinPlan(List<PhysicalPlan> subplans) throws QueryOptimizationException {

		int indexOfRandomPlan = random.nextInt(subplans.size()); // nextInt returns an int between 0 (inclusive) and size (exclusive).

		PhysicalPlan currentPlan = subplans.get(indexOfRandomPlan); // Picks out one plan to be the first, at random.
		subplans.remove(indexOfRandomPlan); // Then removes that from the subplans.

		while ( subplans.size() > 0 ){ // After a first plan is picked, runs this loop until the subplans are exhausted.
			indexOfRandomPlan = random.nextInt(subplans.size());  // Picks a plan at random.
			currentPlan = PhysicalPlanFactory.createPlanWithJoin( currentPlan, subplans.get(indexOfRandomPlan)); // Joins it with the current plan.
			subplans.remove(indexOfRandomPlan);	// Remove the joined plan.
		}

		// At this stage, everything is done, and our working plan is now the final plan.
		return currentPlan;
	}
}
