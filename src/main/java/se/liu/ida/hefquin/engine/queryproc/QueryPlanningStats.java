package se.liu.ida.hefquin.engine.queryproc;

import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.utils.Stats;

public interface QueryPlanningStats extends Stats
{
	long getOverallQueryPlanningTime();
	long getSourcePlanningTime();
	long getLogicalOptimizationTime();
	long getPhysicalOptimizationTime();

	SourcePlanningStats getSourcePlanningStats();
	LogicalPlan getResultingSourceAssignment();
	LogicalPlan getResultingLogicalPlan();
	PhysicalQueryOptimizationStats getQueryOptimizationStats();
	PhysicalPlan getResultingPhysicalPlan();
}
