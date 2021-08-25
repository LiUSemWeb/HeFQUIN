package se.liu.ida.hefquin.engine.queryproc;

/**
 * An exception that occurred during query compilation.
 */
public class QueryCompilationException extends QueryProcException
{
	private static final long serialVersionUID = -6759273639867048997L;

	public QueryCompilationException( final String message, final Throwable cause ) {
		super(message, cause);
	}

	public QueryCompilationException( final String message ) {
		super(message);
	}

	public QueryCompilationException( final Throwable cause ) {
		super(cause);
	}

	public QueryCompilationException() {
		super();
	}
}
