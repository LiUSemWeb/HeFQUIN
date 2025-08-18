package se.liu.ida.hefquin.engine.queryproc.impl.cardinality;

import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;

/**
 * Implementations of this class complete the cardinality estimation process
 * for a given query plan (or for multiple such plans) in which every nullary
 * subplan is already annotated with its cardinality estimate.
 */
public interface CardinalityEstimationWorker
{
	/**
	 * Determine a cardinality estimate for each of the given plans
	 * recursively, assuming that every nullary subplan within each
	 * of the given plans is already annotated with a cardinality
	 * estimate.
	 */
	void addCardinalities( LogicalPlan ... plans );

	/**
	 * Determine a cardinality estimate for each of the given plans
	 * recursively, assuming that every nullary subplan within each
	 * of the given plans is already annotated with a cardinality
	 * estimate.
	 */
	void addCardinalities( PhysicalPlan ... plans );
}
