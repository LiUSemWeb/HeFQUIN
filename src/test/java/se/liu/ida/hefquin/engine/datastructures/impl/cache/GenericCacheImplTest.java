package se.liu.ida.hefquin.engine.datastructures.impl.cache;

import org.junit.Test;

import se.liu.ida.hefquin.engine.datastructures.Cache;

import static org.junit.Assert.*;

public class GenericCacheImplTest
{
	@Test
	public void putgetTest() {
		final Cache<Integer,String> c = createCache(2);
		c.put(1, "1");
		c.put(2, "2");

		assertEquals( "1", c.get(1) );
		assertEquals( "2", c.get(2) );

		c.put(2, "2b");

		assertEquals( "1", c.get(1) );
		assertEquals( "2b", c.get(2) );

		assertEquals( null, c.get(3) );

		c.put(3, "3");

		assertEquals( null, c.get(1) );
		assertEquals( "2b", c.get(2) );
		assertEquals( "3", c.get(3) );

		c.put(4, "4");
		c.put(5, "5");

		assertEquals( null, c.get(1) );
		assertEquals( null, c.get(2) );
		assertEquals( null, c.get(3) );
		assertEquals( "4", c.get(4) );
		assertEquals( "5", c.get(5) );
	}

	@Test
	public void evictTest1() {
		final Cache<Integer,String> c = createCache(2);
		c.put(1, "1");
		c.put(2, "2");

		final boolean e1 = c.evict(2);

		assertEquals( true, e1 );
		assertEquals( "1", c.get(1) );
		assertEquals( null, c.get(2) );

		c.put(2, "2b");

		assertEquals( "1", c.get(1) );
		assertEquals( "2b", c.get(2) );

		final boolean e2 = c.evict(3);

		assertEquals( false, e2 );
		assertEquals( "1", c.get(1) );
		assertEquals( "2b", c.get(2) );
	}

	@Test
	public void evictTest2() {
		final Cache<Integer,String> c = createCache(2);
		c.put(1, "1");

		final boolean e1 = c.evict(1); // already before capacity reached
		final boolean e2 = c.evict(2);

		assertEquals( true, e1 );
		assertEquals( false, e2 );
		assertEquals( null, c.get(1) );

		c.put(2, "2");
		c.put(3, "3");

		assertEquals( "2", c.get(2) );
		assertEquals( "3", c.get(3) );

		final boolean e3 = c.evict(3);
		c.put(4, "4");
		c.put(5, "5");

		assertEquals( true, e3 );
		assertEquals( null, c.get(2) ); // replaced
		assertEquals( null, c.get(3) ); // evicted
		assertEquals( "4", c.get(4) );
		assertEquals( "5", c.get(5) );
	}

	@Test
	public void evictTest3() {
		final Cache<Integer,String> c = createCache(2);
		c.put(1, "1");
		c.put(2, "2");

		final boolean e1 = c.evict(2, "2b");

		assertEquals( false, e1 );
		assertEquals( "1", c.get(1) );
		assertEquals( "2", c.get(2) );

		final boolean e2 = c.evict(2, "2");

		assertEquals( true, e2 );
		assertEquals( "1", c.get(1) );
		assertEquals( null, c.get(2) );
	}

	@Test
	public void clearTest() {
		final Cache<Integer,String> c = createCache(2);
		c.put(1, "1");
		c.put(2, "2");

		assertFalse( c.isEmpty() );

		c.clear();

		assertTrue( c.isEmpty() );
		assertEquals( null, c.get(1) );
		assertEquals( null, c.get(2) );

		c.put(1, "1");
		c.put(2, "2");

		assertFalse( c.isEmpty() );
		assertEquals( "1", c.get(1) );
		assertEquals( "2", c.get(2) );
	}

	protected static Cache<Integer,String> createCache( final int capacity ) {
		return new GenericCacheImpl<>( capacity, new CachePoliciesForTest() );
	}

	public static class CachePoliciesForTest implements CachePolicies<Integer,String,CacheEntryBase<String>>
	{
		protected final CacheReplacementPolicyFactory<Integer, String, CacheEntryBase<String>> rpf = new CacheReplacementPolicyFactory<>() {
			@Override
			public CacheReplacementPolicy<Integer, String, CacheEntryBase<String>> create() {
				return new CacheReplacementPolicyLRU<>();
			}
		};

		protected final CacheEntryFactory<CacheEntryBase<String>, String> ef = new CacheEntryFactory<CacheEntryBase<String>, String>() {
			@Override
			public CacheEntryBase<String> createCacheEntry( final String obj ) {
				return new CacheEntryBase<String>(obj);
			}
		};

		@Override
		public CacheReplacementPolicyFactory<Integer, String, CacheEntryBase<String>> getReplacementPolicyFactory() {
			return rpf;
		}
		
		@Override
		public CacheInvalidationPolicy<CacheEntryBase<String>, String> getInvalidationPolicy() {
			return new CacheInvalidationPolicyAlwaysValid<>();
		}
		
		@Override
		public CacheEntryFactory<CacheEntryBase<String>, String> getEntryFactory() {
			return ef;
		}
	}

}
