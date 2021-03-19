package se.liu.ida.hefquin.queryplan.physical;

import se.liu.ida.hefquin.queryplan.logical.BinaryLogicalOp;

public interface BinaryPhysicalOpForLogicalOp extends PhysicalOperatorForLogicalOperator, BinaryPhysicalOp
{
	@Override
	BinaryLogicalOp getLogicalOperator();
}
