package se.liu.ida.hefquin.queryplan.executable.impl;

public class SynchronizedIntermediateResultElementSink<ElmtType>
                   implements ClosableIntermediateResultElementSink<ElmtType>
{
	protected ElmtType currentElement = null;
	protected boolean closed = false;

	@Override
	synchronized public void send( final ElmtType element ) {
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

	synchronized public ElmtType getNextElement() {
		try {
			while (!closed && currentElement == null) {
				this.wait();
			}
		}
		catch ( final InterruptedException e ) {
			throw new RuntimeException("unexpected interruption of the receiving thread", e);
		}

		if ( currentElement != null ) {
			final ElmtType returnElement = currentElement;
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
