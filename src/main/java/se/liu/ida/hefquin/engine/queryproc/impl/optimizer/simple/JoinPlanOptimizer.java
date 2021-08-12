package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.simple;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizationException;

public interface JoinPlanOptimizer
{
	/**
	 * Returns a plan that combines the given subplans using binary joins.
	 */
	PhysicalPlan determineJoinPlan( PhysicalPlan[] subplans ) throws QueryOptimizationException;
}
