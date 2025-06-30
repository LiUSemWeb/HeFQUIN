package se.liu.ida.hefquin.federation.access.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.VocabularyMapping;
import se.liu.ida.hefquin.base.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.base.query.impl.TriplePatternImpl;
import se.liu.ida.hefquin.federation.BRTPFServer;
import se.liu.ida.hefquin.federation.FederationTestBase;
import se.liu.ida.hefquin.federation.Neo4jServer;
import se.liu.ida.hefquin.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.federation.TPFServer;
import se.liu.ida.hefquin.federation.access.*;
import se.liu.ida.hefquin.federation.access.impl.iface.BRTPFInterfaceImpl;
import se.liu.ida.hefquin.federation.access.impl.iface.TPFInterfaceImpl;
import se.liu.ida.hefquin.federation.access.impl.req.BRTPFRequestImpl;
import se.liu.ida.hefquin.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.federation.access.impl.req.TPFRequestImpl;
import se.liu.ida.hefquin.federation.access.impl.reqproc.BRTPFRequestProcessor;
import se.liu.ida.hefquin.federation.access.impl.reqproc.BRTPFRequestProcessorImpl;
import se.liu.ida.hefquin.federation.access.impl.reqproc.Neo4jRequestProcessor;
import se.liu.ida.hefquin.federation.access.impl.reqproc.SPARQLRequestProcessor;
import se.liu.ida.hefquin.federation.access.impl.reqproc.SPARQLRequestProcessorImpl;
import se.liu.ida.hefquin.federation.access.impl.reqproc.TPFRequestProcessor;
import se.liu.ida.hefquin.federation.access.impl.reqproc.TPFRequestProcessorImpl;
import se.liu.ida.hefquin.federation.access.impl.response.SolMapsResponseImpl;
import se.liu.ida.hefquin.federation.access.impl.response.TPFResponseImpl;

public class FederationAccessManagerWithChronicleMapCacheTest extends FederationTestBase
{
	protected static final long defaultTimeToSleep = 100L;
	protected static final long defaultTimeToLive = 30000; // 30s
	protected static final int defaultCacheCapacity = 100;
	protected static ExecutorService execServiceForFedAccess;
	final TriplePattern tp = new TriplePatternImpl( NodeFactory.createVariable( "s" ),
													NodeFactory.createVariable( "p" ),
													NodeFactory.createVariable( "o" ) );
	final SPARQLRequest sparqlReq = new SPARQLRequestImpl( tp );
	final TPFRequest tpfReq = new TPFRequestImpl( tp );
	final BRTPFRequest brtpfReq = new BRTPFRequestImpl( tp, Collections.emptySet() );

	@BeforeClass
	public static void createExecService() {
		final int numberOfThreads = 10;
		execServiceForFedAccess = Executors.newFixedThreadPool( numberOfThreads );
	}

	@AfterClass
	public static void tearDownExecService() {
		execServiceForFedAccess.shutdownNow();
		try {
			execServiceForFedAccess.awaitTermination( 500L, TimeUnit.MILLISECONDS );
		}
		catch ( final InterruptedException ex )  {
			System.err.println( "Terminating the thread pool was interrupted." );
			ex.printStackTrace();
		}
	}

	// SPARQL request tests

	@Test
	public void sparqlNoCacheHit() throws FederationAccessException, InterruptedException, ExecutionException, IOException {
		final SPARQLEndpoint fm = new SPARQLEndpointForTest();
		final FederationAccessManagerWithChronicleMapCache fedAccessMgr = createFedAccessMgrForTests( 42 );
		fedAccessMgr.clearCardinalityCache();

		// issue reuest (not cached)
		final long startTime = Instant.now().toEpochMilli();
		final CardinalityResponse r = fedAccessMgr.issueCardinalityRequest( sparqlReq, fm ).get();
		final long duration = Instant.now().toEpochMilli() - startTime;

		// assert correct cardinality
		assertEquals( 42, r.getCardinality() );
		// assert that duration is greater than SLEEP_MILLIS (since not cached)
		assertTrue( defaultTimeToSleep <= duration );
	}

	@Test
	public void sparqlCacheHitInMemory() throws FederationAccessException, InterruptedException, ExecutionException, IOException {
		final SPARQLEndpoint fm = new SPARQLEndpointForTest();
		final FederationAccessManagerWithChronicleMapCache fedAccessMgr = createFedAccessMgrForTests( 42 );
		fedAccessMgr.clearCardinalityCache();

		// make sure request is cached
		final CardinalityResponse r1 = fedAccessMgr.issueCardinalityRequest( sparqlReq, fm ).get();
		assertEquals( 42, r1.getCardinality() );

		// issue request again
		final long startTime = Instant.now().toEpochMilli();
		final CardinalityResponse r2 = fedAccessMgr.issueCardinalityRequest( sparqlReq, fm ).get();
		final long duration = Instant.now().toEpochMilli() - startTime;
		// assert correct cardinality
		assertEquals( 42, r2.getCardinality() );
		// assert that duration is less than SLEEP_MILLIS (since cached)
		assertTrue( defaultTimeToSleep > duration );
	}

