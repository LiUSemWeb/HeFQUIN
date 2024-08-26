package se.liu.ida.hefquin.engine.queryplan.physical;

import se.liu.ida.hefquin.engine.queryplan.logical.NaryLogicalOp;

/**
 * An interface for any type of {@link PhysicalOperator}
 * that directly implements a particular logical operator
 * that has an arbitrary arity.
 *
 * That logical operator can be accessed via the
 * {@link #getLogicalOperator()} function.
 */
public interface NaryPhysicalOpForLogicalOp extends PhysicalOperatorForLogicalOperator,
                                                    NaryPhysicalOp
{
	@Override
	NaryLogicalOp getLogicalOperator();
}
