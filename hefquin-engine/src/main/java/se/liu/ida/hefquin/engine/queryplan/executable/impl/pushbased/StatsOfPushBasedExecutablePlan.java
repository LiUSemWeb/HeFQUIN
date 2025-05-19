package se.liu.ida.hefquin.engine.queryplan.executable.impl.pushbased;

import java.util.Arrays;
import java.util.List;

import se.liu.ida.hefquin.base.utils.StatsImpl;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutablePlanStats;

/**
 * An implementation of {@link ExecutablePlanStats}
 * for {@link PushBasedExecutablePlanImpl}.
 */
public class StatsOfPushBasedExecutablePlan extends StatsImpl implements ExecutablePlanStats
{
	protected static final String enNumberOfTasks  = "numberOfTasks";
	protected static final String enStatsOfTasks   = "statsOfTasks";

	public StatsOfPushBasedExecutablePlan( final List<StatsOfPushBasedPlanThread> statsOfTasks ) {
		put( enNumberOfTasks,   Integer.valueOf(statsOfTasks.size()) );
		put( enStatsOfTasks,    statsOfTasks );
	}

	public StatsOfPushBasedExecutablePlan( final StatsOfPushBasedPlanThread[] statsOfTasks ) {
		put( enNumberOfTasks,   Integer.valueOf(statsOfTasks.length) );
		put( enStatsOfTasks,    Arrays.asList(statsOfTasks) );
	}
}