	@Test
	public void sparqlCacheHitFromDisk() throws FederationAccessException, InterruptedException, ExecutionException, IOException {
		final SPARQLEndpoint fm = new SPARQLEndpointForTest();
		final FederationAccessManagerWithChronicleMapCache fedAccessMgr1 = createFedAccessMgrForTests( 42 );
		fedAccessMgr1.clearCardinalityCache();

		// make sure request is cached
		final CardinalityResponse r1 = fedAccessMgr1.issueCardinalityRequest( sparqlReq, fm ).get();
		assertEquals( 42, r1.getCardinality() );

		// create a new federation access manager (cache will be loaded from disk)
		final FederationAccessManagerWithChronicleMapCache fedAccessMgr2 = createFedAccessMgrForTests( -1 );
		final long startTime =  Instant.now().toEpochMilli();
		final CardinalityResponse r2 = fedAccessMgr2.issueCardinalityRequest( sparqlReq, fm ).get();
		final long duration =  Instant.now().toEpochMilli() - startTime;
		// assert correct cardinality
		assertEquals( 42, r2.getCardinality() );
		// assert that duration is less than SLEEP_MILLIS (since cached)
		assertTrue( defaultTimeToSleep > duration );
	}

	@Test
	public void sparqlSameRequestTwoFederationMembers() throws FederationAccessException, InterruptedException, ExecutionException, IOException {
		final SPARQLEndpoint fm1 = new SPARQLEndpointForTest( "http://example.org/sparql/1" );
		final SPARQLEndpoint fm2 = new SPARQLEndpointForTest( "http://example.org/sparql/2" );
		final FederationAccessManagerWithChronicleMapCache fedAccessMgr = createFedAccessMgrForTests( 42 );
		fedAccessMgr.clearCardinalityCache();

		// issue request against fm1 and fm2
		final CardinalityResponse r1 = fedAccessMgr.issueCardinalityRequest( sparqlReq, fm1 ).get();
		final CardinalityResponse r2 = fedAccessMgr.issueCardinalityRequest( sparqlReq, fm2 ).get();
		assertEquals( 42, r1.getCardinality() );
		assertEquals( 43, r2.getCardinality() );

		// create a new federation access manager
		final FederationAccessManagerWithChronicleMapCache fedAccessMgr2 = createFedAccessMgrForTests( -1 );
		final long startTime3 = new Date().getTime();
		// issue request against fm2 and fm1
		final CardinalityResponse r3 = fedAccessMgr2.issueCardinalityRequest( sparqlReq, fm2 ).get();
		final CardinalityResponse r4 = fedAccessMgr2.issueCardinalityRequest( sparqlReq, fm1 ).get();
		final long duration = new Date().getTime() - startTime3;
		assertEquals( 43, r3.getCardinality() );
		assertEquals( 42, r4.getCardinality() );
		assertTrue( defaultTimeToSleep > duration );
	}

	@Test
	public void sparqlTwoRequestsAsync() throws FederationAccessException, InterruptedException, ExecutionException, IOException {
		final SPARQLEndpoint fm = new SPARQLEndpointForTest();
		final FederationAccessManagerWithChronicleMapCache fedAccessMgr = createFedAccessMgrForTests( 42 );
		fedAccessMgr.clearCardinalityCache();

		// issue request against fm
		fedAccessMgr.issueCardinalityRequest( sparqlReq, fm ).get();
		// add small delay
		Thread.sleep(defaultTimeToSleep / 2);

		// issue request against again before it is finished
		// Note: The request should get the same value as the first request
		final CardinalityResponse r = fedAccessMgr.issueCardinalityRequest( sparqlReq, fm ).get();
		assertEquals( 42, r.getCardinality() );
	}

	@Test
	public void sparqlCacheInvalidationFromMemory() throws FederationAccessException, InterruptedException, ExecutionException, IOException {
		final SPARQLEndpoint fm = new SPARQLEndpointForTest();
		final FederationAccessManagerWithChronicleMapCache fedAccessMgr = createFedAccessMgrForTests( 42, 0, 100 );
		fedAccessMgr.clearCardinalityCache();

		// issue request
		fedAccessMgr.issueCardinalityRequest( sparqlReq, fm ).get();
		// wait for entry to become stale
		Thread.sleep(200);

		// issue request again
		final CardinalityResponse r = fedAccessMgr.issueCardinalityRequest( sparqlReq, fm ).get();
		// verify that new value is returned
		assertEquals( 43, r.getCardinality() );
	}

	@Test
	public void sparqlCacheInvalidationFromDisk() throws FederationAccessException, InterruptedException, ExecutionException, IOException {
		final SPARQLEndpoint fm = new SPARQLEndpointForTest();
		final FederationAccessManagerWithChronicleMapCache fedAccessMgr1 = createFedAccessMgrForTests( 42, 0, 100 );
		fedAccessMgr1.clearCardinalityCache();
		fedAccessMgr1.issueCardinalityRequest( sparqlReq, fm ).get();
		// wait for entry to become stale
		Thread.sleep(200);

		// load federation access manager from disk
		final FederationAccessManagerWithChronicleMapCache fedAccessMgr2 = createFedAccessMgrForTests( 52, 0, 100 );

		// repeat request
		final CardinalityResponse r2 = fedAccessMgr2.issueCardinalityRequest( sparqlReq, fm ).get();
		// verify that stale value is not returned
		assertEquals( 52, r2.getCardinality() );
	}

	// TPF request against TPF server tests

