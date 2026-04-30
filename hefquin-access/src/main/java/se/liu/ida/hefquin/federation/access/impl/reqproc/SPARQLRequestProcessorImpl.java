package se.liu.ida.hefquin.federation.access.impl.reqproc;

import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.sparql.exec.http.QueryExecutionHTTPBuilder;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.base.net.http.HttpClientProvider;
import se.liu.ida.hefquin.base.utils.BuildInfo;
import se.liu.ida.hefquin.federation.access.FederationAccessException;
import se.liu.ida.hefquin.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.federation.access.impl.response.SolMapsResponseImpl;
import se.liu.ida.hefquin.federation.members.SPARQLEndpoint;

public class SPARQLRequestProcessorImpl implements SPARQLRequestProcessor
{
	protected final HttpClient httpClient;
	protected final long overallTimeout;

	public SPARQLRequestProcessorImpl() {
		this(-1L, -1L);
	}

	/**
	 * The given timeouts are specified in milliseconds. Any value {@literal <=} 0 means no timeout.
	 */
	public SPARQLRequestProcessorImpl( final long connectionTimeout, final long overallTimeout ) {
		httpClient = HttpClientProvider.client(connectionTimeout);
		this.overallTimeout = overallTimeout;
	}

	@Override
	public SolMapsResponse performRequest( final SPARQLRequest req,
	                                       final SPARQLEndpoint fm )
			throws FederationAccessException
	{
		return performRequestWithQueryExecutionHTTP(req, fm);
		//return performRequestWithRDFConnection(req, fm);
	}

	protected SolMapsResponse performRequestWithQueryExecutionHTTP( final SPARQLRequest req,
	                                                                final SPARQLEndpoint fm )
			throws FederationAccessException
	{
		// see https://jena.apache.org/documentation/sparql-apis/#query-execution

		final QueryExecution qe;
		try {
			qe = QueryExecutionHTTPBuilder.create()
					.endpoint(   fm.getURL() )
					.httpClient( httpClient )
					.query(      req.getQuery().asJenaQuery() )
					.timeout(    overallTimeout, TimeUnit.MILLISECONDS )
					.httpHeader( "User-Agent", BuildInfo.getUserAgent() )
					.build();
		}
		catch ( final Exception e ) {
			throw new FederationAccessException("Initiating the remote execution of a query at the SPARQL endpoint at '" + fm.getURL() + "' caused an exception.", e, req, fm);
		}

		final ResultSet result = qe.execSelect();

		final Date requestStartTime = new Date();

		// consume the query result
		final List<SolutionMapping> solMaps = new ArrayList<>();
		try {
			while ( result.hasNext() ) {
				final QuerySolution s = result.next();
				solMaps.add( SolutionMappingUtils.createSolutionMapping(s) );
			}
		}
		catch ( final Exception ex ) {
			try { result.close(); } catch ( final Exception e ) { e.printStackTrace(); }
			throw new FederationAccessException("Consuming the query result from the SPARQL endpoint at '" + fm.getURL() + "' caused an exception.", ex, req, fm);
		}

		result.close();

		return new SolMapsResponseImpl(solMaps, requestStartTime);
	}

	protected SolMapsResponse performRequestWithRDFConnection( final SPARQLRequest req,
	                                                           final SPARQLEndpoint fm )
			throws FederationAccessException
	{
		// see https://jena.apache.org/documentation/sparql-apis/#ttrdfconnectiontt

		final Query query = req.getQuery().asJenaQuery();
		final MySolutionConsumer sink = new MySolutionConsumer();

		final Date requestStartTime = new Date();

		final RDFConnection conn;
		try {
			conn = RDFConnectionRemote.service( fm.getURL() )
					.httpClient(httpClient)
					.build();
		}
		catch ( final Exception ex ) {
			throw new FederationAccessException("Creating the connection to the SPARQL endpoint at '" + fm.getURL() + "' caused an exception.", ex, req, fm);
		}

		try {
			conn.querySelect(query, sink);
		}
		catch ( final Exception ex ) {
			try { conn.close(); } catch ( final Exception e ) { e.printStackTrace(); }
			throw new FederationAccessException("Issuing the given query to the SPARQL endpoint at '" + fm.getURL() + "' caused an exception.", ex, req, fm);
		}

		try {
			conn.close();
		}
		catch ( final Exception ex ) {
			throw new FederationAccessException("Closing the connection to the SPARQL endpoint at '" + fm.getURL() + "' caused an exception.", ex, req, fm);
		}

		return new SolMapsResponseImpl(sink.solMaps, requestStartTime);
	}


	protected static class MySolutionConsumer implements Consumer<QuerySolution>
	{
		public final List<SolutionMapping> solMaps = new ArrayList<>();

		@Override
		public void accept( final QuerySolution s ) {
			solMaps.add( SolutionMappingUtils.createSolutionMapping(s) );
		}

	} // end of MySolutionConsumer

	/**
	 * Determines whether it is semantically safe to override the projection
	 * of the given query.
	 *
	 * <p>Overriding the projection means replacing the SELECT clause with a
	 * new set of variables (e.g., for projection pushdown). This is only safe
	 * for "simple" SELECT queries without features that depend on the original
	 * projection.</p>
	 *
	 * <p>In particular, projection must not be overridden if the query contains:
	 * <ul>
	 *   <li>GROUP BY (projection affects grouping semantics)</li>
	 *   <li>HAVING clauses (depends on grouped results)</li>
	 *   <li>Aggregators (e.g., COUNT, SUM), which rely on specific projection expressions</li>
	 * </ul>
	 * </p>
	 *
	 * @param q the query to inspect
	 * @return {@code true} if projection can be safely overridden; {@code false} otherwise
	 */
	protected static boolean isSafeToOverrideProjection(final Query q)
	{
		// Query must be SELECT type
		if ( ! q.isSelectType() ) return false;

		// Must not contain GROUP BY
		if ( q.hasGroupBy() ) return false;

		// Must not contain HAVING (optional but safe)
		if ( q.hasHaving() ) return false;

		// Must not have expressions in SELECT
		if ( q.hasAggregators() ) return false;

		return true;
	}
}
