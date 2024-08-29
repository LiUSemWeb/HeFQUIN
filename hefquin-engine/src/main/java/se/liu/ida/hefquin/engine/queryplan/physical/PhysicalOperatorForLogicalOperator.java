package se.liu.ida.hefquin.engine.queryplan.physical;

import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;

/**
 * An interface for any type of {@link PhysicalOperator}
 * that directly implements a particular logical operator.
 *
 * That logical operator can be accessed via the
 * {@link #getLogicalOperator()} function.
 */
public interface PhysicalOperatorForLogicalOperator extends PhysicalOperator
{
	/**
	 * Returns the logical operator implemented by this physical operator.
	 */
	LogicalOperator getLogicalOperator();
}
