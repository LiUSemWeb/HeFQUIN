package se.liu.ida.hefquin.engine;

import java.io.PrintStream;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.resultset.ResultsFormat;
import org.apache.jena.sparql.util.QueryExecUtils;

import se.liu.ida.hefquin.engine.queryproc.QueryProcessor;
import se.liu.ida.hefquin.engine.queryproc.impl.QueryProcessingStatsAndExceptionsImpl;
import se.liu.ida.hefquin.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.federation.access.FederationAccessStats;
import se.liu.ida.hefquin.jenaintegration.sparql.HeFQUINConstants;

/**
 * An object of this class can be used in two ways to process queries over
 * the federation for which the engine has been set up:
 * <ol>
 * <li>On the other hand, it can be used to process arbitrary queries
 *   and have the result written directly to stdout or to any other
 *   {@link PrintStream}, in a format that can be specified. This
 *   functionality is provided via the following functions.
 *   <ul>
 *   <li>{@link #executeQueryAndPrintResult(Query)}</li>
 *   <li>{@link #executeQueryAndPrintResult(Query, PrintStream)}</li>
 *   <li>{@link #executeQueryAndPrintResult(Query, ResultsFormat)}</li>
 *   <li>{@link #executeQueryAndPrintResult(Query, ResultsFormat, PrintStream)}</li>
 *   </ul>
 *   </li>
 * <li>On the one hand, it can be used to process SELECT queries and
 *   obtain the query result as a {@link ResultSet} object, which can
 *   then be consumed in whatever way you like. This functionality is
 *   provided by via {@link #executeSelectQuery(Query)}.</li>
 * </ol>
 *
 * To create a {@link HeFQUINEngine} object used {@link HeFQUINEngineBuilder}.
 * Once you do not need it anymore, make sure to call {@link #shutdown()}.
 * <p>
 * Technically, this is a wrapper around the Jena/ARQ query processing
 * machinery into which the query processor of this engine is integrated.
 */
public class HeFQUINEngine
{
	protected final FederationAccessManager fedAccessMgr;
	protected final QueryProcessor qProc;

	protected boolean wasShutDown = false;

	protected HeFQUINEngine( final FederationAccessManager fedAccessMgr,
	                         final QueryProcessor qProc ) {
		assert fedAccessMgr != null;
		assert qProc != null;

		this.fedAccessMgr = fedAccessMgr;
		this.qProc = qProc;
	}

	/**
	 * Executes the given query and prints the result in text format to stdout.
	 *
	 * @param query - the query to be executed
	 *
	 * @returns An object that captures statistics collected during the query
	 * execution process, together with a list of exceptions that were caught
	 * during query execution (if any).
	 *
	 * @throws UnsupportedQueryException
	 *              thrown if the given query uses features that are not
	 *              supported by HeFQUIN; the message of the exception
	 *              describes the specific limitation and can be passed
	 *              directly to the user.
	 * @throws IllegalQueryException
	 *              thrown if the given query is invalid; the message of
	 *              the exception describes the issue and can be passed
	 *              directly to the user.
	 */
	public QueryProcessingStatsAndExceptions executeQueryAndPrintResult( final Query query )
			throws UnsupportedQueryException, IllegalQueryException
	{
		return executeQueryAndPrintResult(query, ResultsFormat.FMT_TEXT);
	}

	/**
	 * Executes the given query and prints the result to stdout, in the given
	 * format.
	 *
	 * @param query - the query to be executed
	 * @param outputFormat - the format to be used for writing the result
	 *
	 * @returns An object that captures statistics collected during the query
	 * execution process, together with a list of exceptions that were caught
	 * during query execution (if any).
	 *
	 * @throws UnsupportedQueryException
	 *              thrown if the given query uses features that are not
	 *              supported by HeFQUIN; the message of the exception
	 *              describes the specific limitation and can be passed
	 *              directly to the user.
	 * @throws IllegalQueryException
	 *              thrown if the given query is invalid; the message of
	 *              the exception describes the issue and can be passed
	 *              directly to the user.
	 */
	public QueryProcessingStatsAndExceptions executeQueryAndPrintResult( final Query query,
	                                                                     final ResultsFormat outputFormat )
			throws UnsupportedQueryException, IllegalQueryException
	{
		return executeQueryAndPrintResult(query, outputFormat, System.out);
	}

	/**
	 * Executes the given query and prints the result in text format to the
	 * given output.
	 *
	 * @param query - the query to be executed
	 * @param output - the output stream to which the result shall written
	 *
	 * @returns An object that captures statistics collected during the query
	 * execution process, together with a list of exceptions that were caught
	 * during query execution (if any).
	 *
	 * @throws UnsupportedQueryException
	 *              thrown if the given query uses features that are not
	 *              supported by HeFQUIN; the message of the exception
	 *              describes the specific limitation and can be passed
	 *              directly to the user.
	 * @throws IllegalQueryException
	 *              thrown if the given query is invalid; the message of
	 *              the exception describes the issue and can be passed
	 *              directly to the user.
	 */
	public QueryProcessingStatsAndExceptions executeQueryAndPrintResult( final Query query,
	                                                                     final PrintStream output )
			throws UnsupportedQueryException, IllegalQueryException
	{
		return executeQueryAndPrintResult(query, ResultsFormat.FMT_TEXT, output);
	}