	@Test
	public void tpfNoCacheHit() throws FederationAccessException, InterruptedException, ExecutionException, IOException {
		final TPFServer fm = new MyTPFServerForTest();
		final FederationAccessManagerWithChronicleMapCache fedAccessMgr = createFedAccessMgrForTests( 42 );
		fedAccessMgr.clearCardinalityCache();

		// issue reuest (not cached)
		final long startTime = Instant.now().toEpochMilli();
		final CardinalityResponse r = fedAccessMgr.issueCardinalityRequest( tpfReq, fm ).get();
		final long duration = Instant.now().toEpochMilli() - startTime;

		// assert correct cardinality
		assertEquals( 42, r.getCardinality() );
		// assert that duration is greater than SLEEP_MILLIS (since not cached)
		assertTrue( defaultTimeToSleep <= duration );
	}

	@Test
	public void tpfCacheHitInMemory() throws FederationAccessException, InterruptedException, ExecutionException, IOException {
		final TPFServer fm = new MyTPFServerForTest();
		final FederationAccessManagerWithChronicleMapCache fedAccessMgr = createFedAccessMgrForTests( 42 );
		fedAccessMgr.clearCardinalityCache();

		// make sure request is cached
		final CardinalityResponse r1 = fedAccessMgr.issueCardinalityRequest( tpfReq, fm ).get();
		assertEquals( 42, r1.getCardinality() );

		// issue request again
		final long startTime = Instant.now().toEpochMilli();
		final CardinalityResponse r2 = fedAccessMgr.issueCardinalityRequest( tpfReq, fm ).get();
		final long duration = Instant.now().toEpochMilli() - startTime;
		// assert correct cardinality
		assertEquals( 42, r2.getCardinality() );
		// assert that duration is less than SLEEP_MILLIS (since cached)
		assertTrue( defaultTimeToSleep > duration );
	}

	@Test
	public void tpfCacheHitFromDisk() throws FederationAccessException, InterruptedException, ExecutionException, IOException {
		final TPFServer fm = new MyTPFServerForTest();
		final FederationAccessManagerWithChronicleMapCache fedAccessMgr1 = createFedAccessMgrForTests( 42 );
		fedAccessMgr1.clearCardinalityCache();

		// make sure request is cached
		final CardinalityResponse r1 = fedAccessMgr1.issueCardinalityRequest( tpfReq, fm ).get();
		assertEquals( 42, r1.getCardinality() );

		// create a new federation access manager (cache will be loaded from disk)
		final FederationAccessManagerWithChronicleMapCache fedAccessMgr2 = createFedAccessMgrForTests( -1 );
		final long startTime =  Instant.now().toEpochMilli();
		final CardinalityResponse r2 = fedAccessMgr2.issueCardinalityRequest( tpfReq, fm ).get();
		final long duration =  Instant.now().toEpochMilli() - startTime;
		// assert correct cardinality
		assertEquals( 42, r2.getCardinality() );
		// assert that duration is less than SLEEP_MILLIS (since cached)
		assertTrue( defaultTimeToSleep > duration );
	}

	@Test
	public void tpfSameRequestTwoFederationMembers() throws FederationAccessException, InterruptedException, ExecutionException, IOException {
		final TPFServer fm1 = new MyTPFServerForTest( "http://example.org/sparql/1" );
		final TPFServer fm2 = new MyTPFServerForTest( "http://example.org/sparql/2" );
		final FederationAccessManagerWithChronicleMapCache fedAccessMgr = createFedAccessMgrForTests( 42 );
		fedAccessMgr.clearCardinalityCache();

		// issue request against fm1 and fm2
		final CardinalityResponse r1 = fedAccessMgr.issueCardinalityRequest( tpfReq, fm1 ).get();
		final CardinalityResponse r2 = fedAccessMgr.issueCardinalityRequest( tpfReq, fm2 ).get();
		assertEquals( 42, r1.getCardinality() );
		assertEquals( 43, r2.getCardinality() );

		// create a new federation access manager
		final FederationAccessManagerWithChronicleMapCache fedAccessMgr2 = createFedAccessMgrForTests( -1 );
		final long startTime3 = new Date().getTime();
		// issue request against fm2 and fm1
		final CardinalityResponse r3 = fedAccessMgr2.issueCardinalityRequest( tpfReq, fm2 ).get();
		final CardinalityResponse r4 = fedAccessMgr2.issueCardinalityRequest( tpfReq, fm1 ).get();
		final long duration = new Date().getTime() - startTime3;
		assertEquals( 43, r3.getCardinality() );
		assertEquals( 42, r4.getCardinality() );
		assertTrue( defaultTimeToSleep > duration );
	}

	@Test
	public void tpfTwoRequestsAsync() throws FederationAccessException, InterruptedException, ExecutionException, IOException {
		final TPFServer fm = new MyTPFServerForTest();
		final FederationAccessManagerWithChronicleMapCache fedAccessMgr = createFedAccessMgrForTests( 42 );
		fedAccessMgr.clearCardinalityCache();

		// issue request against fm
		fedAccessMgr.issueCardinalityRequest( tpfReq, fm ).get();
		// add small delay
		Thread.sleep(defaultTimeToSleep / 2);

		// issue request against again before it is finished
		// Note: The request should get the same value as the first request
		final CardinalityResponse r = fedAccessMgr.issueCardinalityRequest( tpfReq, fm ).get();
		assertEquals( 42, r.getCardinality() );
	}

