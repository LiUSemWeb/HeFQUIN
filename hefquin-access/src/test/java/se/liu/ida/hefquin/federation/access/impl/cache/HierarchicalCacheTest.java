package se.liu.ida.hefquin.federation.access.impl.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.junit.Test;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.Triple;
import se.liu.ida.hefquin.base.data.impl.SolutionMappingImpl;
import se.liu.ida.hefquin.base.data.impl.TripleImpl;
import se.liu.ida.hefquin.base.datastructures.Cache;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheInvalidationPolicy;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheInvalidationPolicyTimeToLive;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CachePolicies;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheReplacementPolicy;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheReplacementPolicyFactory;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheReplacementPolicyLRU;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.base.query.impl.TriplePatternImpl;
import se.liu.ida.hefquin.federation.FederationTestBase;
import se.liu.ida.hefquin.federation.access.CardinalityResponse;
import se.liu.ida.hefquin.federation.access.DataRetrievalResponse;
import se.liu.ida.hefquin.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.federation.access.TPFResponse;
import se.liu.ida.hefquin.federation.access.UnsupportedOperationDueToRetrievalError;
import se.liu.ida.hefquin.federation.access.impl.cache.chroniclemap.ChronicleMapCache;
import se.liu.ida.hefquin.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.federation.access.impl.response.CachedCardinalityResponse;
import se.liu.ida.hefquin.federation.access.impl.response.SolMapsResponseImpl;
import se.liu.ida.hefquin.federation.access.impl.response.TPFResponseImpl;

public class HierarchicalCacheTest extends FederationTestBase
{
	protected String filename = "cache/test/map.dat";
	protected final TriplePattern tp = new TriplePatternImpl( NodeFactory.createURI("http://example.org/s"),
	                                                          NodeFactory.createURI("http://example.org/p"),
	                                                          NodeFactory.createVariable("o") );

	@Test
	public void putStoresEntriesInL1AndL2Test() throws Exception {
		final Cache<PersistentCacheKey, CompletableFuture<? extends DataRetrievalResponse<?>>> cache;
		final CacheLayer<PersistentCacheKey, CompletableFuture<? extends DataRetrievalResponse<?>>, PersistentCacheEntry> l1;
		final CacheLayer<PersistentCacheKey, CompletableFuture<? extends DataRetrievalResponse<?>>, PersistentCacheEntry> l2;
		
		l1 = new CacheLayer<>( new HashMap<>(), 10, new CachePoliciesForTest() );
		l2 = new ChronicleMapCache( 10, filename, new CachePoliciesForTest() );
		
		cache = new HierarchicalCache<>(l1, l2);
		cache.clear();

		final PersistentCacheKey k = new PersistentCacheKey( new SPARQLRequestImpl(tp),
		                                                     new SPARQLEndpointForTest("http://example.org/sparql"),
		                                                     PersistentCacheKey.ResponseMode.RESULT );

		final SolMapsResponse o = makeSolMapResponse(3);

		// Store entry in both cache layers
		cache.put( k, CompletableFuture.completedFuture(o) );

		// Read entry from L1
		assertEquals( o, l1.get(k).get() );

		// Read entry from L2
		assertSolMapsEqual( o, (SolMapsResponse) l2.get(k).get() );

		cache.close();
	}

	@Test
	public void getPromotesEntryFromL2ToL1Test() throws Exception {
		final Cache<PersistentCacheKey, CompletableFuture<? extends DataRetrievalResponse<?>>> cache;
		final CacheLayer<PersistentCacheKey, CompletableFuture<? extends DataRetrievalResponse<?>>, PersistentCacheEntry> l1;
		final CacheLayer<PersistentCacheKey, CompletableFuture<? extends DataRetrievalResponse<?>>, PersistentCacheEntry> l2;
		
		l1 = new CacheLayer<>( new HashMap<>(), 10, new CachePoliciesForTest() );
		l2 = new ChronicleMapCache( 10, filename, new CachePoliciesForTest() );
		
		cache = new HierarchicalCache<>(l1, l2);
		cache.clear();

		final PersistentCacheKey k = new PersistentCacheKey( new SPARQLRequestImpl(tp),
		                                                     new SPARQLEndpointForTest("http://example.org/sparql"),
		                                                     PersistentCacheKey.ResponseMode.RESULT );
		final SolMapsResponse o = makeSolMapResponse(1);

		// Store entry in both cache layers
		cache.put( k, CompletableFuture.completedFuture(o) );

		// Simulate an L1 cache miss while keeping the entry available in L2
		l1.clear();

		// Verify the entry is no longer present in L1
		assertNull( l1.get(k) );

		// Reading the entry should retrieve it from L2
		assertNotNull( cache.get(k) );

		// The retrieved entry should be promoted back into L1
		assertNotNull( l1.get(k) );

		cache.close();
	}

