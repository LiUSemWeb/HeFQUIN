package se.liu.ida.hefquin.engine.queryproc;

/**
 * An exception that occurred during query planning.
 */
public class QueryPlanningException extends QueryProcException
{
	private static final long serialVersionUID = 6534456106459594460L;

	public QueryPlanningException( final String message, final Throwable cause ) {
		super(message, cause);
	}

	public QueryPlanningException( final String message ) {
		super(message);
	}

	public QueryPlanningException( final Throwable cause ) {
		super(cause);
	}

	public QueryPlanningException() {
		super();
	}
}
