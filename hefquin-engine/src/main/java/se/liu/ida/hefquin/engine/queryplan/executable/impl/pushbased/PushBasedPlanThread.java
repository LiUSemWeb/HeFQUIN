package se.liu.ida.hefquin.engine.queryplan.executable.impl.pushbased;

import java.util.List;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.utils.StatsProvider;

public interface PushBasedPlanThread extends Runnable, StatsProvider
{
	/**
	 * Can be used for cases in which multiple threads consume the output
	 * produced by this thread. While one of these threads may then consume
	 * the output directly (by calling {@link #transferAvailableOutput(List)}),
	 * each of the others must then consume the output via its own, separate
	 * {@link PushBasedPlanThread} instance, as set up and returned by this
	 * method. Hence, for each of these additional consuming threads, this
	 * method needs to be called.
	 *
	 * This method should be called before starting to consume the output.
	 *
	 * The use case for this are query plans that are not tree-shaped (but
	 * still DAGs), which we may have in particular for union-over-join source
	 * assignments where some of the joins have the same request operator as
	 * input (perhaps even with a filter operator on top of the request).
	 */
	PushBasedPlanThread addConnectorForAdditionalConsumer();

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
	 * This function first clears the given list and, then, transfers the
	 * currently-available solution mappings produced by this task from an
	 * internal buffer to the given list, in the order in which the solution
	 * mappings have been produced. The internal buffer will thus be empty
	 * at the point when this function returns (but further solution mappings
	 * may still be produced by this task and placed into the buffer to be
	 * available to be transferred at the next call of this function).
	 *
	 * If the internal buffer is empty at the time when this function is called
	 * (i.e., no solution mappings are currently available to be transferred),
	 * but the task is still running (see {@link #isRunning()}) and, thus, more
	 * solution mappings may still be produced, then the call of this function
	 * causes the calling thread to wait until either more solution mappings
	 * have been produced or it has become clear that no more solution mappings
	 * can be produced anymore. In the former case, the newly-produced solution
	 * mappings are transferred to the given list and the function returns. In
	 * the latter case, the function returns directly and, thus, the given list
	 * remains empty. Also, in the latter case, after the function has returned,
	 * {@link #isCompleted()} will be <code>true</code>.
	 *
	 * If the function is called after the task has completed or failed, then
	 * the function returns directly after clearing the given list (i.e., the
	 * list will be empty after the function call).
	 *
	 * Hence, if the given list is empty after calling this function call, then
	 * the function does not need to be called anymore; this task is not going
	 * to produce more solution mappings.
	 *
	 * This function is expected to be called by (and, thus, run in the context
	 * of) the thread that consumes the solution mappings produced by this task.
	 */
	void transferAvailableOutput( List<SolutionMapping> transferBuffer )
			throws ConsumingPushBasedPlanThreadException;

	/**
	 * Returns true if newly-produced solution mappings are available to be
	 * obtained via {@link #transferAvailableOutput(List)}. Hence, in this
	 * case, calling {@link #transferAvailableOutput(List)} would not cause
	 * the calling thread to wait.
	 */
	boolean hasMoreOutputAvailable();

	@Override
	StatsOfPushBasedPlanThread getStats();
}