	@Test
	public void putReplacesExistingEntryInBothLayersTest() throws InterruptedException, IOException, ExecutionException, UnsupportedOperationDueToRetrievalError {
		final Cache<PersistentCacheKey, CompletableFuture<? extends DataRetrievalResponse<?>>> cache;
		final CacheLayer<PersistentCacheKey, CompletableFuture<? extends DataRetrievalResponse<?>>, PersistentCacheEntry> l1;
		final CacheLayer<PersistentCacheKey, CompletableFuture<? extends DataRetrievalResponse<?>>, PersistentCacheEntry> l2;
		
		l1 = new CacheLayer<>( new HashMap<>(), 10, new CachePoliciesForTest() );
		l2 = new ChronicleMapCache( 10, filename, new CachePoliciesForTest() );
		
		cache = new HierarchicalCache<>(l1, l2);
		cache.clear();

		final PersistentCacheKey k = new PersistentCacheKey( new SPARQLRequestImpl(tp),
		                                                     new SPARQLEndpointForTest("http://example.org/sparql"),
		                                                     PersistentCacheKey.ResponseMode.RESULT );

		final SolMapsResponse o1 = makeSolMapResponse(1);
		final SolMapsResponse o2 = makeSolMapResponse(2);

		// Store entry and then replace with a new value
		cache.put( k, CompletableFuture.completedFuture(o1) );
		cache.put( k, CompletableFuture.completedFuture(o2) );

		// Verify that the replacement is visible in L1
		assertEquals (o2, l1.get(k).get() );

		// Verify that the replacement is visible in L2
		assertSolMapsEqual( o2, (SolMapsResponse) l2.get(k).get() );

		cache.close();
	}

	@Test
	public void capacityEvictionInL1DoesNotRemoveEntriesFromL2Test() throws IOException, InterruptedException, ExecutionException, UnsupportedOperationDueToRetrievalError {
		final Cache<PersistentCacheKey, CompletableFuture<? extends DataRetrievalResponse<?>>> cache;
		final CacheLayer<PersistentCacheKey, CompletableFuture<? extends DataRetrievalResponse<?>>, PersistentCacheEntry> l1;
		final CacheLayer<PersistentCacheKey, CompletableFuture<? extends DataRetrievalResponse<?>>, PersistentCacheEntry> l2;
		
		l1 = new CacheLayer<>( new HashMap<>(), 2, new CachePoliciesForTest() );
		l2 = new ChronicleMapCache( 10, filename, new CachePoliciesForTest() );
		
		cache = new HierarchicalCache<>(l1, l2);
		cache.clear();

		final SolMapsResponse o = makeSolMapResponse(1);
		final PersistentCacheKey k1 = new PersistentCacheKey( new SPARQLRequestImpl(tp),
		                                                      new SPARQLEndpointForTest( "http://example.org/sparql" + 1 ),
		                                                      PersistentCacheKey.ResponseMode.RESULT );
		final PersistentCacheKey k2 = new PersistentCacheKey( new SPARQLRequestImpl(tp),
		                                                      new SPARQLEndpointForTest( "http://example.org/sparql" + 2 ),
		                                                      PersistentCacheKey.ResponseMode.RESULT );
		final PersistentCacheKey k3 = new PersistentCacheKey( new SPARQLRequestImpl(tp),
		                                                      new SPARQLEndpointForTest( "http://example.org/sparql" + 3 ),
		                                                      PersistentCacheKey.ResponseMode.RESULT );

		// Fill L1
		cache.put( k1, CompletableFuture.completedFuture(o) );
		cache.put( k2, CompletableFuture.completedFuture(o)) ;

		// Adding a third entry evicts k1 (the least recently used entry) from L1
		cache.put( k3, CompletableFuture.completedFuture(o) );

		assertNull( l1.get(k1) );

		// The evicted entry should still be available in L2
		assertSolMapsEqual( o, (SolMapsResponse) l2.get(k1).get() );

		cache.close();
	}

