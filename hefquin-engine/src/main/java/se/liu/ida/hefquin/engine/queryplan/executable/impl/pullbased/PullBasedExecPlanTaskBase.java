package se.liu.ida.hefquin.engine.queryplan.executable.impl.pullbased;

import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecPlanTask;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecPlanTaskBase;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecPlanTaskInputException;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecPlanTaskInterruptionException;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

/**
 * Pull-based implementation of {@link ExecPlanTask}.
 * This implementation makes several assumptions:
 * 1) There is only one thread that consumes the output of this
 *    task (by calling {@link #getNextIntermediateResultBlock()}).
 * 2) If a task consumes the output of another task as input, then
 *    that other task must already be running before the consuming
 *    task is started.
 */
public abstract class PullBasedExecPlanTaskBase extends ExecPlanTaskBase
{
	protected PullBasedExecPlanTaskBase( final ExecutionContext execCxt, final int preferredMinimumBlockSize ) {
		super(execCxt, preferredMinimumBlockSize);
	}

	@Override
	public final void run() {
		synchronized (availableResultBlocks) {
			setStatus(Status.RUNNING);
			try {
				availableResultBlocks.wait();
			} catch ( final InterruptedException e ) {
				setStatus(Status.INTERRUPTED);
				availableResultBlocks.notifyAll();
				return;
			}
		}

		boolean completed = false;
		while ( ! completed ) {
			IntermediateResultBlock nextBlock;
			boolean opFailed     = false;
			boolean inputFailed  = false;
			boolean interrupted  = false;
			try {
				nextBlock = produceNextIntermediateResultBlock();
			}
			catch ( final ExecOpExecutionException e ) {
				nextBlock = null;
				opFailed = true;
				setCauseOfFailure(e);
			}
			catch ( final ExecPlanTaskInputException e ) {
				nextBlock = null;
				inputFailed = true;
				setCauseOfFailure(e);
			}
			catch ( final ExecPlanTaskInterruptionException  e ) {
				nextBlock = null;
				interrupted = true;
			}

			if ( nextBlock == null && ! opFailed && ! inputFailed && ! interrupted ) {
				inputFailed = true;
				setCauseOfFailure( new ExecPlanTaskInputException("The returned next block is null.") );
			}

			synchronized (availableResultBlocks) {
				if ( opFailed || inputFailed ) {
					setStatus(Status.FAILED);
					availableResultBlocks.notifyAll();
					return;
				}

				if ( interrupted ) {
					setStatus(Status.INTERRUPTED);
					availableResultBlocks.notifyAll();
					return;
				}

				availableResultBlocks.add(nextBlock);

				if ( nextBlock instanceof LastIntermediateResultBlock || nextBlock.size() < preferredMinimumBlockSize ) {
					setStatus(Status.COMPLETED_NOT_CONSUMED);
					completed = true;
				}

				availableResultBlocks.notify();
				try {
					availableResultBlocks.wait();
				} catch ( final InterruptedException e ) {
					setStatus(Status.INTERRUPTED);
					availableResultBlocks.notifyAll();
					return;
				}
			}
		}
	}

	/**
	 * Produces and returns a new {@link IntermediateResultBlock}. Unless this
	 * is the very last block that can be produced, the block must contain at
	 * least {@link #preferredMinimumBlockSize} solution mappings. To indicate that the
	 * returned block is the last one that can be produced the method may either
	 * return a block with fewer than {@link #preferredMinimumBlockSize} solution mappings
	 * or return a block of type {@link LastIntermediateResultBlock}.
	 */
	protected abstract IntermediateResultBlock produceNextIntermediateResultBlock()
			throws ExecOpExecutionException, ExecPlanTaskInputException, ExecPlanTaskInterruptionException;


	@Override
	public final IntermediateResultBlock getNextIntermediateResultBlock()
			throws ExecPlanTaskInterruptionException, ExecPlanTaskInputException {

		synchronized (availableResultBlocks) {

			if ( getStatus() == Status.COMPLETED_AND_CONSUMED ) {
				return null;
			}

			IntermediateResultBlock nextBlock = null;
			while ( nextBlock == null ) {

				if ( getStatus() == Status.WAITING_TO_BE_STARTED ) {
					throw new ExecPlanTaskInputException("Execution of this task has not started yet (operator: " + getExecOp().toString() + ").");
				}
				else if ( getStatus() == Status.FAILED ) {
					throw new ExecPlanTaskInputException("Execution of this task has failed with an exception (operator: " + getExecOp().toString() + ").", getCauseOfFailure() );
				}
				else if ( getStatus() == Status.INTERRUPTED ) {
					throw new ExecPlanTaskInputException("Execution of this task has been interrupted (operator: " + getExecOp().toString() + ").");
				}

				// try to get the next block
				nextBlock = availableResultBlocks.poll();

				if ( nextBlock == null ) {
					// if no next block was available, inform the producing
					// thread of this task that a new block is expected and
					// start waiting for the notification that a new block
					// as become available
					availableResultBlocks.notify();
					try {
						availableResultBlocks.wait();
					} catch ( final InterruptedException e ) {
						throw new ExecPlanTaskInterruptionException(e);
					}
				}
				else if ( availableResultBlocks.isEmpty() ) {
					// if there was a next block but it was the last block
					// currently available, check whether more blocks can
					// be expected to arrive
					if ( getStatus() == Status.COMPLETED_NOT_CONSUMED ) {
						// no more blocks to be expected; change status
						setStatus(Status.COMPLETED_AND_CONSUMED);
					}
				}
			}

			return nextBlock;
		}
	}

}
