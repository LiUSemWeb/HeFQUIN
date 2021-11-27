package se.liu.ida.hefquin.engine.datastructures.impl.cache;

import org.junit.Test;

import static org.junit.Assert.*;

import java.util.Iterator;

public class CacheReplacementPolicyLRUTest
{
	@Test
	public void getEvictionCandidatesTest() {
		final CacheReplacementPolicy<Integer,String,CacheEntryBase<String>> p = new CacheReplacementPolicyLRU<>();
		p.entryWasAdded(1, null);
		p.entryWasAdded(2, null);
		p.entryWasAdded(3, null);

		final Iterator<Integer> it1 = p.getEvictionCandidates(1).iterator();
		assertEquals( 1, it1.next().intValue() );
		assertFalse( it1.hasNext() );

		final Iterator<Integer> it2 = p.getEvictionCandidates(2).iterator();
		assertEquals( 1, it2.next().intValue() );
		assertEquals( 2, it2.next().intValue() );
		assertFalse( it2.hasNext() );

		final Iterator<Integer> it3 = p.getEvictionCandidates(3).iterator();
		assertEquals( 1, it3.next().intValue() );
		assertEquals( 2, it3.next().intValue() );
		assertEquals( 3, it3.next().intValue() );
		assertFalse( it3.hasNext() );

		final Iterator<Integer> it4 = p.getEvictionCandidates(4).iterator();
		assertEquals( 1, it4.next().intValue() );
		assertEquals( 2, it4.next().intValue() );
		assertEquals( 3, it4.next().intValue() );
		assertFalse( it4.hasNext() );
	}

	@Test
	public void entryWasEvictedTest() {
		final CacheReplacementPolicy<Integer,String,CacheEntryBase<String>> p = new CacheReplacementPolicyLRU<>();
		p.entryWasAdded(1, null);
		p.entryWasAdded(2, null);
		p.entryWasAdded(3, null);

		final Iterator<Integer> it1a = p.getEvictionCandidates(1).iterator();
		assertEquals( 1, it1a.next().intValue() );

		final Iterator<Integer> it1b = p.getEvictionCandidates(1).iterator();
		assertEquals( 1, it1b.next().intValue() );

		p.entryWasEvicted(1);

		final Iterator<Integer> it2a = p.getEvictionCandidates(1).iterator();
		assertEquals( 2, it2a.next().intValue() );
		assertFalse( it2a.hasNext() );

		final Iterator<Integer> it2b = p.getEvictionCandidates(2).iterator();
		assertEquals( 2, it2b.next().intValue() );
		assertEquals( 3, it2b.next().intValue() );
		assertFalse( it2b.hasNext() );

		final Iterator<Integer> it2c = p.getEvictionCandidates(3).iterator();
		assertEquals( 2, it2c.next().intValue() );
		assertEquals( 3, it2c.next().intValue() );
		assertFalse( it2c.hasNext() );

		p.entryWasEvicted(2);
		try {
			p.entryWasEvicted(2);
			fail();
		}
		catch ( final IllegalArgumentException e ) {
			// expected
		}

		final Iterator<Integer> it3 = p.getEvictionCandidates(1).iterator();
		assertEquals( 3, it3.next().intValue() );

		p.entryWasEvicted(3);

		final Iterator<Integer> it4 = p.getEvictionCandidates(1).iterator();
		assertFalse( it4.hasNext() );
	}

	@Test
	public void entryWasRequestedTest() {
		final CacheReplacementPolicy<Integer,String,CacheEntryBase<String>> p = new CacheReplacementPolicyLRU<>();
		p.entryWasAdded(1, null);
		p.entryWasAdded(2, null);
		p.entryWasAdded(3, null);

		p.entryWasRequested(1, null);
		p.entryWasRequested(2, null);
		p.entryWasRequested(1, null);

		final Iterator<Integer> it1 = p.getEvictionCandidates(3).iterator();
		assertEquals( 3, it1.next().intValue() );
		assertEquals( 2, it1.next().intValue() );
		assertEquals( 1, it1.next().intValue() );

		p.entryWasEvicted(2);

		p.entryWasRequested(1, null);

		final Iterator<Integer> it2 = p.getEvictionCandidates(3).iterator();
		assertEquals( 3, it2.next().intValue() );
		assertEquals( 1, it2.next().intValue() );
		assertFalse( it2.hasNext() );

		p.entryWasRequested(1, null);
		p.entryWasRequested(3, null);

		final Iterator<Integer> it3 = p.getEvictionCandidates(3).iterator();
		assertEquals( 1, it3.next().intValue() );
		assertEquals( 3, it3.next().intValue() );
		assertFalse( it3.hasNext() );

		p.entryWasAdded(4, null);
		p.entryWasRequested(3, null);

		final Iterator<Integer> it4 = p.getEvictionCandidates(3).iterator();
		assertEquals( 1, it4.next().intValue() );
		assertEquals( 4, it4.next().intValue() );
		assertEquals( 3, it4.next().intValue() );

		try {
			p.entryWasRequested(2, null);
			fail();
		}
		catch ( final IllegalArgumentException e ) {
			// expected
		}
	}

	@Test
	public void clearTest() {
		final CacheReplacementPolicy<Integer,String,CacheEntryBase<String>> p = new CacheReplacementPolicyLRU<>();
		p.entryWasAdded(1, null);
		p.entryWasAdded(2, null);
		p.entryWasAdded(3, null);

		p.clear();

		final Iterator<Integer> it = p.getEvictionCandidates(1).iterator();
		assertFalse( it.hasNext() );
	}

}
