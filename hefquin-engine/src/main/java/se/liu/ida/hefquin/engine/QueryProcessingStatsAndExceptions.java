package se.liu.ida.hefquin.engine;

import java.util.List;

import se.liu.ida.hefquin.base.utils.Stats;
import se.liu.ida.hefquin.engine.queryproc.ExecutionStats;
import se.liu.ida.hefquin.engine.queryproc.QueryPlanningStats;

public interface QueryProcessingStatsAndExceptions extends Stats
{
	long getOverallQueryProcessingTime();
	long getPlanningTime();
	long getCompilationTime();
	long getExecutionTime();

	QueryPlanningStats getQueryPlanningStats();
	ExecutionStats getExecutionStats();

	/**
	 * Returns <code>true</code> if this object contains exceptions that have
	 * occurred while processing the query for which this object was returned.
	 * The exceptions themselves can be accessed via {@link #getExceptions()}.
	 */
	boolean containsExceptions();

	/**
	 * Returns a list of the exceptions that have occurred while processing
	 * the query for which this object was returned. If no exceptions occurred,
	 * this function returns <code>null</code>.
	 * <p>
	 * The function {@link #containsExceptions()} can be used to ask whether
	 * there are exceptions.
	 */
	List<Exception> getExceptions();
}