	@Test
	public void tpfCacheInvalidationFromMemory() throws FederationAccessException, InterruptedException, ExecutionException, IOException {
		final TPFServer fm = new MyTPFServerForTest();
		final FederationAccessManagerWithChronicleMapCache fedAccessMgr = createFedAccessMgrForTests( 42, 0, 100 );
		fedAccessMgr.clearCardinalityCache();

		// issue request
		fedAccessMgr.issueCardinalityRequest( tpfReq, fm ).get();
		// wait for entry to become stale
		Thread.sleep(200);

		// issue request again
		final CardinalityResponse r = fedAccessMgr.issueCardinalityRequest( tpfReq, fm ).get();
		// verify that new value is returned
		assertEquals( 43, r.getCardinality() );
	}

	@Test
	public void tpfCacheInvalidationFromDisk() throws FederationAccessException, InterruptedException, ExecutionException, IOException {
		final TPFServer fm = new MyTPFServerForTest();
		final FederationAccessManagerWithChronicleMapCache fedAccessMgr1 = createFedAccessMgrForTests( 42, 0, 100 );
		fedAccessMgr1.clearCardinalityCache();
		fedAccessMgr1.issueCardinalityRequest( tpfReq, fm ).get();
		// wait for entry to become stale
		Thread.sleep(200);

		// load federation access manager from disk
		final FederationAccessManagerWithChronicleMapCache fedAccessMgr2 = createFedAccessMgrForTests( 52, 0, 100 );

		// repeat request
		final CardinalityResponse r2 = fedAccessMgr2.issueCardinalityRequest( tpfReq, fm ).get();
		// verify that stale value is not returned
		assertEquals( 52, r2.getCardinality() );
	}

	// TPF request agains BRTPF server tests

	@Test
	public void tpfWithBrtpfServerNoCacheHit() throws FederationAccessException, InterruptedException, ExecutionException, IOException {
		final BRTPFServer fm = new MyBRTPFServerForTest();
		final FederationAccessManagerWithChronicleMapCache fedAccessMgr = createFedAccessMgrForTests( 42 );
		fedAccessMgr.clearCardinalityCache();

		// issue reuest (not cached)
		final long startTime = Instant.now().toEpochMilli();
		final CardinalityResponse r = fedAccessMgr.issueCardinalityRequest( tpfReq, fm ).get();
		final long duration = Instant.now().toEpochMilli() - startTime;

		// assert correct cardinality
		assertEquals( 42, r.getCardinality() );
		// assert that duration is greater than SLEEP_MILLIS (since not cached)
		assertTrue( defaultTimeToSleep <= duration );
	}

	@Test
	public void tpfWithBrtpfServerCacheHitInMemory() throws FederationAccessException, InterruptedException, ExecutionException, IOException {
		final BRTPFServer fm = new MyBRTPFServerForTest();
		final FederationAccessManagerWithChronicleMapCache fedAccessMgr = createFedAccessMgrForTests( 42 );
		fedAccessMgr.clearCardinalityCache();

		// make sure request is cached
		final CardinalityResponse r1 = fedAccessMgr.issueCardinalityRequest( tpfReq, fm ).get();
		assertEquals( 42, r1.getCardinality() );

		// issue request again
		final long startTime = Instant.now().toEpochMilli();
		final CardinalityResponse r2 = fedAccessMgr.issueCardinalityRequest( tpfReq, fm ).get();
		final long duration = Instant.now().toEpochMilli() - startTime;
		// assert correct cardinality
		assertEquals( 42, r2.getCardinality() );
		// assert that duration is less than SLEEP_MILLIS (since cached)
		assertTrue( defaultTimeToSleep > duration );
	}

	@Test
	public void tpfWithBrtpfServerCacheHitFromDisk() throws FederationAccessException, InterruptedException, ExecutionException, IOException {
		final BRTPFServer fm = new MyBRTPFServerForTest();
		final FederationAccessManagerWithChronicleMapCache fedAccessMgr1 = createFedAccessMgrForTests( 42 );
		fedAccessMgr1.clearCardinalityCache();

		// make sure request is cached
		final CardinalityResponse r1 = fedAccessMgr1.issueCardinalityRequest( tpfReq, fm ).get();
		assertEquals( 42, r1.getCardinality() );

		// create a new federation access manager (cache will be loaded from disk)
		final FederationAccessManagerWithChronicleMapCache fedAccessMgr2 = createFedAccessMgrForTests( -1 );
		final long startTime =  Instant.now().toEpochMilli();
		final CardinalityResponse r2 = fedAccessMgr2.issueCardinalityRequest( tpfReq, fm ).get();
		final long duration =  Instant.now().toEpochMilli() - startTime;
		// assert correct cardinality
		assertEquals( 42, r2.getCardinality() );
		// assert that duration is less than SLEEP_MILLIS (since cached)
		assertTrue( defaultTimeToSleep > duration );
	}

	@Test
	public void tpfWithBrtpfServerSameRequestTwoFederationMembers() throws FederationAccessException, InterruptedException, ExecutionException, IOException {
		final BRTPFServer fm1 = new MyBRTPFServerForTest( "http://example.org/sparql/1" );
		final BRTPFServer fm2 = new MyBRTPFServerForTest( "http://example.org/sparql/2" );
		final FederationAccessManagerWithChronicleMapCache fedAccessMgr = createFedAccessMgrForTests( 42 );
		fedAccessMgr.clearCardinalityCache();

		// issue request against fm1 and fm2
		final CardinalityResponse r1 = fedAccessMgr.issueCardinalityRequest( tpfReq, fm1 ).get();
		final CardinalityResponse r2 = fedAccessMgr.issueCardinalityRequest( tpfReq, fm2 ).get();
		assertEquals( 42, r1.getCardinality() );
		assertEquals( 43, r2.getCardinality() );

		// create a new federation access manager
		final FederationAccessManagerWithChronicleMapCache fedAccessMgr2 = createFedAccessMgrForTests( -1 );
		final long startTime3 = new Date().getTime();
		// issue request against fm2 and fm1
		final CardinalityResponse r3 = fedAccessMgr2.issueCardinalityRequest( tpfReq, fm2 ).get();
		final CardinalityResponse r4 = fedAccessMgr2.issueCardinalityRequest( tpfReq, fm1 ).get();
		final long duration = new Date().getTime() - startTime3;
		assertEquals( 43, r3.getCardinality() );
		assertEquals( 42, r4.getCardinality() );
		assertTrue( defaultTimeToSleep > duration );
	}

