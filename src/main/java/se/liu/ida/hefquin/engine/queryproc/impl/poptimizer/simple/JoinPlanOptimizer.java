package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.simple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.PhysicalOptimizationException;

public interface JoinPlanOptimizer
{
	/**
	 * Returns a plan that combines the given subplans using binary joins.
	 */
	PhysicalPlan determineJoinPlan( List<PhysicalPlan> subplans ) throws PhysicalOptimizationException;

	/**
	 * Returns a plan that combines the given subplans using binary joins.
	 */
	default PhysicalPlan determineJoinPlan( PhysicalPlan[] subplans ) throws PhysicalOptimizationException {
		return determineJoinPlan( new ArrayList<>(Arrays.asList(subplans)) );
	}
}
