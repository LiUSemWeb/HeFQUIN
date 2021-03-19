package se.liu.ida.hefquin.queryplan.physical;

import se.liu.ida.hefquin.queryplan.logical.NullaryLogicalOp;

public interface NullaryPhysicalOpForLogicalOp extends PhysicalOperatorForLogicalOperator, NullaryPhysicalOp
{
	@Override
	NullaryLogicalOp getLogicalOperator();
}
