package se.liu.ida.hefquin.federation;

import se.liu.ida.hefquin.base.data.VocabularyMapping;
import se.liu.ida.hefquin.federation.access.*;
import se.liu.ida.hefquin.federation.access.impl.iface.BRTPFInterfaceImpl;
import se.liu.ida.hefquin.federation.access.impl.iface.Neo4jInterfaceImpl;
import se.liu.ida.hefquin.federation.access.impl.iface.SPARQLEndpointInterfaceImpl;
import se.liu.ida.hefquin.federation.access.impl.iface.TPFInterfaceImpl;

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
		final String       tpfServerBaseURL = "http://fragments.dbpedia.org/2016-04/en";
		final TPFInterface tpfServerIface   = new TPFInterfaceImpl(tpfServerBaseURL, "subject", "predicate", "object");
		return new TPFServer() {
			@Override public TPFInterface getInterface() { return tpfServerIface; }

			@Override
			public VocabularyMapping getVocabularyMapping() {
				return null;
			}
		};
	}

	protected static class SPARQLEndpointForTest implements SPARQLEndpoint
	{
		final SPARQLEndpointInterface iface;

		public SPARQLEndpointForTest() { this("http://example.org/sparql"); }

		public SPARQLEndpointForTest( final String ifaceURL ) {
			iface = new SPARQLEndpointInterfaceImpl(ifaceURL);
		}

		@Override
		public SPARQLEndpointInterface getInterface() { return iface; }

		@Override
		public VocabularyMapping getVocabularyMapping() { return null; }

	}

	protected static class TPFServerForTest implements TPFServer
	{
		protected final TPFInterface iface = new TPFInterfaceImpl("http://example.org/", "subject", "predicate", "object");

		public TPFServerForTest() { }

		@Override
		public TPFInterface getInterface() { return iface; }

		@Override
		public VocabularyMapping getVocabularyMapping() { return null; }
	}

	protected static class BRTPFServerForTest implements BRTPFServer
	{
		final BRTPFInterface iface = new BRTPFInterfaceImpl("http://example.org/", "subject", "predicate", "object", "values");

		public BRTPFServerForTest() { }

		@Override
		public BRTPFInterface getInterface() { return iface; }

		@Override
		public VocabularyMapping getVocabularyMapping() { return null; }
	}

	protected static class Neo4jServerImpl4Test implements Neo4jServer
	{
		public Neo4jServerImpl4Test() {}

		@Override
		public Neo4jInterface getInterface() {
			return new Neo4jInterfaceImpl("http://localhost:7474/db/neo4j/tx");
		}

		@Override
		public VocabularyMapping getVocabularyMapping() {
			return null;
		}
	}

}
