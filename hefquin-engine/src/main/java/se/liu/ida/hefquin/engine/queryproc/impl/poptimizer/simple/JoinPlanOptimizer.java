package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.simple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.PhysicalOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContextExt;

public interface JoinPlanOptimizer
{
	/**
	 * Returns a plan that combines the given subplans using binary joins.
	 */
	PhysicalPlan determineJoinPlan( List<PhysicalPlan> subplans,
	                                QueryProcContextExt ctx ) throws PhysicalOptimizationException;

	/**
	 * Returns a plan that combines the given subplans using binary joins.
	 */
	default PhysicalPlan determineJoinPlan( final PhysicalPlan[] subplans,
	                                        final QueryProcContextExt ctx )
			throws PhysicalOptimizationException
	{
		return determineJoinPlan( new ArrayList<>(Arrays.asList(subplans)), ctx );
	}
}
