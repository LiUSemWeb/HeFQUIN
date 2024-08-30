package se.liu.ida.hefquin.engine.queryproc.impl;

import se.liu.ida.hefquin.base.utils.StatsImpl;
import se.liu.ida.hefquin.engine.queryproc.ExecutionStats;
import se.liu.ida.hefquin.engine.queryproc.QueryPlanningStats;
import se.liu.ida.hefquin.engine.queryproc.QueryProcStats;

public class QueryProcStatsImpl extends StatsImpl implements QueryProcStats
{
	protected static final String enOverallProcTime     = "overallQueryProcessingTime";
	protected static final String enPlanningTime        = "planningTime";
	protected static final String enCompilationTime     = "compilationTime";
	protected static final String enExecutionTime       = "executionTime";
	protected static final String enQueryPlanningStats  = "queryPlanningStats";
	protected static final String enExecStats           = "executionStats";

	public QueryProcStatsImpl( final long overallQueryProcessingTime,
	                           final long planningTime,
	                           final long compilationTime,
	                           final long executionTime,
	                           final QueryPlanningStats queryPlanningStats,
	                           final ExecutionStats execStats )
	{
		put( enOverallProcTime, Long.valueOf(overallQueryProcessingTime) );
		put( enPlanningTime,    Long.valueOf(planningTime) );
		put( enCompilationTime, Long.valueOf(compilationTime) );
		put( enExecutionTime,   Long.valueOf(executionTime) );

		put( enQueryPlanningStats, queryPlanningStats );
		put( enExecStats,          execStats );
	}

	@Override
	public long getOverallQueryProcessingTime() {
		return (Long) getEntry(enOverallProcTime);
	}

	@Override
	public long getPlanningTime() {
		return (Long) getEntry(enPlanningTime);
	}

	@Override
	public long getCompilationTime() {
		return (Long) getEntry(enCompilationTime);
	}

	@Override
	public long getExecutionTime() {
		return (Long) getEntry(enExecutionTime);
	}

	@Override
	public QueryPlanningStats getQueryPlanningStats() {
		return (QueryPlanningStats) getEntry(enQueryPlanningStats);
	}

	@Override
	public ExecutionStats getExecutionStats() {
		return (ExecutionStats) getEntry(enExecStats);
	}

}
