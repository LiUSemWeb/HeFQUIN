package se.liu.ida.hefquin.federation.access.impl;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.jena.graph.NodeFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.base.query.impl.TriplePatternImpl;
import se.liu.ida.hefquin.federation.BRTPFServer;
import se.liu.ida.hefquin.federation.FederationTestBase;
import se.liu.ida.hefquin.federation.Neo4jServer;
import se.liu.ida.hefquin.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.federation.TPFServer;
import se.liu.ida.hefquin.federation.access.*;
import se.liu.ida.hefquin.federation.access.impl.req.TPFRequestImpl;
import se.liu.ida.hefquin.federation.access.impl.reqproc.BRTPFRequestProcessor;
import se.liu.ida.hefquin.federation.access.impl.reqproc.Neo4jRequestProcessor;
import se.liu.ida.hefquin.federation.access.impl.reqproc.SPARQLRequestProcessor;
import se.liu.ida.hefquin.federation.access.impl.reqproc.TPFRequestProcessor;
import se.liu.ida.hefquin.federation.access.impl.response.SolMapsResponseImpl;
import se.liu.ida.hefquin.federation.access.impl.response.TPFResponseImpl;

public class AsyncFederationAccessManagerImplTest extends FederationTestBase
{
	protected static boolean PRINT_TIME = false; protected static final long SLEEP_MILLIES = 0L;
	//protected static boolean PRINT_TIME = true;  protected static final long SLEEP_MILLIES = 100L;

	protected static ExecutorService execServiceForFedAccess;

	@BeforeClass
	public static void createExecService() {
		final int numberOfThreads = 10;
		execServiceForFedAccess = Executors.newFixedThreadPool(numberOfThreads);
	}

	@AfterClass
	public static void tearDownExecService() {
		execServiceForFedAccess.shutdownNow();
		try {
			execServiceForFedAccess.awaitTermination(500L, TimeUnit.MILLISECONDS);
		}
		catch ( final InterruptedException ex )  {
			System.err.println("Terminating the thread pool was interrupted." );
			ex.printStackTrace();
		}
	}

	@Test
	public void twoRequestsInSequence()
			throws FederationAccessException, InterruptedException, ExecutionException
	{
		final TriplePattern tp = new TriplePatternImpl( NodeFactory.createBlankNode(),
		                                                NodeFactory.createBlankNode(),
		                                                NodeFactory.createBlankNode() );
		final TPFRequest req1 = new TPFRequestImpl(tp);
		final TPFRequest req2 = new TPFRequestImpl(tp);
		final TPFServer fm1 = new TPFServerForTest();
		final TPFServer fm2 = new TPFServerForTest();

		final FederationAccessManager fedAccessMgr = createFedAccessMgrForTests(execServiceForFedAccess, SLEEP_MILLIES);

		final long startTime = new Date().getTime();

		final CompletableFuture<TPFResponse> fr1 = fedAccessMgr.issueRequest(req1, fm1);
		final TPFResponse r1 = fr1.get();

		final CompletableFuture<TPFResponse> fr2 = fedAccessMgr.issueRequest(req2, fm2);
		final TPFResponse r2 = fr2.get();

		final long endTime = new Date().getTime();
		if ( PRINT_TIME ) System.out.println( "twoRequestsInSequence \t milliseconds passed: " + (endTime - startTime) );

		assertEquals( req1, r1.getRequest() );
		assertEquals( req2, r2.getRequest() );
		assertEquals( fm1, r1.getFederationMember() );
		assertEquals( fm2, r2.getFederationMember() );
	}

	@Test
	public void twoRequestsInParallel()
			throws FederationAccessException, InterruptedException, ExecutionException
	{
		final TriplePattern tp = new TriplePatternImpl( NodeFactory.createBlankNode(),
		                                                NodeFactory.createBlankNode(),
		                                                NodeFactory.createBlankNode() );
		final TPFRequest req1 = new TPFRequestImpl(tp);
		final TPFRequest req2 = new TPFRequestImpl(tp);
		final TPFServer fm1 = new TPFServerForTest();
		final TPFServer fm2 = new TPFServerForTest();

		final FederationAccessManager fedAccessMgr = createFedAccessMgrForTests(execServiceForFedAccess, SLEEP_MILLIES);

		final long startTime = new Date().getTime();

		final CompletableFuture<TPFResponse> fr1 = fedAccessMgr.issueRequest(req1, fm1);
		final CompletableFuture<TPFResponse> fr2 = fedAccessMgr.issueRequest(req2, fm2);

		final TPFResponse r1 = fr1.get();
		final TPFResponse r2 = fr2.get();

		final long endTime = new Date().getTime();
		if ( PRINT_TIME ) System.out.println( "twoRequestsInParallel \t milliseconds passed: " + (endTime - startTime) );

		assertEquals( req1, r1.getRequest() );
		assertEquals( req2, r2.getRequest() );
		assertEquals( fm1, r1.getFederationMember() );
		assertEquals( fm2, r2.getFederationMember() );
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
			reqs[i] = new TPFRequestImpl(tp);
			fms[i] = new TPFServerForTest();
		}

		final FederationAccessManager fedAccessMgr = createFedAccessMgrForTests(execServiceForFedAccess, SLEEP_MILLIES);

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

	public static FederationAccessManager createFedAccessMgrForTests( final ExecutorService execServiceForFedAccess,
	                                                                  final long sleepMillis ) {
		final SPARQLRequestProcessor reqProc = new MySPARQLRequestProcessor(sleepMillis);
		final TPFRequestProcessor reqProcTPF = new MyTPFRequestProcessor(sleepMillis);
		final BRTPFRequestProcessor reqProcBRTPF = new BRTPFRequestProcessor() {
			@Override public TPFResponse performRequest(BRTPFRequest req, BRTPFServer fm) { return null; }
		};
		final Neo4jRequestProcessor reqProcNeo4j = new Neo4jRequestProcessor() {
			@Override public RecordsResponse performRequest(Neo4jRequest req, Neo4jServer fm) { return null; }
		};

		return new AsyncFederationAccessManagerImpl(execServiceForFedAccess, reqProc, reqProcTPF, reqProcBRTPF, reqProcNeo4j);
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
			return new SolMapsResponseImpl( new ArrayList<>(), fm, req, new Date() );
		}
	}

	protected static class MyTPFRequestProcessor extends FakeRequestProcessorBase
	                                             implements TPFRequestProcessor
	{
		public MyTPFRequestProcessor( final long sleepMillis ) { super(sleepMillis); }

		@Override
		public TPFResponse performRequest(TPFRequest req, TPFServer fm) {
			sleep();
			return new TPFResponseImpl( new ArrayList<>(), new ArrayList<>(), "dummy next page", fm, req, new Date() );
		}

		@Override
		public TPFResponse performRequest(TPFRequest req, BRTPFServer fm) {
			sleep();
			return new TPFResponseImpl( new ArrayList<>(), new ArrayList<>(), "dummy next page", fm, req, new Date() );
		}
	}

}