	@Test
	public void expiredEntriesAreInvalidatedInBothLayersTest() throws IOException, InterruptedException, ExecutionException {
		final Cache<PersistentCacheKey, CompletableFuture<? extends DataRetrievalResponse<?>>> cache;
		final CacheLayer<PersistentCacheKey, CompletableFuture<? extends DataRetrievalResponse<?>>, PersistentCacheEntry> l1;
		final CacheLayer<PersistentCacheKey, CompletableFuture<? extends DataRetrievalResponse<?>>, PersistentCacheEntry> l2;
		
		l1 = new CacheLayer<>( new HashMap<>(), 10, new CachePoliciesForTest(1000) );
		l2 = new ChronicleMapCache( 10, filename, new CachePoliciesForTest(1000) );
		
		cache = new HierarchicalCache<>(l1, l2);
		cache.clear();
		
		final PersistentCacheKey k = new PersistentCacheKey( new SPARQLRequestImpl(tp),
		                                                     new SPARQLEndpointForTest("http://example.org/sparql"),
		                                                     PersistentCacheKey.ResponseMode.RESULT );
		final DataRetrievalResponse<?> o = makeSolMapResponse(1);
		
		// Store entry in both cache layers
		cache.put(k, CompletableFuture.completedFuture(o));

		// Verify the entry is initially available
		assertEquals( o, cache.get(k).get() );

		Thread.sleep(1250);

		// Expired entries should no longer be available from either layer
		assertNull( l1.get(k) );
		assertNull( l2.get(k) );

		// Expired entries should no longer be available in the hierarchical cache
		assertNull( cache.get(k) );

		cache.close();
	}

	@Test
	public void lruEvictionInL1DoesNotRemoveEntriesFromL2Test() throws IOException, InterruptedException, ExecutionException, UnsupportedOperationDueToRetrievalError {
		final Cache<PersistentCacheKey, CompletableFuture<? extends DataRetrievalResponse<?>>> cache;
		final CacheLayer<PersistentCacheKey, CompletableFuture<? extends DataRetrievalResponse<?>>, PersistentCacheEntry> l1;
		final CacheLayer<PersistentCacheKey, CompletableFuture<? extends DataRetrievalResponse<?>>, PersistentCacheEntry> l2;
		
		l1 = new CacheLayer<>( new HashMap<>(), 2, new CachePoliciesForTest(1000) );
		l2 = new ChronicleMapCache( 10, filename, new CachePoliciesForTest(1000) );
				
		cache = new HierarchicalCache<>(l1, l2);
		cache.clear();

		final PersistentCacheKey k1 = new PersistentCacheKey( new SPARQLRequestImpl(tp),
		                                                      new SPARQLEndpointForTest( "http://example.org/sparql" + 1 ),
		                                                      PersistentCacheKey.ResponseMode.RESULT );
		final PersistentCacheKey k2 = new PersistentCacheKey( new SPARQLRequestImpl(tp),
		                                                      new SPARQLEndpointForTest( "http://example.org/sparql" + 2 ),
		                                                      PersistentCacheKey.ResponseMode.RESULT );
		final PersistentCacheKey k3 = new PersistentCacheKey( new SPARQLRequestImpl(tp),
		                                                      new SPARQLEndpointForTest( "http://example.org/sparql" + 3 ),
		                                                      PersistentCacheKey.ResponseMode.RESULT );
		final SolMapsResponse o = makeSolMapResponse(1);

		// Fill L1 cache
		cache.put( k1, CompletableFuture.completedFuture(o) );
		cache.put( k2, CompletableFuture.completedFuture(o) );

		// Get k1, k2 becomes the LRU entry
		cache.get(k1);
	
		// Adding a third entry should evict k2 from L1
		cache.put( k3, CompletableFuture.completedFuture(o) );
		assertNull( l1.get(k2) );

		// Verify that the remaining entries are still present in L1
		assertNotNull( l1.get(k1).get() );
		assertNotNull( l1.get(k3).get() );

		// Verify that the evicted entry is still available through L2
		assertSolMapsEqual( o, (SolMapsResponse) cache.get(k2).get() );

		cache.close();
	}

