package se.liu.ida.hefquin.queryplan.executable.impl.pullbased;

import java.util.NoSuchElementException;

import se.liu.ida.hefquin.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.queryplan.executable.impl.ops.NullaryExecutableOp;
import se.liu.ida.hefquin.queryproc.ExecutionContext;

public class ResultElementIterWithNullaryExecOp<OutElmtType> implements ResultElementIterator<OutElmtType>
{
	private final NullaryExecutableOp<OutElmtType> op;
	protected final ExecutionContext execCxt;

	protected final SynchronizedIntermediateResultElementSink sink;
	protected final OpRunnerThread opRunnerThread;

	protected boolean exhausted = false;
	protected OutElmtType nextElement = null;

	public ResultElementIterWithNullaryExecOp( final NullaryExecutableOp<OutElmtType> op,
	                                           final ExecutionContext execCxt )
	{
		assert op != null;
		assert execCxt != null;

		this.op = op;
		this.execCxt = execCxt;

		this.sink = new SynchronizedIntermediateResultElementSink();
		this.opRunnerThread = new OpRunnerThread();
	}

	public NullaryExecutableOp<OutElmtType> getOp() {
		return op;
	}

	public boolean hasNext() {
		if ( exhausted ) {
			return false;
		}

		if ( opRunnerThread.getState() == Thread.State.NEW ) {
			opRunnerThread.start();
		}

		nextElement = sink.getNextElement();
		if ( nextElement == null ) {
			exhausted = true;
		}

		return ! exhausted;
	}

	public OutElmtType next() {
		if ( ! hasNext() ) {
			throw new NoSuchElementException();
		}

		return nextElement;
	}


	protected class SynchronizedIntermediateResultElementSink implements IntermediateResultElementSink<OutElmtType>
	{
		protected OutElmtType currentElement = null;
		protected boolean lastElementReached = false;

		@Override
		synchronized public void send( final OutElmtType element ) {
			try {
				while (currentElement != null) {
					this.wait();
				}
			}
			catch ( final InterruptedException e ) {
				throw new RuntimeException("unexpected interruption of the sending thread", e);
			}

			currentElement = element;
			this.notifyAll();
		}

		synchronized public void lastElementReached() {
			lastElementReached = true;
			this.notifyAll();
		}

		synchronized public OutElmtType getNextElement() {
			try {
				while (!lastElementReached && currentElement == null) {
					this.wait();
				}
			}
			catch ( final InterruptedException e ) {
				throw new RuntimeException("unexpected interruption of the receiving thread", e);
			}

			if (lastElementReached) {
				return null;
			}
			else {
				final OutElmtType returnElement = currentElement;
				currentElement = null;
				this.notifyAll();
				return returnElement;
			}
		}

		synchronized public boolean hasLastElementReached() {
			return lastElementReached;
		}

	} // end of class SynchronizedIntermediateResultElementSink


	protected class OpRunnerThread extends Thread
	{
		public void run() {
			op.execute(sink, execCxt);
			sink.lastElementReached();
		}

	} // end of class OpRunner
	
}
