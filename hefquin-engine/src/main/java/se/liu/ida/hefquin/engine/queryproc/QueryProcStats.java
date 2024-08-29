package se.liu.ida.hefquin.engine.queryproc;

import se.liu.ida.hefquin.engine.utils.Stats;

public interface QueryProcStats extends Stats
{
	long getOverallQueryProcessingTime();
	long getPlanningTime();
	long getCompilationTime();
	long getExecutionTime();

	QueryPlanningStats getQueryPlanningStats();
	ExecutionStats getExecutionStats();
}
