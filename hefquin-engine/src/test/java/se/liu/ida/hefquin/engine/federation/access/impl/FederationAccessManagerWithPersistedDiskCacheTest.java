
package se.liu.ida.hefquin.engine.federation.access.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.base.query.impl.TriplePatternImpl;
import se.liu.ida.hefquin.engine.EngineTestBase;
import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.Neo4jServer;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.TPFServer;
import se.liu.ida.hefquin.engine.federation.access.*;
import se.liu.ida.hefquin.engine.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.BRTPFRequestProcessor;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.Neo4jRequestProcessor;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.SPARQLRequestProcessor;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.SPARQLRequestProcessorImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.TPFRequestProcessor;
import se.liu.ida.hefquin.engine.federation.access.impl.response.SolMapsResponseImpl;

public class FederationAccessManagerWithPersistedDiskCacheTest extends EngineTestBase {
	protected static final long SLEEP_MILLIS = 500L;
	protected static ExecutorService execServiceForFedAccess;
	final TriplePattern tp = new TriplePatternImpl( NodeFactory.createVariable( "s" ),
		                                            NodeFactory.createVariable( "p" ),
		                                            NodeFactory.createVariable( "o" ) );
	final SPARQLRequest req = new SPARQLRequestImpl( tp );

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
	public void noCacheHit() throws FederationAccessException, InterruptedException, ExecutionException {
		final SPARQLEndpoint fm = new SPARQLEndpointForTest("http://example.org/sparql");
		final FederationAccessManagerWithPersistedDiskCache fedAccessMgr = createFedAccessMgrForTests( execServiceForFedAccess, SLEEP_MILLIS, 42 );
		// clear cache
		fedAccessMgr.clearCardinalityCache();
		final long startTime = new Date().getTime();
		final CardinalityResponse r = fedAccessMgr.issueCardinalityRequest( req, fm ).get();
		final long duration = new Date().getTime() - startTime;
		// assert correct cardinality
		assertEquals( 42, r.getCardinality() );
		// assert that duration was above SLEEP_MILLIS (since not cached)
		assertTrue( SLEEP_MILLIS < duration );
	}

	@Test
	public void cacheHitInMemory() throws FederationAccessException, InterruptedException, ExecutionException {
		final SPARQLEndpoint fm = new SPARQLEndpointForTest("http://example.org/sparql");
		final FederationAccessManagerWithPersistedDiskCache fedAccessMgr = createFedAccessMgrForTests( execServiceForFedAccess, SLEEP_MILLIS, 42 );
		// clear cache
		fedAccessMgr.clearCardinalityCache();
		// issue request, not cached
		final CardinalityResponse r1 = fedAccessMgr.issueCardinalityRequest( req, fm ).get();
		r1.getCardinality();

		// issue request again
		final long startTime = new Date().getTime();
		final CardinalityResponse r2 = fedAccessMgr.issueCardinalityRequest( req, fm ).get();
		final long duration = new Date().getTime() - startTime;
		// assert correct cardinality
		assertEquals( 42, r2.getCardinality() );
		// assert that duration is below SLEEP_MILLIS (since it is now cached)
		assertTrue( SLEEP_MILLIS > duration );
	}

	@Test
	public void cacheHitFromDisk() throws FederationAccessException, InterruptedException, ExecutionException {
		final SPARQLEndpoint fm = new SPARQLEndpointForTest("http://example.org/sparql");
		final FederationAccessManagerWithPersistedDiskCache fedAccessMgr1 = createFedAccessMgrForTests( execServiceForFedAccess, SLEEP_MILLIS, 42 );
		// clear cache
		fedAccessMgr1.clearCardinalityCache();
		// issue request, not cached
		final CardinalityResponse r1 = fedAccessMgr1.issueCardinalityRequest( req, fm ).get();
		r1.getCardinality();

		// create a new federation access manager
		final FederationAccessManagerWithPersistedDiskCache fedAccessMgr2 = createFedAccessMgrForTests( execServiceForFedAccess, SLEEP_MILLIS, -1 );
		final long startTime = new Date().getTime();
		final CardinalityResponse r2 = fedAccessMgr2.issueCardinalityRequest( req, fm ).get();
		final long duration = new Date().getTime() - startTime;
		// assert correct cardinality
		assertEquals( 42, r2.getCardinality() );
		// assert that duration is below SLEEP_MILLIS (since it is now cached)
		assertTrue( SLEEP_MILLIS > duration );
	}

