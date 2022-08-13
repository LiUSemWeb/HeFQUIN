package se.liu.ida.hefquin.engine.queryproc;

/**
 * An exception that occurred during query optimization.
 */
public class PhysicalQueryOptimizationException extends QueryPlanningException
{
	private static final long serialVersionUID = -2107515540797606242L;

	public PhysicalQueryOptimizationException( final String message, final Throwable cause ) {
		super(message, cause);
	}

	public PhysicalQueryOptimizationException( final String message ) {
		super(message);
	}

	public PhysicalQueryOptimizationException( final Throwable cause ) {
		super(cause);
	}

	public PhysicalQueryOptimizationException() {
		super();
	}

}
