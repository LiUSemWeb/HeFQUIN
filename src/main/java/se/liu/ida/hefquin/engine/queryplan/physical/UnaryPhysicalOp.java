package se.liu.ida.hefquin.engine.queryplan.physical;

import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.UnaryExecutableOp;

public interface UnaryPhysicalOp extends PhysicalOperator
{
	@Override
	UnaryExecutableOp createExecOp( ExpectedVariables ... inputVars );
}
