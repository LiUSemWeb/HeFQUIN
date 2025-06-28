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

import se.liu.ida.hefquin.engine.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessStats;
import se.liu.ida.hefquin.engine.queryproc.QueryProcessingStatsAndExceptions;
import se.liu.ida.hefquin.engine.queryproc.QueryProcessor;
import se.liu.ida.hefquin.engine.queryproc.impl.QueryProcessingStatsAndExceptionsImpl;
import se.liu.ida.hefquin.jenaintegration.sparql.HeFQUINConstants;

/**
 * Cannot be created directly (via the constructor) but needs to be created
 * via a {@link HeFQUINEngineBuilder}.
 * Need to call {@link #shutdown()} in the end.
 */
public class HeFQUINEngine
{
	protected final FederationAccessManager fedAccessMgr;
	protected final QueryProcessor qProc;

	protected HeFQUINEngine( final FederationAccessManager fedAccessMgr,
	                         final QueryProcessor qProc ) {
		assert fedAccessMgr != null;
		assert qProc != null;

		this.fedAccessMgr = fedAccessMgr;
		this.qProc = qProc;
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
	public QueryProcessingStatsAndExceptions executeQuery( final Query query )
			throws UnsupportedQueryException, IllegalQueryException
	{
		return executeQuery(query, ResultsFormat.FMT_TEXT);
	}

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
	public QueryProcessingStatsAndExceptions executeQuery( final Query query,
	                                                       final ResultsFormat outputFormat )
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
	public QueryProcessingStatsAndExceptions executeQuery( final Query query,
	                                                       final PrintStream output )
			throws UnsupportedQueryException, IllegalQueryException
	{
		return executeQuery(query, ResultsFormat.FMT_TEXT, output);
	}

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
	public QueryProcessingStatsAndExceptions executeQuery( final Query query,
	                                                       final ResultsFormat outputFormat,
	                                                       final PrintStream output )
		throws UnsupportedQueryException, IllegalQueryException
	{
		return _exec(query, outputFormat, output);
	}

	public FederationAccessStats getFederationAccessStats() {
		return fedAccessMgr.getStats();
	}

	/**
	 * Shuts down the relevant components used by this engine, such as the
	 * federation access manager and the query processor component.
	 */
	public void shutdown() {
		fedAccessMgr.shutdown();
		qProc.shutdown();
	}


	// ------------------- main implementation ----------------

	protected QueryProcessingStatsAndExceptions  _exec( final Query query,
	                                                    final ResultsFormat outputFormat,
	                                                    final PrintStream output )
		throws UnsupportedQueryException, IllegalQueryException
	{
		ValuesServiceQueryResolver.expandValuesPlusServicePattern(query);

		final DatasetGraph dsg = DatasetGraphFactory.createGeneral();
		final QueryExecution qe = QueryExecutionFactory.create(query, dsg);

		Exception ex = null;
		try {
			if ( query.isSelectType() )
				executeSelectQuery(qe, outputFormat, output);
			else
				executeNonSelectQuery(qe, outputFormat, output);
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

	protected void executeSelectQuery( final QueryExecution qe,
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

	protected void executeNonSelectQuery( final QueryExecution qe,
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
