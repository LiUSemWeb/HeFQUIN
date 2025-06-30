package se.liu.ida.hefquin.federation.access.impl.reqproc;

import java.net.http.HttpClient;
import java.time.Duration;
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
import se.liu.ida.hefquin.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.federation.access.FederationAccessException;
import se.liu.ida.hefquin.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.federation.access.impl.response.SolMapsResponseImpl;

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
		httpClient = createHttpClient(connectionTimeout);
		this.overallTimeout = overallTimeout;
	}

	protected static HttpClient createHttpClient( final long connectionTimeout ) {
		final HttpClient.Builder httpClientBuilder = HttpClient.newBuilder()
				.followRedirects( HttpClient.Redirect.ALWAYS );

		if ( connectionTimeout > 0L )
			httpClientBuilder.connectTimeout( Duration.ofMillis(connectionTimeout) );

		return httpClientBuilder.build();
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
					.endpoint(   fm.getInterface().getURL() )
					.httpClient( httpClient )
					.query(      req.getQuery().asJenaQuery() )
					.timeout(    overallTimeout, TimeUnit.MILLISECONDS )
					.build();
		}
		catch ( final Exception e ) {
			throw new FederationAccessException("Initiating the remote execution of a query at the SPARQL endpoint at '" + fm.getInterface().getURL() + "' caused an exception.", e, req, fm);
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
			throw new FederationAccessException("Consuming the query result from the SPARQL endpoint at '" + fm.getInterface().getURL() + "' caused an exception.", ex, req, fm);
		}

		result.close();

		return new SolMapsResponseImpl(solMaps, fm, req, requestStartTime);
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
			conn = RDFConnectionRemote.service( fm.getInterface().getURL() )
					.httpClient(httpClient)
					.build();
		}
		catch ( final Exception ex ) {
			throw new FederationAccessException("Creating the connection to the SPARQL endpoint at '" + fm.getInterface().getURL() + "' caused an exception.", ex, req, fm);
		}

		try {
			conn.querySelect(query, sink);
		}
		catch ( final Exception ex ) {
			try { conn.close(); } catch ( final Exception e ) { e.printStackTrace(); }
			throw new FederationAccessException("Issuing the given query to the SPARQL endpoint at '" + fm.getInterface().getURL() + "' caused an exception.", ex, req, fm);
		}

		try {
			conn.close();
		}
		catch ( final Exception ex ) {
			throw new FederationAccessException("Closing the connection to the SPARQL endpoint at '" + fm.getInterface().getURL() + "' caused an exception.", ex, req, fm);
		}

		return new SolMapsResponseImpl(sink.solMaps, fm, req, requestStartTime);
	}


	protected static class MySolutionConsumer implements Consumer<QuerySolution>
	{
		public final List<SolutionMapping> solMaps = new ArrayList<>();

		@Override
		public void accept( final QuerySolution s ) {
			solMaps.add( SolutionMappingUtils.createSolutionMapping(s) );
		}
		
	} // end of MySolutionConsumer

}
