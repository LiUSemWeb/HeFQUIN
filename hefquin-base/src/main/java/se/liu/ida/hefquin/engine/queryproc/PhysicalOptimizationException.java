package se.liu.ida.hefquin.engine.queryproc;

/**
 * An exception that occurred during query optimization.
 */
public class PhysicalOptimizationException extends QueryPlanningException
{
	private static final long serialVersionUID = -2107515540797606242L;

	public PhysicalOptimizationException( final String message, final Throwable cause ) {
		super(message, cause);
	}

	public PhysicalOptimizationException( final String message ) {
		super(message);
	}

	public PhysicalOptimizationException( final Throwable cause ) {
		super(cause);
	}

	public PhysicalOptimizationException() {
		super();
	}

}
