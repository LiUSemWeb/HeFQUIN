package se.liu.ida.hefquin.engine;

import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;

/**
 * Base class functions should be used only after the query result has been consumed.
 */
public interface QueryProcessingOutput
{
	/**
	 * Returns the result set produced for the query for which this object
	 * was returned, assuming the query was a SELECT query. If the query was
	 * not a SELECT query, this function returns <code>null</code>.
	 * <p>
	 * The returned result set can be consumed only once. If you want to
	 * consume it multiple times (i.e., make it rewindable), pass it to
	 * {@link ResultSetFactory#makeRewindable(ResultSet)} (which consumes
	 * the result set and returns a rewindable version of it).
	 */
	ResultSet getResultSet();

	/**
	 * When called after the query result has been consumed, this function
	 * returns an object that captures query processing statistics and
	 * exceptions that have occurred while processing the query (if any).
	 * <p>
	 * This function may return <code>null</code>, which may happen in
	 * two cases: either the query result has not been consumed yet or
	 * the Jena/ARQ query processing machinery did not invoke the HeFQUIN
	 * engine when processing the query.
	 */
	QueryProcessingStatsAndExceptions getStatsAndExceptions();
}
