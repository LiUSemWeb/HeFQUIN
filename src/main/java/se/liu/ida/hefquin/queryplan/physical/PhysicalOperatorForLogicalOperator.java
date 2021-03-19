package se.liu.ida.hefquin.queryplan.physical;

import se.liu.ida.hefquin.queryplan.LogicalOperator;
import se.liu.ida.hefquin.queryplan.PhysicalOperator;

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
