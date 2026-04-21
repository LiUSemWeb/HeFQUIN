package se.liu.ida.hefquin.federation.access.impl.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
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
import se.liu.ida.hefquin.federation.access.impl.cache.chroniclemap.ChronicleMapCacheEntry;
import se.liu.ida.hefquin.federation.access.impl.cache.chroniclemap.ChronicleMapCacheEntryFactory;
import se.liu.ida.hefquin.federation.access.impl.cache.chroniclemap.ChronicleMapCacheKey;
import se.liu.ida.hefquin.federation.access.impl.req.BRTPFRequestImpl;
import se.liu.ida.hefquin.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.federation.access.impl.req.TPFRequestImpl;
import se.liu.ida.hefquin.federation.access.impl.response.CachedCardinalityResponse;
import se.liu.ida.hefquin.federation.access.impl.response.SolMapsResponseImpl;
import se.liu.ida.hefquin.federation.access.impl.response.TPFResponseImpl;

public class ChronicleMapCacheTest extends FederationTestBase
{

	protected final TriplePattern tp = new TriplePatternImpl( NodeFactory.createURI("http://example.org/s"),
	                                                          NodeFactory.createURI("http://example.org/p"),
	                                                          NodeFactory.createVariable("o") );

	@Test
	public void addAndGetTest() throws Exception {
		final ChronicleMapCache cache1 = new ChronicleMapCache( "cache/test/chronicle-map.dat",
		                                                        new CachePoliciesForTest() );
		cache1.clear();

		final ChronicleMapCacheKey k1 = new ChronicleMapCacheKey( new SPARQLRequestImpl(tp),
		                                                          new SPARQLEndpointForTest("http://example.org/sparql"),
		                                                          ChronicleMapCacheKey.ResponseMode.RESULT );

		final ChronicleMapCacheKey k2 = new ChronicleMapCacheKey( new SPARQLRequestImpl(tp),
		                                                          new SPARQLEndpointForTest("http://example.org/sparql"),
		                                                          ChronicleMapCacheKey.ResponseMode.COUNT );

		final ChronicleMapCacheKey k3 = new ChronicleMapCacheKey( new TPFRequestImpl(tp),
		                                                          new TPFServerForTest("http://example.org/tpf"),
		                                                          ChronicleMapCacheKey.ResponseMode.RESULT );

		final ChronicleMapCacheKey k4 = new ChronicleMapCacheKey( new TPFRequestImpl(tp),
		                                                          new TPFServerForTest("http://example.org/tpf"),
		                                                          ChronicleMapCacheKey.ResponseMode.COUNT );

		final ChronicleMapCacheKey k5 = new ChronicleMapCacheKey( new BRTPFRequestImpl( tp, new HashSet<>() ),
		                                                          new BRTPFServerForTest("http://example.org/brtpf"),
		                                                          ChronicleMapCacheKey.ResponseMode.RESULT );

		final ChronicleMapCacheKey k6 = new ChronicleMapCacheKey( new BRTPFRequestImpl(tp, new HashSet<>() ),
		                                                          new BRTPFServerForTest("http://example.org/brtpf"),
		                                                          ChronicleMapCacheKey.ResponseMode.COUNT );

		final SolMapsResponse o1 = makeSolMapResponse(3);
		final CardinalityResponse o2 = makeCardinalityResponse(3);
		final TPFResponse o3 = makeTPFResponse(4);
		final CardinalityResponse o4 = makeCardinalityResponse(4);
		final TPFResponse o5 = makeTPFResponse(5);
		final CardinalityResponse o6 = makeCardinalityResponse(5);

		// Add to map
		cache1.put( k1, CompletableFuture.completedFuture(o1) );
		cache1.put( k2, CompletableFuture.completedFuture(o2) );
		cache1.put( k3, CompletableFuture.completedFuture(o3) );
		cache1.put( k4, CompletableFuture.completedFuture(o4) );
		cache1.put( k5, CompletableFuture.completedFuture(o5) );
		cache1.put( k6, CompletableFuture.completedFuture(o6) );

		// Assert equals
		assertEquals( o1, cache1.get(k1).get() );
		assertEquals( o2, cache1.get(k2).get() );
		assertEquals( o3, cache1.get(k3).get() );
		assertEquals( o4, cache1.get(k4).get() );
		assertEquals( o5, cache1.get(k5).get() );
		assertEquals( o6, cache1.get(k6).get() );
		
		cache1.close();

		// Load map from disk
		final ChronicleMapCache cache2 = new ChronicleMapCache( "cache/test/chronicle-map.dat",
		                                                        new CachePoliciesForTest() );

		// Assert equals
		assertSolMapsEqual( o1, (SolMapsResponse) cache2.get(k1).get() );
		assertCardinalityEqual( o2, (CardinalityResponse) cache2.get(k2).get() );
		assertTPFEqual( o3, (TPFResponse) cache2.get(k3).get() );
		assertCardinalityEqual( o4, (CardinalityResponse) cache2.get(k4).get() );
		assertTPFEqual( o5, (TPFResponse) cache2.get(k5).get() );
		assertCardinalityEqual( o6, (CardinalityResponse) cache2.get(k6).get() );

		cache2.close();
	}

