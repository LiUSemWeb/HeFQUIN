package se.liu.ida.hefquin.federation.access.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.jena.graph.NodeFactory;
import org.junit.Test;

import se.liu.ida.hefquin.base.datastructures.Cache;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CachePolicies;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.base.query.impl.TriplePatternImpl;
import se.liu.ida.hefquin.federation.FederationTestBase;
import se.liu.ida.hefquin.federation.access.*;
import se.liu.ida.hefquin.federation.access.impl.cache.CacheLayer;
import se.liu.ida.hefquin.federation.access.impl.cache.PersistentCacheEntry;
import se.liu.ida.hefquin.federation.access.impl.cache.PersistentCacheKey;
import se.liu.ida.hefquin.federation.access.impl.cache.chroniclemap.ChronicleMapCache;
import se.liu.ida.hefquin.federation.access.impl.req.TPFRequestImpl;
import se.liu.ida.hefquin.federation.members.TPFServer;

public class FederationAccessManagerWithHierarchicalCacheTest extends FederationTestBase
{
	@Test
	public void twoRequestsInSequence()
			throws FederationAccessException, InterruptedException, ExecutionException, IOException
	{
		final TriplePattern tp = new TriplePatternImpl( NodeFactory.createURI("http://example.org/s"),
		                                                NodeFactory.createURI("http://example.org/p"),
		                                                NodeFactory.createURI("http://example.org/o") );
		final TPFRequest req1 = new TPFRequestImpl(tp);
		final TPFServer fm1 = new TPFServerForTest();
		final TPFServer fm2 = new TPFServerForTest();

		final ExecutorService execServiceForFedAccess = Executors.newFixedThreadPool(10);
		final FederationAccessManager fedAccessMgr = createFedAccessMgrForTests(execServiceForFedAccess, 5);

		final CompletableFuture<TPFResponse> fr1 = fedAccessMgr.issueRequest(req1, fm1);
		fr1.get();

		final CompletableFuture<TPFResponse> fr2 = fedAccessMgr.issueRequest(req1, fm2);
		fr2.get();

		fedAccessMgr.shutdown();
	}

	@Test
	public void twoRequestsInParallel()
			throws FederationAccessException, InterruptedException, ExecutionException, IOException
	{
		final TriplePattern tp = new TriplePatternImpl( NodeFactory.createURI("http://example.org/s"),
		                                                NodeFactory.createURI("http://example.org/p"),
		                                                NodeFactory.createURI("http://example.org/o") );
		final TPFRequest req1 = new TPFRequestImpl(tp);
		final TPFServer fm1 = new TPFServerForTest();
		final TPFServer fm2 = new TPFServerForTest();

		final ExecutorService execServiceForFedAccess = Executors.newFixedThreadPool(10);
		final FederationAccessManager fedAccessMgr = createFedAccessMgrForTests(execServiceForFedAccess, 5);

		final CompletableFuture<TPFResponse> fr1 = fedAccessMgr.issueRequest(req1, fm1);
		final CompletableFuture<TPFResponse> fr2 = fedAccessMgr.issueRequest(req1, fm2);

		fr1.get();
		fr2.get();

		fedAccessMgr.shutdown();
	}

	@Test
	public void manyRequestsInParallel()
			throws FederationAccessException, InterruptedException, ExecutionException, IOException
	{
		final int n = 10;
		final TriplePattern tp = new TriplePatternImpl( NodeFactory.createURI("http://example.org/s"),
		                                                NodeFactory.createURI("http://example.org/p"),
		                                                NodeFactory.createURI("http://example.org/o") );
		final TPFRequest[] reqs = new TPFRequest[n];
		final TPFServer[] fms = new TPFServer[n];
		for ( int i = 0; i < n; ++i ) {
			reqs[i] = new TPFRequestImpl(tp);
			fms[i] = new TPFServerForTest();
		}

		final ExecutorService execServiceForFedAccess = Executors.newFixedThreadPool(10);
		final FederationAccessManager fedAccessMgr = createFedAccessMgrForTests(execServiceForFedAccess, 5);

		@SuppressWarnings("unchecked")
		final CompletableFuture<TPFResponse>[] futures = new CompletableFuture[n];

		for ( int i = 0; i < n; ++i ) {
			futures[i] = fedAccessMgr.issueRequest(reqs[i], fms[i]);
		}

		for ( int i = 0; i < n; ++i ) {
			futures[i].get();
		}

		fedAccessMgr.shutdown();
	}


	// ------------ helper code ------------

	protected static FederationAccessManagerWithCache createFedAccessMgrForTests( final ExecutorService execServiceForFedAccess,
	                                                                              final int timeToLive ) throws IOException
	{
		final FederationAccessManager fedAccessMgr = AsyncFederationAccessManagerImplTest.createFedAccessMgrForTests(
			execServiceForFedAccess,
			timeToLive
		);
		final CachePolicies<PersistentCacheKey, CompletableFuture<? extends DataRetrievalResponse<?>>, PersistentCacheEntry> l1Policies =
				new FederationAccessManagerWithHierarchicalCache.DefaultHierarchicalMapCachePolicies(timeToLive);
		final CachePolicies<PersistentCacheKey, CompletableFuture<? extends DataRetrievalResponse<?>>, PersistentCacheEntry> l2Policies =
				new FederationAccessManagerWithHierarchicalCache.DefaultHierarchicalMapCachePolicies(timeToLive);
		final Cache<PersistentCacheKey, CompletableFuture<? extends DataRetrievalResponse<?>>> l1 =
				new CacheLayer<>(new HashMap<>(), 100, l1Policies);
		final Cache<PersistentCacheKey, CompletableFuture<? extends DataRetrievalResponse<?>>> l2 =
				new ChronicleMapCache(100, l2Policies);
		
		return new FederationAccessManagerWithHierarchicalCache(fedAccessMgr, l1, l2);
	}

}
