package se.liu.ida.hefquin.federation.access.impl.cache.mapdb;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.liu.ida.hefquin.base.datastructures.Cache;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheEntryFactory;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheInvalidationPolicy;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CachePolicies;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheReplacementPolicy;
import se.liu.ida.hefquin.federation.access.DataRetrievalResponse;
import se.liu.ida.hefquin.federation.access.impl.cache.PersistentCacheEntry;
import se.liu.ida.hefquin.federation.access.impl.cache.PersistentCacheKey;

/**
 * Persistent cache backed by a {@link HTreeMap}.
 *
 * <p>
 * This implementation uses a two-level design:
 * </p>
 * <ul>
 * <li>An off-heap {@link HTreeMap} as the persistent store</li>
 * <li>An on-heap cache ({@link HashMap}) for recently accessed or written entries</li>
 * </ul>
 *
 * <p>
 * The in-memory cache is a performance optimization that avoids repeated
 * deserialization of entries from off-heap storage. All in-memory entries
 * originate from and are synchronized with the HTreeMap.
 * </p>
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
 * cache objects point to the same persisted file, this class does
 * not coordinate:
 * </p>
 * <ul>
 * <li>replacement policy state</li>
 * <li>capacity enforcement</li>
 * <li>invalidation/eviction decisions</li>
 * </ul>
 */
public class MapDBCache implements Cache<PersistentCacheKey, CompletableFuture<? extends DataRetrievalResponse<?>>>, AutoCloseable
{
	private static final Logger logger = LoggerFactory.getLogger( MapDBCache.class );

	protected static final int DEFAULT_CAPACITY = 1000;
	protected static final String DEFAULT_FILENAME = "cache/mapdb-map.dat";

	protected final DB db;
	protected final HTreeMap<PersistentCacheKey, PersistentCacheEntry> map;
	protected final Map<PersistentCacheKey, PersistentCacheEntry> inMemoryCache;

	protected final CacheEntryFactory<PersistentCacheEntry, CompletableFuture<? extends DataRetrievalResponse<?>>> entryFactory;
	protected final CacheReplacementPolicy<PersistentCacheKey, CompletableFuture<? extends DataRetrievalResponse<?>>, PersistentCacheEntry> replacementPolicy;
	protected final CacheInvalidationPolicy<PersistentCacheEntry, CompletableFuture<? extends DataRetrievalResponse<?>>> invalidPolicy;

	protected final int capacity;
	protected final String filename;

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
		assert capacity > 0;
		assert policies != null;
		assert filename != null && ! filename.isBlank();

		this.capacity = capacity;
		entryFactory = policies.getEntryFactory();
		replacementPolicy = policies.getReplacementPolicyFactory().create();
		invalidPolicy = policies.getInvalidationPolicy();

		this.filename = filename;

		final File file = ensureParentDirectoryExists(filename);
		db = DBMaker.fileDB(file)
			.fileMmapEnableIfSupported()
			.make();

		@SuppressWarnings("unchecked")
		final HTreeMap<PersistentCacheKey, PersistentCacheEntry> tmpMap = db.hashMap("cache")
			.keySerializer( Serializer.JAVA )
			.valueSerializer( MapDBCacheEntrySerializer.INSTANCE )
			.createOrOpen();
		map = tmpMap;

		inMemoryCache = new HashMap<>(capacity);

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

	@Override
	public boolean isEmpty() {
		synchronized (map) {
			return map.isEmpty();
		}
	}

	/**
	 * Removes all entries from both the map and the in-memory cache, and
	 * resets the replacement policy state.
	 */
	@Override
	public void clear() {
		synchronized (map) {
			map.clear();
			inMemoryCache.clear();
			replacementPolicy.clear();
		}
	}

