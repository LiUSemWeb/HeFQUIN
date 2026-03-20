package se.liu.ida.hefquin.federation.access.impl.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

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
import se.liu.ida.hefquin.federation.access.impl.cache.chroniclemap.ChronicleMapCache;
import se.liu.ida.hefquin.federation.access.impl.cache.chroniclemap.ChronicleMapCacheEntry;
import se.liu.ida.hefquin.federation.access.impl.cache.chroniclemap.ChronicleMapCacheEntryFactory;
import se.liu.ida.hefquin.federation.access.impl.cache.chroniclemap.ChronicleMapCacheKey;
import se.liu.ida.hefquin.federation.access.impl.cache.chroniclemap.ChronicleMapCacheObject;
import se.liu.ida.hefquin.federation.access.impl.req.BRTPFRequestImpl;
import se.liu.ida.hefquin.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.federation.access.impl.req.TPFRequestImpl;

public class ChronicleMapCacheTest extends FederationTestBase
{

	protected final TriplePattern tp = new TriplePatternImpl( NodeFactory.createURI( "http://example.org/s"),
	                                                          NodeFactory.createURI( "http://example.org/p"),
	                                                          NodeFactory.createVariable("o") );

	@Test
	public void addAndGetTest() throws IOException {
		final ChronicleMapCache cache1 = new ChronicleMapCache( "cache/test/chronicle-map.dat", new CachePoliciesForTest() );
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

		final ChronicleMapCacheObject o1 = makeSPARQLObject(3);
		final ChronicleMapCacheObject o2 = makeCardinalityObject(3);
		final ChronicleMapCacheObject o3 = makeTPFObject(4);
		final ChronicleMapCacheObject o4 = makeCardinalityObject(4);
		final ChronicleMapCacheObject o5 = makeBRTPFObject(4);
		final ChronicleMapCacheObject o6 = makeCardinalityObject(4);

		// Add to map
		cache1.put(k1, o1);
		cache1.put(k2, o2);
		cache1.put(k3, o3);
		cache1.put(k4, o4);
		cache1.put(k5, o5);
		cache1.put(k6, o6);

		// Assert equals
		assertEquals( o1, cache1.get(k1) );
		assertEquals( o2, cache1.get(k2) );
		assertEquals( o3, cache1.get(k3) );
		assertEquals( o4, cache1.get(k4) );
		assertEquals( o5, cache1.get(k5) );
		assertEquals( o6, cache1.get(k6) );
		
		cache1.close();

		// Load map from disk
		final ChronicleMapCache cache2 = new ChronicleMapCache( "cache/test/chronicle-map.dat", new CachePoliciesForTest() );

		// Assert equals
		assertEquals( o1, cache2.get(k1) );
		assertEquals( o2, cache2.get(k2) );
		assertEquals( o3, cache2.get(k3) );
		assertEquals( o4, cache2.get(k4) );
		assertEquals( o5, cache2.get(k5) );
		assertEquals( o6, cache2.get(k6) );

		cache2.close();
	}

	@Test
	public void replaceTest() throws IOException {
		final ChronicleMapCache cache = new ChronicleMapCache( "cache/test/chronicle-map.dat", new CachePoliciesForTest() );
		cache.clear();

		final ChronicleMapCacheKey k = new ChronicleMapCacheKey( new SPARQLRequestImpl(tp),
		                                                         new SPARQLEndpointForTest("http://example.org/sparql"),
		                                                         ChronicleMapCacheKey.ResponseMode.RESULT );

		final ChronicleMapCacheObject o1 = makeSPARQLObject(1);
		final ChronicleMapCacheObject o2 = makeSPARQLObject(2);

		// Add to map
		cache.put(k, o1);
		cache.put(k, o2);

		// Assert equals
		assertEquals( 1, cache.size() );
		assertEquals( o2, cache.get(k) );
		cache.close();
	}

	@Test
	public void sizeLimitTest() throws IOException, InterruptedException {
		final ChronicleMapCache cache = new ChronicleMapCache( 25, "cache/test/chronicle-map.dat", new CachePoliciesForTest() );
		cache.clear();
		
		final ChronicleMapCacheObject o = makeSPARQLObject(1);
		
		for( int i=0; i < 50; i++){
			final ChronicleMapCacheKey k = new ChronicleMapCacheKey( new SPARQLRequestImpl(tp),
																	 new SPARQLEndpointForTest("http://example.org/sparql" + i),
																	 ChronicleMapCacheKey.ResponseMode.RESULT );
			Thread.sleep(10);
			cache.put(k, o);
		}

		// Assert size
		assertEquals( 25, cache.size() );
		
		// Assert keys
		for ( int i = 25; i < 50; i++ ) {
			final ChronicleMapCacheKey k = new ChronicleMapCacheKey( new SPARQLRequestImpl( tp ),
			                                                         new SPARQLEndpointForTest( "http://example.org/sparql" + i ),
			                                                         ChronicleMapCacheKey.ResponseMode.RESULT );
			assertNotNull( cache.get(k) );
		}
		cache.close();
	}

