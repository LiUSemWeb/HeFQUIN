package se.liu.ida.hefquin.engine.federation.access.impl.cache;

import org.apache.jena.graph.NodeFactory;
import org.junit.Test;

import se.liu.ida.hefquin.base.data.VocabularyMapping;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheEntryFactory;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheInvalidationPolicy;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheInvalidationPolicyAlwaysValid;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CachePolicies;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheReplacementPolicy;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheReplacementPolicyFactory;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheReplacementPolicyLRU;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.base.query.impl.TriplePatternImpl;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.SPARQLEndpointInterface;
import se.liu.ida.hefquin.engine.federation.access.impl.iface.SPARQLEndpointInterfaceImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.req.SPARQLRequestImpl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

public class ChronicleMapCardinaltyCacheTest
{
	protected final FederationMember fm = new SPARQLEndpointForTest( "http://example.org" );

	@Test
	public void loadTooLargeCacheFromDisk() throws IOException, InterruptedException {
		final CardinalityCacheKey k1 = new CardinalityCacheKey( createRequest( "1" ), fm );
		final CardinalityCacheKey k2 = new CardinalityCacheKey( createRequest( "2" ), fm );
		final CardinalityCacheKey k3 = new CardinalityCacheKey( createRequest( "3" ), fm );
		final CardinalityCacheKey k4 = new CardinalityCacheKey( createRequest( "4" ), fm );
		final CardinalityCacheKey k5 = new CardinalityCacheKey( createRequest( "5" ), fm );

		// Attention: The precision of the timestamps is in milliseconds. The delay between
		// insertions ensures that order of eviction is predictable when loaded from disk.
		final ChronicleMapCardinalityCache cache1 = createCache( 5 );
		cache1.clear();
		cache1.put( k1, 1 );
		Thread.sleep( 1 );
		cache1.put( k2, 2 );
		Thread.sleep( 1 );
		cache1.put( k3, 3 );
		Thread.sleep( 1 );
		cache1.put( k4, 4 );
		Thread.sleep( 1 );
		cache1.put( k5, 5 );

		assertEquals( (Integer) 1, cache1.get( k1 ).getObject() );
		assertEquals( (Integer) 2, cache1.get( k2 ).getObject() );
		assertEquals( (Integer) 3, cache1.get( k3 ).getObject() );
		assertEquals( (Integer) 4, cache1.get( k4 ).getObject() );
		assertEquals( (Integer) 5, cache1.get( k5 ).getObject() );

		final ChronicleMapCardinalityCache cache2 = createCache( 2 );
		assertEquals( null, cache2.get( k1 ) );
		assertEquals( null, cache2.get( k2 ) );
		assertEquals( null, cache2.get( k3 ) );
		assertEquals( (Integer) 4, cache2.get( k4 ).getObject() );
		assertEquals( (Integer) 5, cache2.get( k5 ).getObject() );
	}

	@Test
	public void loadFromDiskTest() throws IOException {
		final CardinalityCacheKey k1 = new CardinalityCacheKey( createRequest( "1" ), fm );
		final CardinalityCacheKey k2 = new CardinalityCacheKey( createRequest( "2" ), fm );
		final CardinalityCacheKey k3 = new CardinalityCacheKey( createRequest( "3" ), fm );
		final CardinalityCacheKey k4 = new CardinalityCacheKey( createRequest( "4" ), fm );
		final CardinalityCacheKey k5 = new CardinalityCacheKey( createRequest( "5" ), fm );

		final ChronicleMapCardinalityCache cache1 = createCache( 2 );
		cache1.clear();
		cache1.put( k1, 1 );
		cache1.put( k2, 2 );

		final ChronicleMapCardinalityCache cache2 = createCache( 2 );
		assertEquals( (Integer) 1, cache2.get( k1 ).getObject() );
		assertEquals( (Integer) 2, cache2.get( k2 ).getObject() );

		cache2.put( k2, -2 );
		assertEquals( Integer.valueOf( 1 ), cache2.get( k1 ).getObject() );
		assertEquals( Integer.valueOf( -2 ), cache2.get( k2 ).getObject() );

		assertEquals( null, cache2.get( k3 ) );

		cache2.put( k3, 3 );

		final ChronicleMapCardinalityCache cache3 = createCache( 2 );
		assertEquals( null, cache3.get( k1 ) );
		assertEquals( Integer.valueOf( -2 ), cache3.get( k2 ).getObject() );
		assertEquals( Integer.valueOf( 3 ), cache3.get( k3 ).getObject() );

		cache3.put( k4, 4 );
		cache3.put( k5, 5 );

		final ChronicleMapCardinalityCache cache4 = createCache( 2 );
		assertEquals( null, cache4.get( k1 ) );
		assertEquals( null, cache4.get( k2 ) );
		assertEquals( null, cache4.get( k3 ) );
		assertEquals( Integer.valueOf( 4 ), cache4.get( k4 ).getObject() );
		assertEquals( Integer.valueOf( 5 ), cache4.get( k5 ).getObject() );
	}

