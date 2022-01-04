package se.liu.ida.hefquin.engine.federation.access.impl;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.jena.graph.NodeFactory;
import org.junit.Test;

import se.liu.ida.hefquin.engine.EngineTestBase;
import se.liu.ida.hefquin.engine.federation.TPFServer;
import se.liu.ida.hefquin.engine.federation.access.*;
import se.liu.ida.hefquin.engine.federation.access.impl.req.TPFRequestImpl;
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
		final TPFRequest req1 = new TPFRequestImpl(tp);
		final TPFServer fm1 = new TPFServerForTest();
		final TPFServer fm2 = new TPFServerForTest();

		final FederationAccessManager fedAccessMgr = createFedAccessMgrForTests(SLEEP_MILLIES);

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
		final TPFRequest req1 = new TPFRequestImpl(tp);
		final TPFServer fm1 = new TPFServerForTest();
		final TPFServer fm2 = new TPFServerForTest();

		final FederationAccessManager fedAccessMgr = createFedAccessMgrForTests(SLEEP_MILLIES);

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
			reqs[i] = new TPFRequestImpl(tp);
			fms[i] = new TPFServerForTest();
		}

		final FederationAccessManager fedAccessMgr = createFedAccessMgrForTests(SLEEP_MILLIES);

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

	protected static FederationAccessManagerWithCache createFedAccessMgrForTests( final long sleepMillis ) {
		return new FederationAccessManagerWithCache(
				AsyncFederationAccessManagerImplTest.createFedAccessMgrForTests(sleepMillis),
				10 );
	}

}
