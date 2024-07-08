package se.liu.ida.hefquin.engine.queryplan.physical;

import se.liu.ida.hefquin.engine.queryplan.logical.BinaryLogicalOp;

/**
 * An interface for any type of {@link PhysicalOperator}
 * that directly implements a particular logical operator
 * that has an arity of two.
 *
 * That logical operator can be accessed via the
 * {@link #getLogicalOperator()} function.
 */
public interface BinaryPhysicalOpForLogicalOp extends PhysicalOperatorForLogicalOperator, BinaryPhysicalOp
{
	@Override
	BinaryLogicalOp getLogicalOperator();
}
