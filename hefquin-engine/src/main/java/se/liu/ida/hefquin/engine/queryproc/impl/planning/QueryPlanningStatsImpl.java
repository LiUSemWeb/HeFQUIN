package se.liu.ida.hefquin.engine.queryproc.impl.planning;

import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.PhysicalOptimizationStats;
import se.liu.ida.hefquin.engine.queryproc.QueryPlanningStats;
import se.liu.ida.hefquin.engine.queryproc.SourcePlanningStats;
import se.liu.ida.hefquin.engine.utils.StatsImpl;

public class QueryPlanningStatsImpl extends StatsImpl implements QueryPlanningStats
{
	protected static final String enOverallPlanningTime  = "overallQueryPlanningTime";
	protected static final String enSrcPlanningTime      = "sourcePlanningTime";
	protected static final String enLogicalOptimizationTime     = "logicalOptimizationTime";
	protected static final String enPhysicalOptimizationTime    = "physicalOptimizationTime";
	protected static final String enSrcPlanningStats     = "sourcePlanningStats";
	protected static final String enSrcAssignment        = "resultingSourceAssignment";
	protected static final String enLogicalOptimizationResult   = "resultingLogicalPlan";
	protected static final String enPhysicalOptimizationResult  = "resultingPhysicalPlan";
	protected static final String enOptimizationStats    = "queryOptimizationStats";

	public QueryPlanningStatsImpl( final long overallQueryPlanningTime,
	                               final long sourcePlanningTime,
	                               final long logicalOptimizationTime,
	                               final long physicalOptimizationTime,
	                               final SourcePlanningStats sourcePlanningStats,
	                               final LogicalPlan resultingSourceAssignment,
	                               final LogicalPlan resultingLogicalPlan,
	                               final PhysicalPlan resultingPhysicalPlan,
	                               final PhysicalOptimizationStats queryOptimizationStats )
	{
		put( enOverallPlanningTime, Long.valueOf(overallQueryPlanningTime) );
		put( enSrcPlanningTime,     Long.valueOf(sourcePlanningTime) );
		put( enLogicalOptimizationTime,    Long.valueOf(logicalOptimizationTime) );
		put( enPhysicalOptimizationTime,   Long.valueOf(physicalOptimizationTime) );

		put( enSrcPlanningStats,   sourcePlanningStats );
		put( enSrcAssignment,      resultingSourceAssignment );
		put( enLogicalOptimizationResult,  resultingLogicalPlan );
		put( enPhysicalOptimizationResult, resultingPhysicalPlan );
		put( enOptimizationStats,  queryOptimizationStats );
	}

	@Override
	public long getOverallQueryPlanningTime() {
		return (Long) getEntry(enOverallPlanningTime);
	}

	@Override
	public long getSourcePlanningTime() {
		return (Long) getEntry(enSrcPlanningTime);
	}

	@Override
	public long getLogicalOptimizationTime() {
		return (Long) getEntry(enLogicalOptimizationTime);
	}

	@Override
	public long getPhysicalOptimizationTime() {
		return (Long) getEntry(enPhysicalOptimizationTime);
	}

	@Override
	public SourcePlanningStats getSourcePlanningStats() {
		return (SourcePlanningStats) getEntry(enSrcPlanningStats);
	}

	@Override
	public LogicalPlan getResultingSourceAssignment() {
		return (LogicalPlan) getEntry(enSrcAssignment);
	}

	@Override
	public LogicalPlan getResultingLogicalPlan() {
		return (LogicalPlan) getEntry(enLogicalOptimizationResult);
	}

	@Override
	public PhysicalOptimizationStats getQueryOptimizationStats() {
		return (PhysicalOptimizationStats) getEntry(enOptimizationStats);
	}

	@Override
	public PhysicalPlan getResultingPhysicalPlan() {
		return (PhysicalPlan) getEntry(enPhysicalOptimizationResult);
	}

}