	@Test
	public void replaceTest() throws IOException, InterruptedException, ExecutionException {
		final ChronicleMapCache cache = new ChronicleMapCache( "cache/test/chronicle-map.dat",
		                                                       new CachePoliciesForTest() );
		cache.clear();

		final ChronicleMapCacheKey k = new ChronicleMapCacheKey( new SPARQLRequestImpl(tp),
		                                                         new SPARQLEndpointForTest("http://example.org/sparql"),
		                                                         ChronicleMapCacheKey.ResponseMode.RESULT );

		final DataRetrievalResponse<?> o1 = makeSolMapResponse(1);
		final DataRetrievalResponse<?> o2 = makeSolMapResponse(2);

		// Add to map
		cache.put( k, CompletableFuture.completedFuture(o1) );
		cache.put( k, CompletableFuture.completedFuture(o2) );

		// Assert equals
		assertEquals( 1, cache.size() );
		assertEquals( o2, cache.get(k).get() );
		cache.close();
	}

	@Test
	public void sizeLimitTest() throws IOException, InterruptedException, ExecutionException {
		final ChronicleMapCache cache = new ChronicleMapCache( 25,
		                                                       "cache/test/chronicle-map.dat",
		                                                       new CachePoliciesForTest() );
		cache.clear();
		
		final DataRetrievalResponse<?> o = makeSolMapResponse(1);
		
		for( int i=0; i < 50; i++){
			final ChronicleMapCacheKey k = new ChronicleMapCacheKey( new SPARQLRequestImpl(tp),
			                                                         new SPARQLEndpointForTest("http://example.org/sparql" + i),
			                                                         ChronicleMapCacheKey.ResponseMode.RESULT );
			Thread.sleep(10);
			cache.put( k, CompletableFuture.completedFuture(o) );
		}

		// Assert size
		assertEquals( 25, cache.size() );
		
		// Assert keys
		for ( int i = 25; i < 50; i++ ) {
			final ChronicleMapCacheKey k = new ChronicleMapCacheKey( new SPARQLRequestImpl( tp ),
			                                                         new SPARQLEndpointForTest( "http://example.org/sparql" + i ),
			                                                         ChronicleMapCacheKey.ResponseMode.RESULT );
			assertNotNull( cache.get(k).get() );
		}
		cache.close();
	}

	@Test
	public void shrinkTest() throws IOException {
		final ChronicleMapCache cache1 = new ChronicleMapCache( 100,
		                                                        "cache/test/chronicle-map.dat",
		                                                        new CachePoliciesForTest() );
		cache1.clear();
		
		final DataRetrievalResponse<?> o = makeSolMapResponse(1);
		
		for( int i=0; i < 100; i++){
			final ChronicleMapCacheKey k = new ChronicleMapCacheKey( new SPARQLRequestImpl(tp),
			                                                         new SPARQLEndpointForTest("http://example.org/sparql" + i),
			                                                         ChronicleMapCacheKey.ResponseMode.RESULT );
			cache1.put( k, CompletableFuture.completedFuture(o) );
		}

		// Map size is 100
		assertEquals( 100, cache1.size() );
		cache1.close();

		// Map should now shrink to 50
		final ChronicleMapCache cache2 = new ChronicleMapCache( 50,
		                                                        "cache/test/chronicle-map.dat",
		                                                        new CachePoliciesForTest() );
		assertEquals( 50, cache2.size() );
		cache2.close();
	}

