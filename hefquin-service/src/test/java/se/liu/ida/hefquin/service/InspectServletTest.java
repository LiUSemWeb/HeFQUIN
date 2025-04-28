package se.liu.ida.hefquin.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.engine.main.QC;
import org.eclipse.jetty.server.Server;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Ignore("Disabled since it uses live web tests")
public class InspectServletTest {
	private static CloseableHttpClient httpClient;
	private static int port = 4567;
	private static String uri = "http://localhost:" + port + "/query-inspect";
	private static Server server;

	// Default query
	private static final String DEFAULT_QUERY = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
	                                            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
												"PREFIX owl: <http://www.w3.org/2002/07/owl#> " + 
												"PREFIX dbo: <http://dbpedia.org/ontology/> " +
												"SELECT * " +
												"WHERE { " +
												"  SERVICE <http://dbpedia.org/sparql> { " +
												"    <http://dbpedia.org/resource/Berlin> dbo:country ?c . " +
												"    ?c owl:sameAs ?cc " +
											    "  }" +
											    "}";

	final String[] queryInspectFields = new String[]{ "exceptions",
	                                                  "queryMetrics",
	                                                  "logicalPlan",
	                                                  "physicalPlan",
	                                                  "sourceAssignment",
	                                                  "federationAccessStats" };

	// Request Content-Types
	private static final String CONTENT_TYPE_FORM_URLENCODED = "application/x-www-form-urlencoded";
	private static final String CONTENT_TYPE_SPARQL_QUERY    = "application/sparql-query";

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

