package se.liu.ida.hefquin.engine.queryplan.executable.impl;

import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.engine.utils.StatsProvider;

public interface ExecPlanTask extends Runnable, StatsProvider
{
	/**
	 * true if the execution of this task is currently running; that is, the execution has started
	 * and has neither been interrupted nor completed
	 */
	boolean isRunning();

	/**
	 * true if the execution of this task has completed successfully
	 * false if either the execution is still running (in which case {@link #isRunning()} is <code>true</code>) or
	 * the execution has been interrupted (in which case {@link #isRunning()} is <code>false</code>)
	 */
	boolean isCompleted();
	/**
	 * true if the execution of this task has failed with an exception
	 * The exception that caused the failed can be obtained by calling
	 * {@link #getCauseOfFailure()}.
	 */
	boolean hasFailed();

	/**
	 * Returns the exception that caused the execution of this task to fail (in case {@link #hasFailed()} is <code>true</code>).
	 */
	Exception getCauseOfFailure();

	IntermediateResultBlock getNextIntermediateResultBlock() throws ExecPlanTaskInterruptionException, ExecPlanTaskInputException;

	@Override
	ExecPlanTaskStats getStats();
}
