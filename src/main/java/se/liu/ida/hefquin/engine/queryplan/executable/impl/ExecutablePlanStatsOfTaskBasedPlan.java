package se.liu.ida.hefquin.engine.queryplan.executable.impl;

import java.util.Arrays;
import java.util.List;

import se.liu.ida.hefquin.engine.queryplan.executable.ExecutablePlanStats;
import se.liu.ida.hefquin.engine.utils.StatsImpl;

/**
 * An implementation of {@link ExecutablePlanStats}
 * for {@link TaskBasedExecutablePlanImpl}.
 */
public class ExecutablePlanStatsOfTaskBasedPlan extends StatsImpl implements ExecutablePlanStats
{
	protected static final String enNumberOfTasks  = "numberOfTasks";
	protected static final String enStatsOfTasks   = "statsOfTasks";

	public ExecutablePlanStatsOfTaskBasedPlan( final List<ExecPlanTaskStats> statsOfTasks ) {
		put( enNumberOfTasks,   Integer.valueOf(statsOfTasks.size()) );
		put( enStatsOfTasks,    statsOfTasks );
	}

	public ExecutablePlanStatsOfTaskBasedPlan( final ExecPlanTaskStats[] statsOfTasks ) {
		put( enNumberOfTasks,   Integer.valueOf(statsOfTasks.length) );
		put( enStatsOfTasks,    Arrays.asList(statsOfTasks) );
	}
}
