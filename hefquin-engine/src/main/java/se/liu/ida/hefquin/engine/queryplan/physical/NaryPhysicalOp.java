package se.liu.ida.hefquin.engine.queryplan.physical;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.NaryExecutableOp;

/**
 * An interface for any type of {@link PhysicalOperator} whose algorithm
 * consumes an arbitrary number of sequences of solution mappings as input.
 */
public interface NaryPhysicalOp extends PhysicalOperator
{
	@Override
	NaryExecutableOp createExecOp( boolean collectExceptions, ExpectedVariables ... inputVars );
}
