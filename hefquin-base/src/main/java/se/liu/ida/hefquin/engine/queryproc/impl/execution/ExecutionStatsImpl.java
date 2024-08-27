package se.liu.ida.hefquin.engine.queryproc.impl.execution;

import se.liu.ida.hefquin.engine.queryplan.executable.ExecutablePlanStats;
import se.liu.ida.hefquin.engine.queryproc.ExecutionStats;
import se.liu.ida.hefquin.engine.utils.StatsImpl;

public class ExecutionStatsImpl extends StatsImpl implements ExecutionStats
{
	protected static final String enPlanStats = "planStats";

	public ExecutionStatsImpl( final ExecutablePlanStats planStats ) {
		put( enPlanStats,  planStats );
	}

	@Override
	public ExecutablePlanStats getPlanStats() {
		return (ExecutablePlanStats) getEntry(enPlanStats);
	}

}
