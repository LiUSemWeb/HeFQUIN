package se.liu.ida.hefquin.service;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.sparql.engine.main.QC;
import org.eclipse.jetty.server.Server;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.http.NameValuePair;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

public class SparqlServletTest {
	private static CloseableHttpClient httpClient;
	private static int port = 4567;
	private static String uri = "http://localhost:" + port + "/sparql";
	private static Server server;

	// Default query
	private static final String DEFAULT_QUERY                = "SELECT (1 AS ?x) WHERE {}";

	// Request Content-Types
	private static final String CONTENT_TYPE_FORM_URLENCODED = "application/x-www-form-urlencoded";
	private static final String CONTENT_TYPE_SPARQL_QUERY    = "application/sparql-query";

	// Accept headers (Response formats for SELECT/ASK results)
	private static final String ACCEPT_SPARQL_RESULTS_XML    = "application/sparql-results+xml";
	private static final String ACCEPT_SPARQL_RESULTS_JSON   = "application/sparql-results+json";
	private static final String ACCEPT_CSV                   = "text/csv";
	private static final String ACCEPT_TSV                   = "text/tab-separated-values";

	// Accept wildcard and invalid formats
	private static final String ACCEPT_WILDCARD              = "*/*";
	private static final String ACCEPT_INVALID               = "application/invalid";

	// Invalid or unsupported content-types (used in 415 tests)
	private static final String CONTENT_TYPE_INVALID         = "application/unsupported";

	@BeforeClass
	public static void setUp() throws Exception {
		System.setProperty( "hefquin.configuration", "TestEngineConf.ttl" );
		System.setProperty( "hefquin.federation", "TestFedConf.ttl" );
		server = TestServer.run( port );
		httpClient = HttpClients.createDefault();
		server.start();
	}

	@AfterClass
	public static void tearDown() throws Exception {
		httpClient.close();
		// Unregister engine to prevent interference with other tests
		QC.setFactory( ARQ.getContext(), null );
		server.stop();
	}

	private HttpPost createPostRequest( final String contentType, final String acceptHeader, final String query ) {
		final HttpPost request = new HttpPost( uri );
		if ( contentType != null ) {
			request.addHeader( "Content-Type", contentType );
		}

		if ( acceptHeader != null ) {
			request.addHeader( "Accept", acceptHeader );
		}

		if ( query != null ) {
			if ( CONTENT_TYPE_FORM_URLENCODED.equals( contentType ) ) {
				final List<NameValuePair> list = Collections.singletonList( new BasicNameValuePair( "query", query ) );
				request.setEntity( new UrlEncodedFormEntity( list, StandardCharsets.UTF_8 ) );
			}
			else if ( CONTENT_TYPE_SPARQL_QUERY.equals( contentType ) ) {
				request.setEntity( new StringEntity( query, StandardCharsets.UTF_8 ) );
			}
		}

		return request;
	}

	private HttpGet createGetRequest( final String acceptHeader, final String query ) {
		final String fullUri;
		if ( query != null ) {
			fullUri = uri + "?query=" + URLEncoder.encode( query, StandardCharsets.UTF_8 );
		}
		else {
			fullUri = uri;
		}

		final HttpGet request = new HttpGet( fullUri );
		if ( acceptHeader != null )
			request.addHeader( "Accept", acceptHeader );

		return request;
	}

	private void validateResultSet( final ResultSet results ) {
		final List<String> vars = results.getResultVars();
		assertEquals( 1, vars.size() );
		assertEquals( "x", vars.get( 0 ) );
		final QuerySolution qs = results.next();
		assertEquals( 1, qs.get( "x" ).asLiteral().getInt() );
		assertFalse( results.hasNext() );
	}

	private void validateDelimited( final String results, final boolean tsv ) throws IOException {
		final CSVFormat format = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord( true ).build();
		try ( CSVParser parser = CSVParser.parse( results, format ) ) {
			final List<String> vars = parser.getHeaderNames();
			final List<CSVRecord> values = parser.getRecords();
			assertEquals( 1, vars.size() );
			assertEquals( 1, values.size() );
			final String var = tsv ? "?x" : "x";
			assertEquals( var, vars.get( 0 ) );
			assertEquals( "1", values.get( 0 ).get( var ) );
		}
	}

	@Test
	public void testPostUrlEncodedRequestReturnsXmlResults() throws Exception {
		final HttpPost request = createPostRequest( CONTENT_TYPE_FORM_URLENCODED, ACCEPT_SPARQL_RESULTS_XML,
				DEFAULT_QUERY );
		try ( final CloseableHttpResponse response = httpClient.execute( request ) ) {
			assertEquals( 200, response.getStatusLine().getStatusCode() );
			final ResultSet resultSet = ResultSetFactory.fromXML( response.getEntity().getContent() );
			validateResultSet( resultSet );
		}
	}

