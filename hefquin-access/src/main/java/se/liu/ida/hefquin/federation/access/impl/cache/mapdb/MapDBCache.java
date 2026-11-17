package se.liu.ida.hefquin.federation.access.impl.cache.mapdb;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.liu.ida.hefquin.base.datastructures.impl.cache.CachePolicies;
import se.liu.ida.hefquin.federation.access.DataRetrievalResponse;
import se.liu.ida.hefquin.federation.access.impl.cache.CacheLayer;
import se.liu.ida.hefquin.federation.access.impl.cache.PersistentCacheEntry;
import se.liu.ida.hefquin.federation.access.impl.cache.PersistentCacheKey;

/**
 * Persistent cache backed by a MapDB {@link HTreeMap}.
 *
 * <p>
 * This class is thread-safe for concurrent access through its public methods on
 * a single {@link MapDBCache} instance. Cache operations are synchronized using
 * a dedicated lock.
 * </p>
 *
 * <p>
 * Cache entry creation, invalidation, and replacement behavior are delegated to
 * the configured cache policies.
 * </p>
 *
 * <p>
 * The cache enforces a maximum capacity. If the persisted map contains more
 * entries than allowed when opened, entries are evicted according to the
 * configured replacement policy until the capacity constraint is satisfied.
 * </p>
 *
 * <p>
 * Modifications are applied to the MapDB map immediately (an insertion as soon
 * as the future of the inserted response completes), but they are not committed
 * one by one. Instead, the {@link MapDBGroupCommitter} commits them
 * periodically in the background: at most once per commit interval, the
 * modifications of all threads (and, thus, of all queries running in parallel)
 * are persisted in a single MapDB commit. If the number of uncommitted
 * modifications reaches a configured maximum, they are committed without
 * waiting for the remainder of the interval.
 * </p>
 *
 * <p>
 * All uncommitted modifications are committed when {@link #close()} is called.
 * Insertions whose futures have not completed at that point are not persisted;
 * closing does not wait for them. If the JVM shuts down without
 * {@link #close()} having been called (e.g., {@link System#exit},
 * SIGTERM/SIGINT, or a crash), MapDB closes the database without committing,
 * and the modifications since the last commit are lost. MapDB's write-ahead log
 * keeps the file consistent in that case. {@link #flush()} commits all
 * uncommitted modifications immediately.
 * </p>
 *
 * <p>
 * <strong>Caveats:</strong>
 * </p>
 * <p>
 * Thread safety is provided only at the level of this cache instance. If two
 * MapDBCache objects point to the same persisted file, this class does not
 * coordinate:
 * </p>
 * <ul>
 * <li>replacement policy state</li>
 * <li>capacity enforcement</li>
 * <li>invalidation/eviction decisions</li>
 * </ul>
 */
