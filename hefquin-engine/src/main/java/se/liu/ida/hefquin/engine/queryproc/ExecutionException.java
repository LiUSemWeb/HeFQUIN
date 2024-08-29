package se.liu.ida.hefquin.engine.queryproc;

/**
 * An exception that may occur during the query execution process.
 */
public class ExecutionException extends QueryProcException
{
	private static final long serialVersionUID = 5701161003870070130L;

	public ExecutionException( final String message, final Throwable cause ) {
		super(message, cause);
	}

	public ExecutionException( final String message ) {
		super(message);
	}

	public ExecutionException( final Throwable cause ) {
		super(cause);
	}

	public ExecutionException() {
		super();
	}
}
