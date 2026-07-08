package se.liu.ida.hefquin.federation.access.impl.cache.chroniclemap;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.openhft.chronicle.map.ChronicleMap;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CachePolicies;
import se.liu.ida.hefquin.federation.access.DataRetrievalResponse;
import se.liu.ida.hefquin.federation.access.impl.cache.CacheLayer;
import se.liu.ida.hefquin.federation.access.impl.cache.PersistentCacheEntry;
import se.liu.ida.hefquin.federation.access.impl.cache.PersistentCacheKey;

/**
 * Persistent cache backed by a {@link ChronicleMap}.
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
 * configured replacement policy until the capacity constraint is satisfied.
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
public class ChronicleMapCache extends CacheLayer<PersistentCacheKey,
                                                  CompletableFuture<? extends DataRetrievalResponse<?>>,
                                                  PersistentCacheEntry>
{
	private static final Logger logger = LoggerFactory.getLogger( ChronicleMapCache.class );

	protected static final int DEFAULT_CAPACITY = 1000;
	protected static final String DEFAULT_FILENAME = "cache/chronicle-map.dat";

	/**
	 * Creates a cache with the default capacity and default file path.
	 *
	 * @param policies the cache policies to use
	 * @throws IOException if the cache file cannot be created or opened
	 */
	public ChronicleMapCache( final CachePolicies<PersistentCacheKey, CompletableFuture<? extends DataRetrievalResponse<?>>, PersistentCacheEntry> policies )
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
	                          final CachePolicies<PersistentCacheKey, CompletableFuture<? extends DataRetrievalResponse<?>>, PersistentCacheEntry> policies )
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
	public ChronicleMapCache( final int capacity,
	                          final String filename,
	                          final CachePolicies<PersistentCacheKey, CompletableFuture<? extends DataRetrievalResponse<?>>, PersistentCacheEntry> policies )
			throws IOException
	{
		super(
			createChronicleMap(filename, capacity),
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
			"ChronicleMap-based cache created: filename={}, capacity={}, size={}",
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

	protected static Map<PersistentCacheKey, PersistentCacheEntry> createChronicleMap( final String filename,
	                                                                                   final int capacity )
			throws IOException
	{
		final File file = ensureParentDirectoryExists(filename);
		return ChronicleMap.of( PersistentCacheKey.class, PersistentCacheEntry.class )
			.name("cache-map")
			.entries(10 * capacity)
			.averageKeySize(248)
			.averageValueSize(4096)
			.maxBloatFactor(10.0)
			.valueMarshaller(ChronicleMapCacheEntryMarshaller.INSTANCE)
			.createPersistedTo(file);
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
			((ChronicleMap<PersistentCacheKey, PersistentCacheEntry>) map).close();
		}
	}
}
