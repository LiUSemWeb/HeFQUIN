package se.liu.ida.hefquin.engine.queryplan.physical;

import se.liu.ida.hefquin.engine.queryplan.logical.NullaryLogicalOp;

/**
 * An interface for any type of {@link PhysicalOperator}
 * that directly implements a particular logical operator
 * that has an arity of zero.
 *
 * That logical operator can be accessed via the
 * {@link #getLogicalOperator()} function.
 */
public interface NullaryPhysicalOpForLogicalOp extends PhysicalOperatorForLogicalOperator, NullaryPhysicalOp
{
	@Override
	NullaryLogicalOp getLogicalOperator();
}
