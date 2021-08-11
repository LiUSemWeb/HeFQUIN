package se.liu.ida.hefquin.engine.queryproc.impl.optimizer;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizationException;

/**
 * An exception that occurred while processing
 * the estimated cost of a given plan.
 */
public class CostEstimateProcessingException extends QueryOptimizationException
{
	private static final long serialVersionUID = 7124187847216394941L;

	protected final double estimatedCost;
	protected final PhysicalPlan plan;

	public CostEstimateProcessingException( final String message,
	                                        final Throwable cause,
	                                        final double estimatedCost,
	                                        final PhysicalPlan plan ) {
		super(message, cause);

		this.estimatedCost = estimatedCost;
		this.plan = plan;
	}

	public CostEstimateProcessingException( final String message,
	                                        final double estimatedCost,
	                                        final PhysicalPlan plan ) {
		super(message);

		this.estimatedCost = estimatedCost;
		this.plan = plan;
	}

	public CostEstimateProcessingException( final Throwable cause,
	                                        final double estimatedCost,
	                                        final PhysicalPlan plan ) {
		super(cause);

		this.estimatedCost = estimatedCost;
		this.plan = plan;
	}

	public CostEstimateProcessingException( final double estimatedCost,
	                                        final PhysicalPlan plan ) {
		super();

		this.estimatedCost = estimatedCost;
		this.plan = plan;
	}

	/**
	 * Returns the estimated cost for which the processing failed with this exception.
	 */
	public double getEstimatedCost() {
		return estimatedCost;
	}

	/**
	 * Returns the plan for which processing the cost estimate failed with this exception.
	 */
	public PhysicalPlan getPlan() {
		return plan;
	}

}
