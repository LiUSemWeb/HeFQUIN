package se.liu.ida.hefquin.engine.queryplan.physical;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.BinaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;

/**
 * An interface for any type of {@link PhysicalOperator} whose algorithm
 * consumes two sequences of solution mappings as input.
 */
public interface BinaryPhysicalOp extends PhysicalOperator
{
	@Override
	BinaryExecutableOp createExecOp( boolean collectExceptions,
	                                 QueryPlanningInfo qpInfo,
	                                 ExpectedVariables ... inputVars );
}
