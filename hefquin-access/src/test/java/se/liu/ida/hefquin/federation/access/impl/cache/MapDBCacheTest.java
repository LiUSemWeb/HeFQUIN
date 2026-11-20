package se.liu.ida.hefquin.federation.access.impl.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.After;
import org.junit.Test;

import se.liu.ida.hefquin.base.datastructures.Cache;
import se.liu.ida.hefquin.federation.access.DataRetrievalResponse;
import se.liu.ida.hefquin.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.federation.access.impl.cache.mapdb.MapDBCache;
import se.liu.ida.hefquin.federation.access.impl.req.SPARQLRequestImpl;

public class MapDBCacheTest extends CacheTestBase
{
	protected String filename = "cache/test/mapdb-map.dat";

	@After
	public void cleanup() {
		final File file = new File( filename );
		if ( file.exists() && ! file.delete() ) {
			throw new RuntimeException( "Failed to delete test cache file: " + filename );
		}
	}

	@Override
	protected Cache<PersistentCacheKey, CompletableFuture<? extends DataRetrievalResponse<?>>> createCache(
			int capacity, CachePoliciesForTest cachePolicies ) throws IOException {
		return new MapDBCache( capacity, filename, cachePolicies );
	}

	@Test
	public void insertionIsVisibleBeforeCommit() throws Exception {
		// The long commit interval ensures that nothing is committed during the test.
		final MapDBCacheForTest cache = new MapDBCacheForTest( 10, filename, 60_000, 1000 );
		cache.clear();

		final PersistentCacheKey k = makeKey(0);
		final SolMapsResponse o = makeSolMapResponse(1);
		cache.put( k, CompletableFuture.completedFuture(o) );

		assertSolMapsEqual( o, (SolMapsResponse) cache.get(k).get() );
		assertEquals( 0L, cache.getNumberOfCommits() );
		cache.close();
	}

	@Test
	public void evictionIsPersisted() throws Exception {
		final MapDBCache cache1 = new MapDBCache( 10, filename, 60_000, 1000, new CachePoliciesForTest() );
		cache1.clear();

		final PersistentCacheKey k = makeKey(0);
		// Commit the entry first, such that the test checks that the eviction is persisted.
		cache1.put( k, CompletableFuture.completedFuture( makeSolMapResponse(1) ) );
		cache1.flush();

		assertTrue( cache1.evict(k) );
		assertNull( cache1.get(k) );
		cache1.close();

		final Cache<PersistentCacheKey, CompletableFuture<? extends DataRetrievalResponse<?>>> cache2 =
				createCache( 10, new CachePoliciesForTest() );
		assertNull( cache2.get(k) );
		cache2.close();
	}

	@Test
	public void concurrentPutsArePersisted() throws Exception {
		final int numberOfThreads = 8;
		final int putsPerThread = 42;
		final int numberOfEntries = numberOfThreads * putsPerThread;

		final MapDBCache cache1 = new MapDBCache( numberOfEntries, filename, new CachePoliciesForTest() );
		cache1.clear();

		final DataRetrievalResponse<?> o = makeSolMapResponse(1);
		final ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
		final List<Future<?>> tasks = new ArrayList<>();
		for ( int t = 0; t < numberOfThreads; t++ ) {
			final int threadId = t;
			tasks.add( executor.submit( () -> {
				for ( int i = 0; i < putsPerThread; i++ ) {
					final PersistentCacheKey k = makeKey( threadId * putsPerThread + i );
					cache1.put( k, CompletableFuture.completedFuture(o) );

					// Entries must be visible right after put().
					assertNotNull( cache1.get(k) );
				}
			} ) );
		}
		// Wait for all threads.
		for ( final Future<?> task : tasks ) {
			task.get();
		}
		executor.shutdown();
		cache1.close();

		final Cache<PersistentCacheKey, CompletableFuture<? extends DataRetrievalResponse<?>>> cache2 =
				createCache( numberOfEntries, new CachePoliciesForTest() );
		int found = 0;
		for ( int i = 0; i < numberOfEntries; i++ ) {
			if ( cache2.get( makeKey(i) ) != null ) found++;
		}
		assertEquals(numberOfEntries, found);
		cache2.close();
	}

