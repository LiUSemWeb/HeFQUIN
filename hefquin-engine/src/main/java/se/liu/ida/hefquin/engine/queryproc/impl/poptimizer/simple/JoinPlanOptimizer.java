package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.simple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.PhysicalOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;

public interface JoinPlanOptimizer
{
	/**
	 * Returns a plan that combines the given subplans using binary joins.
	 */
	PhysicalPlan determineJoinPlan( List<PhysicalPlan> subplans,
	                                QueryProcContext ctxt ) throws PhysicalOptimizationException;

	/**
	 * Returns a plan that combines the given subplans using binary joins.
	 */
	default PhysicalPlan determineJoinPlan( final PhysicalPlan[] subplans,
	                                        final QueryProcContext ctxt )
			throws PhysicalOptimizationException
	{
		return determineJoinPlan( new ArrayList<>(Arrays.asList(subplans)), ctxt );
	}
}