	@Test
	public void tpfWithBrtpfServerTwoRequestsAsync() throws FederationAccessException, InterruptedException, ExecutionException, IOException {
		final BRTPFServer fm = new MyBRTPFServerForTest();
		final FederationAccessManagerWithChronicleMapCache fedAccessMgr = createFedAccessMgrForTests( 42 );
		fedAccessMgr.clearCardinalityCache();

		// issue request against fm
		fedAccessMgr.issueCardinalityRequest( tpfReq, fm ).get();
		// add small delay
		Thread.sleep(defaultTimeToSleep / 2);

		// issue request against again before it is finished
		// Note: The request should get the same value as the first request
		final CardinalityResponse r = fedAccessMgr.issueCardinalityRequest( tpfReq, fm ).get();
		assertEquals( 42, r.getCardinality() );
	}

	@Test
	public void tpfWithBrtpfServerCacheInvalidationFromMemory() throws FederationAccessException, InterruptedException, ExecutionException, IOException {
		final BRTPFServer fm = new MyBRTPFServerForTest();
		final FederationAccessManagerWithChronicleMapCache fedAccessMgr = createFedAccessMgrForTests( 42, 0, 100 );
		fedAccessMgr.clearCardinalityCache();

		// issue request
		fedAccessMgr.issueCardinalityRequest( tpfReq, fm ).get();
		// wait for entry to become stale
		Thread.sleep(200);

		// issue request again
		final CardinalityResponse r = fedAccessMgr.issueCardinalityRequest( tpfReq, fm ).get();
		// verify that new value is returned
		assertEquals( 43, r.getCardinality() );
	}

	@Test
	public void tpfWithBrtpfServerCacheInvalidationFromDisk() throws FederationAccessException, InterruptedException, ExecutionException, IOException {
		final BRTPFServer fm = new MyBRTPFServerForTest();
		final FederationAccessManagerWithChronicleMapCache fedAccessMgr1 = createFedAccessMgrForTests( 42, 0, 100 );
		fedAccessMgr1.clearCardinalityCache();
		fedAccessMgr1.issueCardinalityRequest( tpfReq, fm ).get();
		// wait for entry to become stale
		Thread.sleep(200);

		// load federation access manager from disk
		final FederationAccessManagerWithChronicleMapCache fedAccessMgr2 = createFedAccessMgrForTests( 52, 0, 100 );

		// repeat request
		final CardinalityResponse r2 = fedAccessMgr2.issueCardinalityRequest( tpfReq, fm ).get();
		// verify that stale value is not returned
		assertEquals( 52, r2.getCardinality() );
	}

	// BRTPF request tests

	@Test
	public void brtpfNoCacheHit() throws FederationAccessException, InterruptedException, ExecutionException, IOException {
		final BRTPFServer fm = new MyBRTPFServerForTest();
		final FederationAccessManagerWithChronicleMapCache fedAccessMgr = createFedAccessMgrForTests( 42 );
		fedAccessMgr.clearCardinalityCache();

		// issue reuest (not cached)
		final long startTime = Instant.now().toEpochMilli();
		final CardinalityResponse r = fedAccessMgr.issueCardinalityRequest( brtpfReq, fm ).get();
		final long duration = Instant.now().toEpochMilli() - startTime;

		// assert correct cardinality
		assertEquals( 42, r.getCardinality() );
		// assert that duration is greater than SLEEP_MILLIS (since not cached)
		assertTrue( defaultTimeToSleep <= duration );
	}

	@Test
	public void brtpfCacheHitInMemory() throws FederationAccessException, InterruptedException, ExecutionException, IOException {
		final BRTPFServer fm = new MyBRTPFServerForTest();
		final FederationAccessManagerWithChronicleMapCache fedAccessMgr = createFedAccessMgrForTests( 42 );
		fedAccessMgr.clearCardinalityCache();

		// make sure request is cached
		final CardinalityResponse r1 = fedAccessMgr.issueCardinalityRequest( brtpfReq, fm ).get();
		assertEquals( 42, r1.getCardinality() );

		// issue request again
		final long startTime = Instant.now().toEpochMilli();
		final CardinalityResponse r2 = fedAccessMgr.issueCardinalityRequest( brtpfReq, fm ).get();
		final long duration = Instant.now().toEpochMilli() - startTime;
		// assert correct cardinality
		assertEquals( 42, r2.getCardinality() );
		// assert that duration is less than SLEEP_MILLIS (since cached)
		assertTrue( defaultTimeToSleep > duration );
	}

