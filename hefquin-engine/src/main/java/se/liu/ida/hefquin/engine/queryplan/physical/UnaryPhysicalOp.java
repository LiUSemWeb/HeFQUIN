package se.liu.ida.hefquin.engine.queryplan.physical;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.UnaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;

/**
 * An interface for any type of {@link PhysicalOperator} whose algorithm
 * consumes a single sequence of solution mappings as input.
 */
public interface UnaryPhysicalOp extends PhysicalOperator
{
	@Override
	UnaryExecutableOp createExecOp( boolean collectExceptions,
	                                QueryPlanningInfo qpInfo,
	                                ExpectedVariables ... inputVars );
}
