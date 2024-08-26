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
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class HeFQUINServletTest {
	private static CloseableHttpClient httpClient;
	private static int port = 4567;
	private static String uri = "http://localhost:" + port + "/sparql";
	private static Server server;

	@BeforeClass
	public static void setUp() throws Exception {
		HeFQUINServlet.setConfigProperties("config-test.properties");
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

	private HttpPost getPostRequest( final String contentType, final String acceptType )
			throws UnsupportedEncodingException {
		return getPostRequest( contentType, acceptType, "SELECT (1 AS ?x) WHERE {}" );
	}

	private HttpPost getPostRequest( final String contentType, final String acceptType, final String query )
			throws UnsupportedEncodingException {

		final HttpPost request = new HttpPost( uri );
		request.addHeader( "Content-Type", contentType );
		request.addHeader( "Accept", acceptType );

		if ( contentType.equals( "application/x-www-form-urlencoded" ) ) {
			final List<NameValuePair> list = new ArrayList<>();
			list.add( new BasicNameValuePair( "query", query ) );
			request.setEntity( new UrlEncodedFormEntity( list ) );
		} else {
			request.setEntity( new StringEntity( query ) );
		}
		return request;
	}

	private void validateResponse( final CloseableHttpResponse response, final String expectedContentType ) {
		assertEquals( 200, response.getStatusLine().getStatusCode() );
		final String contentType = response.getHeaders( "Content-Type" )[0].getValue();
		assertTrue( contentType.startsWith( expectedContentType ) );
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
	public void postRequestWithXMLFormat1() throws Exception {
		final String contentType = "application/x-www-form-urlencoded";
		final String acceptType = "application/sparql-results+xml";
		final HttpPost request = getPostRequest( contentType, acceptType );
		try ( CloseableHttpResponse response = httpClient.execute( request ) ) {
			validateResponse( response, acceptType );
			final ResultSet resultSet = ResultSetFactory.fromXML( response.getEntity().getContent() );
			validateResultSet( resultSet );
		}
	}

	@Test
	public void postRequestWithJSONFormat1() throws Exception {
		final String contentType = "application/x-www-form-urlencoded";
		final String acceptType = "application/sparql-results+json";
		final HttpPost request = getPostRequest( contentType, acceptType );
		try ( final CloseableHttpResponse response = httpClient.execute( request ) ) {
			validateResponse( response, acceptType );
			final ResultSet resultSet = ResultSetFactory.fromJSON( response.getEntity().getContent() );
			validateResultSet( resultSet );
		}
	}

	@Test
	public void postRequestWithCSVFormat1() throws Exception {
		final String contentType = "application/x-www-form-urlencoded";
		final String acceptType = "text/csv";
		final HttpPost request = getPostRequest( contentType, acceptType );
		try ( final CloseableHttpResponse response = httpClient.execute( request ) ) {
			validateResponse( response, acceptType );
			validateDelimited( EntityUtils.toString( response.getEntity() ), false );
		}
	}

	@Test
	public void postRequestWithTSVFormat1() throws Exception {
		final String contentType = "application/x-www-form-urlencoded";
		final String acceptType = "text/tsv";
		final HttpPost request = getPostRequest( contentType, acceptType );
		try ( final CloseableHttpResponse response = httpClient.execute( request ) ) {
			validateResponse( response, acceptType );
			validateDelimited( EntityUtils.toString( response.getEntity() ), true );
		}
	}

	@Test
	public void postRequestWithEmptyQuery1() throws Exception {
		final String contentType = "application/x-www-form-urlencoded";
		final String acceptType = "text/tsv";
		final HttpPost request = getPostRequest( contentType, acceptType, "" );

		try ( final CloseableHttpResponse response = httpClient.execute( request ) ) {
			assertEquals( 400, response.getStatusLine().getStatusCode() );
		}
	}

	@Test
	public void postRequestWithMissingQuery1() throws Exception {
		final String contentType = "application/x-www-form-urlencoded";
		final String acceptType = "text/tsv";
		final HttpPost request = new HttpPost( uri );
		request.addHeader( "Content-Type", contentType );
		request.addHeader( "Accept", acceptType );

		try ( final CloseableHttpResponse response = httpClient.execute( request ) ) {
			assertEquals( 400, response.getStatusLine().getStatusCode() );
		}
	}

	@Test
	public void postRequestWithUnsupportedFormat1() throws Exception {
		final String contentType = "application/x-www-form-urlencoded";
		final String acceptType = "text/invalid";
		final HttpPost request = getPostRequest( contentType, acceptType );
		try ( final CloseableHttpResponse response = httpClient.execute( request ) ) {
			validateResponse( response, "application/sparql-results+json" );
		}
	}

	@Test
	public void postRequestWithNullQuery1() throws Exception {
		final String contentType = "application/x-www-form-urlencoded";
		final String acceptType = "text/tsv";
		final HttpPost request = getPostRequest( contentType, acceptType, null );

		try ( final CloseableHttpResponse response = httpClient.execute( request ) ) {
			assertEquals( 400, response.getStatusLine().getStatusCode() );
		}
	}

	@Test
	public void postRequestWithInvalidQuery1() throws Exception {
		final String contentType = "application/x-www-form-urlencoded";
		final String acceptType = "text/tsv";
		final HttpPost request = getPostRequest( contentType, acceptType, "SELECT id FROM person;" );

		try ( final CloseableHttpResponse response = httpClient.execute( request ) ) {
			assertEquals( 500, response.getStatusLine().getStatusCode() );
		}
	}

	@Test
	public void postRequestWithXMLFormat2() throws Exception {
		final String contentType = "application/sparql-query";
		final String acceptType = "application/sparql-results+xml";
		final HttpPost request = getPostRequest( contentType, acceptType );
		try ( final CloseableHttpResponse response = httpClient.execute( request ) ) {
			validateResponse( response, acceptType );
			final ResultSet resultSet = ResultSetFactory.fromXML( response.getEntity().getContent() );
			validateResultSet( resultSet );
		}
	}

	@Test
	public void postRequestWithJSONFormat2() throws Exception {
		final String contentType = "application/sparql-query";
		final String acceptType = "application/sparql-results+json";
		final HttpPost request = getPostRequest( contentType, acceptType );
		try ( final CloseableHttpResponse response = httpClient.execute( request ) ) {
			validateResponse( response, acceptType );
			final ResultSet resultSet = ResultSetFactory.fromJSON( response.getEntity().getContent() );
			validateResultSet( resultSet );
		}
	}

	@Test
	public void postRequestWithCSVFormat2() throws Exception {
		final String contentType = "application/sparql-query";
		final String acceptType = "text/csv";
		final HttpPost request = getPostRequest( contentType, acceptType );
		try ( final CloseableHttpResponse response = httpClient.execute( request ) ) {
			validateResponse( response, acceptType );
			validateDelimited( EntityUtils.toString( response.getEntity() ), false );
		}
	}

	@Test
	public void postRequestWithTSVFormat2() throws Exception {
		final String contentType = "application/sparql-query";
		final String acceptType = "text/tsv";
		final HttpPost request = getPostRequest( contentType, acceptType );
		try ( final CloseableHttpResponse response = httpClient.execute( request ) ) {
			validateResponse( response, acceptType );
			validateDelimited( EntityUtils.toString( response.getEntity() ), true );
		}
	}

	@Test
	public void postRequestWithUnsupportedFormat2() throws Exception {
		final String contentType = "application/sparql-query";
		final String acceptType = "text/invalid";
		final HttpPost request = getPostRequest( contentType, acceptType );
		try ( final CloseableHttpResponse response = httpClient.execute( request ) ) {
			validateResponse( response, "application/sparql-results+json" );
		}
	}

	@Test
	public void postRequestWithEmptyQuery2() throws Exception {
		final String contentType = "application/sparql-query";
		final String acceptType = "text/tsv";
		final HttpPost request = getPostRequest( contentType, acceptType, "" );

		try ( final CloseableHttpResponse response = httpClient.execute( request ) ) {
			assertEquals( 400, response.getStatusLine().getStatusCode() );
		}
	}

	@Test
	public void postRequestWithMissingQuery2() throws Exception {
		final String contentType = "application/sparql-query";
		final String acceptType = "text/tsv";
		final HttpPost request = new HttpPost( uri );
		request.addHeader( "Content-Type", contentType );
		request.addHeader( "Accept", acceptType );

		try ( CloseableHttpResponse response = httpClient.execute( request ) ) {
			assertEquals( 400, response.getStatusLine().getStatusCode() );
		}
	}

	@Test
	public void postRequestWithInvalidQuery2() throws Exception {
		final String contentType = "application/sparql-query";
		final String acceptType = "text/tsv";
		final HttpPost request = getPostRequest( contentType, acceptType, "SELECT id FROM person;" );

		try ( final CloseableHttpResponse response = httpClient.execute( request ) ) {
			assertEquals( 500, response.getStatusLine().getStatusCode() );
		}
	}

	@Test
	public void getRequestWithXMLFormat() throws Exception {
		final String acceptType = "application/sparql-results+xml";
		final String query = URLEncoder.encode( "SELECT (1 AS ?x) WHERE {}", "utf-8" );
		final HttpGet request = new HttpGet( uri + "?query=" + query );
		request.addHeader( "Accept", acceptType );
		try ( final CloseableHttpResponse response = httpClient.execute( request ) ) {
			validateResponse( response, acceptType );
			final ResultSet resultSet = ResultSetFactory.fromXML( response.getEntity().getContent() );
			validateResultSet( resultSet );
		}
	}

	@Test
	public void getRequestWithJSONFormat() throws Exception {
		final String acceptType = "application/sparql-results+json";
		final String query = URLEncoder.encode( "SELECT (1 AS ?x) WHERE {}", "utf-8" );
		final HttpGet request = new HttpGet( uri + "?query=" + query );
		request.addHeader( "Accept", acceptType );
		try ( final CloseableHttpResponse response = httpClient.execute( request ) ) {
			validateResponse( response, acceptType );
			final ResultSet resultSet = ResultSetFactory.fromJSON( response.getEntity().getContent() );
			validateResultSet( resultSet );
		}
	}

	@Test
	public void getRequestWithCSVFormat() throws Exception {
		final String acceptType = "text/csv";
		final String query = URLEncoder.encode( "SELECT (1 AS ?x) WHERE {}", "utf-8" );
		final HttpGet request = new HttpGet( uri + "?query=" + query );
		request.addHeader( "Accept", acceptType );
		try ( final CloseableHttpResponse response = httpClient.execute( request ) ) {
			validateResponse( response, acceptType );
			validateDelimited( EntityUtils.toString( response.getEntity() ), false );
		}
	}

	@Test
	public void getRequestWithTSVFormat() throws Exception {
		final String acceptType = "text/tsv";
		final String query = URLEncoder.encode( "SELECT (1 AS ?x) WHERE {}", "utf-8" );
		final HttpGet request = new HttpGet( uri + "?query=" + query );
		request.addHeader( "Accept", acceptType );
		try ( final CloseableHttpResponse response = httpClient.execute( request ) ) {
			validateResponse( response, acceptType );
			validateDelimited( EntityUtils.toString( response.getEntity() ), true );
		}
	}

	@Test
	public void getRequestWithInvalidQuery() throws Exception {
		final String acceptType = "text/tsv";
		final String query = URLEncoder.encode( "SELECT id FROM person;", "utf-8" );
		final HttpGet request = new HttpGet( uri + "?query=" + query );
		request.addHeader( "Accept", acceptType );
		try ( CloseableHttpResponse response = httpClient.execute( request ) ) {
			assertEquals( 500, response.getStatusLine().getStatusCode() );
		}
	}

	@Test
	public void getRequestWithEmptyQuery() throws Exception {
		final String acceptType = "text/tsv";
		final HttpGet request = new HttpGet( uri + "?query=" );
		request.addHeader( "Accept", acceptType );
		try ( final CloseableHttpResponse response = httpClient.execute( request ) ) {
			assertEquals( 400, response.getStatusLine().getStatusCode() );
		}
	}

	@Test
	public void getRequestWithMissingQuery() throws Exception {
		final String acceptType = "text/tsv";
		final HttpGet request = new HttpGet( uri );
		request.addHeader( "Accept", acceptType );
		try ( final CloseableHttpResponse response = httpClient.execute( request ) ) {
			assertEquals( 400, response.getStatusLine().getStatusCode() );
		}
	}
}