	@Test
	public void evictRemovesEntryFromBothLayersTest() throws Exception {
		final CacheLayer<PersistentCacheKey, CompletableFuture<? extends DataRetrievalResponse<?>>, PersistentCacheEntry> l1;
		final CacheLayer<PersistentCacheKey, CompletableFuture<? extends DataRetrievalResponse<?>>, PersistentCacheEntry> l2;
		final Cache<PersistentCacheKey, CompletableFuture<? extends DataRetrievalResponse<?>>> cache;

		l1 = new CacheLayer<>( new HashMap<>(), 10, new CachePoliciesForTest() );
		l2 = new ChronicleMapCache( 10, filename, new CachePoliciesForTest() );

		cache = new HierarchicalCache<>(l1, l2);
		cache.clear();

		final PersistentCacheKey k = new PersistentCacheKey( new SPARQLRequestImpl(tp),
		                                                     new SPARQLEndpointForTest("http://example.org/sparql"),
		                                                     PersistentCacheKey.ResponseMode.RESULT );

		cache.put( k, CompletableFuture.completedFuture( makeSolMapResponse(1) ) );

		assertNotNull( l1.get(k) );
		assertNotNull( l2.get(k) );

		cache.evict(k);

		assertNull( l1.get(k) );
		assertNull( l2.get(k) );
		assertNull( cache.get(k) );

		cache.close();
	}

	@Test
	public void clearRemovesEntriesFromBothLayersTest() throws Exception {
		final CacheLayer<PersistentCacheKey, CompletableFuture<? extends DataRetrievalResponse<?>>, PersistentCacheEntry> l1;
		final CacheLayer<PersistentCacheKey, CompletableFuture<? extends DataRetrievalResponse<?>>, PersistentCacheEntry> l2;
		final Cache<PersistentCacheKey, CompletableFuture<? extends DataRetrievalResponse<?>>> cache;

		l1 = new CacheLayer<>( new HashMap<>(), 10, new CachePoliciesForTest() );
		l2 = new ChronicleMapCache( 10, filename, new CachePoliciesForTest() );

		cache = new HierarchicalCache<>(l1, l2);
		cache.clear();

		final PersistentCacheKey k = new PersistentCacheKey( new SPARQLRequestImpl( tp ),
		                                                     new SPARQLEndpointForTest("http://example.org/sparql"),
		                                                     PersistentCacheKey.ResponseMode.RESULT );

		cache.put( k, CompletableFuture.completedFuture( makeSolMapResponse(1) ) );

		assertNotNull( l1.get(k) );
		assertNotNull( l2.get(k) );

		cache.clear();

		assertNull( l1.get(k) );
		assertNull( l2.get(k) );
		assertNull( cache.get(k) );

		cache.close();
	}

	@Test
	public void cacheIsEmptyOnlyWhenBothLayersAreEmptyTest() throws Exception {
		final CacheLayer<PersistentCacheKey, CompletableFuture<? extends DataRetrievalResponse<?>>, PersistentCacheEntry> l1;
		final CacheLayer<PersistentCacheKey, CompletableFuture<? extends DataRetrievalResponse<?>>, PersistentCacheEntry> l2;
		final Cache<PersistentCacheKey, CompletableFuture<? extends DataRetrievalResponse<?>>> cache;

		l1 = new CacheLayer<>( new HashMap<>(), 10, new CachePoliciesForTest() );
		l2 = new ChronicleMapCache( 10, filename, new CachePoliciesForTest() );

		cache = new HierarchicalCache<>(l1, l2);
		cache.clear();

		final PersistentCacheKey k = new PersistentCacheKey( new SPARQLRequestImpl( tp ),
		                                                     new SPARQLEndpointForTest( "http://example.org/sparql" ),
		                                                     PersistentCacheKey.ResponseMode.RESULT );

		cache.put( k, CompletableFuture.completedFuture( makeSolMapResponse(1) ) );

		assertFalse( cache.isEmpty() );
		l1.clear();

		// Entry still exists in L2
		assertFalse( cache.isEmpty() );
		l2.clear();

		assertTrue( cache.isEmpty() );

		cache.close();
	}

	/* --- Helper functions --- */

	protected Triple makeTestTriple( final int i ){
		return new TripleImpl( NodeFactory.createURI("http://example.org/s"),
		                       NodeFactory.createURI("http://example.org/p"),
		                       NodeFactory.createLiteralByValue(i) );
	}

	protected CardinalityResponse makeCardinalityResponse( final int i ) {
		return new CachedCardinalityResponse(i);
	}

	protected SolMapsResponse makeSolMapResponse( final int numberOfResults ) {
		final List<SolutionMapping> solMaps = new ArrayList<>();
		for ( int i = 0; i < numberOfResults; i++ ) {
			final Binding binding = BindingBuilder.create()
				.add( Var.alloc("o"), NodeFactory.createLiteralByValue(i) )
				.build();
			final SolutionMapping sm = new SolutionMappingImpl(binding);
			solMaps.add(sm);
		}

		return new SolMapsResponseImpl( solMaps, new Date() );
	}

