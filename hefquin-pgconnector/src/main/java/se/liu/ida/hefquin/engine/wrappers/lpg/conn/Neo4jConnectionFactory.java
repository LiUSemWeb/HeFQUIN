package se.liu.ida.hefquin.engine.wrappers.lpg.conn;

import com.fasterxml.jackson.core.JsonProcessingException;

import se.liu.ida.hefquin.base.utils.BuildInfo;
import se.liu.ida.hefquin.engine.wrappers.lpg.Neo4jException;
import se.liu.ida.hefquin.engine.wrappers.lpg.data.TableRecord;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.CypherQuery;
import se.liu.ida.hefquin.engine.wrappers.lpg.utils.CypherUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Set;
import java.util.Base64;

public class Neo4jConnectionFactory
{

	static public Neo4jConnection connect( final String url ) {
		return new Neo4jConnection(url);
	}

	static public Neo4jConnection connect( final String url,
	                                       final String user,
	                                       final String password ) {
		return new Neo4jConnection(url, user, password);
	}

	public static class Neo4jConnection
	{
		protected final URI uri;
		protected final String authHeader;

		public Neo4jConnection( final String url ) {
			this( URI.create(url), null, null );
		}
		
		public Neo4jConnection( final String url, final String username, final String password ) {
			this( URI.create(url), username, password );
		}

		public Neo4jConnection( final URI uri, final String username, final String password ) {
			assert uri != null;
			this.uri = uri;
			if ( username != null && password != null ){
				final String auth = username + ":" + password;
				final String encodedAuth = Base64.getEncoder().encodeToString( auth.getBytes() );
				authHeader = "Basic " + encodedAuth;
			}
			else {
				authHeader = null;
			}
		}

		public URI getURI() {
			return uri;
		}

		public List<TableRecord> execute( final CypherQuery q ) throws Neo4jException {
			return execute( q.toString() );
		}

		public List<TableRecord> execute( final String cypherQuery ) throws Neo4jException {
			final String data = "{ "
					+ "\"statements\" : [ {"
					+ "    \"statement\" : \""+ cypherQuery +"\""
					+ "  , \"parameters\" : {} } ]"
					+ "}";

			final Builder builder = HttpRequest.newBuilder(uri)
					.header("Accept", "application/json;charset=UTF-8")
					.header("User-Agent", BuildInfo.getUserAgent())
					.header("Content-Type", "application/json")
					.POST( HttpRequest.BodyPublishers.ofString(data) );
			if( authHeader != null ) {
				builder.header("Authorization", authHeader );
			}

			final var request = builder.build();
			final HttpClient client = HttpClient.newHttpClient();
			final HttpResponse<String> response;
			try {
				response = client.send( request, HttpResponse.BodyHandlers.ofString() );
			}
			catch ( final IOException e ) {
				throw new Neo4jConnectionException(this, "Data could not be sent to the server", e);
			}
			catch ( final InterruptedException e ) {
				throw new Neo4jConnectionException(this, "Neo4j server could not be reached.", e);
			}

			// According to the Neo4J API documentation, a query can return 200 OK or 201
			// Created when executed over the HTTP API because it wraps the request in an
			// implicit transaction (i.e., the 201 can indicate that the transaction was
			// successfully created, not that data was inserted/changed). It also lists 202
			// Accepted as a possible response code in some circumstances. See
			// https://neo4j.com/docs/query-api/current/query/
			if ( ! (Set.of( 200, 201, 202 ).contains( response.statusCode() )) ) {
				throw new Neo4jConnectionException(this, "Unexpected status code in HTTP response from Neo4j server: " + response.statusCode());
			}

			try {
				return CypherUtils.parse( response.body() );
			}
			catch ( final JsonProcessingException e ) {
				throw new Neo4jException("Neo4j server responded a malformed or unexpected JSON object", e);
			}
		}
	}

}
