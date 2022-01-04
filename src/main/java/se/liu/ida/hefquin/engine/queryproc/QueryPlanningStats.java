package se.liu.ida.hefquin.engine.queryproc;

import se.liu.ida.hefquin.engine.utils.Stats;

public interface QueryPlanningStats extends Stats
{
	long getOverallQueryPlanningTime();
	long getSourcePlanningTime();
	long getQueryOptimizationTime();

	SourcePlanningStats getSourcePlanningStats();
	QueryOptimizationStats getQueryOptimizationStats();
}
