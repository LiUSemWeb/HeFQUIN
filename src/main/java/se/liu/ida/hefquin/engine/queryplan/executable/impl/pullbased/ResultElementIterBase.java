package se.liu.ida.hefquin.engine.queryplan.executable.impl.pullbased;

import java.util.NoSuchElementException;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.queryplan.ExecutableOperator;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.SynchronizedIntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

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
	public boolean hasNext() {
		if ( exhausted ) {
			return false;
		}

		if ( nextElement != null ) {
			return true;
		}

		ensureOpRunnerThreadIsStarted();

		nextElement = sink.getNextElement();
		if ( nextElement == null ) {
			exhausted = true;
		}

		return ! exhausted;
	}

	@Override
	public SolutionMapping next() {
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

	public ExecutableOperator getOp() {
		return getOpRunnerThread().getOp();
	}

	protected abstract OpRunnerThread getOpRunnerThread();

	protected abstract class OpRunnerThread extends Thread
	{
		public abstract ExecutableOperator getOp();
	}

}
