package se.liu.ida.hefquin.federation.access.impl.cache.mapdb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

/**
 * Tests for {@link MapDBGroupCommitter}.
 *
 * <p>
 * The tests check when commits happen, that modifications are grouped in
 * commits, and how failures are handled.
 * </p>
 *
 * <p>
 * Each test uses a simple in-memory MapDB database with transactions. To see
 * what has been committed, a test calls {@link DB#rollback()}. This discards
 * all uncommitted modifications, so the map then contains only the committed
 * ones.
 * </p>
 */
public class MapDBGroupCommitterTest
{
	protected final Object lock = new Object();
	protected DB db;
	protected Map<Integer, Integer> map;

	@Before
	public void setUp() {
		db = DBMaker.memoryDB().transactionEnable().make();
		map = db.hashMap("test", Serializer.INTEGER, Serializer.INTEGER).createOrOpen();

		// Commit the map creation, such that db.rollback() does not remove the map.
		db.commit();
	}

	@After
	public void tearDown() {
		if ( ! db.isClosed() ) db.close();
	}

	@Test
	public void uncommittedModificationsAreLostOnRollback() {
		// Sanity check for the rollback-based checks of the other tests.
		try ( final MapDBGroupCommitter committer = new MapDBGroupCommitter(db, lock, 60_000L, 1000) ) {
			put(committer, 0);
			db.rollback();
			assertEquals( 0, map.size() );
		}
	}

	@Test
	public void flushCommitsAllModifications() {
		try ( final MapDBGroupCommitter committer = new MapDBGroupCommitter(db, lock, 60_000L, 1000) ) {
			// The long commit interval ensures that only flush() commits.
			for ( int i = 0; i < 10; i++ ) put(committer, i);
			committer.flush();

			// All modifications must have been committed in one commit.
			db.rollback();
			assertEquals( 10, map.size() );
			assertEquals( 1L, committer.getNumberOfCommits() );
		}
	}

	@Test
	public void flushWithoutModificationsDoesNotCommit() {
		try ( final MapDBGroupCommitter committer = new MapDBGroupCommitter(db, lock, 60_000L, 1000) ) {
			// Without uncommitted modifications, flush() must not commit.
			committer.flush();
			assertEquals( 0L, committer.getNumberOfCommits() );
		}
	}

	@Test
	public void modificationsWithinIntervalShareOneCommit() throws Exception {
		try ( final MapDBGroupCommitter committer = new MapDBGroupCommitter(db, lock, 300L, 1000) ) {
			// The periodic commit after 300 ms must commit all modifications at once.
			for ( int i = 0; i < 100; i++ ) put(committer, i);
			Thread.sleep(500L); // wait for the periodic commit

			db.rollback();
			assertEquals( 100, map.size() );
			assertEquals( 1L, committer.getNumberOfCommits() );
		}
	}

	@Test
	public void reachingMaxPendingModificationsTriggersCommit() throws Exception {
		try ( final MapDBGroupCommitter committer = new MapDBGroupCommitter(db, lock, 60_000L, 5) ) {
			// Reaching the limit of 5 must trigger a commit long before the
			// 60 s interval ends.
			for ( int i = 0; i < 5; i++ ) put(committer, i);
			Thread.sleep(100L); // wait for the early commit in the background

			db.rollback();
			assertEquals( 5, map.size() );
		}
	}

	@Test
	public void concurrentModificationsAreGrouped() throws Exception {
		final int numberOfThreads = 8;
		final int modificationsPerThread = 42;

		try ( final MapDBGroupCommitter committer = new MapDBGroupCommitter(db, lock, 60_000L, 1000) ) {
			final List<Thread> threads = new ArrayList<>();
			for ( int t = 0; t < numberOfThreads; t++ ) {
				final int threadId = t;
				final Thread thread = new Thread( () -> {
					for ( int i = 0; i < modificationsPerThread; i++ ) {
						put( committer, threadId * modificationsPerThread + i );
					}
				} );
				threads.add(thread);
				thread.start();
			}
			// Wait for all threads.
			for ( final Thread thread : threads ) {
				thread.join();
			}

			// The long commit interval ensures that only flush() commits, so
			// the modifications of all threads must be committed in one commit.
			committer.flush();

			db.rollback();
			assertEquals( numberOfThreads * modificationsPerThread, map.size() );
			assertEquals( 1L, committer.getNumberOfCommits() );
		}
	}

	@Test
	public void closeCommitsWithoutWaitingForInterval() {
		// The long commit interval ensures that only close() commits.
		final MapDBGroupCommitter committer = new MapDBGroupCommitter(db, lock, 60_000L, 1000);
		put(committer, 0);
		committer.close();

		// The modification must have been committed.
		db.rollback();
		assertEquals( 1, map.size() );
	}

	@Test
	public void closedDatabaseIsNotCommitted() {
		try ( final MapDBGroupCommitter committer = new MapDBGroupCommitter(db, lock, 60_000L, 1000) ) {
			put(committer, 0);
			db.close();

			// flush() must not fail if the database has been closed.
			committer.flush();
		}
	}

	@Test
	public void failedCommitLeavesModificationsForNextCommit() {
		// Fails the first commit. The long commit interval ensures that only
		// flush() commits, in the test thread.
		final MapDBGroupCommitter committer = new MapDBGroupCommitter(db, lock, 60_000L, 1000) {
			boolean failNext = true;

			@Override
			protected void commit() {
				if ( failNext ) {
					failNext = false;
					throw new RuntimeException("The first commit is expected to throw");
				}
				super.commit();
			}
		};

		try ( committer ) {
			put(committer, 0);

			// The first flush() must throw the failure. If it returns normally,
			// fail() throws an AssertionError, which the catch clause below
			// does not catch (it is not a RuntimeException).
			try {
				committer.flush();
				fail("Expected the failed commit to be reported");
			}
			catch ( final RuntimeException e ) {
				assertEquals( "The first commit is expected to throw", e.getMessage() );
			}

			// The modification stays uncommitted and is committed by the next flush().
			committer.flush();
			db.rollback();
			assertEquals( 1, map.size() );
			assertEquals( 1L, committer.getNumberOfCommits() );
		}
	}

	@Test
	public void invalidArgumentsAreRejected() {
		try {
			new MapDBGroupCommitter(db, lock, 0L, 1000).close();
			fail("Expected IllegalArgumentException for the interval");
		}
		catch ( final IllegalArgumentException e ) { /* expected */ }

		try {
			new MapDBGroupCommitter(db, lock, 1000L, 0).close();
			fail("Expected IllegalArgumentException for the maximum");
		}
		catch ( final IllegalArgumentException e ) { /* expected */ }
	}

	/* --- Helper functions --- */

	/**
	 * Modifies the map while holding the lock and notifies the committer.
	 */
	protected void put( final MapDBGroupCommitter committer, final int i ) {
		synchronized (lock) {
			map.put(i, i);
			committer.notifyModification();
		}
	}
}
