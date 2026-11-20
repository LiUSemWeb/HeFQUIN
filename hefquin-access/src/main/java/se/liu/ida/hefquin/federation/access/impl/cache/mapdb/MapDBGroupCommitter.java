package se.liu.ida.hefquin.federation.access.impl.cache.mapdb;

import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.mapdb.DB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Commits the modifications of a transactional MapDB database in groups instead
 * of one by one.
 *
 * <p>
 * Instead of committing after each modification of the database, callers call
 * {@link #notifyModification()}. The committer does not track the modifications
 * themselves, instead MapDB keeps them in its open transaction until the next
 * commit. The committer only counts them, to know whether a commit is needed. A
 * background thread commits once per commit interval if there are uncommitted
 * modifications, or earlier if their number reaches the configured maximum.
 * </p>
 *
 * <p>
 * Every commit is executed while holding the given lock. Callers must modify
 * the database while holding the same lock, such that a commit never captures a
 * partially completed modification.
 * </p>
 *
 * <p>
 * Commits never run concurrently. Once the database has been closed, no further
 * commits are made. If a commit fails, the modifications stay uncommitted and
 * are included in the next commit.
 * </p>
 */
public class MapDBGroupCommitter implements AutoCloseable
{
	private static final Logger logger = LoggerFactory.getLogger( MapDBGroupCommitter.class );

	protected final DB db;
	protected final Object lock;
	protected final int maxPendingModifications;
	protected final ScheduledExecutorService executor;

	private final AtomicInteger pendingModifications = new AtomicInteger();
	private final AtomicLong numberOfCommits = new AtomicLong();
	private volatile boolean closed = false;

	/**
	 * Creates a group committer for the given database.
	 *
	 * @param db                      database with transactions enabled
	 * @param lock                    lock that guards all modifications of the
	 *                                database
	 * @param commitIntervalMillis    delay (in milliseconds) between periodic
	 *                                commits
	 * @param maxPendingModifications number of uncommitted modifications that
	 *                                triggers an early commit
	 * @throws IllegalArgumentException if {@code commitIntervalMillis} or
	 *                                  {@code maxPendingModifications} is not
	 *                                  positive
	 */
	public MapDBGroupCommitter( final DB db,
	                            final Object lock,
	                            final long commitIntervalMillis,
	                            final int maxPendingModifications ) {
		assert db != null;
		assert lock != null;

		if ( commitIntervalMillis <= 0L )
			throw new IllegalArgumentException("commitIntervalMillis must be positive");

		if ( maxPendingModifications <= 0 )
			throw new IllegalArgumentException("maxPendingModifications must be positive");

		this.db = db;
		this.lock = lock;
		this.maxPendingModifications = maxPendingModifications;

		executor = Executors.newSingleThreadScheduledExecutor( r -> {
			final Thread t = new Thread(r, "MapDBGroupCommitter");
			t.setDaemon(true);
			return t;
		} );

		executor.scheduleWithFixedDelay( this::commitAndLogFailure,
		                                 commitIntervalMillis,
		                                 commitIntervalMillis,
		                                 TimeUnit.MILLISECONDS );
	}

	/**
	 * Records that a modification has been applied to the database. The
	 * modification must be applied before calling this method. This method does not
	 * block. After {@link #close()}, it has no effect.
	 */
	public void notifyModification() {
		if ( pendingModifications.incrementAndGet() == maxPendingModifications && ! closed ) {
			logger.debug( "Scheduling early commit: {} pending modifications reached (thread: {})",
			              maxPendingModifications, Thread.currentThread().getName() );
			try {
				executor.execute(this::commitAndLogFailure);
			}
			catch ( final RejectedExecutionException e ) {
				// DB was closed concurrently, and close() commits the modification
			}
		}
	}

	/**
	 * Commits all uncommitted modifications in the calling thread. Does nothing if
	 * there are no uncommitted modifications.
	 *
	 * @throws RuntimeException if the commit fails, in which case the modifications
	 *                          remain uncommitted
	 */
	public synchronized void flush() {
		final int n = pendingModifications.getAndSet(0);
		if ( n == 0 ) return;

		logger.debug( "Starting commit of {} modifications (thread: {})",
		              n, Thread.currentThread().getName() );
		final long start = System.nanoTime();

		try {
			commit();
			final long commitNumber = numberOfCommits.incrementAndGet();

			logger.debug( "Commit #{} of {} modifications done in {} ms in total",
			              commitNumber, n, (System.nanoTime() - start) / 1_000_000L );
		}
		catch ( final RuntimeException e ) {
			pendingModifications.addAndGet(n);
			throw e;
		}
	}

	/**
	 * Returns the number of successful commits so far.
	 */
	public long getNumberOfCommits() {
		return numberOfCommits.get();
	}

	/**
	 * Stops the background thread and commits all uncommitted modifications. Must
	 * be called before the database is closed.
	 */
	@Override
	public void close() {
		if ( closed ) return;
		closed = true;

		executor.shutdown();
		commitAndLogFailure();
	}

	/**
	 * Commits the uncommitted modifications while holding the lock. Does
	 * nothing if the database has been closed.
	 */
	protected void commit() {
		final long beforeLock = System.nanoTime();
		synchronized (lock) {
			final long afterLock = System.nanoTime();
			if ( ! db.isClosed() ) {
				db.commit();
			}

			logger.debug( "Waited {} ms for the cache lock; db.commit() took {} ms",
			              (afterLock - beforeLock) / 1_000_000L,
			              (System.nanoTime() - afterLock) / 1_000_000L );
		}
	}

	/**
	 * Calls {@link #flush()} and logs a failure instead of throwing it. A
	 * failure is not logged if the database has been closed in the meantime
	 * (e.g., by MapDB's shutdown hook), because that is expected.
	 */
	protected void commitAndLogFailure() {
		try {
			flush();
		}
		catch ( final RuntimeException e ) {
			if ( ! db.isClosed() ) {
				logger.error( "Group commit of the MapDB database failed", e );
			}
		}
	}
}
