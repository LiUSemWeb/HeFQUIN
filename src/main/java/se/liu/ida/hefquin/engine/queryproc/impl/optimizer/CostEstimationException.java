package se.liu.ida.hefquin.engine.queryproc.impl.optimizer;

import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.PhysicalQueryOptimizationException;

/**
 * An exception that occurred during the process
 * of estimating the cost of a given plan.
 */
public class CostEstimationException extends PhysicalQueryOptimizationException
{
	private static final long serialVersionUID = -4430461958184061161L;

	protected final PhysicalPlan plan;

	public CostEstimationException( final String message,
	                                final Throwable cause,
	                                final PhysicalPlan plan ) {
		super(message, cause);
		this.plan = plan;
	}

	public CostEstimationException( final String message,
	                                final PhysicalPlan plan ) {
		super(message);
		this.plan = plan;
	}

	public CostEstimationException( final Throwable cause,
	                                final PhysicalPlan plan ) {
		super(cause);
		this.plan = plan;
	}

	public CostEstimationException( final PhysicalPlan plan ) {
		super();
		this.plan = plan;
	}

	/**
	 * Returns the plan for which the cost estimation failed with this exception.
	 */
	public PhysicalPlan getPlan() {
		return plan;
	}

}
