package se.liu.ida.hefquin.engine.queryplan.physical;

import se.liu.ida.hefquin.engine.queryplan.logical.NaryLogicalOp;

public interface NaryPhysicalOpForLogicalOp extends PhysicalOperatorForLogicalOperator,
                                                    NaryPhysicalOp
{
	@Override
	NaryLogicalOp getLogicalOperator();
}
