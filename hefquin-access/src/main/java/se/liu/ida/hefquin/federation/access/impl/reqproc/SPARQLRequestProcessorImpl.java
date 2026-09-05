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
	                                       final SPARQLEndpoint fm ) {
		return performRequestWithQueryExecutionHTTP(req, fm);
		//return performRequestWithRDFConnection(req, fm);
	}

	protected SolMapsResponse performRequestWithQueryExecutionHTTP( final SPARQLRequest req,
	                                                                final SPARQLEndpoint fm ) {
		// see https://jena.apache.org/documentation/sparql-apis/#query-execution

		final Date requestStartTime = new Date();

		final QueryExecution qe;
		try {
			final QueryExecutionHTTPBuilder builder = QueryExecutionHTTPBuilder.create()
					.endpoint(   fm.getURL() )
					.httpClient( httpClient )
					.query(      req.getQuery().asJenaQuery() )
					.timeout(    overallTimeout, TimeUnit.MILLISECONDS )
					.httpHeader( "User-Agent", BuildInfo.getUserAgent() );

					if ( fm.getAuthenticationInformation() != null )
						fm.getAuthenticationInformation().applyTo( builder );

					qe = builder.build();
		}
		catch ( final Exception e ) {
			final String msg = "Initiating the remote execution of a query at " +
					"the SPARQL endpoint with service URI " + fm.getServiceURI() +
					" caused an exception (type: " + e.getClass().getName() +
					") with the following message: " + e.getMessage();
			return new SolMapsResponseImpl(
					new FederationAccessException(msg,e,req,fm),
					requestStartTime );
		}

		final ResultSet result;
		try {
			result = qe.execSelect();
		}
		catch ( final Exception e ) {
			final String msg = "Performing the remote execution of a query at " +
					"the SPARQL endpoint with service URI " + fm.getServiceURI() +
					" caused an exception (type: " + e.getClass().getName() +
					") with the following message: " + e.getMessage();
			return new SolMapsResponseImpl(
					new FederationAccessException(msg,e,req,fm),
					requestStartTime );
		}

		// consume the query result
		final List<SolutionMapping> solMaps = new ArrayList<>();
		try {
			while ( result.hasNext() ) {
				final QuerySolution s = result.next();
				solMaps.add( SolutionMappingUtils.createSolutionMapping(s) );
			}
		}
		catch ( final Exception e ) {
			try { result.close(); } catch ( final Exception ex ) { ex.printStackTrace(); }

			final String msg = "Consuming the result of a query executed at " +
					"the SPARQL endpoint with service URI " + fm.getServiceURI() +
					" caused an exception (type: " + e.getClass().getName() +
					") with the following message: " + e.getMessage();
			return new SolMapsResponseImpl(
					new FederationAccessException(msg,e,req,fm),
					requestStartTime );
		}

		result.close();

		return new SolMapsResponseImpl(solMaps, requestStartTime);
	}

	protected SolMapsResponse performRequestWithRDFConnection( final SPARQLRequest req,
	                                                           final SPARQLEndpoint fm ) {
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
		catch ( final Exception e ) {
			final String msg = "Creating the connection to the SPARQL endpoint " +
					"with service URI " + fm.getServiceURI() + " caused an " +
					"exception (type: " + e.getClass().getName() + ") with " +
					"the following message: " + e.getMessage();
			return new SolMapsResponseImpl(
					new FederationAccessException(msg,e,req,fm),
					requestStartTime );
		}

		try {
			conn.querySelect(query, sink);
		}
		catch ( final Exception e ) {
			try { conn.close(); } catch ( final Exception ex ) { ex.printStackTrace(); }

			final String msg = "Running the remote execution of a query at " +
					"the SPARQL endpoint with service URI " + fm.getServiceURI() +
					" caused an exception (type: " + e.getClass().getName() +
					") with the following message: " + e.getMessage();
			return new SolMapsResponseImpl(
					new FederationAccessException(msg,e,req,fm),
					requestStartTime );
		}

		try {
			conn.close();
		}
		catch ( final Exception e ) {
			final String msg = "Creating the connection to the SPARQL endpoint " +
					"with service URI " + fm.getServiceURI() + " caused an " +
					"exception (type: " + e.getClass().getName() + ") with " +
					"the following message: " + e.getMessage();
			return new SolMapsResponseImpl(
					new FederationAccessException(msg,e,req,fm),
					requestStartTime );
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

}
