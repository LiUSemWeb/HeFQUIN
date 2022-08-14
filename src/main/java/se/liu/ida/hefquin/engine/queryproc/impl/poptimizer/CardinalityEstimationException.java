package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer;

import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.PhysicalOptimizationException;

/**
 * An exception that occurred during the process of estimating the
 * cardinality of the result that will be produced by a given plan.
 */
public class CardinalityEstimationException extends PhysicalOptimizationException
{
	private static final long serialVersionUID = 4926298334556900658L;

	protected final PhysicalPlan plan;

	public CardinalityEstimationException( final String message,
	                                       final Throwable cause,
	                                       final PhysicalPlan plan ) {
		super(message, cause);
		this.plan = plan;
	}

	public CardinalityEstimationException( final String message,
	                                       final PhysicalPlan plan ) {
		super(message);
		this.plan = plan;
	}

	public CardinalityEstimationException( final Throwable cause,
	                                       final PhysicalPlan plan ) {
		super(cause);
		this.plan = plan;
	}

	public CardinalityEstimationException( final PhysicalPlan plan ) {
		super();
		this.plan = plan;
	}

	/**
	 * Returns the plan for which estimating the result
	 * cardinality failed with this exception.
	 */
	public PhysicalPlan getPlan() {
		return plan;
	}

}