	@Test
	public void brtpfCacheHitFromDisk() throws FederationAccessException, InterruptedException, ExecutionException, IOException {
		final BRTPFServer fm = new MyBRTPFServerForTest();
		final FederationAccessManagerWithChronicleMapCache fedAccessMgr1 = createFedAccessMgrForTests( 42 );
		fedAccessMgr1.clearCardinalityCache();

		// make sure request is cached
		final CardinalityResponse r1 = fedAccessMgr1.issueCardinalityRequest( brtpfReq, fm ).get();
		assertEquals( 42, r1.getCardinality() );

		// create a new federation access manager (cache will be loaded from disk)
		final FederationAccessManagerWithChronicleMapCache fedAccessMgr2 = createFedAccessMgrForTests( -1 );
		final long startTime =  Instant.now().toEpochMilli();
		final CardinalityResponse r2 = fedAccessMgr2.issueCardinalityRequest( brtpfReq, fm ).get();
		final long duration =  Instant.now().toEpochMilli() - startTime;
		// assert correct cardinality
		assertEquals( 42, r2.getCardinality() );
		// assert that duration is less than SLEEP_MILLIS (since cached)
		assertTrue( defaultTimeToSleep > duration );
	}

	@Test
	public void brtpfSameRequestTwoFederationMembers() throws FederationAccessException, InterruptedException, ExecutionException, IOException {
		final BRTPFServer fm1 = new MyBRTPFServerForTest( "http://example.org/sparql/1" );
		final BRTPFServer fm2 = new MyBRTPFServerForTest( "http://example.org/sparql/2" );
		final FederationAccessManagerWithChronicleMapCache fedAccessMgr = createFedAccessMgrForTests( 42 );
		fedAccessMgr.clearCardinalityCache();

		// issue request against fm1 and fm2
		final CardinalityResponse r1 = fedAccessMgr.issueCardinalityRequest( brtpfReq, fm1 ).get();
		final CardinalityResponse r2 = fedAccessMgr.issueCardinalityRequest( brtpfReq, fm2 ).get();
		assertEquals( 42, r1.getCardinality() );
		assertEquals( 43, r2.getCardinality() );

		// create a new federation access manager
		final FederationAccessManagerWithChronicleMapCache fedAccessMgr2 = createFedAccessMgrForTests( -1 );
		final long startTime3 = new Date().getTime();
		// issue request against fm2 and fm1
		final CardinalityResponse r3 = fedAccessMgr2.issueCardinalityRequest( brtpfReq, fm2 ).get();
		final CardinalityResponse r4 = fedAccessMgr2.issueCardinalityRequest( brtpfReq, fm1 ).get();
		final long duration = new Date().getTime() - startTime3;
		assertEquals( 43, r3.getCardinality() );
		assertEquals( 42, r4.getCardinality() );
		assertTrue( defaultTimeToSleep > duration );
	}

	@Test
	public void brtpfTwoRequestsAsync() throws FederationAccessException, InterruptedException, ExecutionException, IOException {
		final BRTPFServer fm = new MyBRTPFServerForTest();
		final FederationAccessManagerWithChronicleMapCache fedAccessMgr = createFedAccessMgrForTests( 42 );
		fedAccessMgr.clearCardinalityCache();

		// issue request against fm
		fedAccessMgr.issueCardinalityRequest( brtpfReq, fm ).get();
		// add small delay
		Thread.sleep(defaultTimeToSleep / 2);

		// issue request against again before it is finished
		// Note: The request should get the same value as the first request
		final CardinalityResponse r = fedAccessMgr.issueCardinalityRequest( brtpfReq, fm ).get();
		assertEquals( 42, r.getCardinality() );
	}

	@Test
	public void brtpfOneRequestDifferentBindings() throws FederationAccessException, InterruptedException, ExecutionException, IOException {
		final BRTPFServer fm = new MyBRTPFServerForTest();
		final FederationAccessManagerWithChronicleMapCache fedAccessMgr = createFedAccessMgrForTests( 42 );
		fedAccessMgr.clearCardinalityCache();

		final Var x1 = Var.alloc( "x" );
		final Node v1 = NodeFactory.createURI( "http://example.org/1" );
		final Binding solMap1 = BindingFactory.binding( x1, v1 );
		final BRTPFRequest brtpfReq1 = new BRTPFRequestImpl( tp, Set.of(solMap1) );

		final Var x2 = Var.alloc( "x" );
		final Node v2 = NodeFactory.createURI( "http://example.org/2" );
		final Binding solMap2 = BindingFactory.binding( x2, v2 );
		final BRTPFRequest brtpfReq2 = new BRTPFRequestImpl( tp, Set.of(solMap2) );

		// issue requests
		final CardinalityResponse r1 = fedAccessMgr.issueCardinalityRequest( brtpfReq1, fm ).get();
		final CardinalityResponse r2 = fedAccessMgr.issueCardinalityRequest( brtpfReq2, fm ).get();

		// assert correct cardinality
		assertEquals( 42, r1.getCardinality() );
		assertEquals( 43, r2.getCardinality() );
	}