	@Test
	public void invalidationTest() throws IOException, InterruptedException, ExecutionException {
		final ChronicleMapCache cache = new ChronicleMapCache( "cache/test/chronicle-map.dat",
		                                                       new CachePoliciesForTest(1000) );
		cache.clear();
		
		final ChronicleMapCacheKey k = new ChronicleMapCacheKey( new SPARQLRequestImpl(tp),
		                                                         new SPARQLEndpointForTest("http://example.org/sparql"),
		                                                         ChronicleMapCacheKey.ResponseMode.RESULT );
		final DataRetrievalResponse<?> o = makeSolMapResponse(1);
		cache.put( k, CompletableFuture.completedFuture(o) );
		
		assertEquals( o, cache.get(k).get() );
		Thread.sleep(1250);
		assertNull( cache.get(k) );
		cache.close();
	}

	@Test
	public void leastRecentlyUsedTest() throws IOException, InterruptedException, ExecutionException {
		final ChronicleMapCache cache = new ChronicleMapCache( 2,
		                                                       "cache/test/chronicle-map.dat",
		                                                       new CachePoliciesForTest() );
		cache.clear();
		
		final ChronicleMapCacheKey k1 = new ChronicleMapCacheKey( new SPARQLRequestImpl(tp),
		                                                          new SPARQLEndpointForTest( "http://example.org/sparql" + 1 ),
		                                                          ChronicleMapCacheKey.ResponseMode.RESULT );
		final ChronicleMapCacheKey k2 = new ChronicleMapCacheKey( new SPARQLRequestImpl(tp),
		                                                          new SPARQLEndpointForTest( "http://example.org/sparql" + 2 ),
		                                                          ChronicleMapCacheKey.ResponseMode.RESULT );
		final ChronicleMapCacheKey k3 = new ChronicleMapCacheKey( new SPARQLRequestImpl(tp),
		                                                          new SPARQLEndpointForTest( "http://example.org/sparql" + 3 ),
		                                                          ChronicleMapCacheKey.ResponseMode.RESULT );
		final DataRetrievalResponse<?> o = makeSolMapResponse(1);

		// Fill map
		cache.put( k1, CompletableFuture.completedFuture(o) );
		Thread.sleep(10);
		cache.put( k2, CompletableFuture.completedFuture(o) );
		Thread.sleep(10);

		// Move k1 to the end of the eviction queue
		cache.get(k1);
	
		// Replace k2 as the eviction candidate
		cache.put( k3, CompletableFuture.completedFuture(o) );

		assertNull( cache.get(k2) );
		assertNotNull( cache.get(k1).get() );
		assertNotNull( cache.get(k3).get() );

		cache.close();
	}

	@Test
	public void readFromDisk() throws IOException, InterruptedException, ExecutionException, Throwable {
		final ChronicleMapCache cache = new ChronicleMapCache( "cache/test/chronicle-map.dat",
		                                                        new CachePoliciesForTest() );
		cache.clear();

		final ChronicleMapCacheKey k1 = new ChronicleMapCacheKey( new SPARQLRequestImpl(tp),
		                                                          new SPARQLEndpointForTest("http://example.org/sparql"),
		                                                          ChronicleMapCacheKey.ResponseMode.RESULT );

		final ChronicleMapCacheKey k2 = new ChronicleMapCacheKey( new SPARQLRequestImpl(tp),
		                                                          new SPARQLEndpointForTest("http://example.org/sparql"),
		                                                          ChronicleMapCacheKey.ResponseMode.COUNT );

		final ChronicleMapCacheKey k3 = new ChronicleMapCacheKey( new TPFRequestImpl(tp),
		                                                          new TPFServerForTest("http://example.org/tpf"),
		                                                          ChronicleMapCacheKey.ResponseMode.RESULT );

		final ChronicleMapCacheKey k4 = new ChronicleMapCacheKey( new TPFRequestImpl(tp),
		                                                          new TPFServerForTest("http://example.org/tpf"),
		                                                          ChronicleMapCacheKey.ResponseMode.COUNT );

		final ChronicleMapCacheKey k5 = new ChronicleMapCacheKey( new BRTPFRequestImpl( tp, new HashSet<>() ),
		                                                          new BRTPFServerForTest("http://example.org/brtpf"),
		                                                          ChronicleMapCacheKey.ResponseMode.RESULT );

		final ChronicleMapCacheKey k6 = new ChronicleMapCacheKey( new BRTPFRequestImpl(tp, new HashSet<>() ),
		                                                          new BRTPFServerForTest("http://example.org/brtpf"),
		                                                          ChronicleMapCacheKey.ResponseMode.COUNT );

		final SolMapsResponse o1 = makeSolMapResponse(3);
		final CardinalityResponse o2 = makeCardinalityResponse(3);
		final TPFResponse o3 = makeTPFResponse(4);
		final CardinalityResponse o4 = makeCardinalityResponse(4);
		final TPFResponse o5 = makeTPFResponse(5);
		final CardinalityResponse o6 = makeCardinalityResponse(5);

		// Add to map
		cache.put( k1, CompletableFuture.completedFuture(o1) );
		cache.put( k2, CompletableFuture.completedFuture(o2) );
		cache.put( k3, CompletableFuture.completedFuture(o3) );
		cache.put( k4, CompletableFuture.completedFuture(o4) );
		cache.put( k5, CompletableFuture.completedFuture(o5) );
		cache.put( k6, CompletableFuture.completedFuture(o6) );

		cache.close();

		// Load map from disk
		final ChronicleMapCache loadedCache = new ChronicleMapCache( "cache/test/chronicle-map.dat",
		                                                             new CachePoliciesForTest() );

		// Assert equals
		assertSolMapsEqual( o1, (SolMapsResponse) loadedCache.get(k1).get() );
		assertCardinalityEqual( o2, (CardinalityResponse) loadedCache.get(k2).get() );
		assertTPFEqual( o3, (TPFResponse) loadedCache.get(k3).get() );
		assertCardinalityEqual( o4, (CardinalityResponse) loadedCache.get(k4).get() );
		assertTPFEqual( o5, (TPFResponse) loadedCache.get(k5).get() );
		assertCardinalityEqual( o6, (CardinalityResponse) loadedCache.get(k6).get() );

		loadedCache.close();
	}

