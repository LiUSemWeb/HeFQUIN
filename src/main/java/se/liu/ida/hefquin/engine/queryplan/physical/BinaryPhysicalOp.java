package se.liu.ida.hefquin.engine.queryplan.physical;

import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.BinaryExecutableOp;

public interface BinaryPhysicalOp extends PhysicalOperator
{
	@Override
	BinaryExecutableOp createExecOp( ExpectedVariables ... inputVars );
}