	@Test
	public void testPostUrlEncodedRequestReturnsJsonResults() throws Exception {
		final HttpPost request = createPostRequest( CONTENT_TYPE_FORM_URLENCODED, ACCEPT_SPARQL_RESULTS_JSON,
				DEFAULT_QUERY );
		try ( final CloseableHttpResponse response = httpClient.execute( request ) ) {
			assertEquals( 200, response.getStatusLine().getStatusCode() );
			final ResultSet resultSet = ResultSetFactory.fromJSON( response.getEntity().getContent() );
			validateResultSet( resultSet );
		}
	}

	@Test
	public void testPostUrlEncodedRequestReturnsCsvResults() throws Exception {
		final HttpPost request = createPostRequest( CONTENT_TYPE_FORM_URLENCODED, ACCEPT_CSV, DEFAULT_QUERY );
		try ( final CloseableHttpResponse response = httpClient.execute( request ) ) {
			assertEquals( 200, response.getStatusLine().getStatusCode() );
			final String result = EntityUtils.toString( response.getEntity() );
			validateDelimited( result, false );
		}
	}

	@Test
	public void testPostUrlEncodedRequestReturnsTsvResults() throws Exception {
		final HttpPost request = createPostRequest( CONTENT_TYPE_FORM_URLENCODED, ACCEPT_TSV, DEFAULT_QUERY );
		try ( final CloseableHttpResponse response = httpClient.execute( request ) ) {
			assertEquals( 200, response.getStatusLine().getStatusCode() );
			final String result = EntityUtils.toString( response.getEntity() );
			validateDelimited( result, true );
		}
	}

	@Test
	public void testPostUrlEncodedRequestWithEmptyQueryReturns400() throws Exception {
		final HttpPost request = createPostRequest( CONTENT_TYPE_FORM_URLENCODED, ACCEPT_WILDCARD, "" );
		try ( final CloseableHttpResponse response = httpClient.execute( request ) ) {
			assertEquals( 400, response.getStatusLine().getStatusCode() );
		}
	}

	@Test
	public void testPostUrlEncodedRequestWithMissingQueryReturns400() throws Exception {
		final HttpPost request = createPostRequest( CONTENT_TYPE_FORM_URLENCODED, ACCEPT_WILDCARD, null );
		try ( final CloseableHttpResponse response = httpClient.execute( request ) ) {
			assertEquals( 400, response.getStatusLine().getStatusCode() );
		}
	}

	@Test
	public void testPostUrlEncodedRequestWithUnsupportedAcceptReturns406() throws Exception {
		final HttpPost request = createPostRequest( CONTENT_TYPE_FORM_URLENCODED, ACCEPT_INVALID, DEFAULT_QUERY );
		try ( final CloseableHttpResponse response = httpClient.execute( request ) ) {
			assertEquals( 406, response.getStatusLine().getStatusCode() );
		}
	}

	@Test
	public void testPostUrlEncodedRequestWithInvalidQueryReturns500() throws Exception {
		final HttpPost request = createPostRequest( CONTENT_TYPE_FORM_URLENCODED, ACCEPT_WILDCARD, "Invalid query" );
		try ( final CloseableHttpResponse response = httpClient.execute( request ) ) {
			assertEquals( 500, response.getStatusLine().getStatusCode() );
		}
	}

	@Test
	public void testPostUrlEncodedRequestWithWildcardAcceptReturnsJsonResults() throws Exception {
		final HttpPost request = createPostRequest( CONTENT_TYPE_FORM_URLENCODED, ACCEPT_WILDCARD, DEFAULT_QUERY );
		try ( final CloseableHttpResponse response = httpClient.execute( request ) ) {
			assertEquals( 200, response.getStatusLine().getStatusCode() );
			final String contentType = response.getHeaders( "Content-Type" )[0].getValue().split( ";" )[0];
			assertEquals( contentType, ACCEPT_SPARQL_RESULTS_JSON );
			final ResultSet resultSet = ResultSetFactory.fromJSON( response.getEntity().getContent() );
			validateResultSet( resultSet );
		}
	}