public class MapDBCache extends CacheLayer<PersistentCacheKey,
                                           CompletableFuture<? extends DataRetrievalResponse<?>>,
                                           PersistentCacheEntry>
{
	private static final Logger logger = LoggerFactory.getLogger( MapDBCache.class );

	protected static final int DEFAULT_CAPACITY = 1000;
	protected static final String DEFAULT_FILENAME = "cache/mapdb-map.dat";
	protected static final int DEFAULT_COMMIT_INTERVAL_MILLIS = 5000;
	protected static final int DEFAULT_MAX_PENDING_MODIFICATIONS = 10000;

	protected final DB db;
	private volatile boolean closed = false;
	protected final MapDBGroupCommitter committer;

	/**
	 * Creates a cache with the default capacity and default file path.
	 *
	 * @param policies the cache policies to use
	 * @throws IOException if the cache file cannot be created or opened
	 */
	public MapDBCache( final CachePolicies<PersistentCacheKey, CompletableFuture<? extends DataRetrievalResponse<?>>, PersistentCacheEntry> policies ) {
		this(DEFAULT_CAPACITY, DEFAULT_FILENAME, policies);
	}

	/**
	 * Creates a cache with the given capacity and the default file path.
	 *
	 * @param capacity the maximum number of cache entries
	 * @param policies the cache policies to use
	 * @throws IOException if the cache file cannot be created or opened
	 */
	public MapDBCache( final int capacity,
	                   final CachePolicies<PersistentCacheKey, CompletableFuture<? extends DataRetrievalResponse<?>>, PersistentCacheEntry> policies ) {
		this(capacity, DEFAULT_FILENAME, policies);
	}

	/**
	 * Constructs a new {@link MapDBCache} with a custom file path and the
	 * default cache capacity.
	 *
	 * @param filename the path to the cache file
	 * @param policies the cache policies to use
	 * @throws IOException if the cache file cannot be created or opened
	 */
	public MapDBCache( final String filename,
	                   final CachePolicies<PersistentCacheKey, CompletableFuture<? extends DataRetrievalResponse<?>>, PersistentCacheEntry> policies ) {
		this(DEFAULT_CAPACITY, filename, policies);
       }

	/**
	 * Creates a cache with the given capacity and file path.
	 *
	 * <p>
	 * If the persisted map already contains entries, the replacement policy is
	 * initialized accordingly and eviction is performed if necessary to satisfy the
	 * capacity constraint.
	 * </p>
	 *
	 * @param capacity the maximum number of cache entries
	 * @param filename the path to the cache file
	 * @param policies the cache policies to use
	 * @throws IOException if the cache file cannot be created or opened
	 */
	public MapDBCache( final int capacity,
	                   final String filename,
	                   final CachePolicies<PersistentCacheKey, CompletableFuture<? extends DataRetrievalResponse<?>>, PersistentCacheEntry> policies ) {
		this(capacity, filename, DEFAULT_COMMIT_INTERVAL_MILLIS, DEFAULT_MAX_PENDING_MODIFICATIONS, policies);
	}

	/**
	 * Creates a cache with the given capacity, file path, and commit settings.
	 *
	 * <p>
	 * If the persisted map already contains entries, the replacement policy is
	 * initialized accordingly and eviction is performed if necessary to satisfy the
	 * capacity constraint.
	 * </p>
	 *
	 * @param capacity                the maximum number of cache entries
	 * @param filename                the path to the cache file
	 * @param commitIntervalMillis    the interval between commits (in milliseconds)
	 * @param maxPendingModifications number of uncommitted modifications at which a
	 *                                commit is triggered without waiting for the
	 *                                end of the interval
	 * @param policies                the cache policies to use
	 */
	public MapDBCache( final int capacity,
	                   final String filename,
	                   final int commitIntervalMillis,
	                   final int maxPendingModifications,
	                   final CachePolicies<PersistentCacheKey, CompletableFuture<? extends DataRetrievalResponse<?>>, PersistentCacheEntry> policies ) {
		this(
			open(filename),
			capacity,
			commitIntervalMillis,
			maxPendingModifications,
			policies
		);

		// Initialize replacement policy
		for( final Entry<PersistentCacheKey, PersistentCacheEntry> entry : map.entrySet() ) {
			replacementPolicy.entryWasAdded( entry.getKey(), entry.getValue() );
		}

		// Enforce max capacity
		if ( map.size() > capacity ) {
			final Iterable<PersistentCacheKey> evictionCandidates = replacementPolicy
				.getEvictionCandidates( map.size() - capacity );
			evictionCandidates.forEach(this::evict);
		}

		logger.debug(
			"MapDB-based cache created: filename={}, capacity={}, size={}, commitIntervalMillis={}, maxPendingModifications={}",
			filename,
			capacity,
			map.size(),
			commitIntervalMillis,
			maxPendingModifications
		);
	}

	private MapDBCache( final DB db,
	                    final int capacity,
	                    final int commitIntervalMillis,
	                    final int maxPendingModifications,
	                    final CachePolicies<PersistentCacheKey, CompletableFuture<? extends DataRetrievalResponse<?>>, PersistentCacheEntry> policies ) {
		super(
			createMap(db),
			capacity,
			policies
		);

		this.db = db;

		this.committer = new MapDBGroupCommitter( db,
		                                          lock,
		                                          commitIntervalMillis,
		                                          maxPendingModifications );
	}

	/**
	 * Opens the MapDB database associated with the given file.
	 *
	 * @param filename path to the database file
	 *
	 * @return the opened MapDB database
	 */
	private static DB open( final String filename ) {
		final File file = ensureParentDirectoryExists(filename);
		return DBMaker.fileDB(file)
			.fileMmapEnableIfSupported()
			.transactionEnable()
			.closeOnJvmShutdown()
			.make();
	}

	/**
	 * Creates or opens the persisted MapDB hash map used to store cache entries.
	 *
	 * @param db the MapDB database that owns the map
	 *
	 * @return an {@link HTreeMap} containing the persisted cache entries
	 */
	private static Map<PersistentCacheKey, PersistentCacheEntry> createMap( final DB db ) {
		@SuppressWarnings("unchecked")
		final HTreeMap<PersistentCacheKey, PersistentCacheEntry> map =
				db.hashMap("cache")
				.keySerializer(Serializer.JAVA)
				.valueSerializer(MapDBCacheEntrySerializer.INSTANCE)
				.counterEnable()
				.createOrOpen();

		return map;
	}

	/**
	 * Ensures that the parent directory of the given file path exists.
	 *
	 * @param filename the path to the file
	 * @return a {@link File} object for the given path
	 * @throws IllegalStateException if the parent directory does not exist and
	 *                               cannot be created
	 */
	protected static File ensureParentDirectoryExists( final String filename ) {
		final File file = new File(filename);
		final File parent = file.getParentFile();
		if ( parent != null && ! parent.exists() && ! parent.mkdirs() ) {
			throw new IllegalStateException( "Failed to create directory: " + parent.getAbsolutePath() );
		}
		return file;
	}

	/**
	 * Closes the underlying MapDB database and releases associated resources.
	 *
	 * <p>
	 * Prevents new cache writes, commits all uncommitted modifications, and then
	 * closes the database. Insertions whose futures have not completed yet are not
	 * persisted.
	 * </p>
	 *
	 * <p>
	 * After this method returns, the cache must not be used anymore.
	 * </p>
	 */
	@Override
	public void close() {
		synchronized (lock) {
			if (closed) return;
			closed = true;
		}

		final long t0 = System.nanoTime();

		// Stops the background thread and commits all modifications.
		committer.close();

		final long t1 = System.nanoTime();

		// MapDB may have closed the db already at JVM shutdown.
		synchronized (lock) {
			if ( ! db.isClosed() ) {
				db.close();
			}
		}

		if ( logger.isDebugEnabled() ) {
			final long t2 = System.nanoTime();
			logger.debug( "Cache closed in {} ms: committer.close() {} ms, db.close() {} ms",
			              (t2 - t0) / 1_000_000L, (t1 - t0) / 1_000_000L, (t2 - t1) / 1_000_000L );
		}
	}

	/**
	 * Commits all uncommitted modifications of this cache, without waiting for
	 * the end of the commit interval.
	 */
	public void flush() {
		committer.flush();
	}

	/**
	 * Removes the cache entry associated with the given key, as in
	 * {@link CacheLayer#evict(Object)}, and schedules a commit if an entry was
	 * removed.
	 */
	@Override
	public boolean evict( final PersistentCacheKey key ) {
		synchronized (lock) {
			final boolean removed = super.evict(key);
			if (removed) scheduleCommit();
			return removed;
		}
	}

	/**
	 * Removes all cache entries, as in {@link CacheLayer#clear()}, and
	 * schedules a commit.
	 */
	@Override
	public void clear() {
		synchronized (lock) {
			super.clear();
			scheduleCommit();
		}
	}

	/**
	 * Registers a modification for the next group commit. Call while holding
	 * the lock, so that {@link #close()} cannot miss it.
	 */
	protected void scheduleCommit() {
		committer.notifyModification();
	}

	/**
	 * Inserts or updates a cache entry once the given future completes
	 * successfully.
	 *
	 * <p>
	 * Unlike the implementation in {@link CacheLayer}, this method does not insert
	 * the entry immediately. The cache entry is created and stored only after
	 * {@code value} has completed successfully. This ensures that the persistent
	 * MapDB cache never attempts to serialize an incomplete
	 * {@link CompletableFuture}.
	 * </p>
	 *
	 * <p>
	 * If {@code value} completes exceptionally, no entry is added or updated in the
	 * cache.
	 * </p>
	 *
	 * <p>
	 * Once the future has completed successfully, insertion is delegated to
	 * {@link CacheLayer#put(Object, Object)}, which handles replacement policy,
	 * capacity enforcement, and the actual insertion into the backing map. The
	 * changes are committed to the database with the next group commit (see
	 * {@link MapDBGroupCommitter}). Writes are synchronized with shutdown to
	 * prevent database access after it has been closed.
	 * </p>
	 *
	 * @param key   cache key, must not be {@code null}
	 * @param value future containing the object to cache, must not be {@code null}
	 *
	 * @throws IllegalArgumentException if {@code key} or {@code value} is
	 *                                  {@code null}
	 */
	@Override
	public void put( final PersistentCacheKey key, final CompletableFuture<? extends DataRetrievalResponse<?>> value ) {
		if ( key == null )
			throw new IllegalArgumentException("Cache key must not be null");

		if ( value == null )
			throw new IllegalArgumentException("Cache value must not be null");

		if ( closed )
			throw new IllegalStateException("Cache is already closed");

		value.thenAccept( response -> {
			synchronized (lock) {
				if (closed) return;
				super.put(key, value);
				scheduleCommit();
			}
		} );
	}
}
