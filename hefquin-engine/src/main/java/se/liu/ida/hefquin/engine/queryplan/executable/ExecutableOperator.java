package se.liu.ida.hefquin.engine.queryplan.executable;

import java.util.List;

import se.liu.ida.hefquin.base.utils.StatsProvider;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;

/**
 * An executable operator provides the implementation of the concrete
 * algorithm that a physical operator is associated with. This interface
 * is the top-level interface for all executable operators of HeFQUIN.
 */
public interface ExecutableOperator extends StatsProvider
{
	/**
	 * Indicates whether this executable operator is permitted (and potentially
	 * encouraged) to reduce duplicate solution mappings as part of its execution.
	 * <p>
	 * If this method returns {@code true}, the operator may remove duplicate
	 * solution mappings if it has the capability to do so. If it returns
	 * {@code false}, the operator must preserve duplicates, even if it would
	 * otherwise be able to eliminate them.
	 *
	 * @return {@code true} if the operator may reduce duplicates; {@code false} otherwise.
	 */
	boolean mayReduce();

	/**
	 * Returns the {@link QueryPlanningInfo} object that was populated for
	 * a physical plan whose root operator was the physical operator for
	 * which this executable operator was created.
	 *
	 * @return the {@link QueryPlanningInfo} object or {@code null}.
	 */
	QueryPlanningInfo getQueryPlanningInfo();

	/**
	 * This method sets a 'termination' flag within the operator, which is
	 * a signal to the operator that it may terminate and does not need to
	 * continue its internal processing, even if it has not completed yet.
	 * There is no requirement, however, that the operator does indeed
	 * terminate its internal processing. In fact, implementations of
	 * operators may ignore the flag (but once the flag has been set,
	 * {@link #terminationFlagIsSet()} returns {@code true}, making it
	 * possible to check whether the flag has been set for this operator,
	 * independent of whether the operator acts on this flag or not).
	 * <p>
	 * This method is expected to be called by a thread that coordinates
	 * the execution of the whole plan or by a thread that consumes the
	 * solution mappings produced by this operator. Therefore, this method
	 * should be implemented in a thread-safe manner, and it should not
	 * block the calling thread but, instead, return immediately. Hence,
	 * the operator may still be running when this method returns.
	 */
	void setTerminationFlag();

	/**
	 * Returns {@code true} if the termination flag has been set via
	 * the {@link #setTerminationFlag()} method. This method should
	 * be implemented in a thread-safe manner.
	 */
	boolean terminationFlagIsSet();

	@Override
	ExecutableOperatorStats getStats();

	/**
	 * Returns exceptions that were caught and collected during the execution
	 * of this operator (if any). If no exceptions were caught (which should
	 * be the normal case), then this function returns an empty list.
	 */
	List<Exception> getExceptionsCaughtDuringExecution();
}
