package se.liu.ida.hefquin.engine.federation.access.impl;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.jena.graph.NodeFactory;
import org.junit.Test;

import se.liu.ida.hefquin.engine.EngineTestBase;
import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.Neo4jServer;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.TPFServer;
import se.liu.ida.hefquin.engine.federation.access.BRTPFRequest;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessException;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.access.Neo4jRequest;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.engine.federation.access.StringResponse;
import se.liu.ida.hefquin.engine.federation.access.TPFRequest;
import se.liu.ida.hefquin.engine.federation.access.TPFResponse;
import se.liu.ida.hefquin.engine.federation.access.impl.req.TPFRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.BRTPFRequestProcessor;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.Neo4jRequestProcessor;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.SPARQLRequestProcessor;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.TPFRequestProcessor;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.TriplePatternImpl;

public class FederationAccessManagerWithCacheTest extends EngineTestBase
{
	//protected static boolean PRINT_TIME = false; protected static final long SLEEP_MILLIES = 0L;
	protected static boolean PRINT_TIME = true;  protected static final long SLEEP_MILLIES = 100L;
	
	@Test
	public void twoRequestsInSequence()
			throws FederationAccessException, InterruptedException, ExecutionException
	{
		final TriplePattern tp = new TriplePatternImpl( NodeFactory.createBlankNode(),
		                                                NodeFactory.createBlankNode(),
		                                                NodeFactory.createBlankNode() );
		final TPFRequest req1 = new TPFRequestImpl(tp, 0);
		final TPFServer fm1 = new TPFServerForTest();
		final TPFServer fm2 = new TPFServerForTest();

		final FederationAccessManager AsyncfedAccManObj = createFedAccessMgr(SLEEP_MILLIES);
		FederationAccessManagerWithCache fedAccessMgr = new FederationAccessManagerWithCache(AsyncfedAccManObj);


		final long startTime = new Date().getTime();

		final CompletableFuture<TPFResponse> fr1 = fedAccessMgr.issueRequest(req1, fm1);
		final TPFResponse r1 = fr1.get();

		final CompletableFuture<TPFResponse> fr2 = fedAccessMgr.issueRequest(req1, fm2);
		final TPFResponse r2 = fr2.get();

		final long endTime = new Date().getTime();
		if ( PRINT_TIME ) System.out.println( "twoRequestsInSequence \t milliseconds passed: " + (endTime - startTime) );

		assertEquals( r1, r2 );
		assertEquals( r1, null );
		assertEquals( r2, null );
	}

	@Test
	public void twoRequestsInParallel()
			throws FederationAccessException, InterruptedException, ExecutionException
	{
		final TriplePattern tp = new TriplePatternImpl( NodeFactory.createBlankNode(),
		                                                NodeFactory.createBlankNode(),
		                                                NodeFactory.createBlankNode() );
		final TPFRequest req1 = new TPFRequestImpl(tp, 0);
		final TPFServer fm1 = new TPFServerForTest();
		final TPFServer fm2 = new TPFServerForTest();

		final FederationAccessManager AsyncfedAccManObj = createFedAccessMgr(SLEEP_MILLIES);
		FederationAccessManagerWithCache fedAccessMgr = new FederationAccessManagerWithCache(AsyncfedAccManObj);


		final long startTime = new Date().getTime();

		final CompletableFuture<TPFResponse> fr1 = fedAccessMgr.issueRequest(req1, fm1);
		final CompletableFuture<TPFResponse> fr2 = fedAccessMgr.issueRequest(req1, fm2);

		final TPFResponse r1 = fr1.get();
		final TPFResponse r2 = fr2.get();

		final long endTime = new Date().getTime();
		if ( PRINT_TIME ) System.out.println( "twoRequestsInParallel \t milliseconds passed: " + (endTime - startTime) );

		assertEquals( r1, r2 );
		assertEquals( r1, null );
		assertEquals( r2, null );
	}

	@Test
	public void manyRequestsInParallel()
			throws FederationAccessException, InterruptedException, ExecutionException
	{
		final int n = 10;
		final TriplePattern tp = new TriplePatternImpl( NodeFactory.createBlankNode(),
		                                                NodeFactory.createBlankNode(),
		                                                NodeFactory.createBlankNode() );
		final TPFRequest[] reqs = new TPFRequest[n];
		final TPFServer[] fms = new TPFServer[n];
		for ( int i = 0; i < n; ++i ) {
			reqs[i] = new TPFRequestImpl(tp, 0);
			fms[i] = new TPFServerForTest();
		}

		final FederationAccessManager AsyncfedAccManObj = createFedAccessMgr(SLEEP_MILLIES);
		FederationAccessManagerWithCache fedAccessMgr = new FederationAccessManagerWithCache(AsyncfedAccManObj);


		@SuppressWarnings("unchecked")
		final CompletableFuture<TPFResponse>[] futures = new CompletableFuture[n];

		final long startTime = new Date().getTime();

		for ( int i = 0; i < n; ++i ) {
			futures[i] = fedAccessMgr.issueRequest(reqs[i], fms[i]);
		}

		for ( int i = 0; i < n; ++i ) {
			futures[i].get();
		}

		final long endTime = new Date().getTime();
		if ( PRINT_TIME ) System.out.println( "manyRequestsInParallel \t milliseconds passed: " + (endTime - startTime) );
	}

	// ------------ helper code ------------

	protected FederationAccessManager createFedAccessMgr( final long sleepMillis ) {
		final SPARQLRequestProcessor reqProc = new MySPARQLRequestProcessor(sleepMillis);
		final TPFRequestProcessor reqProcTPF = new MyTPFRequestProcessor(sleepMillis);
		final BRTPFRequestProcessor reqProcBRTPF = new BRTPFRequestProcessor() {
			@Override public TPFResponse performRequest(BRTPFRequest req, BRTPFServer fm) { return null; }
		};
		final Neo4jRequestProcessor reqProcNeo4j = new Neo4jRequestProcessor() {
			@Override public StringResponse performRequest(Neo4jRequest req, Neo4jServer fm) { return null; }
		};

		return new AsyncFederationAccessManagerImpl(reqProc, reqProcTPF, reqProcBRTPF, reqProcNeo4j);
	}

	protected static class FakeRequestProcessorBase
	{
		protected final long sleepMillis;

		public FakeRequestProcessorBase( final long sleepMillis ) {
			this.sleepMillis = sleepMillis;
		}

		protected void sleep() {
			if ( sleepMillis > 0L ) {
				try {
					Thread.sleep(sleepMillis);
				} catch ( final InterruptedException e ) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	protected static class MySPARQLRequestProcessor extends FakeRequestProcessorBase
	                                                implements SPARQLRequestProcessor
	{
		public MySPARQLRequestProcessor( final long sleepMillis ) { super(sleepMillis); }

		@Override
		public SolMapsResponse performRequest(SPARQLRequest req, SPARQLEndpoint fm) {
			sleep();
			return null;
		}
	}

	protected static class MyTPFRequestProcessor extends FakeRequestProcessorBase
	                                             implements TPFRequestProcessor
	{
		public MyTPFRequestProcessor( final long sleepMillis ) { super(sleepMillis); }

		@Override
		public TPFResponse performRequest(TPFRequest req, TPFServer fm) {
			sleep(); return null;
		}

		@Override
		public TPFResponse performRequest(TPFRequest req, BRTPFServer fm) {
			sleep(); return null;
		}
	}
	
}
