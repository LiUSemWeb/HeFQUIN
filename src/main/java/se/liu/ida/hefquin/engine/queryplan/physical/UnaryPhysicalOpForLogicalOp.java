package se.liu.ida.hefquin.engine.queryplan.physical;

import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;

public interface UnaryPhysicalOpForLogicalOp extends PhysicalOperatorForLogicalOperator, UnaryPhysicalOp
{
	@Override
	UnaryLogicalOp getLogicalOperator();
}
