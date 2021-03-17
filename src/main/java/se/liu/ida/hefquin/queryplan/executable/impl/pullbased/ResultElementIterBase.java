package se.liu.ida.hefquin.queryplan.executable.impl.pullbased;

import java.util.NoSuchElementException;

import se.liu.ida.hefquin.query.SolutionMapping;
import se.liu.ida.hefquin.queryplan.executable.impl.SynchronizedIntermediateResultElementSink;

public abstract class ResultElementIterBase implements ResultElementIterator
{
	protected final SynchronizedIntermediateResultElementSink sink;

	protected boolean exhausted = false;
	protected SolutionMapping nextElement = null;

	protected ResultElementIterBase() {
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

	protected abstract void ensureOpRunnerThreadIsStarted();

}