	/**
	 * Stores the given key-value pair in the cache.
	 *
	 * <p>
	 * The value is written to the map and also inserted into the in-memory
	 * cache to avoid future deserialization overhead.
	 * </p>
	 *
	 * <p>
	 * If the key already exists, the corresponding entry is replaced and the
	 * replacement policy is notified accordingly. Otherwise, the entry is added. If the
	 * cache exceeds max capacity, an entry is evicted according to the replacement
	 * policy.
	 * </p>
	 *
	 * @param key   the cache key
	 * @param value the cache value
	 * @throws IllegalArgumentException if {@code key} or {@code value} is
	 *                                  {@code null}
	 */
	@Override
	public void put( final PersistentCacheKey key, final CompletableFuture<? extends DataRetrievalResponse<?>> value ) {
		if ( key == null )
			throw new IllegalArgumentException( "Cache key must not be null" );

		if ( value == null )
			throw new IllegalArgumentException( "Cache value must not be null" );

		final PersistentCacheEntry entry = entryFactory.createCacheEntry(value);

		synchronized (map) {
			final PersistentCacheEntry oldEntry = map.get(key);
			map.put(key, entry);
			inMemoryCache.put(key, entry);

			if ( oldEntry == null )
				replacementPolicy.entryWasAdded(key, entry);
			else
				replacementPolicy.entryWasRewritten(key, entry);

			// Enforce max capacity
			if ( map.size() > capacity ) {
				final Iterable<PersistentCacheKey> evictionCandidates = replacementPolicy
						.getEvictionCandidates( map.size() - capacity );
				evictionCandidates.forEach(this::evict);
			}
		}
	}

	/**
	 * Returns the cached object for the given key.
	 *
	 * <p>
	 * This method first checks the in-memory cache. If a valid entry is found, the
	 * replacement policy is notified and the entry returned immediately. Otherwise,
	 * the map is consulted.
	 * </p>
	 *
	 * <p>
	 * If the key is present but the corresponding entry is no longer valid
	 * according to the invalidation policy, the entry is evicted and {@code null}
	 * is returned.
	 * </p>
	 *
	 * @param key the cache key
	 * @return the cached object, or {@code null} if no valid entry exists
	 */
	@Override
	public CompletableFuture<? extends DataRetrievalResponse<?>> get( final PersistentCacheKey key ) {
		synchronized (map) {
			final PersistentCacheEntry inMemoryEntry = inMemoryCache.get(key);
			final PersistentCacheEntry entry;

			if ( inMemoryEntry != null ) {
				entry = inMemoryEntry;
			}
			else {
				entry = map.get(key);
				if ( entry != null ) {
					inMemoryCache.put(key, entry);
				}
			}

			if ( entry == null ) {
				return null;
			}

			if ( ! invalidPolicy.isStillValid(entry) ) {
				evict(key);
				return null;
			}

			replacementPolicy.entryWasRequested(key, entry);
			return entry.getObject();
		}
	}

	/**
	 * Evicts the cache entry for the given key.
	 *
	 * <p>
	 * The entry is removed from both the map and the in-memory cache.
	 * </p>
	 *
	 * @param key the key to evict
	 * @return {@code true} if an entry was removed, otherwise {@code false}
	 */
	@Override
	public boolean evict( final PersistentCacheKey key ) {
		synchronized (map) {
			inMemoryCache.remove(key);
			if( map.remove(key) != null ) {
				replacementPolicy.entryWasEvicted(key);
				return true;
			}
			return false;
		}
	}

	/**
	 * Evicts the cache entry for the given key if it currently maps to the given
	 * value.
	 *
	 * @param key   the key to evict
	 * @param value the expected value
	 * @return {@code true} if an entry was removed, otherwise {@code false}
	 */
	@Override
	public boolean evict( final PersistentCacheKey key, final CompletableFuture<? extends DataRetrievalResponse<?>> value ) {
		synchronized (map) {
			final PersistentCacheEntry entry = map.get(key);
			if ( entry != null && entry.getObject().equals(value) ) {
				return evict(key);
			}
			return false;
		}
	}

	/**
	 * Returns the current number of entries stored in the cache.
	 *
	 * @return the cache size
	 */
	public int size() {
		synchronized (map) {
			return map.size();
		}
	}

	@Override
	public String toString() {
		return "MapDBCache{filename=" + filename + ", capacity=" + capacity + ", size=" + map.size() + "}";
	}

	/**
	 * Closes the underlying {@link HTreeMap}, {@link DB} and releases associated resources.
	 *
	 * <p>
	 * Closing the map is not required but ensures that all data is properly flushed
	 * to disk and that off-heap resources are released.
	 * </p>
	 */
	@Override
	public void close() {
		synchronized (map) {
			db.close();
		}
	}

	/**
	 * Returns the set of keys currently stored in the cache.
	 *
	 * @return  an immutable copy of the current key set
	 */
	public Set<PersistentCacheKey> keySet() {
		synchronized (map) {
			return Set.copyOf( map.keySet() );
		}
	}
}
