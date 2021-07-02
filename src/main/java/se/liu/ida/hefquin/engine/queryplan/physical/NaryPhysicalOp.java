package se.liu.ida.hefquin.engine.queryplan.physical;

import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.NaryExecutableOp;

public interface NaryPhysicalOp extends PhysicalOperator
{
	@Override
	NaryExecutableOp createExecOp( ExpectedVariables ... inputVars );
}
