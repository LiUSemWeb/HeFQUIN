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
 * a single {@link MapDBCache} instance. All access is guarded by
 * synchronization on {@code map}.
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

	protected final DB db;

	/**
	 * Creates a cache with the default capacity and default file path.
	 *
	 * @param policies the cache policies to use
	 * @throws IOException if the cache file cannot be created or opened
	 */
	public MapDBCache( final CachePolicies<PersistentCacheKey, CompletableFuture<? extends DataRetrievalResponse<?>>, PersistentCacheEntry> policies )
			throws IOException {
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
	                   final CachePolicies<PersistentCacheKey, CompletableFuture<? extends DataRetrievalResponse<?>>, PersistentCacheEntry> policies )
			throws IOException {
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
	                   final CachePolicies<PersistentCacheKey, CompletableFuture<? extends DataRetrievalResponse<?>>, PersistentCacheEntry> policies )
			throws IOException {
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
	                   final CachePolicies<PersistentCacheKey, CompletableFuture<? extends DataRetrievalResponse<?>>, PersistentCacheEntry> policies )
			throws IOException {
		this(
			open(filename),
			capacity,
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

		logger.info(
			"MapDB-based cache created: filename={}, capacity={}, size={}",
			filename,
			capacity,
			map.size()
		);
	}

	private MapDBCache( final DB db,
	                    final int capacity,
	                    final CachePolicies<PersistentCacheKey, CompletableFuture<? extends DataRetrievalResponse<?>>, PersistentCacheEntry> policies )
			throws IOException {
		super(
			createMap(db),
			capacity,
			policies
		);

		this.db = db;
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
	 * After this method returns, the cache must not be used anymore.
	 * </p>
	 */
	@Override
	public void close() {
		db.close();
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
	 * capacity enforcement, and the actual insertion into the backing map.
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
		value.thenAccept( response -> {
			super.put( key, value );
		} );
	}
}
