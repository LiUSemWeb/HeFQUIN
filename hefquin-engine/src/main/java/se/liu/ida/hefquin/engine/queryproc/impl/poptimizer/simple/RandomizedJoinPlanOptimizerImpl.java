package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.simple;

import java.util.List;
import java.util.Random;

import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalToPhysicalOpConverter;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.PhysicalOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;

public class RandomizedJoinPlanOptimizerImpl implements JoinPlanOptimizer
{
	/**
	 * The class contains a random seed object.
	 */
	protected final Random random = new Random();
	
	@Override
	public PhysicalPlan determineJoinPlan( final List<PhysicalPlan> subplans,
	                                       final QueryProcContext ctxt )
			throws PhysicalOptimizationException
	{
		final LogicalToPhysicalOpConverter lop2pop = ctxt.getLogicalToPhysicalOpConverter();

		// nextInt returns an int between 0 (inclusive) and size (exclusive).
		int indexOfRandomPlan = random.nextInt( subplans.size() );

		// Picks out one plan to be the first, at random.
		PhysicalPlan currentPlan = subplans.get(indexOfRandomPlan);
		// Then removes that from the subplans.
		subplans.remove(indexOfRandomPlan);

		// After a first plan is picked, runs this loop until the subplans are exhausted.
		while ( subplans.size() > 0 ){
			// Pick a plan at random.
			indexOfRandomPlan = random.nextInt(subplans.size());
			// Join it with the current plan.
			currentPlan = PhysicalPlanFactory.createPlanWithJoin( currentPlan,
			                                                      subplans.get(indexOfRandomPlan),
			                                                      lop2pop );
			// Remove the joined plan.
			subplans.remove(indexOfRandomPlan);
		}

		// At this stage, everything is done and our working plan is now the final plan.
		return currentPlan;
	}

}
