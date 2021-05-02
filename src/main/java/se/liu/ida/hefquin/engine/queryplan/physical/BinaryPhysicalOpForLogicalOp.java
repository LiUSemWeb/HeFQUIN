package se.liu.ida.hefquin.engine.queryplan.physical;

import se.liu.ida.hefquin.engine.queryplan.logical.BinaryLogicalOp;

public interface BinaryPhysicalOpForLogicalOp extends PhysicalOperatorForLogicalOperator, BinaryPhysicalOp
{
	@Override
	BinaryLogicalOp getLogicalOperator();
}
