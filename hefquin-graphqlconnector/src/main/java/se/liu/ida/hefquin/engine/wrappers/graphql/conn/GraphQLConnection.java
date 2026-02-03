package se.liu.ida.hefquin.engine.wrappers.graphql.conn;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonParseException;

import se.liu.ida.hefquin.base.shared.http.HttpClientProvider;
import se.liu.ida.hefquin.base.utils.BuildInfo;
import se.liu.ida.hefquin.engine.wrappers.graphql.query.GraphQLQuery;

import org.apache.jena.atlas.json.JSON;

public class GraphQLConnection
{
	public static JsonObject performRequest( final GraphQLQuery query,
	                                         final String url,
	                                         final int connectionTimeout,
	                                         final int readTimeout )
			throws GraphQLConnectionException {

		// Get HTTP client
		final HttpClient client = HttpClientProvider.client(connectionTimeout);
	
		// Sending post body
		final JsonObject postBody = new JsonObject();
		postBody.put( "query", query.toString() );
		postBody.put( "variables", query.getArgumentValues() );
		postBody.put( "raw", true );

		// Create the request
		final HttpRequest request = HttpRequest.newBuilder()
				.uri( URI.create(url) )
				.POST( BodyPublishers.ofString( postBody.toString() ) )
				.timeout( Duration.ofMillis( readTimeout ) )
				.header( "Accept", "application/json" )
				.header( "User-Agent", BuildInfo.getUserAgent() )
				.header( "Content-Type", "application/json" )
				.build();

		try {
			// Get response
			final HttpResponse<String> response = client.send( request, BodyHandlers.ofString() );
			final int status = response.statusCode();
			if ( status < 200 || status >= 300 ) {
				throw new GraphQLConnectionException(
						"Couldn't establish a connection to endpoint. Response code: " + status );
			}

			// Parse JSON responseBody into a json object
			final JsonObject jsonObj;
			try {
				jsonObj = JSON.parse( response.body() );
			} catch ( final JsonParseException e ) {
				throw new GraphQLConnectionException( "Unable to parse the retrieved JSON", e );
			}

			return jsonObj;
		} catch ( final IOException e ) {
			throw new GraphQLConnectionException( "I/O error while communicating with GraphQL endpoint", e );
		} catch ( final InterruptedException e ) {
			// Restore interrupt status
			Thread.currentThread().interrupt();
			throw new GraphQLConnectionException( "Request to GraphQL endpoint was interrupted", e );
		}
	}
}
