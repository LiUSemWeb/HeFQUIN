package se.liu.ida.hefquin.engine.queryproc.impl;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.liu.ida.hefquin.base.query.Query;
import se.liu.ida.hefquin.base.utils.Pair;
import se.liu.ida.hefquin.base.utils.StatsPrinter;
import se.liu.ida.hefquin.engine.QueryProcessingStatsAndExceptions;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutablePlan;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalPlanWithoutResult;
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
	private static final Logger log = LoggerFactory.getLogger( QueryProcessorImpl.class );

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
		log.debug("Starting query processing.");

		final long t1 = System.currentTimeMillis();
		log.debug("Creating physical plan.");
		final Pair<PhysicalPlan, QueryPlanningStats> qepAndStats = planner.createPlan(query, ctxt);
		final PhysicalPlan qep = qepAndStats.object1;

		final long t2 = System.currentTimeMillis();

		log.debug( "Physical plan created in {} ms using {}.", (t2 - t1), planner.getClass().getSimpleName() );

		final long t3, t4;
		final ExecutionStats execStats;
		final List<Exception> exceptionsCaughtDuringExecution;

		if ( ctxt.skipExecution() || qep instanceof PhysicalPlanWithoutResult ) {
			log.debug(
				"Skipping execution (skipExecution={}, planWithoutResult={}).",
				ctxt.skipExecution(),
				qep instanceof PhysicalPlanWithoutResult );
			t3 = System.currentTimeMillis();
			t4 = System.currentTimeMillis();
			execStats = null;
			exceptionsCaughtDuringExecution = null;
		}
		else {
			log.debug("Compiling executable plan.");

			final ExecutablePlan prg = planCompiler.compile(qepAndStats.object1);

			if ( planner.getExecutablePlanPrinter() != null ) {
				log.debug( "Printing executable plan." );
				planner.getExecutablePlanPrinter().print( prg );
			}
			t3 = System.currentTimeMillis();

			log.debug( "Executable plan compiled in {} ms using {}.", (t3 - t2), planCompiler.getClass().getSimpleName() );

			log.debug( "Starting execution." );

			execStats = execEngine.execute(prg, resultSink);

			t4 = System.currentTimeMillis();

			log.debug( "Execution finished in {} ms.", (t4 - t3) );

			exceptionsCaughtDuringExecution = prg.getExceptionsCaughtDuringExecution();

			if ( ! exceptionsCaughtDuringExecution.isEmpty() )
				log.debug( "Execution completed with {} caught exception(s).", exceptionsCaughtDuringExecution.size() );
		}

		final QueryProcessingStatsAndExceptions s = new QueryProcessingStatsAndExceptionsImpl( t4-t1, t2-t1, t3-t2, t4-t3, qepAndStats.object2, execStats, exceptionsCaughtDuringExecution );

		log.debug( "Query processing finished. Total time: {} ms.", (t4 - t1) );

		if ( ctxt.isExperimentRun() ) {
			StatsPrinter.print( s, System.out, true );
		}

		return s;
	}

	@Override
	public void shutdown() {
		final ExecutorService threadPool = ctxt.getExecutorServiceForPlanTasks();
		log.debug( "Shutting down query processor thread pool." );
		threadPool.shutdown();
		try {
			if ( ! threadPool.awaitTermination(500L, TimeUnit.MILLISECONDS) ) {
				log.debug( "Thread pool did not terminate gracefully; forcing shutdown." );
				threadPool.shutdownNow();
			}
			else log.debug( "Thread pool terminated gracefully." );
		} catch ( InterruptedException ex ) {
			log.debug( "Interrupted while waiting for thread pool termination.", ex );
			Thread.currentThread().interrupt();
			threadPool.shutdownNow();
		}
	}

}
