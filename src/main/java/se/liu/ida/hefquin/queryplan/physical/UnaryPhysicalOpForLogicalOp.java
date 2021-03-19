package se.liu.ida.hefquin.queryplan.physical;

import se.liu.ida.hefquin.queryplan.logical.UnaryLogicalOp;

public interface UnaryPhysicalOpForLogicalOp extends PhysicalOperatorForLogicalOperator, UnaryPhysicalOp
{
	@Override
	UnaryLogicalOp getLogicalOperator();
}
