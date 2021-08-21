package se.liu.ida.hefquin.engine.federation.access.impl;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.QueryFactory;
import org.junit.Test;

import se.liu.ida.hefquin.engine.EngineTestBase;
import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.Neo4jServer;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.TPFServer;
import se.liu.ida.hefquin.engine.federation.access.BRTPFRequest;
import se.liu.ida.hefquin.engine.federation.access.CardinalityResponse;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessException;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.access.Neo4jRequest;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.engine.federation.access.StringResponse;
import se.liu.ida.hefquin.engine.federation.access.TPFRequest;
import se.liu.ida.hefquin.engine.federation.access.TPFResponse;
import se.liu.ida.hefquin.engine.federation.access.impl.req.BGPRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.req.TPFRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.req.TriplePatternRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.BRTPFRequestProcessor;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.Neo4jRequestProcessor;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.SPARQLRequestProcessor;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.SPARQLRequestProcessorImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.TPFRequestProcessor;
import se.liu.ida.hefquin.engine.federation.access.impl.response.SolMapsResponseImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.response.TPFResponseImpl;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.BGPImpl;
import se.liu.ida.hefquin.engine.query.impl.SPARQLGraphPatternImpl;
import se.liu.ida.hefquin.engine.query.impl.TriplePatternImpl;

public class FederationAccessManagerBase1Test extends EngineTestBase
{
	protected static boolean PRINT_TIME = false; protected static final long SLEEP_MILLIES = 0L;
	//protected static boolean PRINT_TIME = true;  protected static final long SLEEP_MILLIES = 100L;

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

	@Test
	public void performCardinalityRequestSPARQL()
			throws FederationAccessException, InterruptedException, ExecutionException
	{
		final TriplePattern tp = new TriplePatternImpl( NodeFactory.createBlankNode(),
		                                                NodeFactory.createBlankNode(),
		                                                NodeFactory.createBlankNode() );
		final SPARQLRequest req = new SPARQLRequestImpl(tp);
		final SPARQLEndpoint fm = new SPARQLEndpointForTest();

		final int card = 42;
		final FederationAccessManager fedAccessMgr = createMyFedAccessMgr(card);

		final CardinalityResponse r = fedAccessMgr.issueCardinalityRequest(req, fm).get();

		assertEquals( fm, r.getFederationMember() );
		assertEquals( card, r.getCardinality() );
	}

	@Test
	public void performCardinalityRequestTPF()
			throws FederationAccessException, InterruptedException, ExecutionException
	{
		final TriplePattern tp = new TriplePatternImpl( NodeFactory.createBlankNode(),
		                                                NodeFactory.createBlankNode(),
		                                                NodeFactory.createBlankNode() );
		final TPFRequest req = new TPFRequestImpl(tp, 0);
		final TPFServer fm = new TPFServerForTest();

		final int card = 42;
		final FederationAccessManager fedAccessMgr = createMyFedAccessMgr(card);

		final CardinalityResponse r = fedAccessMgr.issueCardinalityRequest(req, fm).get();

		assertEquals( fm, r.getFederationMember() );
		assertEquals( card, r.getCardinality() );
	}

	@Test
	public void twoCardinalityRequestsInParallel()
			throws FederationAccessException, InterruptedException, ExecutionException
	{
		final TriplePattern tp = new TriplePatternImpl( NodeFactory.createBlankNode(),
		                                                NodeFactory.createBlankNode(),
		                                                NodeFactory.createBlankNode() );
		final TPFRequest req1 = new TPFRequestImpl(tp, 0);
		final TPFRequest req2 = new TPFRequestImpl(tp, 0);
		final TPFServer fm1 = new TPFServerForTest();
		final TPFServer fm2 = new TPFServerForTest();

		final int card = 42;
		final FederationAccessManager fedAccessMgr = createMyFedAccessMgr(card);

		final long startTime = new Date().getTime();

		final CompletableFuture<CardinalityResponse> fr1 = fedAccessMgr.issueCardinalityRequest(req1, fm1);
		final CompletableFuture<CardinalityResponse> fr2 = fedAccessMgr.issueCardinalityRequest(req2, fm2);

		final CardinalityResponse r1 = fr1.get();
		final CardinalityResponse r2 = fr2.get();

		final long endTime = new Date().getTime();
		if ( PRINT_TIME ) System.out.println( "twoCardinalityRequestsInParallel \t milliseconds passed: " + (endTime - startTime) );

		assertEquals( fm1, r1.getFederationMember() );
		assertEquals( fm2, r2.getFederationMember() );
		assertEquals( card, r1.getCardinality() );
		assertEquals( card, r2.getCardinality() );
	}

