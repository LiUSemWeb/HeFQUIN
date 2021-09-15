package se.liu.ida.hefquin.engine.queryproc;

import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.CostEstimationException;

/**
 * An exception that occurred during query optimization.
 */
public class QueryOptimizationException extends QueryPlanningException
{
	private static final long serialVersionUID = -2107515540797606242L;

	public QueryOptimizationException( final String message, final Throwable cause ) {
		super(message, cause);
	}

	public QueryOptimizationException( final String message ) {
		super(message);
	}

	public QueryOptimizationException( final Throwable cause ) {
		super(cause);
	}

	public QueryOptimizationException() {
		super();
	}

	public QueryOptimizationException( final String message, final CostEstimationException cause  ) {
		super(message, cause);
	}

}
