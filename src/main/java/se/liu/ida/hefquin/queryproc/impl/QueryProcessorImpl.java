package se.liu.ida.hefquin.queryproc.impl;

import se.liu.ida.hefquin.query.Query;
import se.liu.ida.hefquin.queryplan.ExecutablePlan;
import se.liu.ida.hefquin.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.queryproc.ExecutionEngine;
import se.liu.ida.hefquin.queryproc.QueryPlanCompiler;
import se.liu.ida.hefquin.queryproc.QueryPlanner;
import se.liu.ida.hefquin.queryproc.QueryProcessor;
import se.liu.ida.hefquin.queryproc.QueryResultSink;

/**
 * Simple implementation of {@link QueryProcessor}.
 */
public class QueryProcessorImpl implements QueryProcessor
{
	protected final QueryPlanner planner;
	protected final QueryPlanCompiler planCompiler;
	protected final ExecutionEngine execEngine;

	QueryProcessorImpl( final QueryPlanner planner,
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

	public void processQuery( final Query query, final QueryResultSink resultSink ) {
		final PhysicalPlan qep = planner.createPlan(query);
		final ExecutablePlan prg = planCompiler.compile(qep);
		execEngine.execute(prg, resultSink);
	}

}