	@Test
	public void putgetTest() throws IOException {
		final ChronicleMapCardinalityCache cache = createCache( 2 );
		cache.clear();

		final CardinalityCacheKey k1 = new CardinalityCacheKey( createRequest( "1" ), fm );
		final CardinalityCacheKey k2 = new CardinalityCacheKey( createRequest( "2" ), fm );
		final CardinalityCacheKey k3 = new CardinalityCacheKey( createRequest( "3" ), fm );
		final CardinalityCacheKey k4 = new CardinalityCacheKey( createRequest( "4" ), fm );
		final CardinalityCacheKey k5 = new CardinalityCacheKey( createRequest( "5" ), fm );

		cache.put( k1, 1 );
		cache.put( k2, 2 );
		assertEquals( (Integer) 1, cache.get( k1 ).getObject() );
		assertEquals( (Integer) 2, cache.get( k2 ).getObject() );

		cache.put( k2, -2 );
		assertEquals( Integer.valueOf( 1 ), cache.get( k1 ).getObject() );
		assertEquals( Integer.valueOf( -2 ), cache.get( k2 ).getObject() );

		assertEquals( null, cache.get( k3 ) );

		cache.put( k3, 3 );

		assertEquals( null, cache.get( k1 ) );
		assertEquals( Integer.valueOf( -2 ), cache.get( k2 ).getObject() );
		assertEquals( Integer.valueOf( 3 ), cache.get( k3 ).getObject() );

		cache.put( k4, 4 );
		cache.put( k5, 5 );

		assertEquals( null, cache.get( k1 ) );
		assertEquals( null, cache.get( k2 ) );
		assertEquals( null, cache.get( k3 ) );
		assertEquals( Integer.valueOf( 4 ), cache.get( k4 ).getObject() );
		assertEquals( Integer.valueOf( 5 ), cache.get( k5 ).getObject() );
	}

	@Test
	public void evictTest1() throws IOException {
		final ChronicleMapCardinalityCache cache = createCache( 2 );
		cache.clear();

		final CardinalityCacheKey k1 = new CardinalityCacheKey( createRequest( "1" ), fm );
		final CardinalityCacheKey k2 = new CardinalityCacheKey( createRequest( "2" ), fm );
		final CardinalityCacheKey k3 = new CardinalityCacheKey( createRequest( "3" ), fm );

		cache.put( k1, 1 );
		cache.put( k2, 2 );

		final boolean e1 = cache.evict( k2 );

		assertEquals( true, e1 );
		assertEquals( Integer.valueOf( 1 ), cache.get( k1 ).getObject() );
		assertEquals( null, cache.get( k2 ) );

		cache.put( k2, -2 );

		assertEquals( Integer.valueOf( 1 ), cache.get( k1 ).getObject() );
		assertEquals( Integer.valueOf( -2 ), cache.get( k2 ).getObject() );

		final boolean e2 = cache.evict( k3 );

		assertEquals( false, e2 );
		assertEquals( Integer.valueOf( 1 ), cache.get( k1 ).getObject() );
		assertEquals( Integer.valueOf( -2 ), cache.get( k2 ).getObject() );
	}

	@Test
	public void evictTest2() throws IOException {
		final ChronicleMapCardinalityCache cache = createCache( 2 );
		cache.clear();

		final CardinalityCacheKey k1 = new CardinalityCacheKey( createRequest( "1" ), fm );
		final CardinalityCacheKey k2 = new CardinalityCacheKey( createRequest( "2" ), fm );
		final CardinalityCacheKey k3 = new CardinalityCacheKey( createRequest( "3" ), fm );
		final CardinalityCacheKey k4 = new CardinalityCacheKey( createRequest( "4" ), fm );
		final CardinalityCacheKey k5 = new CardinalityCacheKey( createRequest( "5" ), fm );

		cache.put( k1, 1 );

		final boolean e1 = cache.evict( k1 ); // already before capacity reached
		final boolean e2 = cache.evict( k2 );

		assertEquals( true, e1 );
		assertEquals( false, e2 );
		assertEquals( null, cache.get( k1 ) );

		cache.put( k2, 2 );
		cache.put( k3, 3 );

		assertEquals( Integer.valueOf( 2 ), cache.get( k2 ).getObject() );
		assertEquals( Integer.valueOf( 3 ), cache.get( k3 ).getObject() );

		final boolean e3 = cache.evict( k3 );
		cache.put( k4, 4 );
		cache.put( k5, 5 );

		assertEquals( true, e3 );
		assertEquals( null, cache.get( k2 ) ); // replaced (evicted after capacity reached)
		assertEquals( null, cache.get( k3 ) ); // evicted
		assertEquals( Integer.valueOf( 4 ), cache.get( k4 ).getObject() );
		assertEquals( Integer.valueOf( 5 ), cache.get( k5 ).getObject() );
	}

