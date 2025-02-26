package se.liu.ida.hefquin.engine.federation.access.impl.cache;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import net.openhft.chronicle.map.ChronicleMap;
import se.liu.ida.hefquin.base.datastructures.PersistableCache;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheEntryFactory;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheInvalidationPolicy;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CachePolicies;

/**
 * A thread-safe persistent cache implementation for storing cardinality entries. This
 * cache uses a {@link ChroncicleMap}.
 *
 * @param <K> The key type for caching cardinality responses.
 */
public class ChronicleMapCardinalityCache implements PersistableCache<CardinalityCacheKey, CardinalityCacheEntry>
{
	protected final Map<CardinalityCacheKey, CardinalityCacheEntry> map;
	protected final static int defaultCapacity = 50_000;
	protected final static String defaultFilename = "cache/chronicle-map.dat";

	protected final CacheEntryFactory<CardinalityCacheEntry, Integer> entryFactory;
	protected final CacheInvalidationPolicy<CardinalityCacheEntry, Integer> invalidationPolicy;

	/**
	 * Constructs a new {@link ChronicleMapCardinalityCache} with the default
	 * cache file and the default capacity.
	 * 
	 * @throws IOException
	 */
	public ChronicleMapCardinalityCache( final CachePolicies<CardinalityCacheKey, Integer, CardinalityCacheEntry> policies ) throws IOException {
		this( policies, defaultCapacity, defaultFilename );
	}

	/**
	 * Constructs a new {@link ChronicleMapCardinalityCache} with the default
	 * cache file and a maximum capacity.
	 *
	 * @param capacity Maximum cache capacity.
	 * @throws IOException
	 */
	public ChronicleMapCardinalityCache( final CachePolicies<CardinalityCacheKey, Integer, CardinalityCacheEntry> policies,
	                                     final int capacity ) throws IOException {
		this( policies, capacity, defaultFilename );
	}
	
	/**
	 * Constructs a new {@link ChronicleMapCardinalityCache} with a custom file
	 * path.
	 *
	 * @param filename Path to the cache file.
	 * @throws IOException
	 */
	public ChronicleMapCardinalityCache( final CachePolicies<CardinalityCacheKey, Integer, CardinalityCacheEntry> policies,
	                                     final int capacity,
		                                 final String filename ) throws IOException {
		entryFactory = policies.getEntryFactory();
		invalidationPolicy = policies.getInvalidationPolicy();

		// Ensure file exists, try to create otherwise
		ensureFileExists( filename );

		// ChronicleMap for persistent storage
		map = ChronicleMap.of( CardinalityCacheKey.class, CardinalityCacheEntry.class )
			.name( "cardinality-map" )
			.entries( capacity )
			.averageKeySize( 512 )
			.averageValueSize( 64 )
			.createPersistedTo( new File( filename ) );
	}
	
	/**
	 * Ensures that the cache file exists before initialization. If the file does
	 * not exist, it is created along with necessary directories.
	 * 
	 * @param filename The path of the file to ensure exists
	 * @return {@code File} object representing the ensured file
	 * @throws RuntimeException if the file cannot be created due to an I/O error
	 */
	private static File ensureFileExists( final String filename ) {
		final File file = new File( filename );
		try {
			if ( ! file.exists() ) {
				file.getParentFile().mkdirs();
				file.createNewFile();
			}
			return file;
		} catch ( IOException e ) {
			throw new RuntimeException( "Failed to create file: " + file.getAbsolutePath(), e );
		}
	}

	/**
	 * Adds a new value to the cache, associated with the given key. If an entry
	 * already exists for this key, it is replaced.
	 *
	 * @param key   The key identifying the response.
	 * @param value The entry to store.
	 */
	public void put( final CardinalityCacheKey key, final Integer value ) {
		final CardinalityCacheEntry entry = entryFactory.createCacheEntry( value );
		map.put( key, entry );
	}

	/**
	 * Adds a new cache entry to the cache, associated with the given key. If an
	 * entry already exists for this key, it is replaced.
	 *
	 * @param key   The key identifying the response.
	 * @param value The entry to store.
	 */
	@Override
	public void put( final CardinalityCacheKey key, final CardinalityCacheEntry entry ) {
		map.put( key, entry );
	}

	/**
	 * Retrieves the cache entry associated with the given key.
	 *
	 * @param key The key to look up.
	 * @return The entry, or {@link null} if not found.
	 */
	@Override
	public CardinalityCacheEntry get( final CardinalityCacheKey key ) {
		final CardinalityCacheEntry entry = map.get( key );
		if ( entry == null ) {
			return null;
		}

		// lazy evict
		if ( ! invalidationPolicy.isStillValid( entry ) ) {
			evict( key );
			return null;
		}

		return entry;
	}

	/**
	 * Removes the cache entry associated with the given key from the cache.
	 *
	 * @param key The key to remove.
	 * @return {@link true} if an entry was removed, {@link false} otherwise.
	 */
	@Override
	public boolean evict( final CardinalityCacheKey key ) {
		return map.remove( key ) != null;
	}

	/**
	 * Removes the specified entry from the cache only if the current value matches.
	 *
	 * @param key   The key to remove.
	 * @param value The expected value to match before removal.
	 * @return {@link true} if the entry was removed, {@link false} otherwise.
	 */
	@Override
	public boolean evict( final CardinalityCacheKey key, final CardinalityCacheEntry entry ) {
		return map.remove( key, entry );
	}

	/**
	 * Checks whether the cache is currently empty.
	 *
	 * @return {@link true} if the cache is empty, {@link false} otherwise.
	 */
	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	/**
	 * Clears all entries from the cache.
	 */
	@Override
	public void clear() {
		map.clear();
	}

	/**
	 * No-op implementation.
	 * 
	 * This method is required by the interface but is not used because ChronicleMap
	 * persists changes automatically. There is no need for an explicit save
	 * operation.
	 */
	@Override
	public void save() {
		// Do nothing
	}

	/**
	 * No-op implementation.
	 * 
	 * This method is required by the interface but is not used because ChronicleMap
	 * automatically loads data from the mapped file upon creation.
	 */
	@Override
	public void load() {
		// Do nothing
	}
}
