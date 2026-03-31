package se.liu.ida.hefquin.federation.access.impl.cache.chroniclemap;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.openhft.chronicle.map.ChronicleMap;
import se.liu.ida.hefquin.base.datastructures.Cache;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheEntryFactory;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheInvalidationPolicy;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CachePolicies;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheReplacementPolicy;

/**
 * Persistent cache backed by a {@link ChronicleMap}.
 *
 * <p>
 * This implementation uses a two-level design:
 * </p>
 * <ul>
 * <li>An off-heap {@link ChronicleMap} as the persistent store</li>
 * <li>An on-heap cache ({@link HashMap}) for recently accessed or written entries</li>
 * </ul>
 *
 * <p>
 * The in-memory cache is a performance optimization that avoids repeated
 * deserialization of entries from off-heap storage. All in-memory entries
 * originate from and are synchronized with the ChronicleMap.
 * </p>
 *
 * <p>
 * This class is thread-safe for concurrent access through its public methods on
 * a single {@link ChronicleMapCache} instance. All access is guarded by
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
 * configured replacement policy until the capacity constraint is met.
 * </p>
 *
 * <p>
 * <strong>Caveats:</strong>
 * </p>
 * <p>
 * Thread safety is provided only at the level of this cache instance. If two
 * ChronicleMapCache objects point to the same persisted file, this class does
 * not coordinate:
 * </p>
 * <ul>
 * <li>replacement policy state</li>
 * <li>capacity enforcement</li>
 * <li>invalidation/eviction decisions</li>
 * </ul>
 */
public class ChronicleMapCache implements Cache<ChronicleMapCacheKey, ChronicleMapCacheObject>, AutoCloseable
{
	private static final Logger logger = LoggerFactory.getLogger( ChronicleMapCache.class );

	protected static final int DEFAULT_CAPACITY = 1000;
	protected static final String DEFAULT_FILENAME = "cache/chronicle-map.dat";

	protected final ChronicleMap<ChronicleMapCacheKey, ChronicleMapCacheEntry> map;
	protected final Map<ChronicleMapCacheKey, ChronicleMapCacheEntry> inMemoryCache;
	protected final CacheEntryFactory<ChronicleMapCacheEntry,ChronicleMapCacheObject> entryFactory;
	protected final CacheInvalidationPolicy<ChronicleMapCacheEntry,ChronicleMapCacheObject> invalidPolicy;
	protected final CacheReplacementPolicy<ChronicleMapCacheKey, ChronicleMapCacheObject, ChronicleMapCacheEntry> replacementPolicy;

	protected final int capacity;
	protected final String filename;

	/**
	 * Creates a cache with the default capacity and default file path.
	 *
	 * @param policies the cache policies to use
	 * @throws IOException if the cache file cannot be created or opened
	 */
	public ChronicleMapCache( final CachePolicies<ChronicleMapCacheKey, ChronicleMapCacheObject, ChronicleMapCacheEntry> policies )
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
	public ChronicleMapCache( final int capacity,
	                          final CachePolicies<ChronicleMapCacheKey, ChronicleMapCacheObject, ChronicleMapCacheEntry> policies )
			throws IOException {
		this(capacity, DEFAULT_FILENAME, policies);
	}

	/**
	 * Constructs a new {@link ChronicleMapCache} with a custom file path and the
	 * default cache capacity.
	 *
	 * @param filename the path to the cache file
	 * @param policies the cache policies to use
	 * @throws IOException if the cache file cannot be created or opened
	 */
	public ChronicleMapCache( final String filename,
	                          final CachePolicies<ChronicleMapCacheKey, ChronicleMapCacheObject, ChronicleMapCacheEntry> policies )
			throws IOException {
		this(DEFAULT_CAPACITY, filename, policies);
	}

