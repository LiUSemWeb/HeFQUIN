package se.liu.ida.hefquin.engine.queryproc.impl.execution;

import se.liu.ida.hefquin.base.utils.StatsImpl;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutablePlanStats;
import se.liu.ida.hefquin.engine.queryproc.ExecutionStats;

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
