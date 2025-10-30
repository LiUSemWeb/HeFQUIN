package se.liu.ida.hefquin.federation;

import se.liu.ida.hefquin.federation.impl.BRTPFServerImpl;
import se.liu.ida.hefquin.federation.impl.Neo4jServerImpl;
import se.liu.ida.hefquin.federation.impl.SPARQLEndpointImpl;
import se.liu.ida.hefquin.federation.impl.TPFServerImpl;

public abstract class FederationTestBase
{
	/**
	 * Change this flag to true if you also want to run the
	 * unit tests that access servers on the actual Web.
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
		return new TPFServerImpl( "http://fragments.dbpedia.org/2016-04/en",
		                          null ); // no vocab.mapping
	}

	protected static class SPARQLEndpointForTest extends SPARQLEndpointImpl
	{
		public SPARQLEndpointForTest() {
			super("http://example.org/sparql", null);
		}

		public SPARQLEndpointForTest( final String url ) {
			super(url, null);
		}
	}

	protected static class TPFServerForTest extends TPFServerImpl
	{
		public TPFServerForTest() {
			super("http://example.org/", null);
		}

		public TPFServerForTest( final String baseURL ) {
			super(baseURL, null);
		}
	}

	protected static class BRTPFServerForTest extends BRTPFServerImpl
	{
		public BRTPFServerForTest() {
			super("http://example.org/", null);
		}

		public BRTPFServerForTest( final String baseURL ) {
			super(baseURL, null);
		}
	}

	protected static class Neo4jServerImpl4Test extends Neo4jServerImpl
	{
		public Neo4jServerImpl4Test() {
			super("http://localhost:7474/db/neo4j/tx", null);
		}
	}

}