	@Test
	public void twoCardinalityRequestsInSequence()
			throws FederationAccessException, InterruptedException, ExecutionException
	{
		final TriplePattern tp = new TriplePatternImpl( NodeFactory.createBlankNode(),
		                                                NodeFactory.createBlankNode(),
		                                                NodeFactory.createBlankNode() );
		final TPFRequest req1 = new TPFRequestImpl(tp, 0);
		final TPFRequest req2 = new TPFRequestImpl(tp, 0);
		final TPFServer fm1 = new TPFServerForTest();
		final TPFServer fm2 = new TPFServerForTest();

		final int card = 42;
		final FederationAccessManager fedAccessMgr = createMyFedAccessMgr(card);

		final long startTime = new Date().getTime();

		final CompletableFuture<CardinalityResponse> fr1 = fedAccessMgr.issueCardinalityRequest(req1, fm1);
		final CardinalityResponse r1 = fr1.get();

		final CompletableFuture<CardinalityResponse> fr2 = fedAccessMgr.issueCardinalityRequest(req2, fm2);
		final CardinalityResponse r2 = fr2.get();

		final long endTime = new Date().getTime();
		if ( PRINT_TIME ) System.out.println( "twoCardinalityRequestsInSequence \t milliseconds passed: " + (endTime - startTime) );

		assertEquals( fm1, r1.getFederationMember() );
		assertEquals( fm2, r2.getFederationMember() );
		assertEquals( card, r1.getCardinality() );
		assertEquals( card, r2.getCardinality() );
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
		final CompletableFuture<CardinalityResponse> futureResp = mgr.issueCardinalityRequest(req, fm);

		final CardinalityResponse response;
		try {
			response = futureResp.get();
		} catch (final InterruptedException | ExecutionException e) {
			throw new FederationAccessException("Getting the response caused an exception.", e, req, fm);
		} 

		// checking the response
		assertEquals( fm, response.getFederationMember() );
		assertEquals( expectedCardinality, response.getCardinality() );
	}

	protected FederationAccessManager createMyFedAccessMgr( final int card ) {
		return new MyFederationAccessManager(Integer.valueOf(card), SLEEP_MILLIES);
	}

	protected class MyFederationAccessManager extends FederationAccessManagerBase1
	{
		protected final Integer card;
		protected final long sleepMillis;

		public MyFederationAccessManager( final Integer card, final long sleepMillis ) {
			this.card = card;
			this.sleepMillis = sleepMillis;
		}

		@Override
		public CompletableFuture<SolMapsResponse> issueRequest( final SPARQLRequest req,
		                                                        final SPARQLEndpoint fm ) {
			final Node countNode = NodeFactory.createLiteralByValue(card, XSDDatatype.XSDint);
			final SolutionMapping sm = SolutionMappingUtils.createSolutionMapping( countVar, countNode);
			final SolMapsResponse r = new SolMapsResponseImpl( Arrays.asList(sm), fm, req, new Date() );

			return CompletableFuture.supplyAsync( () -> {
				if ( sleepMillis > 0L ) {
					try {
						Thread.sleep(sleepMillis);
					} catch ( final InterruptedException e ) {
						throw new RuntimeException(e);
					}
				}

				return r;
			});
		}

		@Override
		public CompletableFuture<TPFResponse> issueRequest( final TPFRequest req,
		                                                    final TPFServer fm ) {
			return createFutureTPFResponse(fm, req);
		}

		@Override
		public CompletableFuture<TPFResponse> issueRequest( final TPFRequest req,
		                                                    final BRTPFServer fm ) {
			return createFutureTPFResponse(fm, req);
		}

		@Override
		public CompletableFuture<TPFResponse> issueRequest( final BRTPFRequest req,
		                                                    final BRTPFServer fm ) {
			return createFutureTPFResponse(fm, req);
		}

		@Override
		public CompletableFuture<StringResponse> issueRequest( final Neo4jRequest req,
		                                                       final Neo4jServer fm ) {
			// TODO Auto-generated method stub
			return null;
		}

		protected CompletableFuture<TPFResponse> createFutureTPFResponse(
				final FederationMember fm, final DataRetrievalRequest req )
		{
			final TPFResponse r = new TPFResponseImpl( Collections.emptyList(),
			                                           Collections.emptyList(),
			                                           fm, req,
			                                           new Date() ) {
				@Override
				public Integer getCardinalityEstimate() { return card; };
			};

			return CompletableFuture.supplyAsync( () -> {
				if ( sleepMillis > 0L ) {
					try {
						Thread.sleep(sleepMillis);
					} catch ( final InterruptedException e ) {
						throw new RuntimeException(e);
					}
				}

				return r;
			});
		}
	}

}
