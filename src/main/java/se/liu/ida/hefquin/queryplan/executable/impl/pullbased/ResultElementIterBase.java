package se.liu.ida.hefquin.queryplan.executable.impl.pullbased;

import java.util.NoSuchElementException;

import se.liu.ida.hefquin.queryplan.executable.impl.SynchronizedIntermediateResultElementSink;

public abstract class ResultElementIterBase<OutElmtType> implements ResultElementIterator<OutElmtType>
{
	protected final SynchronizedIntermediateResultElementSink<OutElmtType> sink;

	protected boolean exhausted = false;
	protected OutElmtType nextElement = null;

	protected ResultElementIterBase() {
		sink = new SynchronizedIntermediateResultElementSink<OutElmtType>();
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
	public OutElmtType next() {
		if ( ! hasNext() ) {
			throw new NoSuchElementException();
		}

		final OutElmtType returnElement = nextElement;
		nextElement = null;
		return returnElement;
	}

	protected abstract void ensureOpRunnerThreadIsStarted();

}