	@Test
	public void testPostSparqlQueryRequestReturnsXmlResults() throws Exception {
		final HttpPost request = createPostRequest( CONTENT_TYPE_SPARQL_QUERY, ACCEPT_SPARQL_RESULTS_XML,
				DEFAULT_QUERY );
		try ( final CloseableHttpResponse response = httpClient.execute( request ) ) {
			assertEquals( 200, response.getStatusLine().getStatusCode() );
			final ResultSet resultSet = ResultSetFactory.fromXML( response.getEntity().getContent() );
			validateResultSet( resultSet );
		}
	}

	@Test
	public void testPostSparqlQueryRequestReturnsJsonResults() throws Exception {
		final HttpPost request = createPostRequest( CONTENT_TYPE_SPARQL_QUERY, ACCEPT_SPARQL_RESULTS_JSON,
				DEFAULT_QUERY );
		try ( final CloseableHttpResponse response = httpClient.execute( request ) ) {
			assertEquals( 200, response.getStatusLine().getStatusCode() );
			final ResultSet resultSet = ResultSetFactory.fromJSON( response.getEntity().getContent() );
			validateResultSet( resultSet );
		}
	}

	@Test
	public void testPostSparqlQueryRequestReturnsCsvResults() throws Exception {
		final HttpPost request = createPostRequest( CONTENT_TYPE_SPARQL_QUERY, ACCEPT_CSV, DEFAULT_QUERY );
		try ( final CloseableHttpResponse response = httpClient.execute( request ) ) {
			assertEquals( 200, response.getStatusLine().getStatusCode() );
			final String result = EntityUtils.toString( response.getEntity() );
			validateDelimited( result, false );
		}
	}

	@Test
	public void testPostSparqlQueryRequestReturnsTsvResults() throws Exception {
		final HttpPost request = createPostRequest( CONTENT_TYPE_SPARQL_QUERY, ACCEPT_TSV, DEFAULT_QUERY );
		try ( final CloseableHttpResponse response = httpClient.execute( request ) ) {
			assertEquals( 200, response.getStatusLine().getStatusCode() );
			final String result = EntityUtils.toString( response.getEntity() );
			validateDelimited( result, true );
		}
	}

	@Test
	public void testPostSparqlQueryRequestWithEmptyQueryReturns400() throws Exception {
		final HttpPost request = createPostRequest( CONTENT_TYPE_SPARQL_QUERY, ACCEPT_WILDCARD, "" );
		try ( final CloseableHttpResponse response = httpClient.execute( request ) ) {
			assertEquals( 400, response.getStatusLine().getStatusCode() );
		}
	}

	@Test
	public void testPostSparqlQueryRequestWithMissingQueryReturns400() throws Exception {
		final HttpPost request = createPostRequest( CONTENT_TYPE_SPARQL_QUERY, ACCEPT_WILDCARD, null );
		try ( final CloseableHttpResponse response = httpClient.execute( request ) ) {
			assertEquals( 400, response.getStatusLine().getStatusCode() );
		}
	}

	@Test
	public void testPostSparqlQueryRequestWithUnsupportedAcceptReturns406() throws Exception {
		final HttpPost request = createPostRequest( CONTENT_TYPE_SPARQL_QUERY, ACCEPT_INVALID, DEFAULT_QUERY );
		try ( final CloseableHttpResponse response = httpClient.execute( request ) ) {
			assertEquals( 406, response.getStatusLine().getStatusCode() );
		}
	}

	@Test
	public void testPostSparqlQueryRequestWithInvalidQueryReturns500() throws Exception {
		final HttpPost request = createPostRequest( CONTENT_TYPE_SPARQL_QUERY, ACCEPT_WILDCARD, "Invalid query" );
		try ( final CloseableHttpResponse response = httpClient.execute( request ) ) {
			assertEquals( 500, response.getStatusLine().getStatusCode() );
		}
	}

	@Test
	public void testPostUrlSparqlQueryRequestWithWildcardAcceptReturnsJsonResults() throws Exception {
		final HttpPost request = createPostRequest( CONTENT_TYPE_SPARQL_QUERY, ACCEPT_WILDCARD, DEFAULT_QUERY );
		try ( final CloseableHttpResponse response = httpClient.execute( request ) ) {
			assertEquals( 200, response.getStatusLine().getStatusCode() );
			final String contentType = response.getHeaders( "Content-Type" )[0].getValue().split( ";" )[0];
			assertEquals( contentType, ACCEPT_SPARQL_RESULTS_JSON );
			final ResultSet resultSet = ResultSetFactory.fromJSON( response.getEntity().getContent() );
			validateResultSet( resultSet );
		}
	}