	@Test
	public void brtpfTwoRequestSameBindings() throws FederationAccessException, InterruptedException, ExecutionException, IOException {
		final Var x1 = Var.alloc( "x" );
		final Node v1 = NodeFactory.createURI( "http://example.org/1" );
		final Binding solMap1 = BindingFactory.binding( x1, v1 );
		final BRTPFRequest brtpfReq1 = new BRTPFRequestImpl( tp, Set.of(solMap1) );

		final Var x2 = Var.alloc( "x" );
		final Node v2 = NodeFactory.createURI( "http://example.org/1" );
		final Binding solMap2 = BindingFactory.binding( x2, v2 );
		final BRTPFRequest brtpfReq2 = new BRTPFRequestImpl( tp, Set.of(solMap2) );

		final BRTPFServer fm = new MyBRTPFServerForTest();
		final FederationAccessManagerWithChronicleMapCache fedAccessMgr = createFedAccessMgrForTests( 42 );
		fedAccessMgr.clearCardinalityCache();

		final CardinalityResponse r1 = fedAccessMgr.issueCardinalityRequest( brtpfReq1, fm ).get();
		r1.getCardinality();

		final long startTime = new Date().getTime();
		final CardinalityResponse r2 = fedAccessMgr.issueCardinalityRequest( brtpfReq2, fm ).get();
		r2.getCardinality();
		final long duration = new Date().getTime() - startTime;

		// assert correct cardinality
		assertEquals( 42, r1.getCardinality() );
		assertEquals( 42, r2.getCardinality() );
		// assert that duration is below SLEEP_MILLIS (since it is now cached)
		assertTrue( defaultTimeToSleep > duration );
	}

	@Test
	public void brtpfCacheInvalidationFromMemory() throws FederationAccessException, InterruptedException, ExecutionException, IOException {
		final BRTPFServer fm = new MyBRTPFServerForTest();
		final FederationAccessManagerWithChronicleMapCache fedAccessMgr = createFedAccessMgrForTests( 42, 0, 100 );
		fedAccessMgr.clearCardinalityCache();

		// issue request
		fedAccessMgr.issueCardinalityRequest( brtpfReq, fm ).get();
		// wait for entry to become stale
		Thread.sleep(200);

		// issue request again
		final CardinalityResponse r = fedAccessMgr.issueCardinalityRequest( brtpfReq, fm ).get();
		// verify that new value is returned
		assertEquals( 43, r.getCardinality() );
	}

	@Test
	public void brtpfCacheInvalidationFromDisk() throws FederationAccessException, InterruptedException, ExecutionException, IOException {
		final BRTPFServer fm = new MyBRTPFServerForTest();
		final FederationAccessManagerWithChronicleMapCache fedAccessMgr1 = createFedAccessMgrForTests( 42, 0, 100 );
		fedAccessMgr1.clearCardinalityCache();
		fedAccessMgr1.issueCardinalityRequest( brtpfReq, fm ).get();
		// wait for entry to become stale
		Thread.sleep(200);

		// load federation access manager from disk
		final FederationAccessManagerWithChronicleMapCache fedAccessMgr2 = createFedAccessMgrForTests( 52, 0, 100 );

		// repeat request
		final CardinalityResponse r2 = fedAccessMgr2.issueCardinalityRequest( brtpfReq, fm ).get();
		// verify that stale value is not returned
		assertEquals( 52, r2.getCardinality() );
	}

	// ------------ helper code ------------

	public static FederationAccessManagerWithChronicleMapCache createFedAccessMgrForTests( final int cardinality ) throws IOException {
		return createFedAccessMgrForTests( cardinality, defaultTimeToSleep, defaultTimeToLive, defaultCacheCapacity );
	}

	public static FederationAccessManagerWithChronicleMapCache createFedAccessMgrForTests( final int cardinality,
	                                                                                       final long timeToSleep ) throws IOException
	{
		return createFedAccessMgrForTests( cardinality, timeToSleep, defaultTimeToLive, defaultCacheCapacity );
	}

	public static FederationAccessManagerWithChronicleMapCache createFedAccessMgrForTests( final int cardinality,
	                                                                                       final long timeToSleep,
	                                                                                       final long timeToLive ) throws IOException
	{
		return createFedAccessMgrForTests( cardinality, timeToSleep, timeToLive, defaultCacheCapacity );
	}

	public static FederationAccessManagerWithChronicleMapCache createFedAccessMgrForTests( final int cardinality,
	                                                                                       final long timeToSleep,
	                                                                                       final long timeToLive,
	                                                                                       final int cacheCapacity )
		throws IOException
	{
		final SPARQLRequestProcessor reqProc = new MySPARQLRequestProcessor( timeToSleep, cardinality );
		final TPFRequestProcessor reqProcTPF = new MyTPFRequestProcessor( timeToSleep, cardinality );
		final BRTPFRequestProcessor reqProcBRTPF = new MyBRTPFRequestProcessor( timeToSleep, cardinality );
		final Neo4jRequestProcessor reqProcNeo4j = new Neo4jRequestProcessor() {
			@Override
			public RecordsResponse performRequest( Neo4jRequest req, Neo4jServer fm ) {
				return null;
			}
		};

		final FederationAccessManager fedAccMan = new AsyncFederationAccessManagerImpl( execServiceForFedAccess,
			                                                                            reqProc,
			                                                                            reqProcTPF,
			                                                                            reqProcBRTPF,
			                                                                            reqProcNeo4j );
		return new FederationAccessManagerWithChronicleMapCache( fedAccMan, cacheCapacity, timeToLive );
	}

	protected static class MyTPFServerForTest implements TPFServer
	{
		protected final TPFInterface iface;

		public MyTPFServerForTest() {
			this( "http://example.org/tpf" );
		}

		public MyTPFServerForTest(final String url) {
			iface = new TPFInterfaceImpl( url, "subject", "predicate", "object" );
		}

		@Override
		public VocabularyMapping getVocabularyMapping() {
			return null;
		}

