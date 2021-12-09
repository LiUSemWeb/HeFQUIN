package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.simple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizationException;

public interface JoinPlanOptimizer
{
	/**
	 * Returns a plan that combines the given subplans using binary joins.
	 */
	PhysicalPlan determineJoinPlan( List<PhysicalPlan> subplans ) throws QueryOptimizationException;

	/**
	 * Returns a plan that combines the given subplans using binary joins.
	 */
	default PhysicalPlan determineJoinPlan( PhysicalPlan[] subplans ) throws QueryOptimizationException {
		return determineJoinPlan( new ArrayList<>(Arrays.asList(subplans)) );
	}
}
