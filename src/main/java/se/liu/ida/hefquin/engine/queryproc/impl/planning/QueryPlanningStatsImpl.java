package se.liu.ida.hefquin.engine.queryproc.impl.planning;

import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.PhysicalQueryOptimizationStats;
import se.liu.ida.hefquin.engine.queryproc.QueryPlanningStats;
import se.liu.ida.hefquin.engine.queryproc.SourcePlanningStats;
import se.liu.ida.hefquin.engine.utils.StatsImpl;

public class QueryPlanningStatsImpl extends StatsImpl implements QueryPlanningStats
{
	protected static final String enOverallPlanningTime  = "overallQueryPlanningTime";
	protected static final String enSrcPlanningTime      = "sourcePlanningTime";
	protected static final String enOptimizationTime     = "queryOptimizationTime";
	protected static final String enSrcPlanningStats     = "sourcePlanningStats";
	protected static final String enSrcAssignment        = "resultingSourceAssignment";
	protected static final String enOptimizationStats    = "queryOptimizationStats";
	protected static final String enOptimizationResult   = "resultingPhysicalPlan";

	public QueryPlanningStatsImpl( final long overallQueryPlanningTime,
	                               final long sourcePlanningTime,
	                               final long queryOptimizationTime,
	                               final SourcePlanningStats sourcePlanningStats,
	                               final LogicalPlan resultingSourceAssignment,
	                               final PhysicalQueryOptimizationStats queryOptimizationStats,
	                               final PhysicalPlan resultingPhysicalPlan )
	{
		put( enOverallPlanningTime, Long.valueOf(overallQueryPlanningTime) );
		put( enSrcPlanningTime,     Long.valueOf(sourcePlanningTime) );
		put( enOptimizationTime,    Long.valueOf(queryOptimizationTime) );

		put( enSrcPlanningStats,   sourcePlanningStats );
		put( enSrcAssignment,      resultingSourceAssignment );
		put( enOptimizationStats,  queryOptimizationStats );
		put( enOptimizationResult, resultingPhysicalPlan );
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
	public long getQueryOptimizationTime() {
		return (Long) getEntry(enOptimizationTime);
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
	public PhysicalQueryOptimizationStats getQueryOptimizationStats() {
		return (PhysicalQueryOptimizationStats) getEntry(enOptimizationStats);
	}

	@Override
	public PhysicalPlan getResultingPhysicalPlan() {
		return (PhysicalPlan) getEntry(enOptimizationResult);
	}

}
