package se.liu.ida.hefquin.engine.queryplan.physical;

import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;

/**
 * An interface for any type of {@link PhysicalOperator}
 * that directly implements a particular logical operator
 * that has an arity of one.
 *
 * That logical operator can be accessed via the
 * {@link #getLogicalOperator()} function.
 */
public interface UnaryPhysicalOpForLogicalOp extends PhysicalOperatorForLogicalOperator, UnaryPhysicalOp
{
	@Override
	UnaryLogicalOp getLogicalOperator();
}
