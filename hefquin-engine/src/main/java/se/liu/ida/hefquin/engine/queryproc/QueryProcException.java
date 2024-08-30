package se.liu.ida.hefquin.engine.queryproc;

/**
 * An exception that may occur when processing a query.
 */
public class QueryProcException extends Exception
{
	private static final long serialVersionUID = 9193326477469724272L;

	public QueryProcException( final String message, final Throwable cause ) {
		super(message, cause);
	}

	public QueryProcException( final String message ) {
		super(message);
	}

	public QueryProcException( final Throwable cause ) {
		super(cause);
	}

	public QueryProcException() {
		super();
	}
}
