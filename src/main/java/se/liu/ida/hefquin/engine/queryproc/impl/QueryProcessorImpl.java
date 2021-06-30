package se.liu.ida.hefquin.engine.queryproc.impl;

import se.liu.ida.hefquin.engine.query.Query;
import se.liu.ida.hefquin.engine.queryplan.ExecutablePlan;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.ExecutionEngine;
import se.liu.ida.hefquin.engine.queryproc.QueryPlanCompiler;
import se.liu.ida.hefquin.engine.queryproc.QueryPlanner;
import se.liu.ida.hefquin.engine.queryproc.QueryProcException;
import se.liu.ida.hefquin.engine.queryproc.QueryProcessor;
import se.liu.ida.hefquin.engine.queryproc.QueryResultSink;

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

	public QueryPlanner getPlanner() { return planner; }

	public QueryPlanCompiler getPlanCompiler() { return planCompiler; }

	public ExecutionEngine getExecutionEngine() { return execEngine; }

	public void processQuery( final Query query, final QueryResultSink resultSink )
			throws QueryProcException
	{
		final PhysicalPlan qep = planner.createPlan(query);
		final ExecutablePlan prg = planCompiler.compile(qep);
		execEngine.execute(prg, resultSink);
	}

}