	/**
	 * Executes the given query and prints the result to the given output, in
	 * the given format.
	 *
	 * @returns An object that captures statistics collected during the query
	 * execution process, together with a list of exceptions that were caught
	 * during query execution (if any).
	 *
	 * @param query - the query to be executed
	 * @param outputFormat - the format to be used for writing the result
	 * @param output - the output stream to which the result shall written
	 *
	 * @throws UnsupportedQueryException
	 *              thrown if the given query uses features that are not
	 *              supported by HeFQUIN; the message of the exception
	 *              describes the specific limitation and can be passed
	 *              directly to the user.
	 * @throws IllegalQueryException
	 *              thrown if the given query is invalid; the message of
	 *              the exception describes the issue and can be passed
	 *              directly to the user.
	 */
	public QueryProcessingStatsAndExceptions executeQueryAndPrintResult( final Query query,
	                                                                     final ResultsFormat outputFormat,
	                                                                     final PrintStream output )
		throws UnsupportedQueryException, IllegalQueryException
	{
		return _execAndPrint(query, outputFormat, output);
	}

	/**
	 * Assuming the given query is a SELECT query, this function executes
	 * that query and returns an output object from which the query result
	 * can be obtained as a {@link ResultSet}.
	 *
	 * @param query - the query to be executed; it needs to be a SELECT query
	 *
	 * @throws UnsupportedQueryException
	 *              thrown if the given query uses features that are not
	 *              supported by HeFQUIN; the message of the exception
	 *              describes the specific limitation and can be passed
	 *              directly to the user.
	 * @throws IllegalQueryException
	 *              thrown if the given query is invalid; the message of
	 *              the exception describes the issue and can be passed
	 *              directly to the user.
	 */
	public QueryProcessingOutput executeSelectQuery( final Query query )
		throws UnsupportedQueryException, IllegalQueryException
	{
		if ( wasShutDown == true )
			throw new IllegalStateException("This HeFQUINEngine instance has been shut down already.");

		if ( ! query.isSelectType() )
			throw new IllegalQueryException(query, "The given query is not a SELECT query.");

		final QueryExecution qe = _prepareExecution(query);
		final ResultSet rs = qe.execSelect();

		return new QueryProcessingOutput() {
			@Override
			public ResultSet getResultSet() {
				return rs;
			}

			@Override
			public QueryProcessingStatsAndExceptions getStatsAndExceptions() {
				return (QueryProcessingStatsAndExceptions) qe.getContext().get(HeFQUINConstants.sysQProcStatsAndExceptions);
			}
		};
	}

	/**
	 * Returns a current version of the statistics collected by the
	 * {@link FederationAccessManager} that this engine uses.
	 * <p>
	 * This function simply calls {@link FederationAccessManager#getStats()}
	 * and forwards the returns stats.
	 */
	public FederationAccessStats getFederationAccessStats() {
		return fedAccessMgr.getStats();
	}

	/**
	 * Shuts down the relevant components used by this engine, such as the
	 * federation access manager and the query processor component.
	 */
	public void shutdown() {
		if ( wasShutDown == true )
			return;

		wasShutDown = true;
		fedAccessMgr.shutdown();
		qProc.shutdown();
	}


	// ------------------- main implementation ----------------

	protected QueryProcessingStatsAndExceptions _execAndPrint( final Query query,
	                                                           final ResultsFormat outputFormat,
	                                                           final PrintStream output )
		throws UnsupportedQueryException, IllegalQueryException
	{
		if ( wasShutDown == true )
			throw new IllegalStateException("This HeFQUINEngine instance has been shut down already.");

		final QueryExecution qe = _prepareExecution(query);

		Exception ex = null;
		try {
			if ( query.isSelectType() )
				_execSelectQuery(qe, outputFormat, output);
			else
				_execNonSelectQuery(qe, outputFormat, output);
		}
		catch ( final Exception e ) {
			ex = e;
		}

		final QueryProcessingStatsAndExceptions stats = (QueryProcessingStatsAndExceptions) qe.getContext().get(HeFQUINConstants.sysQProcStatsAndExceptions);

		if ( ex == null )
			return stats;
		else
			return new QueryProcessingStatsAndExceptionsImpl(stats, ex);
	}

	protected QueryExecution _prepareExecution( final Query query )
			throws UnsupportedQueryException, IllegalQueryException
	{
		ValuesServiceQueryResolver.expandValuesPlusServicePattern(query);

		final DatasetGraph dsg = DatasetGraphFactory.createGeneral();
		final QueryExecution qe = QueryExecutionFactory.create(query, dsg);

		return qe;
	}

	protected void _execSelectQuery( final QueryExecution qe,
	                                 final ResultsFormat outputFormat,
	                                 final PrintStream output ) throws Exception {
		final ResultSet rs;
		try {
			rs = qe.execSelect();
		}
		catch ( final Exception e ) {
			throw new Exception("Exception occurred when executing a SELECT query using the Jena machinery.", e);
		}

		try {
			QueryExecUtils.outputResultSet(rs, qe.getQuery().getPrologue(), outputFormat, output);
		}
		catch ( final Exception e ) {
			throw new Exception("Exception occurred when outputting the result of a SELECT query using the Jena machinery.", e);
		}
	}

	protected void _execNonSelectQuery( final QueryExecution qe,
	                                    final ResultsFormat outputFormat,
	                                    final PrintStream output ) throws Exception {
		try {
			QueryExecUtils.executeQuery( qe.getQuery().getPrologue(),
			                             qe,
			                             outputFormat,
			                             output );
		}
		catch ( final Exception e ) {
			throw new Exception("Exception occurred when executing an ASK/DESCRIBE/CONSTRUCT query using the Jena machinery.", e);
		}
	}

}