	@Test
	public void invalidationFromDiskTest() throws IOException, InterruptedException {
		final ChronicleMapCache cache = new ChronicleMapCache( "cache/test/chronicle-map.dat",
		                                                       new CachePoliciesForTest(1000) );
		cache.clear();

		final ChronicleMapCacheKey k = new ChronicleMapCacheKey( new SPARQLRequestImpl(tp),
		                                                         new SPARQLEndpointForTest("http://example.org/sparql"),
		                                                         ChronicleMapCacheKey.ResponseMode.RESULT );
		final DataRetrievalResponse<?> o = makeSolMapResponse(1);
		cache.put(k, CompletableFuture.completedFuture(o) );
		cache.close();
		Thread.sleep(1250);

		final ChronicleMapCache loadedCache = new ChronicleMapCache( "cache/test/chronicle-map.dat",
		                                                             new CachePoliciesForTest(1000) );
		assertNull( loadedCache.get(k) );
		loadedCache.close();
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
		return new TPFResponseImpl( matchingTriples,
		                            new ArrayList<>(),
		                            "http://example.org/page",
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
				implements CachePolicies<ChronicleMapCacheKey,
				                         CompletableFuture<? extends DataRetrievalResponse<?>>,
				                         ChronicleMapCacheEntry>
	{
		final ChronicleMapCacheEntryFactory cef = new ChronicleMapCacheEntryFactory();

		final CacheReplacementPolicyFactory<ChronicleMapCacheKey,
		                                    CompletableFuture<? extends DataRetrievalResponse<?>>,
		                                    ChronicleMapCacheEntry
		                                   > crpf= new CacheReplacementPolicyFactory<>() {
			@Override
			public CacheReplacementPolicy<ChronicleMapCacheKey,
			                              CompletableFuture<? extends DataRetrievalResponse<?>>,
			                              ChronicleMapCacheEntry> create() {
				return new CacheReplacementPolicyLRU<>();
			}
		};

		final CacheInvalidationPolicy<ChronicleMapCacheEntry, CompletableFuture<? extends DataRetrievalResponse<?>>> cip;

		public CachePoliciesForTest() {
			cip = new CacheInvalidationPolicyTimeToLive<>(60_000);
		}

		public CachePoliciesForTest( final long timeToLive ) {
			cip = new CacheInvalidationPolicyTimeToLive<>(timeToLive);
		}

		@Override
		public ChronicleMapCacheEntryFactory getEntryFactory() {
			return cef;
		}

		@Override
		public CacheReplacementPolicyFactory<ChronicleMapCacheKey,
		                                     CompletableFuture<? extends DataRetrievalResponse<?>>,
		                                     ChronicleMapCacheEntry> getReplacementPolicyFactory() {
			return crpf;
		}

		@Override
		public CacheInvalidationPolicy<ChronicleMapCacheEntry,
		                               CompletableFuture<? extends DataRetrievalResponse<?>>> getInvalidationPolicy() {
			return cip;
		}
	}
}
