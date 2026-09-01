package se.liu.ida.hefquin.federation;

import org.apache.jena.graph.NodeFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

import se.liu.ida.hefquin.engine.wrappers.graphql.data.GraphQLEntrypoint;
import se.liu.ida.hefquin.engine.wrappers.graphql.data.GraphQLField;
import se.liu.ida.hefquin.engine.wrappers.graphql.data.GraphQLSchema;
import se.liu.ida.hefquin.engine.wrappers.graphql.data.impl.GraphQLEntrypointType;
import se.liu.ida.hefquin.engine.wrappers.graphql.data.impl.GraphQLFieldType;
import se.liu.ida.hefquin.federation.members.TPFServer;
import se.liu.ida.hefquin.federation.members.impl.BRTPFServerImpl;
import se.liu.ida.hefquin.federation.members.impl.GraphQLEndpointImpl;
import se.liu.ida.hefquin.federation.members.impl.Neo4jServerImpl;
import se.liu.ida.hefquin.federation.members.impl.RESTEndpointImpl;
import se.liu.ida.hefquin.federation.members.impl.SPARQLEndpointImpl;
import se.liu.ida.hefquin.federation.members.impl.TPFServerImpl;

public abstract class FederationTestBase
{
	/**
	 * If this flag is true, tests that access servers on the actual
	 * Web will be skipped.
	 */
	public static boolean skipLiveWebTests = true;

	/**
	 * If this flag is true, tests that make requests to local neo4j
	 * instances will be skipped.
	 */
	public static boolean skipLocalNeo4jTests = true;

	/**
	 * If true, skip tests to local GraphQL endpoint
	 */
	public static boolean skipLocalGraphQLTests = true;


	protected TPFServer getDBpediaTPFServer() {
		return new TPFServerImpl( NodeFactory.createURI("http://example.org/tpf"),
		                          "http://fragments.dbpedia.org/2016-04/en",
		                          null, // no authentication info
								  null ); // no vocab.mapping
	}

	protected static class SPARQLEndpointForTest extends SPARQLEndpointImpl
	{
		public SPARQLEndpointForTest() {
			super( NodeFactory.createURI("http://example.org/sparql"),
			       "http://example.org/sparql",
			       null,
			       null );
		}

		public SPARQLEndpointForTest( final String url ) {
			super( NodeFactory.createURI("http://example.org/sparql"),
			       url,
			       null,
			       null );
		}
	}

	protected static class TPFServerForTest extends TPFServerImpl
	{
		public TPFServerForTest() {
			super( NodeFactory.createURI("http://example.org/tpf"),
			       "http://example.org/",
			       null,
			       null );
		}

		public TPFServerForTest( final String baseURL ) {
			super( NodeFactory.createURI("http://example.org/tpf"),
			       baseURL,
			       null,
			       null );
		}
	}

	protected static class BRTPFServerForTest extends BRTPFServerImpl
	{
		public BRTPFServerForTest() {
			super( NodeFactory.createURI("http://example.org/brtpf"),
			       "http://example.org/",
			       null,
			       null );
		}

		public BRTPFServerForTest( final String baseURL ) {
			super( NodeFactory.createURI("http://example.org/brtpf"),
			       baseURL,
			       null,
			       null );
		}
	}

	protected static class Neo4jServerForTest extends Neo4jServerImpl
	{
		public Neo4jServerForTest() {
			super( NodeFactory.createURI("http://example.org/neo"),
			       "http://localhost:7474/db/neo4j/tx",
			       null );
		}

		public Neo4jServerForTest( final String baseURL ) {
			super( NodeFactory.createURI("http://example.org/neo"),
			       baseURL,
			       null );
		}
	}

	protected static class RESTEndpointForTest extends RESTEndpointImpl
	{
		public RESTEndpointForTest() {
			super( NodeFactory.createURI("http://example.org/rest"),
			       "http://example.org/",
			       List.of(),
			       null );
		}

		public RESTEndpointForTest( final String urlTemplate, final List<Parameter> params ) {
			super( NodeFactory.createURI("http://example.org/rest"),
			       urlTemplate,
			       params,
			       null );
		}

		public RESTEndpointForTest( final String baseURL, final String urlTemplate, final List<Parameter> params ) {
			super( NodeFactory.createURI(baseURL),
			       urlTemplate,
			       params,
			       null );
		}
	}

	protected static class GraphQLEndpointForTest extends GraphQLEndpointImpl
	{
		public GraphQLEndpointForTest() {
			super( NodeFactory.createURI("http://example.org/graphql"),
			       "http://example.org/",
			       null,
			       new EmptyGraphQLSchema() );
		}

		public GraphQLEndpointForTest( final String url ) {
			super( NodeFactory.createURI("http://example.org/graphl"),
			       url,
			       null,
			       new EmptyGraphQLSchema() );
		}
	}

	protected static class EmptyGraphQLSchema implements GraphQLSchema {
		@Override
		public boolean containsGraphQLObjectType( final String objectTypeName ) { return false; }

		@Override
		public boolean containsGraphQLField( final String objectTypeName, final String fieldName ) { return false; }

		@Override
		public GraphQLFieldType getGraphQLFieldType( final String objectTypeName, final String fieldName ) { return null; }

		@Override
		public String getGraphQLFieldValueType( final String objectTypeName, final String fieldName ) { return null; }

		@Override
		public Set<String> getGraphQLObjectTypes() { return Set.of(); }

		@Override
		public Map<String, GraphQLField> getGraphQLObjectFields( final String objectTypeName ) { return Map.of(); }

		@Override
		public GraphQLEntrypoint getEntrypoint( final String objectTypeName, final GraphQLEntrypointType fieldType ) { return null; }
	}
}
