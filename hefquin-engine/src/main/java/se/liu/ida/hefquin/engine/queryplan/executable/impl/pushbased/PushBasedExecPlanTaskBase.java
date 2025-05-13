package se.liu.ida.hefquin.engine.queryplan.executable.impl.pushbased;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.utils.StatsPrinter;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

/**
 * Push-based implementation of {@link ExecPlanTask}.
 * This implementation makes the following assumption:
 * - There is only one thread that consumes the output of this
 *   task (by calling {@link #transferAvailableOutput()}).
 */
public abstract class PushBasedExecPlanTaskBase extends ExecPlanTaskBase
                                                implements PushBasedExecPlanTask, IntermediateResultElementSink
{
	protected static final int DEFAULT_OUTPUT_BLOCK_SIZE = 50;

	protected final int outputBlockSize;

	protected List<ConnectorForAdditionalConsumer> extraConnectors = null;

	protected PushBasedExecPlanTaskBase( final ExecutionContext execCxt ) {
		super(execCxt);

		outputBlockSize = DEFAULT_OUTPUT_BLOCK_SIZE;
	}

	@Override
	public ExecPlanTask addConnectorForAdditionalConsumer() {
		if ( extraConnectors == null ) {
			extraConnectors = new ArrayList<>();
		}

		final ConnectorForAdditionalConsumer c = new ConnectorForAdditionalConsumer(execCxt);
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
		synchronized (availableOutput) {
			// Make the given solution mapping available for consumption and
			// inform the consuming thread in case it is already waiting for
			// more solution mappings.
			availableOutput.add(element);
			availableOutput.notify();
		}

		// Forward the given solution mapping to the extra connectors as
		// well (if there are any).
		if ( extraConnectors != null ) {
			for ( final ConnectorForAdditionalConsumer c : extraConnectors ) {
				c.send(element);
			}
		}
	}

	@Override
	public int send( final Iterable<SolutionMapping> it ) {
		if ( ! (it instanceof Collection<?>) ) {
			return send( it.iterator() );
		}

		final Collection<SolutionMapping> coll = (Collection<SolutionMapping>) it;

		if ( coll.isEmpty() ) {
			return 0;
		}

		synchronized (availableOutput) {
			// Make the given solution mappings available for consumption and
			// inform the consuming thread in case it is already waiting for
			// more solution mappings.
			availableOutput.addAll(coll);
			availableOutput.notify();
		}

		// Forward the given solution mapping to the extra connectors as
		// well (if there are any).
		if ( extraConnectors != null ) {
			for ( final ConnectorForAdditionalConsumer c : extraConnectors ) {
				c.send(coll);
			}
		}

		return coll.size();
	}

	@Override
	public int send( final Iterator<SolutionMapping> it ) {
		if ( ! it.hasNext() ) {
			return 0;
		}

		if ( extraConnectors != null ) {
			final List<SolutionMapping> list = new ArrayList<>();
			while ( it.hasNext() ) {
				list.add( it.next() );
			}
			return send(list);
		}

		int cnt = 0;

		synchronized (availableOutput) {
			// Make the given solution mappings available for consumption and
			// inform the consuming thread in case it is already waiting for
			// more solution mappings.
			while ( it.hasNext() ) {
				cnt++;
				availableOutput.add( it.next() );
			}
			availableOutput.notify();
		}

		return cnt;
	}

	protected void wrapUp( final boolean failed, final boolean interrupted )
	{
		synchronized (availableOutput) {
			if ( failed ) {
				setStatus(Status.FAILED);
				availableOutput.notifyAll();
			}
			else if ( interrupted ) {
				setStatus(Status.INTERRUPTED);
				availableOutput.notifyAll();
			}
			else {
				// Everything went well. Set the completion status
				// depending on whether there still are output solution
				// mappings available to be consumed, and ...
				if ( availableOutput.isEmpty() )
					setStatus(Status.COMPLETED_AND_CONSUMED);
				else
					setStatus(Status.COMPLETED_NOT_CONSUMED);

				// ... inform the consuming thread in case it is
				// currently waiting for more solution mappings.
				availableOutput.notify();
			}
		}
	}


	@Override
	public final void transferAvailableOutput( final List<SolutionMapping> transferBuffer )
			throws ExecPlanTaskInterruptionException, ExecPlanTaskInputException
	{
		transferBuffer.clear();

		synchronized (availableOutput) {

			while ( transferBuffer.isEmpty() && getStatus() != Status.COMPLETED_AND_CONSUMED ) {
				// Before trying to transfer solution mappings, make sure that
				// we have not already reached some problematic status.
				final Status currentStatus = getStatus();
				if ( currentStatus == Status.FAILED ) {
					throw new ExecPlanTaskInputException("Execution of this task has failed with an exception (operator: " + getExecOp().toString() + ").", getCauseOfFailure() );
				}
				else if ( currentStatus == Status.INTERRUPTED ) {
					throw new ExecPlanTaskInputException("Execution of this task has been interrupted (operator: " + getExecOp().toString() + ").");
				}
				else if ( currentStatus == Status.WAITING_TO_BE_STARTED ) {
					// This status is not actually a problem. The code in the
					// remainder of this while loop will simply start waiting
					// for the first solution mappings to become available
					// (which should happen after the execution of this task
					// got started eventually).
					//System.err.println("Execution of this task has not started yet (operator: " + getExecOp().toString() + ").");
				}

				// Try to transfer solution mappings from the
				// availableOutput buffer to the given list.
				transferBuffer.addAll(availableOutput);
				availableOutput.clear();

				// Did we reach the end of all output solution mappings that
				// can be expected?
				if ( currentStatus == Status.COMPLETED_NOT_CONSUMED ) {
					// If yes, set the status to indicate that all solution
					// mappings have now been transferred to the consuming
					// thread, which happens precisely with the particular
					// execution of this function that ends up in this if
					// block (remember that this function is executed by
					// the consuming thread).
					setStatus(Status.COMPLETED_AND_CONSUMED);
				}

				if ( transferBuffer.isEmpty() && getStatus() != Status.COMPLETED_AND_CONSUMED ) {
					// If no output solution mappings have been available and
					// the producing thread is still active, wait for the
					// notification that a new block has become available.
					// After being notified (i.e., when the wait finishes),
					// the transfer buffer is still empty and, thus, the
					// execution continues at the beginning of the while
					// loop again.
					try {
						availableOutput.wait();
					} catch ( final InterruptedException e ) {
						throw new ExecPlanTaskInterruptionException(e);
					}
				}
			}
		}
	}

}
