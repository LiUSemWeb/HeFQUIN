package se.liu.ida.hefquin.engine.queryproc;

public class LogicalOptimizationException extends QueryPlanningException
{
	private static final long serialVersionUID = -2523385212776720835L;

	public LogicalOptimizationException( final String message, final Throwable cause ) {
		super(message, cause);
	}

	public LogicalOptimizationException( final String message ) {
		super(message);
	}

	public LogicalOptimizationException( final Throwable cause ) {
		super(cause);
	}

	public LogicalOptimizationException() {
		super();
	}

}
