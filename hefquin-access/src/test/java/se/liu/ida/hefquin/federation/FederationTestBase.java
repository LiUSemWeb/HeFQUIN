package se.liu.ida.hefquin.federation;

import org.apache.jena.graph.NodeFactory;

import se.liu.ida.hefquin.federation.members.TPFServer;
import se.liu.ida.hefquin.federation.members.impl.BRTPFServerImpl;
import se.liu.ida.hefquin.federation.members.impl.Neo4jServerImpl;
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
		                          null ); // no vocab.mapping
	}

	protected static class SPARQLEndpointForTest extends SPARQLEndpointImpl
	{
		public SPARQLEndpointForTest() {
			super( NodeFactory.createURI("http://example.org/sparql"),
			       "http://example.org/sparql",
			       null );
		}

		public SPARQLEndpointForTest( final String url ) {
			super( NodeFactory.createURI("http://example.org/sparql"),
			       url,
			       null );
		}
	}

	protected static class TPFServerForTest extends TPFServerImpl
	{
		public TPFServerForTest() {
			super( NodeFactory.createURI("http://example.org/tpf"),
			       "http://example.org/",
			       null );
		}

		public TPFServerForTest( final String baseURL ) {
			super( NodeFactory.createURI("http://example.org/tpf"),
			       baseURL,
			       null );
		}
	}

	protected static class BRTPFServerForTest extends BRTPFServerImpl
	{
		public BRTPFServerForTest() {
			super( NodeFactory.createURI("http://example.org/brtpf"),
			       "http://example.org/",
			       null );
		}

		public BRTPFServerForTest( final String baseURL ) {
			super( NodeFactory.createURI("http://example.org/brtpf"),
			       baseURL,
			       null );
		}
	}

	protected static class Neo4jServerImpl4Test extends Neo4jServerImpl
	{
		public Neo4jServerImpl4Test() {
			super( NodeFactory.createURI("http://example.org/neo"),
			       "http://localhost:7474/db/neo4j/tx" );
		}
	}

}
