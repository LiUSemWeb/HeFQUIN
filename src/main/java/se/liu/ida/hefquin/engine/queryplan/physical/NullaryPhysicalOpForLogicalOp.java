package se.liu.ida.hefquin.engine.queryplan.physical;

import se.liu.ida.hefquin.engine.queryplan.logical.NullaryLogicalOp;

public interface NullaryPhysicalOpForLogicalOp extends PhysicalOperatorForLogicalOperator, NullaryPhysicalOp
{
	@Override
	NullaryLogicalOp getLogicalOperator();
}
