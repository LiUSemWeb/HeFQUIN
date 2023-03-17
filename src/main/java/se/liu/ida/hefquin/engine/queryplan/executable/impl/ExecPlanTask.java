package se.liu.ida.hefquin.engine.queryplan.executable.impl;

import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.engine.utils.StatsProvider;

public interface ExecPlanTask extends Runnable, StatsProvider
{
	/**
	 * Returns true if the execution of this task is currently running;
	 * that is, the execution has started and has neither been interrupted
	 * nor completed.
	 */
	boolean isRunning();

	/**
	 * Returns true if the execution of this task has completed successfully,
	 * and returns false if either the execution is still running (in which
	 * case {@link #isRunning()} is <code>true</code>) or the execution has
	 * been interrupted (in which case {@link #isRunning()} is <code>false</code>).
	 */
	boolean isCompleted();

	/**
	 * Returns true if the execution of this task has failed with an
	 * exception. The exception that caused the failed can be obtained
	 * by calling {@link #getCauseOfFailure()}.
	 */
	boolean hasFailed();

	/**
	 * Returns the exception that caused the execution of this task
	 * to fail (in case {@link #hasFailed()} is <code>true</code>).
	 */
	Exception getCauseOfFailure();

	/**
	 * Returns either the next intermediate result block produced by this task
	 * or <code>null</code> if all these blocks have been returned already to
	 * earlier calls of this function. If no next block is currently available
	 * at the time when this function is called, then the call of this function
	 * causes the calling thread to wait until the next block has been produced
	 * (or it has become clear that no more blocks can be produced anymore).
	 */
	IntermediateResultBlock getNextIntermediateResultBlock() throws ExecPlanTaskInterruptionException, ExecPlanTaskInputException;

	/**
	 * Returns true if an intermediate result block is already be available
	 * to be requested via {@link #getNextIntermediateResultBlock()}. Hence,
	 * in this case, calling {@link #getNextIntermediateResultBlock()} would
	 * not cause the calling thread to wait.
	 */
	boolean hasNextIntermediateResultBlockAvailable();

	@Override
	ExecPlanTaskStats getStats();
}
