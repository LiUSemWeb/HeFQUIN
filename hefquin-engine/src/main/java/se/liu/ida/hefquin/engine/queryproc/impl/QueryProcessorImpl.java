package se.liu.ida.hefquin.engine.queryproc.impl;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import se.liu.ida.hefquin.base.query.Query;
import se.liu.ida.hefquin.base.utils.Pair;
import se.liu.ida.hefquin.base.utils.StatsPrinterJSON;
import se.liu.ida.hefquin.engine.QueryProcessingStatsAndExceptions;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutablePlan;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.ExecutionEngine;
import se.liu.ida.hefquin.engine.queryproc.ExecutionStats;
import se.liu.ida.hefquin.engine.queryproc.QueryPlanCompiler;
import se.liu.ida.hefquin.engine.queryproc.QueryPlanner;
import se.liu.ida.hefquin.engine.queryproc.QueryPlanningStats;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;
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
	protected final QueryProcContext ctxt;

	public QueryProcessorImpl( final QueryPlanner planner,
	                           final QueryPlanCompiler planCompiler,
	                           final ExecutionEngine execEngine,
	                           final QueryProcContext ctxt ) {
		assert planner != null;
		assert planCompiler != null;
		assert execEngine != null;
		assert ctxt != null;

		this.planner = planner;
		this.planCompiler = planCompiler;
		this.execEngine = execEngine;
		this.ctxt = ctxt;
	}

	@Override
	public QueryPlanner getPlanner() { return planner; }

	@Override
	public QueryPlanCompiler getPlanCompiler() { return planCompiler; }

	@Override
	public ExecutionEngine getExecutionEngine() { return execEngine; }

	@Override
	public QueryProcessingStatsAndExceptions processQuery( final Query query,
	                                                       final QueryResultSink resultSink )
			throws QueryProcException
	{
		final long t1 = System.currentTimeMillis();
		final Pair<PhysicalPlan, QueryPlanningStats> qepAndStats = planner.createPlan(query);

		final long t2 = System.currentTimeMillis();

		final long t3, t4;
		final ExecutablePlan prg;
		final ExecutionStats execStats;
		final List<Exception> exceptionsCaughtDuringExecution;

		if ( ctxt.skipExecution() ) {
			t3 = System.currentTimeMillis();
			t4 = System.currentTimeMillis();
			prg = null;
			execStats = null;
			exceptionsCaughtDuringExecution = null;
		}
		else {
			prg = planCompiler.compile(qepAndStats.object1);

			t3 = System.currentTimeMillis();
			execStats = execEngine.execute(prg, resultSink);

			t4 = System.currentTimeMillis();
			exceptionsCaughtDuringExecution = prg.getExceptionsCaughtDuringExecution();
		}

		final QueryProcessingStatsAndExceptions s = new QueryProcessingStatsAndExceptionsImpl( t4-t1, t2-t1, t3-t2, t4-t3, qepAndStats.object2, execStats, exceptionsCaughtDuringExecution );

		if ( ctxt.isExperimentRun() ) {
			StatsPrinterJSON.print( s, System.out, true );
		}

		return s;
	}

	@Override
	public void shutdown() {
		final ExecutorService threadPool = ctxt.getExecutorServiceForPlanTasks();
		threadPool.shutdown();
		try {
			if ( ! threadPool.awaitTermination(500L, TimeUnit.MILLISECONDS) ) {
				threadPool.shutdownNow();
			}
		} catch ( InterruptedException ex ) {
			Thread.currentThread().interrupt();
			threadPool.shutdownNow();
		}
	}

}
