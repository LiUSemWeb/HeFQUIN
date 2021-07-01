package se.liu.ida.hefquin.engine.queryproc;

public class SourcePlanningException extends QueryPlanningException
{
	private static final long serialVersionUID = 6907355125900557975L;

	public SourcePlanningException( final String message, final Throwable cause ) {
		super(message, cause);
	}

	public SourcePlanningException( final String message ) {
		super(message);
	}

	public SourcePlanningException( final Throwable cause ) {
		super(cause);
	}

	public SourcePlanningException() {
		super();
	}

}
