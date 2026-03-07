package se.liu.ida.hefquin.engine.queryplan.executable.impl.pushbased;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.utils.StatsImpl;
import se.liu.ida.hefquin.base.utils.StatsPrinter;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutableOperator;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

/**
 * Contains the core part of implementing {@link PushBasedPlanThread}.
 */
public abstract class PushBasedPlanThreadImplBase implements PushBasedPlanThread,
                                                         IntermediateResultElementSink
{
	// initialized via the constructor
	protected final ExecutionContext execCxt;

	// initialized if needed (i.e., if addConnectorForAdditionalConsumer is called)
	protected List<ConnectorForAdditionalConsumer> extraConnectors = null;

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

	// access to this list must be synchronized
	protected final List<SolutionMapping> availableOutput = new ArrayList<>();

	// access to this object must be synchronized
	private Status status = Status.WAITING_TO_BE_STARTED;

	private Exception causeOfFailure = null;


	protected PushBasedPlanThreadImplBase( final ExecutionContext execCxt ) {
		assert execCxt != null;
		this.execCxt = execCxt;
	}

	@Override
	public PushBasedPlanThread addConnectorForAdditionalConsumer() {
		if ( extraConnectors == null ) {
			extraConnectors = new ArrayList<>();
		}

		final ConnectorForAdditionalConsumer c = new ConnectorForAdditionalConsumer(execCxt);
		extraConnectors.add(c);
		return c;
	}


	///////////////////////////////////////////////////
	/// Methods that implement the core functionality of this producing thread
	///////////////////////////////////////////////////

	@Override
	public final void run() {
		try {
			setStatus(Status.RUNNING);

			if ( extraConnectors != null ) {
				for ( final ConnectorForAdditionalConsumer c : extraConnectors ) {
					c.setStatus(Status.RUNNING);
				}
			}

			boolean failed       = false;
			boolean interrupted  = false;
			try {
				produceOutput(this);
			}
			catch ( final InterruptedWaitingForPushBasedPlanThreadException e ) {
				setCauseOfFailure(e);
				interrupted = true;
			}
			catch ( final ConsumingPushBasedPlanThreadException | ExecOpExecutionException e ) {
				setCauseOfFailure(e);
				failed = true;
			}

			wrapUp(failed, interrupted);

			if ( extraConnectors != null ) {
				for ( final ConnectorForAdditionalConsumer c : extraConnectors ) {
					c.wrapUp(failed, interrupted);
				}
			}
		}
		catch ( final Throwable th ) {
			System.err.println("Unexpected exception in one of the execution plan threads.");
			System.err.println( "--> The class of the executable operator of this thread is: " + getExecOp().getClass().getName() );
			System.err.println( "--> The stack trace of the exception that was caught is:");
			th.printStackTrace( System.err );

			try {
				final StatsOfPushBasedPlanThread stats = getStats();
				System.err.println( "--> The current runtime statistics of this thread are:");
				StatsPrinter.print( stats, System.err, true ); // true=recursive
			}
			catch ( final Exception e ) {
				System.err.println();
				System.err.println( "--> Obtaining the current runtime statistics of this thread caused another exception:");
				e.printStackTrace( System.err );
			}

			System.err.println();

			synchronized (availableOutput) {
				setStatus(Status.FAILED);
				availableOutput.notifyAll();
			}
		}
	}

	protected void setStatus( final Status newStatus ) {
		synchronized (availableOutput) {
			status = newStatus;
		}
	}

	protected abstract void produceOutput( IntermediateResultElementSink sink )
			throws ExecOpExecutionException, ConsumingPushBasedPlanThreadException;

	protected void setCauseOfFailure( final Exception cause ) {
		causeOfFailure = cause;
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


	///////////////////////////////////////////////////
	/// IntermediateResultElementSink for the producing thread
	///////////////////////////////////////////////////

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


	///////////////////////////////////////////////////
	/// Methods to be called by the consuming thread
	///////////////////////////////////////////////////

	@Override
	public boolean hasMoreOutputAvailable() {
		synchronized (availableOutput) {
			return ! availableOutput.isEmpty();
		}
	}

	@Override
	public final void transferAvailableOutput( final List<SolutionMapping> transferBuffer )
			throws ConsumingPushBasedPlanThreadException
	{
		transferBuffer.clear();

		synchronized (availableOutput) {

			while ( transferBuffer.isEmpty() && getStatus() != Status.COMPLETED_AND_CONSUMED ) {
				// Before trying to transfer solution mappings, make sure that
				// we have not already reached some problematic status.
				final Status currentStatus = getStatus();
				if ( currentStatus == Status.FAILED ) {
					throw new ConsumingPushBasedPlanThreadException("Execution of this task has failed with an exception (operator: " + getExecOp().toString() + ").", getCauseOfFailure() );
				}
				else if ( currentStatus == Status.INTERRUPTED ) {
					throw new ConsumingPushBasedPlanThreadException("Execution of this task has been interrupted (operator: " + getExecOp().toString() + ").");
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
						throw new InterruptedWaitingForPushBasedPlanThreadException("Consuming thread was interrupted while waiting for the producing thread for operator: " + getExecOp().toString(), e);
					}
				}
			}
		}
	}

	protected Status getStatus() {
		return status;
	}

	@Override
	public boolean isRunning() {
		synchronized (availableOutput) {
			return (status == Status.RUNNING);
		}
	}

	@Override
	public boolean isCompleted() {
		synchronized (availableOutput) {
			return (status == Status.COMPLETED_NOT_CONSUMED || status == Status.COMPLETED_AND_CONSUMED);
		}
	}

	@Override
	public boolean hasFailed() {
		synchronized (availableOutput) {
			return (status == Status.FAILED);
		}
	}

	@Override
	public Exception getCauseOfFailure() {
		return causeOfFailure;
	}

	public List<Exception> getExceptionsCaughtDuringExecution() {
		return getExecOp().getExceptionsCaughtDuringExecution();
	}

	protected abstract ExecutableOperator getExecOp();


	///////////////////////////////////////////////////
	/// Stats-related code
	///////////////////////////////////////////////////

	@Override
	public StatsOfPushBasedPlanThread getStats() {
		return new MyStatsImpl( getExecOp() );
	}

	@Override
	public void resetStats() {
		getExecOp().resetStats();
	}


	protected class MyStatsImpl extends StatsImpl implements StatsOfPushBasedPlanThread
	{
		protected static final String enOperatorStats  = "operatorStats";

		public MyStatsImpl( final ExecutableOperator op ) {
			put( enOperatorStats, op.getStats() );
		}

	}

}
