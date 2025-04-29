package se.liu.ida.hefquin.engine.queryplan.physical;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.NullaryExecutableOp;

/**
 * An interface for any type of {@link PhysicalOperator} whose algorithm
 * does not consume any input (but produces a sequence of solution mappings
 * as output).
 */
public interface NullaryPhysicalOp extends PhysicalOperator
{
	@Override
	NullaryExecutableOp createExecOp( boolean collectExceptions, ExpectedVariables ... inputVars );
}
