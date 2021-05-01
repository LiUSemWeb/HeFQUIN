package se.liu.ida.hefquin.queryplan.physical;

import se.liu.ida.hefquin.queryplan.ExpectedVariables;
import se.liu.ida.hefquin.queryplan.PhysicalOperator;
import se.liu.ida.hefquin.queryplan.executable.impl.ops.UnaryExecutableOp;

public interface UnaryPhysicalOp extends PhysicalOperator
{
	@Override
	UnaryExecutableOp createExecOp( ExpectedVariables ... inputVars );
}