	private HttpPost createPostRequest( final String contentType, final String query ) {
		final HttpPost request = new HttpPost( uri );
		if ( contentType != null ) {
			request.addHeader( "Content-Type", contentType );
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

	private HttpGet createGetRequest( final String query ) {
		final String fullUri;
		if ( query != null ) {
			fullUri = uri + "?query=" + URLEncoder.encode( query, StandardCharsets.UTF_8 );
		}
		else {
			fullUri = uri;
		}

		final HttpGet request = new HttpGet( fullUri );
		return request;
	}

	@Test
	public void testPostUrlEncodedRequest() throws Exception {
		final HttpPost request = createPostRequest( CONTENT_TYPE_FORM_URLENCODED, DEFAULT_QUERY );
		try ( final CloseableHttpResponse response = httpClient.execute( request ) ) {

			final String result = EntityUtils.toString( response.getEntity() );
			final JsonObject o = JsonParser.parseString( result ).getAsJsonObject();

			assertEquals( 200, response.getStatusLine().getStatusCode() );
			for ( final String key : queryInspectFields ) {
				assertTrue( o.keySet().contains( key ) );
				assertNotNull( o.get( key ) );
			}
		}
	}

	@Test
	public void testPostUrlEncodedRequestWithEmptyQueryReturns400() throws Exception {
		final HttpPost request = createPostRequest( CONTENT_TYPE_FORM_URLENCODED, "" );
		try ( final CloseableHttpResponse response = httpClient.execute( request ) ) {
			assertEquals( 400, response.getStatusLine().getStatusCode() );
		}
	}

	@Test
	public void testPostUrlEncodedRequestWithMissingQueryReturns400() throws Exception {
		final HttpPost request = createPostRequest( CONTENT_TYPE_FORM_URLENCODED, null );
		try ( final CloseableHttpResponse response = httpClient.execute( request ) ) {
			assertEquals( 400, response.getStatusLine().getStatusCode() );
		}
	}

	@Test
	public void testPostUrlEncodedRequestWithInvalidQueryReturns500() throws Exception {
		final HttpPost request = createPostRequest( CONTENT_TYPE_FORM_URLENCODED, "Invalid query" );
		try ( final CloseableHttpResponse response = httpClient.execute( request ) ) {
			assertEquals( 500, response.getStatusLine().getStatusCode() );
		}
	}

	@Test
	public void testPostSparqlQueryRequest() throws Exception {
		final HttpPost request = createPostRequest( CONTENT_TYPE_SPARQL_QUERY, DEFAULT_QUERY );
		try ( final CloseableHttpResponse response = httpClient.execute( request ) ) {

			final JsonObject result = JsonParser.parseString( EntityUtils.toString( response.getEntity() ) )
					.getAsJsonObject();
			assertEquals( 200, response.getStatusLine().getStatusCode() );
			for ( final String key : queryInspectFields ) {
				assertTrue( result.keySet().contains( key ) );
				assertNotNull( result.get( key ) );
			}
		}
	}

	@Test
	public void testPostSparqlQueryRequestWithEmptyQueryReturns400() throws Exception {
		final HttpPost request = createPostRequest( CONTENT_TYPE_SPARQL_QUERY, "" );
		try ( final CloseableHttpResponse response = httpClient.execute( request ) ) {
			assertEquals( 400, response.getStatusLine().getStatusCode() );
		}
	}

	@Test
	public void testPostSparqlQueryRequestWithMissingQueryReturns400() throws Exception {
		final HttpPost request = createPostRequest( CONTENT_TYPE_SPARQL_QUERY, null );
		try ( final CloseableHttpResponse response = httpClient.execute( request ) ) {
			assertEquals( 400, response.getStatusLine().getStatusCode() );
		}
	}

	@Test
	public void testPostSparqlQueryRequestWithInvalidQueryReturns500() throws Exception {
		final HttpPost request = createPostRequest( CONTENT_TYPE_SPARQL_QUERY, "Invalid query" );
		try ( final CloseableHttpResponse response = httpClient.execute( request ) ) {
			assertEquals( 500, response.getStatusLine().getStatusCode() );
		}
	}

	@Test
	public void testGetSparqlQueryRequest() throws Exception {
		final HttpGet request = createGetRequest( DEFAULT_QUERY );
		try ( final CloseableHttpResponse response = httpClient.execute( request ) ) {

			final JsonObject result = JsonParser.parseString( EntityUtils.toString( response.getEntity() ) )
					.getAsJsonObject();
			assertEquals( 200, response.getStatusLine().getStatusCode() );
			for ( final String key : queryInspectFields ) {
				assertTrue( result.keySet().contains( key ) );
				assertNotNull( result.get( key ) );
			}
		}
	}

	@Test
	public void testGetRequestWithEmptyQueryReturns400() throws Exception {
		final HttpGet request = createGetRequest( "" );
		try ( final CloseableHttpResponse response = httpClient.execute( request ) ) {
			assertEquals( 400, response.getStatusLine().getStatusCode() );
		}
	}

	@Test
	public void testGetRequestWithMissingQueryReturns400() throws Exception {
		final HttpGet request = createGetRequest( null );
		try ( final CloseableHttpResponse response = httpClient.execute( request ) ) {
			assertEquals( 400, response.getStatusLine().getStatusCode() );
		}
	}

	@Test
	public void testGetRequestWithInvalidQueryReturns500() throws Exception {
		final HttpGet request = createGetRequest( "Invalid query" );
		try ( final CloseableHttpResponse response = httpClient.execute( request ) ) {
			assertEquals( 500, response.getStatusLine().getStatusCode() );
		}
	}

	@Test
	public void testPostUrlEncodedRequestWithUnsupportedContentTypeReturns415() throws Exception {
		final HttpPost request = createPostRequest( CONTENT_TYPE_INVALID, DEFAULT_QUERY );
		try ( final CloseableHttpResponse response = httpClient.execute( request ) ) {
			assertEquals( 415, response.getStatusLine().getStatusCode() );
		}
	}

	@Test
	public void testMissingFederationMember() throws Exception {
		final String invalid = "SELECT * WHERE { SERVICE <http://invalid/federation/member> { ?s ?p ?o } }";
		final HttpPost request = createPostRequest( CONTENT_TYPE_FORM_URLENCODED, invalid );
		try ( final CloseableHttpResponse response = httpClient.execute( request ) ) {
			assertEquals( 200, response.getStatusLine().getStatusCode() );

			final String result = EntityUtils.toString( response.getEntity() );
			final JsonObject o = JsonParser.parseString( result ).getAsJsonObject();
			assertEquals(
				"java.util.NoSuchElementException: no federation member with URI <http://invalid/federation/member>",
				o.getAsJsonArray( "exceptions" ).get( 0 )
			);
		}
	}
}
