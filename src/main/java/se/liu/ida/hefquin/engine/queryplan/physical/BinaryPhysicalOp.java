package se.liu.ida.hefquin.engine.queryplan.physical;

import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.BinaryExecutableOp;

public interface BinaryPhysicalOp extends PhysicalOperator
{
	@Override
	BinaryExecutableOp createExecOp( boolean collectExceptions, ExpectedVariables ... inputVars );
}
