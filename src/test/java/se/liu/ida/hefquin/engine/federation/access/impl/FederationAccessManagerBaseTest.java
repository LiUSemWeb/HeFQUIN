package se.liu.ida.hefquin.engine.federation.access.impl;

import static org.junit.Assert.assertEquals;

import org.apache.jena.query.QueryFactory;
import org.junit.Test;

import se.liu.ida.hefquin.engine.EngineTestBase;
import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.TPFServer;
import se.liu.ida.hefquin.engine.federation.access.BRTPFRequest;
import se.liu.ida.hefquin.engine.federation.access.CardinalityResponse;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.federation.access.TPFRequest;
import se.liu.ida.hefquin.engine.federation.access.TPFResponse;
import se.liu.ida.hefquin.engine.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.BRTPFRequestProcessor;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.SPARQLRequestProcessor;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.SPARQLRequestProcessorImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.TPFRequestProcessor;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.query.impl.SPARQLGraphPatternImpl;

public class FederationAccessManagerBaseTest extends EngineTestBase
{
	@Test
	public void testDBpediaWithPattern() {
		if ( ! skipLiveWebTests ) {
			// setting up
			final String queryString = "SELECT * WHERE { <http://dbpedia.org/resource/Toronto> <http://xmlns.com/foaf/0.1/name> ?o }";
			final SPARQLGraphPattern pattern = new SPARQLGraphPatternImpl( QueryFactory.create(queryString).getQueryPattern() );
			final SPARQLRequest req = new SPARQLRequestImpl(pattern);

			final SPARQLEndpoint fm = new SPARQLEndpointForTest("http://dbpedia.org/sparql");

			final SPARQLRequestProcessor recProc = new SPARQLRequestProcessorImpl();
			final TPFRequestProcessor reqProcTPF = new TPFRequestProcessor() {
				@Override public TPFResponse performRequest(TPFRequest req, BRTPFServer fm) { return null; }
				@Override public TPFResponse performRequest(TPFRequest req, TPFServer fm) { return null; }
			};
			final BRTPFRequestProcessor reqProcBRTPF = new BRTPFRequestProcessor() {
				@Override public TPFResponse performRequest(BRTPFRequest req, BRTPFServer fm) { return null; }
			};

			final FederationAccessManager mgr = new BlockingFederationAccessManagerImpl(recProc, reqProcTPF, reqProcBRTPF);

			// executing the tested method
			final CardinalityResponse resp = mgr.performCardinalityRequest(req, fm);

			// checking
			assertEquals( fm, resp.getFederationMember() );

			assertEquals( 2, resp.getCardinality() );
		}
	}

}