	protected TPFResponse makeTPFResponse( final int numberOfResults ) {
		final List<Triple> matchingTriples = new ArrayList<>();
		for ( int i = 0; i < numberOfResults; i++ ) {
			matchingTriples.add( makeTestTriple(i) );
		}
		return makeTPFResponse(numberOfResults, "http://example.org/page");
	}

	protected TPFResponse makeTPFResponse( final int numberOfResults, final String nextPageURL ) {
		final List<Triple> matchingTriples = new ArrayList<>();
		for ( int i = 0; i < numberOfResults; i++ ) {
			matchingTriples.add( makeTestTriple(i) );
		}
		return new TPFResponseImpl( matchingTriples,
		                            new ArrayList<>(),
		                            nextPageURL,
		                            new Date() );
	}

	protected void assertSolMapsEqual( final SolMapsResponse expected, final SolMapsResponse actual )
			throws UnsupportedOperationDueToRetrievalError
	{
		assertNotNull(actual);
		final Set<SolutionMapping> expectedSet = new HashSet<>();
		expected.getResponseData().forEach( expectedSet::add );
		final Set<SolutionMapping> actualSet = new HashSet<>();
		actual.getResponseData().forEach( actualSet::add );
		assertEquals(expectedSet, actualSet);
	}

	protected void assertTPFEqual( final TPFResponse expected, final TPFResponse actual )
			throws UnsupportedOperationDueToRetrievalError
	{
		assertNotNull(actual);
		// Matched triples
		final Set<Triple> expectedMatched = new HashSet<>();
		expected.getResponseData().forEach( expectedMatched::add );
		final Set<Triple> actualMatched = new HashSet<>();
		actual.getResponseData().forEach( actualMatched::add );
		assertEquals(expectedMatched, actualMatched);
		// Metadata triples
		final Set<Triple> expectedMetadata = new HashSet<>();
		expected.getResponseData().forEach( expectedMetadata::add );
		final Set<Triple> actualMetadata = new HashSet<>();
		actual.getResponseData().forEach( actualMetadata::add );
		assertEquals(expectedMetadata, actualMetadata);
		// Next URL
		assertEquals( expected.getNextPageURL(), actual.getNextPageURL() );
	}

	protected void assertCardinalityEqual( final CardinalityResponse expected, final CardinalityResponse actual )
			throws UnsupportedOperationDueToRetrievalError
	{
		assertNotNull(actual);
		assertEquals( expected.getCardinality(), actual.getCardinality() );
	}

	public static class CachePoliciesForTest
				implements CachePolicies<PersistentCacheKey,
				                         CompletableFuture<? extends DataRetrievalResponse<?>>,
				                         PersistentCacheEntry>
	{
		final PersistentCacheEntryFactory cef = new PersistentCacheEntryFactory();

		final CacheReplacementPolicyFactory<PersistentCacheKey,
		                                    CompletableFuture<? extends DataRetrievalResponse<?>>,
		                                    PersistentCacheEntry
		                                   > crpf= new CacheReplacementPolicyFactory<>() {
			@Override
			public CacheReplacementPolicy<PersistentCacheKey,
			                              CompletableFuture<? extends DataRetrievalResponse<?>>,
			                              PersistentCacheEntry> create() {
				return new CacheReplacementPolicyLRU<>();
			}
		};

		final CacheInvalidationPolicy<PersistentCacheEntry, CompletableFuture<? extends DataRetrievalResponse<?>>> cip;

		public CachePoliciesForTest() {
			cip = new CacheInvalidationPolicyTimeToLive<>(60_000);
		}

		public CachePoliciesForTest( final long timeToLive ) {
			cip = new CacheInvalidationPolicyTimeToLive<>(timeToLive);
		}

		@Override
		public PersistentCacheEntryFactory getEntryFactory() {
			return cef;
		}

		@Override
		public CacheReplacementPolicyFactory<PersistentCacheKey,
		                                     CompletableFuture<? extends DataRetrievalResponse<?>>,
		                                     PersistentCacheEntry> getReplacementPolicyFactory() {
			return crpf;
		}

		@Override
		public CacheInvalidationPolicy<PersistentCacheEntry,
		                               CompletableFuture<? extends DataRetrievalResponse<?>>> getInvalidationPolicy() {
			return cip;
		}
	}
}