	@Test
	public void evictTest3() throws IOException {
		final ChronicleMapCardinalityCache cache = createCache( 2 );
		cache.clear();

		final CardinalityCacheKey k1 = new CardinalityCacheKey( createRequest( "1" ), fm );
		final CardinalityCacheKey k2 = new CardinalityCacheKey( createRequest( "2" ), fm );

		cache.put( k1, 1 );
		cache.put( k2, 2 );

		final boolean e1 = cache.evict( k2, -2 );

		assertEquals( false, e1 );
		assertEquals( Integer.valueOf( 1 ), cache.get( k1 ).getObject() );
		assertEquals( Integer.valueOf( 2 ), cache.get( k2 ).getObject() );

		final boolean e2 = cache.evict( k2, 2 );
		assertEquals( true, e2 );
		assertEquals( Integer.valueOf( 1 ), cache.get( k1 ).getObject() );
		assertEquals( null, cache.get( k2 ) );
	}

	@Test
	public void clearTest() throws IOException {
		final ChronicleMapCardinalityCache cache = createCache( 2 );
		cache.clear();

		final CardinalityCacheKey k1 = new CardinalityCacheKey( createRequest( "1" ), fm );
		final CardinalityCacheKey k2 = new CardinalityCacheKey( createRequest( "2" ), fm );

		cache.put( k1, 1 );
		cache.put( k2, 2 );

		assertFalse( cache.isEmpty() );

		cache.clear();

		assertTrue( cache.isEmpty() );
		assertEquals( null, cache.get( k1 ) );
		assertEquals( null, cache.get( k2 ) );

		cache.put( k1, 1 );
		cache.put( k2, 2 );

		assertFalse( cache.isEmpty() );
		assertEquals( Integer.valueOf( 1 ), cache.get( k1 ).getObject() );
		assertEquals( Integer.valueOf( 2 ), cache.get( k2 ).getObject() );
	}

	protected static ChronicleMapCardinalityCache createCache( final int capacity ) throws IOException {
		return new ChronicleMapCardinalityCache( new CachePoliciesForTest(), capacity );
	}

	protected static DataRetrievalRequest createRequest( final String s ) {
		final TriplePattern tp = new TriplePatternImpl( NodeFactory.createVariable( s ),
				NodeFactory.createVariable( "p" ), NodeFactory.createVariable( "o" ) );
		return new SPARQLRequestImpl( tp );
	}

	protected static class SPARQLEndpointForTest implements SPARQLEndpoint
	{
		final String url;

		public SPARQLEndpointForTest( final String url ) {
			this.url = url;
		}

		@Override
		public VocabularyMapping getVocabularyMapping() {
			return null;
		}

		@Override
		public SPARQLEndpointInterface getInterface() {
			return new SPARQLEndpointInterfaceImpl( url );
		}

	}

	public static class CachePoliciesForTest
			implements CachePolicies<CardinalityCacheKey, Integer, CardinalityCacheEntry>
	{
		protected final CacheReplacementPolicyFactory<CardinalityCacheKey, Integer, CardinalityCacheEntry> rpf = new CacheReplacementPolicyFactory<>() {
			@Override
			public CacheReplacementPolicy<CardinalityCacheKey, Integer, CardinalityCacheEntry> create() {
				return new CacheReplacementPolicyLRU<>();
			}
		};

		protected final CardinalityCacheEntryFactory ef = new CardinalityCacheEntryFactory();

		@Override
		public CacheReplacementPolicyFactory<CardinalityCacheKey, Integer, CardinalityCacheEntry> getReplacementPolicyFactory() {
			return rpf;
		}

		@Override
		public CacheInvalidationPolicy<CardinalityCacheEntry, Integer> getInvalidationPolicy() {
			return new CacheInvalidationPolicyAlwaysValid<>();
		}

		@Override
		public CacheEntryFactory<CardinalityCacheEntry, Integer> getEntryFactory() {
			return ef;
		}
	}

}
