package se.liu.ida.hefquin.engine.queryplan.executable.impl.pullbased;

import java.util.NoSuchElementException;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutableOperator;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.SynchronizedIntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;
import se.liu.ida.hefquin.engine.queryproc.ExecutionException;

public abstract class ResultElementIterBase implements ResultElementIterator
{
	protected final ExecutionContext execCxt;
	protected final SynchronizedIntermediateResultElementSink sink;

	protected boolean exhausted = false;
	protected SolutionMapping nextElement = null;

	protected ResultElementIterBase( final ExecutionContext execCxt ) {
		assert execCxt != null;
		this.execCxt = execCxt;

		sink = new SynchronizedIntermediateResultElementSink();
	}

	@Override
	public boolean hasNext() throws ResultElementIterException {
		if ( exhausted ) {
			return false;
		}

		if ( nextElement != null ) {
			return true;
		}

		ensureOpRunnerThreadIsStarted();
		ensureOpRunnerThreadHasNoException();

		nextElement = sink.getNextElement();
		if ( nextElement == null ) {
			exhausted = true;
		}

		ensureOpRunnerThreadHasNoException();

		return ! exhausted;
	}

	@Override
	public SolutionMapping next() throws ResultElementIterException {
		if ( ! hasNext() ) {
			throw new NoSuchElementException();
		}

		final SolutionMapping returnElement = nextElement;
		nextElement = null;
		return returnElement;
	}

	protected void ensureOpRunnerThreadIsStarted() {
		final OpRunnerThread opRunnerThread = getOpRunnerThread();
		if ( opRunnerThread.getState() == Thread.State.NEW ) {
			opRunnerThread.start();
		}
	}

	protected void ensureOpRunnerThreadHasNoException() throws ResultElementIterException {
		final ExecutionException possibleExptn = getOpRunnerThread().getExceptionIfAny();
		if ( possibleExptn != null ) {
			throw new ResultElementIterException(possibleExptn);
		}
	}

	public ExecutableOperator getOp() {
		return getOpRunnerThread().getOp();
	}

	protected abstract OpRunnerThread getOpRunnerThread();

	protected abstract class OpRunnerThread extends Thread
	{
		private ExecutionException ex = null;

		public ExecutionException getExceptionIfAny() { return ex; }

		@Override
		public final void run() {
			try {
				_run();
			}
			catch ( final ExecutionException ex ) {
				this.ex = ex;
			}

			sink.close();
		}

		protected abstract void _run() throws ExecutionException;

		public abstract ExecutableOperator getOp();
	}

}
