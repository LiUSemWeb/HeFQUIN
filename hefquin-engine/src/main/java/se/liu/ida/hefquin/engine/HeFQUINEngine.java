package se.liu.ida.hefquin.engine;

import java.io.PrintStream;
import java.util.List;

import org.apache.jena.query.Query;
import org.apache.jena.sparql.resultset.ResultsFormat;

import se.liu.ida.hefquin.base.utils.Pair;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessStats;
import se.liu.ida.hefquin.engine.queryproc.QueryProcStats;

public interface HeFQUINEngine
{
	/**
	 * Call this one after the engine has been created.
	 */
	void integrateIntoJena();

	/**
	 * Executes the given query, prints the result to the given output (in
	 * the given format), and returns a statistics collected during the query
	 * execution process, together with a list of exception that were caught
	 * during query execution (if any).
	 *
	 * An {@link UnsupportedQueryException} is thrown if the given query uses
	 * features that are not supported by HeFQUIN; the message of the exception
	 * describes the specific limitation and can be passed directly to the user.
	 *
	 * An {@link IllegalQueryException} is thrown if the given query is
	 * invalid; the message of the exception describes the issue and can
	 * be passed directly to the user.
	 */
	Pair<QueryProcStats, List<Exception>> executeQuery( Query query,
	                                                    ResultsFormat outputFormat,
	                                                    PrintStream output )
		throws UnsupportedQueryException, IllegalQueryException;

	/**
	 * Executes the given query, prints the result to stdout (in the given
	 * format), and returns a statistics collected during the query execution
	 * process, together with a list of exception that were caught during
	 * query execution (if any).
	 *
	 * An {@link UnsupportedQueryException} is thrown if the given query uses
	 * features that are not supported by HeFQUIN; the message of the exception
	 * describes the specific limitation and can be passed directly to the user.
	 *
	 * An {@link IllegalQueryException} is thrown if the given query is
	 * invalid; the message of the exception describes the issue and can
	 * be passed directly to the user.
	 */
	default Pair<QueryProcStats, List<Exception>> executeQuery( Query query,
	                                                            ResultsFormat outputFormat )
			throws UnsupportedQueryException, IllegalQueryException
	{
		return executeQuery(query, outputFormat, System.out);
	}

	/**
	 * Executes the given query, prints the result in text format to the given
	 * output, and returns a statistics collected during the query execution
	 * process, together with a list of exception that were caught during query
	 * execution (if any).
	 *
	 * An {@link UnsupportedQueryException} is thrown if the given query uses
	 * features that are not supported by HeFQUIN; the message of the exception
	 * describes the specific limitation and can be passed directly to the user.
	 *
	 * An {@link IllegalQueryException} is thrown if the given query is
	 * invalid; the message of the exception describes the issue and can
	 * be passed directly to the user.
	 */
	default Pair<QueryProcStats, List<Exception>> executeQuery( Query query,
	                                                            PrintStream output )
			throws UnsupportedQueryException, IllegalQueryException
	{
		return executeQuery(query, ResultsFormat.FMT_TEXT, output);
	}

	/**
	 * Executes the given query, prints the result in text format to stdout,
	 * and returns a statistics collected during the query execution process,
	 * together with a list of exception that were caught during query
	 * execution (if any).
	 *
	 * An {@link UnsupportedQueryException} is thrown if the given query uses
	 * features that are not supported by HeFQUIN; the message of the exception
	 * describes the specific limitation and can be passed directly to the user.
	 *
	 * An {@link IllegalQueryException} is thrown if the given query is
	 * invalid; the message of the exception describes the issue and can
	 * be passed directly to the user.
	 */
	default Pair<QueryProcStats, List<Exception>> executeQuery( Query query )
			throws UnsupportedQueryException, IllegalQueryException
	{
		return executeQuery(query, ResultsFormat.FMT_TEXT);
	}

	FederationAccessStats getFederationAccessStats();

	/**
	 * Shuts down the relevant components used by this engine, such as the
	 * federation access manager and the query processor component.
	 */
	void shutdown();
}
