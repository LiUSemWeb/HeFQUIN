package se.liu.ida.hefquin.engine.queryplan.executable.impl.pushbased;

import java.util.ArrayList;
import java.util.List;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.utils.StatsPrinter;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultBlockBuilder;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecPlanTask;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecPlanTaskBase;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecPlanTaskInputException;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecPlanTaskInterruptionException;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecPlanTaskStats;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.GenericIntermediateResultBlockBuilderImpl;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

/**
 * Push-based implementation of {@link ExecPlanTask}.
 * This implementation makes the following assumption:
 * - There is only one thread that consumes the output of this
 *   task (by calling {@link #getNextIntermediateResultBlock()}).
 */
public abstract class PushBasedExecPlanTaskBase extends ExecPlanTaskBase
                                                implements PushBasedExecPlanTask, IntermediateResultElementSink
{
	protected static final int DEFAULT_OUTPUT_BLOCK_SIZE = 50;

	protected final int outputBlockSize;

	private final IntermediateResultBlockBuilder blockBuilder = new GenericIntermediateResultBlockBuilderImpl();

	protected List<ConnectorForAdditionalConsumer> extraConnectors = null;


	protected PushBasedExecPlanTaskBase( final ExecutionContext execCxt, final int preferredMinimumBlockSize ) {
		super(execCxt, preferredMinimumBlockSize);

		if ( preferredMinimumBlockSize == 1 )
			outputBlockSize = DEFAULT_OUTPUT_BLOCK_SIZE;
		else
			outputBlockSize = preferredMinimumBlockSize;
	}

	@Override
	public ExecPlanTask addConnectorForAdditionalConsumer( final int preferredMinimumBlockSize ) {
		if ( extraConnectors == null ) {
			extraConnectors = new ArrayList<>();
		}

		final ConnectorForAdditionalConsumer c = new ConnectorForAdditionalConsumer(execCxt, preferredMinimumBlockSize);
		extraConnectors.add(c);
		return c;
	}

	@Override
	public final void run() {
		try {
			setStatus(Status.RUNNING);

			if ( extraConnectors != null ) {
				for ( final ConnectorForAdditionalConsumer c : extraConnectors ) {
					c.setStatus(Status.RUNNING);
				}
			}

			final IntermediateResultElementSink sink = this;

			boolean failed       = false;
			boolean interrupted  = false;
			try {
				produceOutput(sink);
			}
			catch ( final ExecOpExecutionException | ExecPlanTaskInputException e ) {
				setCauseOfFailure(e);
				failed = true;
			}
			catch ( final ExecPlanTaskInterruptionException  e ) {
				setCauseOfFailure(e);
				interrupted = true;
			}

			wrapUp(failed, interrupted);

			if ( extraConnectors != null ) {
				for ( final ConnectorForAdditionalConsumer c : extraConnectors ) {
					c.wrapUp(failed, interrupted);
				}
			}
		}
		catch ( final Throwable th ) {
			System.err.println("Unexpected exception in one of the ExecPlanTasks.");
			System.err.println( "--> The class of the executable operator of this ExecPlanTask is:" + getExecOp().getClass().getName() );
			System.err.println( "--> The stack trace of the exception that was caught is:");
			th.printStackTrace( System.err );

			try {
				final ExecPlanTaskStats stats = getStats();
				System.err.println( "--> The current runtime statistics of this ExecPlanTask are:");
				StatsPrinter.print( stats, System.err, true ); // true=recursive
			}
			catch ( final Exception e ) {
				System.err.println();
				System.err.println( "--> Obtaining the current runtime statistics of this ExecPlanTask caused another exception:");
				e.printStackTrace( System.err );
			}

			System.err.println();
		}
	}

	protected abstract void produceOutput( final IntermediateResultElementSink sink )
			throws ExecOpExecutionException, ExecPlanTaskInputException, ExecPlanTaskInterruptionException;


	@Override
	public void send( final SolutionMapping element ) {
		synchronized (availableResultBlocks) {
			blockBuilder.add(element);

			// If we have collected enough solution mappings, produce the next
			// output result block with these solution mappings and inform the
			// consuming thread in case it is already waiting for the next block
			if ( blockBuilder.sizeOfCurrentBlock() >= outputBlockSize ) {
				final IntermediateResultBlock nextBlock = blockBuilder.finishCurrentBlock();
				availableResultBlocks.add(nextBlock);
				availableResultBlocks.notify();
			}
		}

		if ( extraConnectors != null ) {
			for ( final ConnectorForAdditionalConsumer c : extraConnectors ) {
				c.send(element);
			}
		}
	}

	protected void wrapUp( final boolean failed, final boolean interrupted )
	{
		synchronized (availableResultBlocks) {
			if ( failed ) {
				setStatus(Status.FAILED);
				availableResultBlocks.notifyAll();
			}
			else if ( interrupted ) {
				setStatus(Status.INTERRUPTED);
				availableResultBlocks.notifyAll();
			}
			else {
				// everything went well; let's see whether we still have some
				// output solution mappings for a final output result block
				if ( blockBuilder.sizeOfCurrentBlock() > 0 ) {
					// yes we have; let's create the output result block and
					// notify the potentially waiting consuming thread of it
					availableResultBlocks.add( blockBuilder.finishCurrentBlock() );
					setStatus(Status.COMPLETED_NOT_CONSUMED);
				}
				else {
					// no more output solution mappings; set the completion
					// status depending on whether there still are output
					// result blocks available to be consumed
					if ( availableResultBlocks.isEmpty() )
						setStatus(Status.COMPLETED_AND_CONSUMED);
					else
						setStatus(Status.COMPLETED_NOT_CONSUMED);
				}
				availableResultBlocks.notify();
			}
		}
	}


	@Override
	public final IntermediateResultBlock getNextIntermediateResultBlock()
			throws ExecPlanTaskInterruptionException, ExecPlanTaskInputException {

		synchronized (availableResultBlocks) {

			IntermediateResultBlock nextBlock = null;
			while ( nextBlock == null && getStatus() != Status.COMPLETED_AND_CONSUMED ) {
				// before trying to get the next block, make sure that
				// we have not already reached some problematic status
				final Status currentStatus = getStatus();
				if ( currentStatus == Status.FAILED ) {
					throw new ExecPlanTaskInputException("Execution of this task has failed with an exception (operator: " + getExecOp().toString() + ").", getCauseOfFailure() );
				}
				else if ( currentStatus == Status.INTERRUPTED ) {
					throw new ExecPlanTaskInputException("Execution of this task has been interrupted (operator: " + getExecOp().toString() + ").");
				}
				else if ( currentStatus == Status.WAITING_TO_BE_STARTED ) {
					// this status is not actually a problem; the code in the
					// remainder of this while loop will simply start waiting
					// for the first result block to become available (which
					// should happen after the execution of this task will
					// get started eventually)
					//System.err.println("Execution of this task has not started yet (operator: " + getExecOp().toString() + ").");
				}

				// try to get the next block
				nextBlock = availableResultBlocks.poll();

				// Did we reach the end of all result blocks to be expected?
				if ( currentStatus == Status.COMPLETED_NOT_CONSUMED && availableResultBlocks.isEmpty() ) {
					setStatus(Status.COMPLETED_AND_CONSUMED); // yes, we did
				}

				if ( nextBlock == null && getStatus() != Status.COMPLETED_AND_CONSUMED ) {
					// if no next block was available and the producing
					// thread is still active, wait for the notification
					// that a new block has become available
					try {
						availableResultBlocks.wait();
					} catch ( final InterruptedException e ) {
						throw new ExecPlanTaskInterruptionException(e);
					}
				}
			}

			return nextBlock;
		}
	}

}
