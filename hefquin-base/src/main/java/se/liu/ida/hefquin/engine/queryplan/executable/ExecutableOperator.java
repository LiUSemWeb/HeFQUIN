package se.liu.ida.hefquin.engine.queryplan.executable;

import java.util.List;

import se.liu.ida.hefquin.engine.utils.StatsProvider;

/**
 * An executable operator provides the implementation of the concrete
 * algorithm that a physical operator is associated with. This interface
 * is the top-level interface for all executable operators of HeFQUIN.
 */
public interface ExecutableOperator extends StatsProvider
{
	@Override
	ExecutableOperatorStats getStats();

	/**
	 * Returns exceptions that were caught and collected during the execution
	 * of this operator (if any). If no exceptions were caught (which should
	 * be the normal case), then this function returns an empty list.
	 */
	List<Exception> getExceptionsCaughtDuringExecution();
}
