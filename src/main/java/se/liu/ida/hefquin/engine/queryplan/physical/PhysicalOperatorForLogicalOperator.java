package se.liu.ida.hefquin.engine.queryplan.physical;

import se.liu.ida.hefquin.engine.queryplan.LogicalOperator;

/**
 * This interface is a base for such types of physical
 * operators that implement exactly one logical operator.
 */
public interface PhysicalOperatorForLogicalOperator extends PhysicalOperator
{
	/**
	 * Returns the logical operator implemented by this physical operator.
	 */
	LogicalOperator getLogicalOperator();
}
