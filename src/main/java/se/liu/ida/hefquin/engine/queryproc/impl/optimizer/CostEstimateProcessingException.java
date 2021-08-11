package se.liu.ida.hefquin.engine.queryproc.impl.optimizer;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizationException;

public class CostEstimateProcessingException extends QueryOptimizationException
{
	private static final long serialVersionUID = 7124187847216394941L;

	protected final PhysicalPlan plan;

	public CostEstimateProcessingException( final PhysicalPlan plan,
	                                        final String message,
	                                        final Throwable cause ) {
		super(message, cause);

		this.plan = plan;
	}

	public CostEstimateProcessingException( final PhysicalPlan plan,
	                                        final String message ) {
		super(message);

		this.plan = plan;
	}

	public CostEstimateProcessingException( final PhysicalPlan plan,
	                                        final Throwable cause ) {
		super(cause);

		this.plan = plan;
	}

	/**
	 * Returns the plan for which the cost estimation failed with this exception.
	 */
	public PhysicalPlan getPlan() {
		return plan;
	}

}
