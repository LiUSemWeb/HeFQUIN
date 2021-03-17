package se.liu.ida.hefquin.queryplan.executable.impl;

import se.liu.ida.hefquin.query.SolutionMapping;

public class SynchronizedIntermediateResultElementSink
                   implements ClosableIntermediateResultElementSink
{
	protected SolutionMapping currentElement = null;
	protected boolean closed = false;

	@Override
	synchronized public void send( final SolutionMapping element ) {
		if ( closed )
			return;

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

	@Override
	synchronized public void close() {
		closed = true;
		this.notifyAll();
	}

	@Override
	synchronized public boolean isClosed() {
		return closed;
	}

	synchronized public SolutionMapping getNextElement() {
		try {
			while (!closed && currentElement == null) {
				this.wait();
			}
		}
		catch ( final InterruptedException e ) {
			throw new RuntimeException("unexpected interruption of the receiving thread", e);
		}

		if ( currentElement != null ) {
			final SolutionMapping returnElement = currentElement;
			currentElement = null;
			this.notifyAll();
			return returnElement;
		}
		else if (closed) {
			return null;
		}
		else {
			throw new IllegalStateException();
		}
	}

}
