package se.liu.ida.hefquin.federation.access.impl;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import se.liu.ida.hefquin.engine.wrappers.graphql.query.GraphQLQuery;
import se.liu.ida.hefquin.engine.wrappers.graphql.query.impl.GraphQLQueryImpl;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.FederationTestBase;
import se.liu.ida.hefquin.federation.access.*;
import se.liu.ida.hefquin.federation.access.impl.req.BRTPFRequestImpl;
import se.liu.ida.hefquin.federation.access.impl.req.GraphQLRequestImpl;
import se.liu.ida.hefquin.federation.access.impl.req.RESTRequestImpl;
import se.liu.ida.hefquin.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.federation.access.impl.req.TPFRequestImpl;
import se.liu.ida.hefquin.federation.members.TPFServer;

import se.liu.ida.hefquin.federation.members.RESTEndpoint.Parameter;

public class FederationAccessManagerWithCacheTest extends FederationTestBase
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
		final TPFServer fm1 = new TPFServerForTest();
		final TPFServer fm2 = new TPFServerForTest();

		final FederationAccessManager fedAccessMgr = createFedAccessMgrForTests(execServiceForFedAccess, SLEEP_MILLIES);

		final long startTime = new Date().getTime();

		final CompletableFuture<TPFResponse> fr1 = fedAccessMgr.issueRequest(req1, fm1);
		fr1.get();

		final CompletableFuture<TPFResponse> fr2 = fedAccessMgr.issueRequest(req1, fm2);
		fr2.get();

		final long endTime = new Date().getTime();
		if ( PRINT_TIME ) System.out.println( "twoRequestsInSequence \t milliseconds passed: " + (endTime - startTime) );
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

		final FederationAccessManager fedAccessMgr = createFedAccessMgrForTests(execServiceForFedAccess, SLEEP_MILLIES);

		final long startTime = new Date().getTime();

		final CompletableFuture<TPFResponse> fr1 = fedAccessMgr.issueRequest(req1, fm1);
		final CompletableFuture<TPFResponse> fr2 = fedAccessMgr.issueRequest(req1, fm2);

		fr1.get();
		fr2.get();

		final long endTime = new Date().getTime();
		if ( PRINT_TIME ) System.out.println( "twoRequestsInParallel \t milliseconds passed: " + (endTime - startTime) );
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

	@Test
	public void tpfResponseCachingTest() throws InterruptedException, ExecutionException, FederationAccessException {
		final FederationAccessManager fedAccessMgr = createFedAccessMgrForTests(execServiceForFedAccess, SLEEP_MILLIES);

		final TriplePattern tp = new TriplePatternImpl( NodeFactory.createVariable("s"),
		                                                NodeFactory.createVariable("p"),
		                                                NodeFactory.createVariable("o") );
		final TPFRequest req = new TPFRequestImpl(tp);

		final FederationMember fm1 = new TPFServerForTest();
		final CompletableFuture<TPFResponse> fr1 = fedAccessMgr.issueRequest(req, fm1);
		final CompletableFuture<TPFResponse> fr2 = fedAccessMgr.issueRequest(req, fm1);

		assertEquals(fr1, fr2);

		final FederationMember fm2 = new BRTPFServerForTest();
		final CompletableFuture<TPFResponse> fr3 = fedAccessMgr.issueRequest(req, fm2);
		final CompletableFuture<TPFResponse> fr4 = fedAccessMgr.issueRequest(req, fm2);

		assertEquals(fr3, fr4);
	}

	@Test
	public void brTPFResponseCachingTest() throws InterruptedException, ExecutionException, FederationAccessException {
		final FederationAccessManager fedAccessMgr = createFedAccessMgrForTests(execServiceForFedAccess, SLEEP_MILLIES);

		final TriplePattern tp = new TriplePatternImpl( NodeFactory.createVariable("s"),
		                                                NodeFactory.createVariable("p"),
		                                                NodeFactory.createVariable("o") );
		final BRTPFRequest req = new BRTPFRequestImpl( tp, Set.of() );

		final FederationMember fm = new BRTPFServerForTest();
		final CompletableFuture<TPFResponse> fr1 = fedAccessMgr.issueRequest(req, fm);
		final CompletableFuture<TPFResponse> fr2 = fedAccessMgr.issueRequest(req, fm);

		assertEquals(fr1, fr2);
	}

	@Test
	public void solMapsResponseCachingTest() throws InterruptedException, ExecutionException, FederationAccessException {
		final FederationAccessManager fedAccessMgr = createFedAccessMgrForTests(execServiceForFedAccess, SLEEP_MILLIES);

		final TriplePattern tp = new TriplePatternImpl( NodeFactory.createVariable("s"),
		                                                NodeFactory.createVariable("p"),
		                                                NodeFactory.createVariable("o") );
		final SPARQLRequest req = new SPARQLRequestImpl(tp);

		final FederationMember fm = new SPARQLEndpointForTest();
		final CompletableFuture<SolMapsResponse> fr1 = fedAccessMgr.issueRequest(req, fm);
		final CompletableFuture<SolMapsResponse> fr2 = fedAccessMgr.issueRequest(req, fm);

		assertEquals(fr1, fr2);
	}

	@Test
	public void restResponseCachingTest() throws InterruptedException, ExecutionException, FederationAccessException {
		final FederationAccessManager fedAccessMgr = createFedAccessMgrForTests(execServiceForFedAccess, SLEEP_MILLIES);

		final String urlTemplate = "http://example.com/api";
		final Map<String, String> bindings = new HashMap<>();
		final RESTRequest req = new RESTRequestImpl(urlTemplate, bindings);

		final List<Parameter> params = new ArrayList<>();
		final FederationMember fm = new RESTEndpointForTest(urlTemplate, params);

		final CompletableFuture<StringResponse> fr1 = fedAccessMgr.issueRequest(req, fm);
		final CompletableFuture<StringResponse> fr2 = fedAccessMgr.issueRequest(req, fm);

		assertEquals(fr1, fr2);
	}

	@Test
	public void graphqlResponseCachingTest() throws InterruptedException, ExecutionException, FederationAccessException {
		final FederationAccessManager fedAccessMgr = createFedAccessMgrForTests(execServiceForFedAccess, SLEEP_MILLIES);

		final GraphQLQuery graphQLQuery = new GraphQLQueryImpl(new HashSet<>(), new HashSet<>());
		final GraphQLRequest req = new GraphQLRequestImpl(graphQLQuery);

		final FederationMember fm = new GraphQLEndpointForTest();
		final CompletableFuture<StringResponse> fr1 = fedAccessMgr.issueRequest(req, fm);
		final CompletableFuture<StringResponse> fr2 = fedAccessMgr.issueRequest(req, fm);

		assertEquals(fr1, fr2);
	}

	// ------------ helper code ------------

	protected static FederationAccessManagerWithCache createFedAccessMgrForTests( final ExecutorService execServiceForFedAccess,
	                                                                              final long sleepMillis ) {
		return new FederationAccessManagerWithCache(
				AsyncFederationAccessManagerImplTest.createFedAccessMgrForTests(execServiceForFedAccess, sleepMillis),
				10 );
	}
}
