package se.liu.ida.hefquin.engine.federation.access.impl;

import static org.junit.Assert.assertEquals;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.QueryFactory;
import org.junit.Test;

import se.liu.ida.hefquin.engine.EngineTestBase;
import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.Neo4jServer;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.TPFServer;
import se.liu.ida.hefquin.engine.federation.access.BRTPFRequest;
import se.liu.ida.hefquin.engine.federation.access.CardinalityResponse;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessException;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.access.Neo4jRequest;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.federation.access.StringResponse;
import se.liu.ida.hefquin.engine.federation.access.TPFRequest;
import se.liu.ida.hefquin.engine.federation.access.TPFResponse;
import se.liu.ida.hefquin.engine.federation.access.impl.req.BGPRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.req.TriplePatternRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.BRTPFRequestProcessor;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.Neo4jRequestProcessor;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.SPARQLRequestProcessor;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.SPARQLRequestProcessorImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.TPFRequestProcessor;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.BGPImpl;
import se.liu.ida.hefquin.engine.query.impl.SPARQLGraphPatternImpl;
import se.liu.ida.hefquin.engine.query.impl.TriplePatternImpl;

public class FederationAccessManagerBaseTest extends EngineTestBase
{
	@Test
	public void performCardinalityRequestWithPatternOnDBpediaSPARQLEndpoint()
			throws FederationAccessException
	{
		if ( ! skipLiveWebTests ) {
			// setting up
			final String queryString = "SELECT * WHERE { <http://dbpedia.org/resource/Toronto> <http://xmlns.com/foaf/0.1/name> ?o }";
			final SPARQLGraphPattern pattern = new SPARQLGraphPatternImpl( QueryFactory.create(queryString).getQueryPattern() );
			final SPARQLRequest req = new SPARQLRequestImpl(pattern);

			performCardinalityRequestOnDBpediaSPARQLEndpointHelper(req, 2);
		}
	}

	@Test
	public void performCardinalityRequestWithTPOnDBpediaSPARQLEndpoint()
			throws FederationAccessException
	{
		if ( ! skipLiveWebTests ) {
			// setting up
			final Node s = NodeFactory.createURI("http://dbpedia.org/resource/Toronto");
			final Node p = NodeFactory.createURI("http://xmlns.com/foaf/0.1/name");
			final Node o = NodeFactory.createVariable("o");
			final TriplePattern tp = new TriplePatternImpl(s,p,o);
			final SPARQLRequest req = new TriplePatternRequestImpl(tp);

			performCardinalityRequestOnDBpediaSPARQLEndpointHelper(req, 2);
		}
	}

	@Test
	public void performCardinalityRequestWithBGPOnDBpediaSPARQLEndpoint()
			throws FederationAccessException
	{
		if ( ! skipLiveWebTests ) {
			// setting up
			final Node s = NodeFactory.createURI("http://dbpedia.org/resource/Toronto");
			final Node p = NodeFactory.createURI("http://xmlns.com/foaf/0.1/name");
			final Node o = NodeFactory.createVariable("o");
			final TriplePattern tp = new TriplePatternImpl(s,p,o);
			final SPARQLRequest req = new BGPRequestImpl( new BGPImpl(tp) );

			performCardinalityRequestOnDBpediaSPARQLEndpointHelper(req, 2);
		}
	}


	// ------------ helper code ------------

	protected void performCardinalityRequestOnDBpediaSPARQLEndpointHelper(
			final SPARQLRequest req,
			final int expectedCardinality ) throws FederationAccessException
	{
		final SPARQLEndpoint fm = new SPARQLEndpointForTest("http://dbpedia.org/sparql");

		final SPARQLRequestProcessor reqProc = new SPARQLRequestProcessorImpl();
		final TPFRequestProcessor reqProcTPF = new TPFRequestProcessor() {
			@Override public TPFResponse performRequest(TPFRequest req, BRTPFServer fm) { return null; }
			@Override public TPFResponse performRequest(TPFRequest req, TPFServer fm) { return null; }
		};
		final BRTPFRequestProcessor reqProcBRTPF = new BRTPFRequestProcessor() {
			@Override public TPFResponse performRequest(BRTPFRequest req, BRTPFServer fm) { return null; }
		};
		final Neo4jRequestProcessor reqProcNeo4j = new Neo4jRequestProcessor() {
			@Override public StringResponse performRequest(Neo4jRequest req, Neo4jServer fm) { return null; }
		};

		final FederationAccessManager mgr = new BlockingFederationAccessManagerImpl(reqProc, reqProcTPF, reqProcBRTPF, reqProcNeo4j);

		// executing the tested method
		final CardinalityResponse resp = mgr.performCardinalityRequest(req, fm);

		// checking
		assertEquals( fm, resp.getFederationMember() );

		assertEquals( expectedCardinality, resp.getCardinality() );
	}

}