	@Test
	public void flushMakesEntriesDurableWithoutClose() throws Exception {
		final MapDBCacheForTest cache1 = new MapDBCacheForTest( 10, filename, 60_000, 1000 );
		cache1.clear();

		final PersistentCacheKey flushed = makeKey(0);
		final PersistentCacheKey notFlushed = makeKey(1);

		// flush() ensures that the first entry is committed.
		cache1.put( flushed, CompletableFuture.completedFuture( makeSolMapResponse(1) ) );
		cache1.flush();

		// The long commit interval ensures that the second entry is not committed.
		cache1.put( notFlushed, CompletableFuture.completedFuture( makeSolMapResponse(2) ) );

		// Simulates an unexpected stop, where the database is closed without a commit.
		cache1.closeDatabaseWithoutCommit();
		cache1.close();

		final Cache<PersistentCacheKey, CompletableFuture<? extends DataRetrievalResponse<?>>> cache2 =
				createCache( 10, new CachePoliciesForTest() );

		// Only the flushed entry must have been persisted.
		assertNotNull( cache2.get(flushed) );
		assertNull( cache2.get(notFlushed) );
		cache2.close();
	}

	@Test
	public void burstOfPutsIsCommittedInFewCommits() throws Exception {
		// The long commit interval ensures that no periodic commit happens
		// during the test. Instead, an early commit is triggered when 100
		// modifications are pending, and flush() commits the rest.
		final int numberOfEntries = 150;
		final MapDBCacheForTest cache1 = new MapDBCacheForTest( numberOfEntries, filename, 60_000, 100 );
		cache1.clear();

		// clear() and the 150 insertions are 151 modifications. The 100th
		// modification (the 99th insertion) triggers the early commit. It runs in
		// the background and commits all modifications pending when it starts.
		// Insertions made after that remain uncommitted until flush() commits
		// them, which makes 2 commits.
		final DataRetrievalResponse<?> o = makeSolMapResponse(1);
		for ( int i = 0; i < numberOfEntries; i++ ) {
			cache1.put( makeKey(i), CompletableFuture.completedFuture(o) );
		}
		cache1.flush();

		// With a commit per modification, there would be 151 commits.
		assertEquals( 2L, cache1.getNumberOfCommits() );
		cache1.close();

		// All entries must be persisted.
		final Cache<PersistentCacheKey, CompletableFuture<? extends DataRetrievalResponse<?>>> cache2 =
				createCache( numberOfEntries, new CachePoliciesForTest() );
		int found = 0;
		for ( int i = 0; i < numberOfEntries; i++ ) {
			if ( cache2.get( makeKey(i) ) != null ) found++;
		}
		assertEquals(numberOfEntries, found);
		cache2.close();
	}

	@Test
	public void closeCommitsWithoutWaitingForInterval() throws Exception {
		// The long commit interval ensures that the entry is still uncommitted
		// when close() is called.
		final MapDBCache cache1 = new MapDBCache( 10, filename, 60_000, 1000, new CachePoliciesForTest() );
		cache1.clear();
		cache1.put( makeKey(0), CompletableFuture.completedFuture( makeSolMapResponse(1) ) );

		// close() must commit the entry itself, instead of leaving it to the
		// next periodic commit.
		cache1.close();

		// The entry must be persisted.
		final Cache<PersistentCacheKey, CompletableFuture<? extends DataRetrievalResponse<?>>> cache2 =
				createCache( 10, new CachePoliciesForTest() );
		assertNotNull( cache2.get( makeKey(0) ) );
		cache2.close();
	}

	/**
	 * Exposes the number of commits of the {@link MapDBCache} and allows closing
	 * the database without a commit (to simulate an unexpected stop).
	 */
	protected static class MapDBCacheForTest extends MapDBCache
	{
		public MapDBCacheForTest( final int capacity,
		                          final String filename,
		                          final int commitIntervalMillis,
		                          final int maxPendingModifications ) {
			super( capacity, filename, commitIntervalMillis, maxPendingModifications, new CachePoliciesForTest() );
		}

		public long getNumberOfCommits() {
			return committer.getNumberOfCommits();
		}

		/**
		 * Closes the database as MapDB's shutdown hook does, discarding
		 * uncommitted modifications.
		 */
		public void closeDatabaseWithoutCommit() {
			db.close();
		}
	}

	protected PersistentCacheKey makeKey( final int i ) {
		return new PersistentCacheKey( new SPARQLRequestImpl(tp),
		                               new SPARQLEndpointForTest( "http://example.org/sparql" + i ),
		                               PersistentCacheKey.ResponseMode.RESULT );
	}
}
