package se.liu.ida.hefquin.engine.queryplan.physical;

import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.UnaryExecutableOp;

public interface UnaryPhysicalOp extends PhysicalOperator
{
	@Override
	UnaryExecutableOp createExecOp( boolean collectExceptions, ExpectedVariables ... inputVars );
}
