
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
	protected static final long THRESHOLD = 50L;

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
	public void cardinalityRequestNoCacheHit() throws FederationAccessException, InterruptedException, ExecutionException {
		final TriplePattern tp = new TriplePatternImpl( NodeFactory.createVariable("s"),
			                                            NodeFactory.createVariable("p"),
			                                            NodeFactory.createVariable("o") );
		final SPARQLRequest req = new SPARQLRequestImpl( tp );
		final SPARQLEndpoint fm = new SPARQLEndpointForTest("http://example.org/sparql");

		final FederationAccessManagerWithPersistedDiskCache fedAccessMgr = createFedAccessMgrForTests( execServiceForFedAccess, SLEEP_MILLIS, 42 );
		// clear cache
		fedAccessMgr.clearCardinalityCache();
		final long startTime = new Date().getTime();
		final CardinalityResponse r = fedAccessMgr.issueCardinalityRequest( req, fm ).get();
		final long duration = new Date().getTime() - startTime;
		
		// assert correct cardinality
		assertEquals( 42, r.getCardinality() );
		// assert that duration was above SLEEP_MILLIS (since not cached yet)
		assertTrue( SLEEP_MILLIS < duration ); // slow
	}

	@Test
	public void cardinalityRequestCacheHit() throws FederationAccessException, InterruptedException, ExecutionException {
		final TriplePattern tp = new TriplePatternImpl( NodeFactory.createVariable("s"),
			                                            NodeFactory.createVariable("p"),
			                                            NodeFactory.createVariable("o") );
		final SPARQLRequest req = new SPARQLRequestImpl( tp );
		final SPARQLEndpoint fm = new SPARQLEndpointForTest("http://example.org/sparql");

		final FederationAccessManagerWithPersistedDiskCache fedAccessMgr1 = createFedAccessMgrForTests( execServiceForFedAccess, SLEEP_MILLIS, 42 );
		fedAccessMgr1.clearCardinalityCache(); // clear cache
		final CardinalityResponse r1 = fedAccessMgr1.issueCardinalityRequest( req, fm ).get();
		assertEquals( 42, r1.getCardinality() ); // slow

		// Read cache from disk using a new federation manager
		final FederationAccessManagerWithPersistedDiskCache fedAccessMgr2 = createFedAccessMgrForTests( execServiceForFedAccess, SLEEP_MILLIS, -1 );
		final long startTime = new Date().getTime();
		final CardinalityResponse r2 = fedAccessMgr2.issueCardinalityRequest( req, fm ).get();
		final long duration = new Date().getTime() - startTime;
		// assert correct cardinality
		assertEquals( 42, r2.getCardinality() );
		// assert that duration is below SLEEP_MILLIS (since it is now cached)
		assertTrue( THRESHOLD > duration ); // fast
	}

	@Test
	public void cardinalityRequestCacheTwoFederationMembers() throws FederationAccessException, InterruptedException, ExecutionException {
		final TriplePattern tp = new TriplePatternImpl( NodeFactory.createVariable("s"),
			                                            NodeFactory.createVariable("p"),
			                                            NodeFactory.createVariable("o") );
		final SPARQLRequest req = new SPARQLRequestImpl( tp );
		final SPARQLEndpoint fm1 = new SPARQLEndpointForTest("http://example.org/sparql/1");
		final SPARQLEndpoint fm2 = new SPARQLEndpointForTest("http://example.org/sparql/2");

		final FederationAccessManagerWithPersistedDiskCache fedAccessMgr1 = createFedAccessMgrForTests( execServiceForFedAccess, SLEEP_MILLIS, 42 );
		fedAccessMgr1.clearCardinalityCache(); // clear cache
		
		// Issue request, not cached
		final long startTime1 = new Date().getTime();
		final CardinalityResponse r1 = fedAccessMgr1.issueCardinalityRequest( req, fm1 ).get();
		final long duration1 = new Date().getTime() - startTime1;
		assertEquals( 42, r1.getCardinality() );
		assertTrue( SLEEP_MILLIS < duration1 ); // slow
		
		// Issue request against a different federation member, not cached
		final long startTime2 = new Date().getTime();
		final CardinalityResponse r2 = fedAccessMgr1.issueCardinalityRequest( req, fm2 ).get();
		assertEquals( 42, r2.getCardinality() );
		final long duration2 = new Date().getTime() - startTime2;
		assertTrue( SLEEP_MILLIS < duration2 ); // slow
		
		// Read cache from disk using a new federation manager
		final FederationAccessManagerWithPersistedDiskCache fedAccessMgr2 = createFedAccessMgrForTests( execServiceForFedAccess, SLEEP_MILLIS, -1 );
		final long startTime3 = new Date().getTime();
		final CardinalityResponse r3 = fedAccessMgr2.issueCardinalityRequest( req, fm1 ).get();
		final CardinalityResponse r4 = fedAccessMgr2.issueCardinalityRequest( req, fm2 ).get();
		final long duration3 = new Date().getTime() - startTime3;
		assertEquals( 42, r3.getCardinality() );
		assertEquals( 42, r4.getCardinality() );
		assertTrue( THRESHOLD > duration3 ); // fast

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

	protected static class MySPARQLRequestProcessor extends SPARQLRequestProcessorImpl
	{
		protected final long sleepMillis;
		protected final int card;

		public MySPARQLRequestProcessor( final long sleepMillis, final int card ) {
			this.sleepMillis = sleepMillis;
			this.card = card;
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

		@Override
		public SolMapsResponse performRequest(SPARQLRequest req, SPARQLEndpoint fm) throws FederationAccessException {
			// add delay
			sleep();
			// Create a QuerySolutionMap (mocked solution mapping)
			QuerySolutionMap solMap = new QuerySolutionMap();
			// Create a typed literal for the count value
			Literal countLiteral = ModelFactory.createDefaultModel().createTypedLiteral(card);
			// Bind the literal to the count variable
			solMap.add("__hefquinCountVar", countLiteral);
			// Wrap in a list
			List<SolutionMapping> mockResult = Collections.singletonList(SolutionMappingUtils.createSolutionMapping(solMap));
			return new SolMapsResponseImpl(mockResult, fm, req, new Date());
		}
	}
}
