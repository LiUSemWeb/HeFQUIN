package se.liu.ida.hefquin.engine.queryplan.executable.impl.pushbased;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutablePlan;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutablePlanStats;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;
import se.liu.ida.hefquin.engine.queryproc.ExecutionException;
import se.liu.ida.hefquin.engine.queryproc.QueryResultSink;

public class PushBasedExecutablePlanImpl implements ExecutablePlan
{
	protected final LinkedList<PushBasedPlanThread> tasks;
	protected ExecutorService threadPool;

	public PushBasedExecutablePlanImpl( final LinkedList<PushBasedPlanThread> tasks, final ExecutionContext ctx ) {
		assert ! tasks.isEmpty();
		this.tasks = tasks;

		threadPool = ctx.getExecutorServiceForPlanTasks();
	}

	@Override
	public void run( final QueryResultSink resultSink ) throws ExecutionException {
		if ( threadPool == null ) {
			throw new ExecutionException("thread pool missing");
		}

		// Start all tasks, beginning with the last ones (which are the
		// ones for the leaf node operators), and collect 'Future's to
		// track their progress (each 'Future' has the same index in
		// 'futures' array as its corresponding task has in the 'tasks'
		// list).
		final Iterator<PushBasedPlanThread> it = tasks.descendingIterator();
		int i = tasks.size();
		final Future<?>[] futures = new Future<?>[tasks.size()];
		while ( it.hasNext() ) {
			final PushBasedPlanThread task = it.next();
			try {
				futures[--i] = threadPool.submit(task);
			}
			catch ( final RejectedExecutionException e ) {
				// If submitting one of the tasks failed, try to
				// cancel the ones that we have already started
				// (to free up the threads that are running them).
				for ( int j = i+1; j < tasks.size(); j++ ) {
					futures[j].cancel(true);
				}
				throw new ExecutionException("Submitting one of the tasks for execution caused an exception.", e);
			}
		}

		// Consume all solution mappings from the root operator
		// and send them to the result sink.
		final List<SolutionMapping> transferBuffer = new ArrayList<>();
		final PushBasedPlanThread rootTask = tasks.getFirst();
		try {
			boolean exhausted = false;
			while ( ! exhausted ) {
				rootTask.transferAvailableOutput(transferBuffer);
				if ( ! transferBuffer.isEmpty() ) {
					for ( final SolutionMapping sm : transferBuffer )
						resultSink.send(sm);
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
			final PushBasedPlanThread task = tasks.get(j);
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
		final StatsOfPushBasedPlanThread[] statsOfTasks = new StatsOfPushBasedPlanThread[ tasks.size() ];
		int i = 0;
		for ( final PushBasedPlanThread t : tasks ) {
			statsOfTasks[i] = t.getStats();
			i++;
		}

		return new StatsOfPushBasedExecutablePlan(statsOfTasks);
	}

	@Override
	public List<Exception> getExceptionsCaughtDuringExecution() {
		final List<Exception> allExceptions = new ArrayList<>();
		for ( final PushBasedPlanThread t : tasks ) {
			final List<Exception> exceptionsOfTask = ( (PushBasedPlanThreadImplBase) t ).getExceptionsCaughtDuringExecution();
			allExceptions.addAll(exceptionsOfTask);
		}

		return allExceptions;
	}

}