	@Test
	public void testGetRequestReturnsXmlResults() throws Exception {
		final HttpGet request = createGetRequest( ACCEPT_SPARQL_RESULTS_XML, DEFAULT_QUERY );
		try ( final CloseableHttpResponse response = httpClient.execute( request ) ) {
			assertEquals( 200, response.getStatusLine().getStatusCode() );
			final ResultSet resultSet = ResultSetFactory.fromXML( response.getEntity().getContent() );
			validateResultSet( resultSet );
		}
	}

	@Test
	public void testGetRequestReturnsJsonResults() throws Exception {
		final HttpGet request = createGetRequest( ACCEPT_SPARQL_RESULTS_JSON, DEFAULT_QUERY );
		try ( final CloseableHttpResponse response = httpClient.execute( request ) ) {
			assertEquals( 200, response.getStatusLine().getStatusCode() );
			final ResultSet resultSet = ResultSetFactory.fromJSON( response.getEntity().getContent() );
			validateResultSet( resultSet );
		}
	}

	@Test
	public void testGetRequestReturnsCsvResults() throws Exception {
		final HttpGet request = createGetRequest( ACCEPT_CSV, DEFAULT_QUERY );
		try ( final CloseableHttpResponse response = httpClient.execute( request ) ) {
			assertEquals( 200, response.getStatusLine().getStatusCode() );
			final String result = EntityUtils.toString( response.getEntity() );
			validateDelimited( result, false );
		}
	}

	@Test
	public void testGetRequestReturnsTsvResults() throws Exception {
		final HttpGet request = createGetRequest( ACCEPT_TSV, DEFAULT_QUERY );
		try ( final CloseableHttpResponse response = httpClient.execute( request ) ) {
			assertEquals( 200, response.getStatusLine().getStatusCode() );
			final String result = EntityUtils.toString( response.getEntity() );
			validateDelimited( result, true );
		}
	}

	@Test
	public void testGetRequestWithEmptyQueryReturns400() throws Exception {
		final HttpGet request = createGetRequest( ACCEPT_WILDCARD, "" );
		try ( final CloseableHttpResponse response = httpClient.execute( request ) ) {
			assertEquals( 400, response.getStatusLine().getStatusCode() );
		}
	}

	@Test
	public void testGetRequestWithMissingQueryReturns400() throws Exception {
		final HttpGet request = createGetRequest( ACCEPT_WILDCARD, null );
		try ( final CloseableHttpResponse response = httpClient.execute( request ) ) {
			assertEquals( 400, response.getStatusLine().getStatusCode() );
		}
	}

	@Test
	public void testGetRequestWithInvalidQueryReturns500() throws Exception {
		final HttpGet request = createGetRequest( ACCEPT_WILDCARD, "Invalid query" );
		try ( final CloseableHttpResponse response = httpClient.execute( request ) ) {
			assertEquals( 500, response.getStatusLine().getStatusCode() );
		}
	}

	@Test
	public void testPostRequestWithMultipleAcceptTypesPrefersXml() throws Exception {
		final String compositeAcceptHeader = "application/invalid;q=0.9, application/sparql-results+xml;q=0.8";
		final HttpPost request = createPostRequest( CONTENT_TYPE_FORM_URLENCODED, compositeAcceptHeader,
				DEFAULT_QUERY );
		try ( final CloseableHttpResponse response = httpClient.execute( request ) ) {
			assertEquals( 200, response.getStatusLine().getStatusCode() );
			final String contentType = response.getHeaders( "Content-Type" )[0].getValue().split( ";" )[0];
			assertEquals( contentType, ACCEPT_SPARQL_RESULTS_XML );
		}
	}

	@Test
	public void testPostUrlEncodedRequestWithUnsupportedContentTypeReturns415() throws Exception {
		final HttpPost request = createPostRequest( CONTENT_TYPE_INVALID, ACCEPT_WILDCARD, DEFAULT_QUERY );
		try ( final CloseableHttpResponse response = httpClient.execute( request ) ) {
			assertEquals( 415, response.getStatusLine().getStatusCode() );
		}
	}

	@Test
	public void testMissingFederationMember() throws Exception {
		final String invalid = "SELECT * WHERE { SERVICE <http://invalid/federation/member> { ?s ?p ?o } }";
		final HttpPost request = createPostRequest( CONTENT_TYPE_FORM_URLENCODED, ACCEPT_SPARQL_RESULTS_XML, invalid );
		try ( final CloseableHttpResponse response = httpClient.execute( request ) ) {
			System.err.println( EntityUtils.toString( response.getEntity() ) );
			assertEquals( 500, response.getStatusLine().getStatusCode() );
		}
	}
}
