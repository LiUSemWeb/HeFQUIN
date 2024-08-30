package se.liu.ida.hefquin.engine.queryproc;

import se.liu.ida.hefquin.base.utils.Stats;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;

public interface QueryPlanningStats extends Stats
{
	long getOverallQueryPlanningTime();
	long getSourcePlanningTime();
	long getLogicalOptimizationTime();
	long getPhysicalOptimizationTime();

	SourcePlanningStats getSourcePlanningStats();
	LogicalPlan getResultingSourceAssignment();
	LogicalPlan getResultingLogicalPlan();
	PhysicalOptimizationStats getQueryOptimizationStats();
	PhysicalPlan getResultingPhysicalPlan();
}
