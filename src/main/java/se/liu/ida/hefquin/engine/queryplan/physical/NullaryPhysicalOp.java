package se.liu.ida.hefquin.engine.queryplan.physical;

import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.NullaryExecutableOp;

public interface NullaryPhysicalOp extends PhysicalOperator
{
	@Override
	NullaryExecutableOp createExecOp( ExpectedVariables ... inputVars );
}
