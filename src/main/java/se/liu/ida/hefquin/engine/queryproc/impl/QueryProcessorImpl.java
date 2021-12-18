package se.liu.ida.hefquin.engine.queryproc.impl;

import se.liu.ida.hefquin.engine.query.Query;
import se.liu.ida.hefquin.engine.queryplan.ExecutablePlan;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.ExecutionEngine;
import se.liu.ida.hefquin.engine.queryproc.ExecutionStats;
import se.liu.ida.hefquin.engine.queryproc.QueryPlanCompiler;
import se.liu.ida.hefquin.engine.queryproc.QueryPlanner;
import se.liu.ida.hefquin.engine.queryproc.QueryPlanningStats;
import se.liu.ida.hefquin.engine.queryproc.QueryProcException;
import se.liu.ida.hefquin.engine.queryproc.QueryProcStats;
import se.liu.ida.hefquin.engine.queryproc.QueryProcessor;
import se.liu.ida.hefquin.engine.queryproc.QueryResultSink;
import se.liu.ida.hefquin.engine.utils.Pair;

/**
 * Simple implementation of {@link QueryProcessor}.
 */
public class QueryProcessorImpl implements QueryProcessor
{
	protected final QueryPlanner planner;
	protected final QueryPlanCompiler planCompiler;
	protected final ExecutionEngine execEngine;

	public QueryProcessorImpl( final QueryPlanner planner,
	                           final QueryPlanCompiler planCompiler,
	                           final ExecutionEngine execEngine ) {
		assert planner != null;
		assert planCompiler != null;
		assert execEngine != null;

		this.planner = planner;
		this.planCompiler = planCompiler;
		this.execEngine = execEngine;
	}

	@Override
	public QueryPlanner getPlanner() { return planner; }

	@Override
	public QueryPlanCompiler getPlanCompiler() { return planCompiler; }

	@Override
	public ExecutionEngine getExecutionEngine() { return execEngine; }

	@Override
	public QueryProcStats processQuery( final Query query, final QueryResultSink resultSink )
			throws QueryProcException
	{
		final long t1 = System.currentTimeMillis();
		final Pair<PhysicalPlan, QueryPlanningStats> qepAndStats = planner.createPlan(query);

		final long t2 = System.currentTimeMillis();
		final ExecutablePlan prg = planCompiler.compile(qepAndStats.object1);

		final long t3 = System.currentTimeMillis();
		final ExecutionStats execStats = execEngine.execute(prg, resultSink);

		final long t4 = System.currentTimeMillis();

		return new QueryProcStatsImpl( t4-t1, t2-t1, t3-t2, t4-t3, qepAndStats.object2, execStats );
	}

}
