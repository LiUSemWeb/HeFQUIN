package se.liu.ida.hefquin.queryplan.executable.impl;

import se.liu.ida.hefquin.queryplan.executable.IntermediateResultElementSink;

public interface ClosableIntermediateResultElementSink
                       extends IntermediateResultElementSink
{
	/**
	 * Closes this sink. Further calls of {@link #send(Object)} will be ignored.
	 * 
	 * If this sink has been closed before, calling this method again has no effect.
	 */
	void close();

	/**
	 * Returns true if this sink has been closed.
	 */
	boolean isClosed();
}