	/**
	 * Creates a cache with the given capacity and file path.
	 *
	 * @param capacity the maximum number of cache entries
	 * @param filename the path to the cache file
	 * @param policies the cache policies to use
	 * @throws IOException if the cache file cannot be created or opened
	 */
	public ChronicleMapCache( final int capacity,
	                          final String filename,
	                          final CachePolicies<ChronicleMapCacheKey, ChronicleMapCacheObject, ChronicleMapCacheEntry> policies )
			throws IOException {
		assert capacity > 0;
		assert policies != null;
		assert filename != null && ! filename.isBlank();

		this.capacity = capacity;
		entryFactory = policies.getEntryFactory();
		replacementPolicy = policies.getReplacementPolicyFactory().create();
		invalidPolicy = policies.getInvalidationPolicy();

		this.filename = filename;
		map = initializeMap(filename, capacity);
		inMemoryCache = new HashMap<>(capacity);

		// Initialize replacement policy
		for( final Entry<ChronicleMapCacheKey, ChronicleMapCacheEntry> entry : map.entrySet() ) {
			replacementPolicy.entryWasAdded( entry.getKey(), entry.getValue() );
		}

		// Enforce max capacity
		while ( map.size() > capacity ) {
			final Iterable<ChronicleMapCacheKey> evictionCandidates = replacementPolicy.getEvictionCandidates(1);
			final ChronicleMapCacheKey evictionCandidate = evictionCandidates.iterator().next();
			evict(evictionCandidate);
		}

		logger.info( toString() );
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
	 * Initializes and returns the persistent Chronicle Map used by this cache.
	 *
	 * <p>
	 * If the specified file does not exist, it is created before the map is
	 * initialized.
	 * </p>
	 *
	 * @param filename the path to the file used for persistent storage
	 * @param capacity the maximum number of entries
	 * @return an instance of {@link ChronicleMap}
	 * @throws IOException if an error occurs while creating or accessing the file
	 */
	protected ChronicleMap<ChronicleMapCacheKey, ChronicleMapCacheEntry> initializeMap( final String filename,
	                                                                                    final int capacity )
			throws IOException {
		final File file = ensureParentDirectoryExists(filename);
		return ChronicleMap.of( ChronicleMapCacheKey.class, ChronicleMapCacheEntry.class )
			.name("cache-map")
			.entries(10 * capacity)
			.averageKeySize(248)
			.averageValueSize(4096)
			.maxBloatFactor(10.0)
			.valueMarshaller(ChronicleMapCacheEntryMarshaller.INSTANCE)
			.createPersistedTo(file);
	}

	@Override
	public boolean isEmpty() {
		synchronized (map) {
			return map.isEmpty();
		}
	}

	/**
	 * Removes all entries from both the ChronicleMap and the in-memory cache, and
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
	 * The value is written to the ChronicleMap and also inserted into the in-memory
	 * cache to avoid future deserialization overhead.
	 * </p>
	 *
	 * <p>
	 * If the key already exists, the corresponding entry is replaced and the
	 * replacement policy is notified of a rewrite, otherwise the key is added to
	 * the cache. If the cache reaches max capacity an entry is evicted according to
	 * the replacement policy.
	 * </p>
	 *
	 * @param key   the cache key
	 * @param value the cache value
	 * @throws IllegalArgumentException if {@code key} or {@code value} is
	 *                                  {@code null}
	 */
	@Override
	public void put( final ChronicleMapCacheKey key, final ChronicleMapCacheObject value ) {
		if ( key == null )
			throw new IllegalArgumentException("Cache key must not be null");

		if ( value == null )
			throw new IllegalArgumentException("Cache value must not be null");

		synchronized (map) {
			map.compute( key, (k, oldEntry) -> {
				final ChronicleMapCacheEntry entry = entryFactory.createCacheEntry(value);
				inMemoryCache.put(k, entry);

				if ( oldEntry == null )
					replacementPolicy.entryWasAdded(k, entry);
				else
					replacementPolicy.entryWasRewritten(k, entry);
				return entry;
			} );

			// Check capacity
			if ( map.size() > capacity ) {
				final Iterable<ChronicleMapCacheKey> evictionCandidates = replacementPolicy.getEvictionCandidates(1);
				final ChronicleMapCacheKey evictionCandidate = evictionCandidates.iterator().next();
				evict(evictionCandidate);
			}
		}
	}

	/**
	 * Returns the cached object for the given key.
	 *
	 * <p>
	 * This method first checks the in-memory cache. If a valid entry is found, the
	 * replacement policy is notified and the entry returned immediately. Otherwise,
	 * the ChronicleMap is consulted.
	 * </p>
	 *
	 * <p>
	 * If the key is present but the corresponding entry is no longer valid, the
	 * entry is evicted and {@code null} is returned.
	 * </p>
	 *
	 * @param key the cache key
	 * @return the cached object, or {@code null} if no valid entry exists
	 */
	@Override
	public ChronicleMapCacheObject get( final ChronicleMapCacheKey key ) {
		synchronized (map) {
			final ChronicleMapCacheEntry e = inMemoryCache.get(key);
			if ( e != null ) {
				if( invalidPolicy.isStillValid(e) ) {
					replacementPolicy.entryWasRequested(key, e);
					return e.getObject();
				}
				else {
					inMemoryCache.remove(key);
				}
			}

			final ChronicleMapCacheEntry entry = map.get(key);
			if ( entry == null )
				return null;

			if ( ! invalidPolicy.isStillValid(entry) ) {
				evict(key);
				return null;
			}

			replacementPolicy.entryWasRequested(key, entry);
			inMemoryCache.put(key, entry);
			return entry.getObject();
		}
	}

	/**
	 * Evicts the cache entry for the given key.
	 *
	 * <p>
	 * The entry is removed from both the ChronicleMap and the in-memory cache.
	 * </p>
	 *
	 * @param key the key to evict
	 * @return {@code true} if an entry was removed, otherwise {@code false}
	 */
	@Override
	public boolean evict( final ChronicleMapCacheKey key ) {
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
	public boolean evict( final ChronicleMapCacheKey key, final ChronicleMapCacheObject value ) {
		synchronized (map) {
			final ChronicleMapCacheEntry entry = map.get(key);
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
		return "ChronicleMapCache{filename=" + filename + ", capacity=" + capacity + ", size=" + map.size() + "}";
	}

	/**
	 * Closes the underlying {@link ChronicleMap} and releases associated resources.
	 *
	 * <p>
	 * Closing the map is not required but ensures that all data is properly flushed
	 * to disk and that off-heap resources are released.
	 * </p>
	 */
	@Override
	public void close() {
		synchronized (map) {
			map.close();
		}
	}

	/**
	 * Returns the set of keys currently stored in the cache.
	 *
	 * @return the current key set
	 */
	public Set<ChronicleMapCacheKey> keySet() {
		synchronized (map) {
			return Set.copyOf( map.keySet() );
		}
	}
}
