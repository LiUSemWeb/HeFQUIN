package se.liu.ida.hefquin.engine.queryproc.impl.execution;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import se.liu.ida.hefquin.engine.queryplan.executable.ExecutablePlan;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.TaskBasedExecutablePlanImpl;
import se.liu.ida.hefquin.engine.queryproc.ExecutionEngine;
import se.liu.ida.hefquin.engine.queryproc.ExecutionException;
import se.liu.ida.hefquin.engine.queryproc.ExecutionStats;
import se.liu.ida.hefquin.engine.queryproc.QueryResultSink;

public class ExecutionEngineImpl implements ExecutionEngine
{
	//protected final ExecutorService threadPool = Executors.newCachedThreadPool();
	protected final ExecutorService threadPool = Executors.newFixedThreadPool(20);

	@Override
	public ExecutionStats execute( final ExecutablePlan plan, final QueryResultSink resultSink )
			throws ExecutionException
	{
		if ( plan instanceof TaskBasedExecutablePlanImpl )
			((TaskBasedExecutablePlanImpl) plan).setThreadPool(threadPool);

		plan.run(resultSink);

		threadPool.shutdownNow();
System.out.println("Shutting down " + threadPool.isShutdown() + " " + threadPool.isTerminated() );
		try {
			threadPool.awaitTermination(500L, TimeUnit.MILLISECONDS);
		} catch ( final InterruptedException e)  {
System.err.println("Terminating the thread pool was interrupted." );
			e.printStackTrace();
		}
System.out.println("Shutting down " + threadPool.isShutdown() + " " + threadPool.isTerminated() );
		return new ExecutionStatsImpl( plan.getStats() );
	}

}
