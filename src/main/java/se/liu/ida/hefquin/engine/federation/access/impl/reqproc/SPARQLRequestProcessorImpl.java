package se.liu.ida.hefquin.engine.federation.access.impl.reqproc;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.query.Query;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.ResultSetMgr;
import org.apache.jena.riot.WebContent;
import org.apache.jena.riot.resultset.ResultSetReaderRegistry;
import org.apache.jena.sparql.engine.http.HttpParams;
import org.apache.jena.sparql.engine.http.HttpQuery;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessException;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.engine.federation.access.impl.response.SolMapsResponseImpl;

public class SPARQLRequestProcessorImpl implements SPARQLRequestProcessor
{
	protected final int connectionTimeout;
	protected final int readTimeout;

	/**
	 * The given timeouts are specified in milliseconds. Any value {@literal <=} 0 means no timeout.
	 */
	public SPARQLRequestProcessorImpl( final int connectionTimeout, final int readTimeout ) {
		this.connectionTimeout = connectionTimeout;
		this.readTimeout = readTimeout;
	}

	public SPARQLRequestProcessorImpl() {
		this(-1, -1);
	}

	@Override
	public SolMapsResponse performRequest( final SPARQLRequest req,
	                                       final SPARQLEndpoint fm )
			throws FederationAccessException
	{
		return performRequestWithHttpQuery(req, fm);
		//return performRequestWithRDFConnection(req, fm);
	}

	protected SolMapsResponse performRequestWithHttpQuery( final SPARQLRequest req,
	                                                       final SPARQLEndpoint fm )
			throws FederationAccessException
	{
		// create and configure the HTTP request
		final HttpQuery httpReq = new HttpQuery( fm.getInterface().getURL() );

		final Query sparqlQuery = req.getQuery().asJenaQuery();
		final String sparqlQueryString = sparqlQuery.toString();
		httpReq.addParam( HttpParams.pQuery, sparqlQueryString );

		httpReq.setAllowCompression(true);
		httpReq.setConnectTimeout(connectionTimeout);
		httpReq.setReadTimeout(readTimeout);
		httpReq.setAccept( QueryEngineHTTP.defaultSelectHeader() );

		final Date requestStartTime = new Date();

		// execute the request
		final InputStream inStream;
		try {
			inStream = httpReq.exec();
		}
		catch ( final Exception ex ) {
			throw new FederationAccessException("Executing an HTTP request for a SPARQL endpoint caused an exception.", ex, req, fm);
		}

		// verify the returned content type
		final String returnedContentType = httpReq.getContentType();
		final Lang lang;
		if ( returnedContentType != null && ! returnedContentType.isEmpty() ) {
			lang = WebContent.contentTypeToLangResultSet(returnedContentType);
			if ( lang == null ) {
				try { inStream.close(); } catch ( final IOException e ) { e.printStackTrace(); }
				throw new FederationAccessException("The SPARQL endpoint returned a content type (" + returnedContentType + ") that is not recognized for SELECT queries", req, fm);
			}

			if ( ! ResultSetReaderRegistry.isRegistered(lang) ) {
				try { inStream.close(); } catch ( final IOException e ) { e.printStackTrace(); }
				throw new FederationAccessException("The SPARQL endpoint returned a content type (" + returnedContentType + ") that is not supported for SELECT queries", req, fm);
			}
		}
		else {
			// If the server did not return a content type, then we assume
			// that the server used the content type that was requested.
			lang = WebContent.contentTypeToLangResultSet( QueryEngineHTTP.defaultSelectHeader() );
		}

		// consume the query result
		final List<SolutionMapping> solMaps = new ArrayList<>();
		try {
			final ResultSet result = ResultSetMgr.read(inStream, lang);

			while ( result.hasNext() ) {
				final QuerySolution s = result.next();
				solMaps.add( SolutionMappingUtils.createSolutionMapping(s) );
			}

			inStream.close();
		}
		catch ( final IOException ex ) {
			throw new FederationAccessException("Closing the input stream from the HTTP response caused an exception.", ex, req, fm);
		}
		catch ( final Exception ex ) {
			try { inStream.close(); } catch ( final IOException e ) { e.printStackTrace(); }
			throw new FederationAccessException("Consuming the query result from the HTTP response caused an exception.", ex, req, fm);
		}

		return new SolMapsResponseImpl(solMaps, fm, req, requestStartTime);
	}

	protected void printInputStreamForDebugging( final InputStream inStream ) {
		final byte b[] = IO.readWholeFile(inStream);
		final String str = new String(b);
		System.out.println(str);
	}

	protected SolMapsResponse performRequestWithRDFConnection( final SPARQLRequest req,
	                                                           final SPARQLEndpoint fm )
			throws FederationAccessException
	{
		final Query query = req.getQuery().asJenaQuery();
		final MySolutionConsumer sink = new MySolutionConsumer();

		final Date requestStartTime = new Date();

		final RDFConnection conn;
		try {
			conn = RDFConnectionFactory.connect( fm.getInterface().getURL(),
                    null,   // updateServiceEndpoint
                    null ); // graphStoreProtocolEndpoint
		}
		catch ( final Exception ex ) {
			throw new FederationAccessException("Connecting to the SPARQL endpoint at '" + fm.getInterface().getURL() + "' caused an exception.", ex, req, fm);
		}

		try {
			conn.querySelect(query, sink);
		}
		catch ( final Exception ex ) {
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