		@Override
		public TPFInterface getInterface() {
			return iface;
		}
	}

	protected static class MyBRTPFServerForTest implements BRTPFServer
	{
		protected final BRTPFInterface iface;

		public MyBRTPFServerForTest() {
			this( "http://example.org/brtpf" );
		}

		public MyBRTPFServerForTest(final String url) {
			 iface = new BRTPFInterfaceImpl(url, "subject", "predicate", "object", "values");
		}

		@Override
		public VocabularyMapping getVocabularyMapping() {
			return null;
		}

		@Override
		public BRTPFInterface getInterface() {
			return iface;
		}
	}

	protected static class MySPARQLRequestProcessor extends SPARQLRequestProcessorImpl
	{
		/** The delay in milliseconds before processing each request. */
		protected final long sleepMillis;

		/** A counter used to generate mock results. */
		protected int cardinality;

		/**
		 * Constructs a {@code MySPARQLRequestProcessor} instance.
		 *
		 * @param sleepMillis  the delay in milliseconds before processing each request
		 * @param cardinality  the initial count value for the generated solution
		 *                     mappings
		 */
		public MySPARQLRequestProcessor( final long sleepMillis, final int cardinality ) {
			this.sleepMillis = sleepMillis;
			this.cardinality = cardinality;
		}

		/**
		 * Introduces a delay if {@code sleepMillis} is greater than zero. Sleeps the
		 * current thread for the specified duration.
		 */
		protected void sleep() {
			if ( sleepMillis > 0L ) {
				try {
					Thread.sleep( sleepMillis );
				} catch ( final InterruptedException e ) {
					throw new RuntimeException( e );
				}
			}
		}

		/**
		 * Performs a request by introducing a delay and generating a mock response.
		 *
		 * @param req the SPARQL request to process
		 * @param fm  the deferation member to query
		 * @return a {@code SolMapsResponse} containing a mock solution mapping with an
		 *         incrementing count variable
		 * @throws FederationAccessException if an error occurs during request execution
		 */
		@Override
		public SolMapsResponse performRequest( SPARQLRequest req, SPARQLEndpoint fm ) throws FederationAccessException {
			// Introduce delay before processing the request
			sleep();

			// Create a mock solution mapping
			final Node countLiteral = NodeFactory.createLiteralByValue( cardinality, XSDDatatype.XSDinteger );
			final Var var = Var.alloc("__hefquinCountVar");
			final SolutionMapping solMap = SolutionMappingUtils.createSolutionMapping( var, countLiteral );
			cardinality++;

			// Wrap in a list and return the response
			List<SolutionMapping> mockResult = Collections.singletonList( solMap );
			return new SolMapsResponseImpl( mockResult, fm, req, new Date() );
		}
	}

	protected static class MyTPFRequestProcessor extends TPFRequestProcessorImpl
	{
		protected final long sleepMillis;
		protected int cardinality;

		public MyTPFRequestProcessor( final long sleepMillis, final int cardinality ) {
			this.sleepMillis = sleepMillis;
			this.cardinality = cardinality;
		}

		protected void sleep() {
			if ( sleepMillis > 0L ) {
				try {
					Thread.sleep( sleepMillis );
				} catch ( final InterruptedException e ) {
					throw new RuntimeException( e );
				}
			}
		}

		@Override
		public TPFResponse performRequest( TPFRequest req, TPFServer fm ) throws FederationAccessException {
			sleep();
			final TPFResponse r = new TPFResponseImpl( Collections.emptyList(),
			                                           Collections.emptyList(),
			                                           null,
			                                           fm,
			                                           req,
			                                           new Date() ) {
				final int cardinalityEstimate = cardinality;

				@Override
				public Integer getCardinalityEstimate() {
					return cardinalityEstimate;
				};
			};
			cardinality++;
			return r;
		}

		@Override
		public TPFResponse performRequest( TPFRequest req, BRTPFServer fm ) throws FederationAccessException {
			sleep();
			final TPFResponse r = new TPFResponseImpl( Collections.emptyList(),
			                                           Collections.emptyList(),
			                                           null,
			                                           fm,
			                                           req,
			                                           new Date() ) {
				final int cardinalityEstimate = cardinality;

				@Override
				public Integer getCardinalityEstimate() {
					return cardinalityEstimate;
				};
			};
			cardinality++;
			return r;
		}
	}

	protected static class MyBRTPFRequestProcessor extends BRTPFRequestProcessorImpl
	{
		protected final long sleepMillis;
		protected int cardinality;

		public MyBRTPFRequestProcessor( final long sleepMillis, final int cardinality ) {
			this.sleepMillis = sleepMillis;
			this.cardinality = cardinality;
		}

		protected void sleep() {
			if ( sleepMillis > 0L ) {
				try {
					Thread.sleep( sleepMillis );
				} catch ( final InterruptedException e ) {
					throw new RuntimeException( e );
				}
			}
		}

		@Override
		public TPFResponse performRequest( BRTPFRequest req, BRTPFServer fm ) throws FederationAccessException {
			sleep();
			final TPFResponse r = new TPFResponseImpl( Collections.emptyList(),
			                                           Collections.emptyList(),
			                                           null,
			                                           fm,
			                                           req,
			                                           new Date() ) {
				final int cardinalityEstimate = cardinality;

				@Override
				public Integer getCardinalityEstimate() {
					return cardinalityEstimate;
				};
			};
			cardinality++;
			return r;
		}
	}
}