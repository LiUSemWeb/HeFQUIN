package se.liu.ida.hefquin.engine.wrappers.lpg.conn;

import com.fasterxml.jackson.core.JsonProcessingException;

import se.liu.ida.hefquin.engine.wrappers.lpg.Neo4jException;
import se.liu.ida.hefquin.engine.wrappers.lpg.data.TableRecord;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.CypherQuery;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.expression.AliasedExpression;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.expression.CypherVar;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.expression.TypeExpression;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.expression.VariableIDExpression;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.match.EdgeMatchClause;
import se.liu.ida.hefquin.engine.wrappers.lpg.utils.CypherQueryBuilder;
import se.liu.ida.hefquin.engine.wrappers.lpg.utils.CypherUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Base64;

public class Neo4jConnectionFactory
{

	public static CypherQuery buildGetEdgesQuery() {
		// MATCH (n1)-[e]->(n2)
		// RETURN ID(n1) AS nid1, ID(n2) AS nid2, e AS edge, TYPE(e) AS reltype

		final CypherVar n1 = new CypherVar("n1");
		final CypherVar n2 = new CypherVar("n2");
		final CypherVar e = new CypherVar("e");

		final CypherVar nid1 = new CypherVar("nid1");
		final CypherVar nid2 = new CypherVar("nid2");
		final CypherVar edge = new CypherVar("edge");
		final CypherVar reltype = new CypherVar("reltype");

		return new CypherQueryBuilder()
				.addMatch( new EdgeMatchClause(n1, e, n2) )
				.addReturn( new AliasedExpression(new VariableIDExpression(n1), nid1) )
				.addReturn( new AliasedExpression(new VariableIDExpression(n2), nid2) )
				.addReturn( new AliasedExpression(e, edge) )
				.addReturn( new AliasedExpression(new TypeExpression(e), reltype) )
				.build();
	}


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

			System.err.println("Code: " + response.statusCode());
			if ( ! ( response.statusCode() >= 200 && response.statusCode() < 300 ) ) {
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
