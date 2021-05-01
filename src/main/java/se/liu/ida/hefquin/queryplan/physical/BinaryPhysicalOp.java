package se.liu.ida.hefquin.queryplan.physical;

import se.liu.ida.hefquin.queryplan.ExpectedVariables;
import se.liu.ida.hefquin.queryplan.PhysicalOperator;
import se.liu.ida.hefquin.queryplan.executable.impl.ops.BinaryExecutableOp;

public interface BinaryPhysicalOp extends PhysicalOperator
{
	@Override
	BinaryExecutableOp createExecOp( ExpectedVariables ... inputVars );
}
