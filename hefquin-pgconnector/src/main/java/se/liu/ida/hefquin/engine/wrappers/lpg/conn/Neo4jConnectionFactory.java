package se.liu.ida.hefquin.engine.wrappers.lpg.conn;

import com.fasterxml.jackson.core.JsonProcessingException;

import se.liu.ida.hefquin.engine.wrappers.lpg.Neo4jException;
import se.liu.ida.hefquin.engine.wrappers.lpg.data.TableRecord;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.CypherQuery;
import se.liu.ida.hefquin.engine.wrappers.lpg.utils.CypherUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class Neo4jConnectionFactory
{
	static public Neo4jConnection connect( final String url ) {
		return new Neo4jConnection(url);
	}

	public static class Neo4jConnection
	{
		protected final URI uri;

		public Neo4jConnection( final String url ) {
			this( URI.create(url) );
		}

		public Neo4jConnection( final URI uri ) {
			assert uri != null;
			this.uri = uri;
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

			final var request = HttpRequest.newBuilder(uri)
					.header("Accept", "application/json;charset=UTF-8")
					.header("Content-Type", "application/json")
					.POST( HttpRequest.BodyPublishers.ofString(data) )
					.build();
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

			if ( response.statusCode() != 200 ) {
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