	@Test
	public void sameRequestTwoFederationMembers() throws FederationAccessException, InterruptedException, ExecutionException {
		final SPARQLEndpoint fm1 = new SPARQLEndpointForTest("http://example.org/sparql/1");
		final SPARQLEndpoint fm2 = new SPARQLEndpointForTest("http://example.org/sparql/2");
		final FederationAccessManagerWithPersistedDiskCache fedAccessMgr1 = createFedAccessMgrForTests( execServiceForFedAccess, SLEEP_MILLIS, 42 );
		// clear cache
		fedAccessMgr1.clearCardinalityCache();
		
		// issue request against fm1, not cached
		final long startTime1 = new Date().getTime();
		final CardinalityResponse r1 = fedAccessMgr1.issueCardinalityRequest( req, fm1 ).get();
		final long duration1 = new Date().getTime() - startTime1;
		assertEquals( 42, r1.getCardinality() );
		assertTrue( SLEEP_MILLIS < duration1 );
		
		// issue request against fm2, not cached
		final long startTime2 = new Date().getTime();
		final CardinalityResponse r2 = fedAccessMgr1.issueCardinalityRequest( req, fm2 ).get();
		assertEquals( 43, r2.getCardinality() );
		final long duration2 = new Date().getTime() - startTime2;
		assertTrue( SLEEP_MILLIS < duration2 ); // slow
		
		// create a new federation access manager
		final FederationAccessManagerWithPersistedDiskCache fedAccessMgr2 = createFedAccessMgrForTests( execServiceForFedAccess, SLEEP_MILLIS, -1 );
		final long startTime3 = new Date().getTime();
		final CardinalityResponse r3 = fedAccessMgr2.issueCardinalityRequest( req, fm1 ).get();
		final CardinalityResponse r4 = fedAccessMgr2.issueCardinalityRequest( req, fm2 ).get();
		final long duration3 = new Date().getTime() - startTime3;
		assertEquals( 42, r3.getCardinality() );
		assertEquals( 43, r4.getCardinality() );
		assertTrue( SLEEP_MILLIS > duration3 ); // fast
	}
	@Test
	public void twoRequestOneFederationMemberAsync() throws FederationAccessException, InterruptedException, ExecutionException {
		final SPARQLEndpoint fm = new SPARQLEndpointForTest("http://example.org/sparql/1");
		final FederationAccessManagerWithPersistedDiskCache fedAccessMgr = createFedAccessMgrForTests( execServiceForFedAccess, SLEEP_MILLIS, 42 );
		// clear cache
		fedAccessMgr.clearCardinalityCache();
		
		// issue request against fm, not cached
		fedAccessMgr.issueCardinalityRequest( req, fm ).get();
		// sleep 100 ms
		Thread.sleep(100);
		// issue request against fm, not cached yet!
		final CardinalityResponse r = fedAccessMgr.issueCardinalityRequest( req, fm ).get();
		// Note: THe second request should get the same CompletableFutre as the first, i.e., return 42
		assertEquals( 42, r.getCardinality() );
	}

	// ------------ helper code ------------

	public static FederationAccessManagerWithPersistedDiskCache createFedAccessMgrForTests( final ExecutorService execServiceForFedAccess,
		                                                                                    final long sleepMillis,
		                                                                                    final int card ) {
		final SPARQLRequestProcessor reqProc = new MySPARQLRequestProcessor( sleepMillis, card );

		final TPFRequestProcessor reqProcTPF = new TPFRequestProcessor() {
			@Override
			public TPFResponse performRequest( TPFRequest req, TPFServer fm ) throws FederationAccessException {
				return null;
			}

			@Override
			public TPFResponse performRequest( TPFRequest req, BRTPFServer fm ) throws FederationAccessException {
				return null;
			}
		};
		final BRTPFRequestProcessor reqProcBRTPF = new BRTPFRequestProcessor() {
			@Override
			public TPFResponse performRequest( BRTPFRequest req, BRTPFServer fm ) {
				return null;
			}
		};
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
		return new FederationAccessManagerWithPersistedDiskCache( fedAccMan, 10000 );
	}

	/**
	 * A custom SPARQL request processor that extends
	 * {@code SPARQLRequestProcessorImpl}. This processor introduces an optional
	 * delay before processing requests and generates mock solution mappings with an
	 * incrementing count variable.
	 */
	protected static class MySPARQLRequestProcessor extends SPARQLRequestProcessorImpl {

		/** The delay in milliseconds before processing each request. */
		protected final long sleepMillis;

		/** A counter used to generate mock results. */
		protected int card;

		/**
		 * Constructs a {@code MySPARQLRequestProcessor} instance.
		 *
		 * @param sleepMillis the delay in milliseconds before processing each request
		 * @param card        the initial count value for the generated solution
		 *                    mappings
		 */
		public MySPARQLRequestProcessor( final long sleepMillis, final int card ) {
			this.sleepMillis = sleepMillis;
			this.card = card;
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
			final Node countLiteral = NodeFactory.createLiteralByValue(card, XSDDatatype.XSDinteger);
			final Var var = Var.alloc("__hefquinCountVar");
			final SolutionMapping solMap = SolutionMappingUtils.createSolutionMapping(var, countLiteral);
			card++;

			// Wrap in a list and return the response
			List<SolutionMapping> mockResult = Collections.singletonList(solMap);
			return new SolMapsResponseImpl( mockResult, fm, req, new Date() );
		}
	}

}
