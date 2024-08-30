package se.liu.ida.hefquin.engine.queryplan.executable.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import se.liu.ida.hefquin.base.utils.StatsImpl;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutableOperator;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

public abstract class ExecPlanTaskBase implements ExecPlanTask
{
	protected final ExecutionContext execCxt;

	protected final int preferredMinimumBlockSize;

	protected enum Status {
		/**
		 * The task has been created but its execution has not yet been started.
		 */
		WAITING_TO_BE_STARTED,

		/**
		 * The execution of the task is currently running.
		 */
		RUNNING,

		/**
		 * The execution of the task was completed successfully
		 * but its results have not yet been consumed completely.
		 */
		COMPLETED_NOT_CONSUMED,

		/**
		 * The execution of the task was completed successfully
		 * and its results have been consumed completely.
		 */
		COMPLETED_AND_CONSUMED,

		/**
		 * The execution of the task failed with an exception;
		 * hence, the execution is not running anymore.
		 */
		FAILED,

		/**
		 * The execution of the task was interrupted;
		 * hence, the execution is not running anymore.
		 */
		INTERRUPTED
	};

	// The different threads that call methods of this class must
	// be synchronized via the 'availableResultBlocks' object.

	// access to this object must be synchronized
	protected final Queue<IntermediateResultBlock> availableResultBlocks = new LinkedList<>();

	// access to this object must be synchronized
	private Status status = Status.WAITING_TO_BE_STARTED;

	private Exception causeOfFailure = null;


	protected ExecPlanTaskBase( final ExecutionContext execCxt, final int preferredMinimumBlockSize ) {
		assert execCxt != null;
		assert preferredMinimumBlockSize > 0;

		this.execCxt = execCxt;
		this.preferredMinimumBlockSize = preferredMinimumBlockSize;
	}

	protected abstract ExecutableOperator getExecOp();

	public List<Exception> getExceptionsCaughtDuringExecution() {
		return getExecOp().getExceptionsCaughtDuringExecution();
	}

	@Override
	public boolean isRunning() {
		synchronized (availableResultBlocks) {
			return (status == Status.RUNNING);
		}
	}

	@Override
	public boolean isCompleted() {
		synchronized (availableResultBlocks) {
			return (status == Status.COMPLETED_NOT_CONSUMED || status == Status.COMPLETED_AND_CONSUMED);
		}
	}

	@Override
	public boolean hasFailed() {
		synchronized (availableResultBlocks) {
			return (status == Status.FAILED);
		}
	}

	@Override
	public Exception getCauseOfFailure() {
		return causeOfFailure;
	}

	@Override
	public boolean hasNextIntermediateResultBlockAvailable() {
		synchronized (availableResultBlocks) {
			return ! availableResultBlocks.isEmpty();
		}
	}

	@Override
	public ExecPlanTaskStats getStats() {
		return new ExecPlanTaskStatsImpl( getExecOp() );
	}

	@Override
	public void resetStats() {
		getExecOp().resetStats();
	}

	protected Status getStatus() {
		return status;
	}

	protected void setStatus( final Status newStatus ) {
		synchronized (availableResultBlocks) {
			status = newStatus;
		}
	}

	protected void setCauseOfFailure( final Exception cause ) {
		causeOfFailure = cause;
	}


	protected class ExecPlanTaskStatsImpl extends StatsImpl implements ExecPlanTaskStats
	{
		protected static final String enOperatorStats  = "operatorStats";

		public ExecPlanTaskStatsImpl( final ExecutableOperator op ) {
			put( enOperatorStats, op.getStats() );
		}

	}

}
