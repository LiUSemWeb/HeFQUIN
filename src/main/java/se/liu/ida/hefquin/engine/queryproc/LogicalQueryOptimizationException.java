package se.liu.ida.hefquin.engine.queryproc;

public class LogicalQueryOptimizationException extends QueryPlanningException
{
	private static final long serialVersionUID = -2523385212776720835L;

	public LogicalQueryOptimizationException( final String message, final Throwable cause ) {
		super(message, cause);
	}

	public LogicalQueryOptimizationException( final String message ) {
		super(message);
	}

	public LogicalQueryOptimizationException( final Throwable cause ) {
		super(cause);
	}

	public LogicalQueryOptimizationException() {
		super();
	}

}