	@Test
	public void shrinkTest() throws IOException {
		final ChronicleMapCache cache1 = new ChronicleMapCache( 100, "cache/test/chronicle-map.dat", new CachePoliciesForTest() );
		cache1.clear();
		
		final ChronicleMapCacheObject o = makeSPARQLObject(1);
		
		for( int i=0; i < 100; i++){
			final ChronicleMapCacheKey k = new ChronicleMapCacheKey( new SPARQLRequestImpl(tp),
																	 new SPARQLEndpointForTest("http://example.org/sparql" + i),
																	 ChronicleMapCacheKey.ResponseMode.RESULT );
			cache1.put(k, o);
		}

		// Map size is 100
		assertEquals( 100, cache1.size() );
		cache1.close();

		// Map should now shrink to 50
		final ChronicleMapCache cache2 = new ChronicleMapCache( 50, "cache/test/chronicle-map.dat", new CachePoliciesForTest() );
		assertEquals( 50, cache2.size() );
		cache2.close();
	}

	@Test
	public void invalidationTest() throws IOException, InterruptedException {
		final ChronicleMapCache cache = new ChronicleMapCache( "cache/test/chronicle-map.dat", new CachePoliciesForTest() );
		cache.clear();
		
		final ChronicleMapCacheKey k = new ChronicleMapCacheKey( new SPARQLRequestImpl(tp),
		                                                         new SPARQLEndpointForTest("http://example.org/sparql"),
		                                                         ChronicleMapCacheKey.ResponseMode.RESULT );
		final ChronicleMapCacheObject o = makeSPARQLObject(1);
		cache.put(k, o);
		
		assertEquals( o, cache.get(k) );
		Thread.sleep(1250);
		assertNull( cache.get(k) );
		cache.close();
	}

	@Test
	public void leastRecentlyUsedTest() throws IOException, InterruptedException {
		final ChronicleMapCache cache = new ChronicleMapCache( 2, "cache/test/chronicle-map.dat", new CachePoliciesForTest() );
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
		final ChronicleMapCacheObject o = makeSPARQLObject(1);

		// Fill map
		cache.put(k1, o);
		Thread.sleep(10);
		cache.put(k2, o);
		Thread.sleep(10);

		// Move k1 to the end of the eviction queue
		cache.get(k1);
	
		// Replace k2 as the eviction candidate
		cache.put(k3, o);

		assertNull( cache.get(k2) );
		assertNotNull( cache.get(k1) );
		assertNotNull( cache.get(k3) );

		cache.close();
	}

	/* --- Helper functions --- */

	protected Triple makeTestTriple( final int i ){
		return new TripleImpl( NodeFactory.createURI("http://example.org/s"),
		                       NodeFactory.createURI("http://example.org/p"),
		                       NodeFactory.createLiteralByValue(i) );
	}

	protected ChronicleMapCacheObject makeCardinalityObject( final int i ) {
		return new ChronicleMapCacheObject(i);
	}

	protected ChronicleMapCacheObject makeSPARQLObject( final int numberOfResults ) {
		final List<SolutionMapping> solMaps = new ArrayList<>();
		for ( int i = 0; i < numberOfResults; i++ ) {
			final Binding binding = BindingBuilder.create()
				.add( Var.alloc("o"), NodeFactory.createLiteralByValue(i) )
				.build();
			final SolutionMapping sm = new SolutionMappingImpl(binding);
			solMaps.add(sm);
		}

		return new ChronicleMapCacheObject(solMaps);
	}

	protected ChronicleMapCacheObject makeBRTPFObject( final int numberOfResults ) {
		return makeTPFObject(numberOfResults);
	}

	protected ChronicleMapCacheObject makeTPFObject( final int numberOfResults ) {
		final List<Triple> matchingTriples = new ArrayList<>();
		for ( int i = 0; i < numberOfResults; i++ ) {
			matchingTriples.add( makeTestTriple(i) );
		}
		return new ChronicleMapCacheObject( matchingTriples,
		                                    new ArrayList<>(),
		                                    "http://example.org/page" );
	}

	public static class CachePoliciesForTest
				implements CachePolicies<ChronicleMapCacheKey,
				                         ChronicleMapCacheObject,
				                         ChronicleMapCacheEntry>
	{
		final ChronicleMapCacheEntryFactory cef = new ChronicleMapCacheEntryFactory();

		final CacheReplacementPolicyFactory<ChronicleMapCacheKey,
		                                    ChronicleMapCacheObject,
		                                    ChronicleMapCacheEntry
		                                   > crpf= new CacheReplacementPolicyFactory<>() {
			@Override
			public CacheReplacementPolicy<ChronicleMapCacheKey,
			                              ChronicleMapCacheObject,
			                              ChronicleMapCacheEntry> create() {
				return new CacheReplacementPolicyLRU<>();
			}
		};

		final CacheInvalidationPolicy<ChronicleMapCacheEntry,
		                              ChronicleMapCacheObject
		                             > cip = new CacheInvalidationPolicyTimeToLive<>(1000);

		@Override
		public ChronicleMapCacheEntryFactory getEntryFactory() {
			return cef;
		}

		@Override
		public CacheReplacementPolicyFactory<ChronicleMapCacheKey,
		                                     ChronicleMapCacheObject,
		                                     ChronicleMapCacheEntry> getReplacementPolicyFactory() {
			return crpf;
		}

		@Override
		public CacheInvalidationPolicy<ChronicleMapCacheEntry,
		                               ChronicleMapCacheObject> getInvalidationPolicy() {
			return cip;
		}
	}

}
