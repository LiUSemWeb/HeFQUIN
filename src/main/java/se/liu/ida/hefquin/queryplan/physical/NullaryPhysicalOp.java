package se.liu.ida.hefquin.queryplan.physical;

import se.liu.ida.hefquin.queryplan.PhysicalOperator;
import se.liu.ida.hefquin.queryplan.executable.impl.ops.NullaryExecutableOp;

public interface NullaryPhysicalOp extends PhysicalOperator
{
	@Override
	NullaryExecutableOp createExecOp();
}
