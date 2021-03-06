package se.liu.ida.hefquin.engine.queryplan.physical;

import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.NullaryExecutableOp;

public interface NullaryPhysicalOp extends PhysicalOperator
{
	@Override
	NullaryExecutableOp createExecOp( ExpectedVariables ... inputVars );
}
