package se.liu.ida.hefquin.engine.queryproc.impl;

import org.apache.jena.atlas.lib.Timer;
import se.liu.ida.hefquin.engine.query.Query;
import se.liu.ida.hefquin.engine.queryplan.ExecutablePlan;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanPrinter;
import se.liu.ida.hefquin.engine.queryproc.ExecutionEngine;
import se.liu.ida.hefquin.engine.queryproc.QueryPlanCompiler;
import se.liu.ida.hefquin.engine.queryproc.QueryPlanner;
import se.liu.ida.hefquin.engine.queryproc.QueryProcException;
import se.liu.ida.hefquin.engine.queryproc.QueryProcessor;
import se.liu.ida.hefquin.engine.queryproc.QueryResultSink;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.QueryOptimizationContext;

/**
 * Simple implementation of {@link QueryProcessor}.
 */
public class QueryProcessorImpl implements QueryProcessor
{
	protected final QueryPlanner planner;
	protected final QueryPlanCompiler planCompiler;
	protected final ExecutionEngine execEngine;
	protected final Timer timer = new Timer() ;
	protected QueryOptimizationContext ctxt;

	public QueryProcessorImpl(final QueryPlanner planner,
							  final QueryPlanCompiler planCompiler,
							  final ExecutionEngine execEngine,
							  final QueryOptimizationContext ctxt ) {

		assert ctxt != null;
		assert planner != null;
		assert planCompiler != null;
		assert execEngine != null;

		this.planner = planner;
		this.planCompiler = planCompiler;
		this.execEngine = execEngine;
		this.ctxt = ctxt;
	}

	public QueryPlanner getPlanner() { return planner; }

	public QueryPlanCompiler getPlanCompiler() { return planCompiler; }

	public ExecutionEngine getExecutionEngine() { return execEngine; }

	public void processQuery( final Query query, final QueryResultSink resultSink )
			throws QueryProcException
	{
		timer.startTimer();
		final PhysicalPlan qep = planner.createPlan(query);
		final long queryPlanningEndTime = timer.readTimer();
		ctxt.getMetrics().putQueryPlanningTime( timer.timeStr(queryPlanningEndTime) );

//		System.out.println("Selected physical query plan: " +"\n" + PhysicalPlanPrinter.print(qep) );

		final long queryPlanCompileStartTime = timer.readTimer();
		final ExecutablePlan prg = planCompiler.compile(qep);
		final long queryPlanCompileEndTime = timer.readTimer();
		ctxt.getMetrics().putPlanCompileTime( timer.timeStr(queryPlanCompileEndTime-queryPlanCompileStartTime) );

		final long queryPlanExecuteStartTime = timer.readTimer();
		execEngine.execute(prg, resultSink);
		final long queryPlanExecuteEndTime = timer.endTimer();
		ctxt.getMetrics().putPlanExecutingTime( timer.timeStr(queryPlanExecuteEndTime-queryPlanExecuteStartTime) );

	}

}
