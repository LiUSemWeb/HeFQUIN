package se.liu.ida.hefquin.engine.queryplan.executable.impl;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutablePlan;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutablePlanStats;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;
import se.liu.ida.hefquin.engine.queryproc.ExecutionException;
import se.liu.ida.hefquin.engine.queryproc.QueryResultSink;
import se.liu.ida.hefquin.engine.utils.StatsPrinter;

public class TaskBasedExecutablePlanImpl implements ExecutablePlan
{
	protected final LinkedList<ExecPlanTask> tasks;
	protected ExecutorService threadPool;

	public TaskBasedExecutablePlanImpl( final LinkedList<ExecPlanTask> tasks, final ExecutionContext ctx ) {
		assert ! tasks.isEmpty();
		this.tasks = tasks;

		threadPool = ctx.getExecutorServiceForPlanTasks();
	}

	@Override
	public void run( final QueryResultSink resultSink ) throws ExecutionException {
		if ( threadPool == null ) {
			throw new ExecutionException("thread pool missing");
		}

		// start all tasks, beginning with the last ones (which are the
		// ones for the leaf node operators), and collect 'Future's to
		// track their progress (each 'Future' has the same index in
		// 'futures' array as its corresponding task has in the 'tasks'
		// list)
		final Iterator<ExecPlanTask> it = tasks.descendingIterator();
		int i = tasks.size();
		final Future<?>[] futures = new Future<?>[tasks.size()];
		while ( it.hasNext() ) {
			final ExecPlanTask task = it.next();
			try {
				futures[--i] = threadPool.submit(task);
			}
			catch ( final RejectedExecutionException e ) {
				// if submitting one of the tasks failed, try to
				// cancel the ones that we have already started
				// (to free up the threads that are running them)
				for ( int j = i+1; j < tasks.size(); j++ ) {
					futures[j].cancel(true);
				}
				throw new ExecutionException("Submitting one of the tasks for execution caused an exception.", e);
			}
		}

		// consume all solution mappings from the root operator and send them to the result sink
		try {
			final ExecPlanTask rootTask = tasks.getFirst();
			boolean exhausted = false;
			while ( ! exhausted ) {
				final IntermediateResultBlock block = rootTask.getNextIntermediateResultBlock();
				if ( block != null ) {
					for ( final SolutionMapping sm : block.getSolutionMappings() ) {
						resultSink.send(sm);
					}
				}
				else {
					exhausted = true;
				}
			}
		}
		catch ( final Exception e ) {
			// try to cancel the tasks in order to free up the
			// up the threads that are running them, starting
			// from the task of the root operator to avoid
			// causing further problems (remember that this
			// is a pull-based execution; hence, for each pair
			// of an input-providing and an input-consuming
			// task, we should kill the input-consuming one
			// first)
			for ( int j = 0; j < tasks.size(); j++ ) {
				futures[j].cancel(true);
			}
			throw new ExecutionException("Consuming the solution mappings caused an exception.", e);
		}

		// check whether all tasks have completed successfully and kill
		// the ones that are still running (which they should not), again
		// we start from the task of the root operator to avoid causing
		// further problems in case some of the task are still active
		// for some reason
		for ( int j = 0; j < tasks.size(); j++ ) {
			final ExecPlanTask task = tasks.get(j);
			if ( ! task.isCompleted() ) {
				if ( task.isRunning() )
					System.err.println("Task #" + j + " seems to be still running.");
				else if ( task.hasFailed() ) {
					final Exception e = task.getCauseOfFailure();
					final String eMsg = ( e == null ) ? "" : " (" + e.getMessage() + ")";
					System.err.println("Task #" + j + " failed" + eMsg );
				}
				else
					System.err.println("Task #" + j + " is not completed but has not failed and is also not running anymore.");
			}

			final Future<?> future = futures[j];
			if ( ! future.isDone() ) {
				System.err.println("Task #" + j + " does not seem to be done.");
				future.cancel(true);
			}
		}
	}

	@Override
	public void resetStats() {
		// TODO Auto-generated method stub

	}

	@Override
	public ExecutablePlanStats getStats() {
		// TODO Auto-generated method stub
		return null;
	}

	public void print( final PrintStream str ) {
		int j = 0;
		for ( final ExecPlanTask t : tasks ) {
			str.println( "Task #" + j );
			StatsPrinter.print( t.getStats(), str, true );

			j++;
		}
	}
}
