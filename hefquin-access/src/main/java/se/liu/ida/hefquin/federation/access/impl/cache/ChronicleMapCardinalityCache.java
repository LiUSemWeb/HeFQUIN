package se.liu.ida.hefquin.federation.access.impl.cache;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.openhft.chronicle.map.ChronicleMap;
import se.liu.ida.hefquin.base.datastructures.PersistableCache;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheEntryFactory;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheInvalidationPolicy;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CachePolicies;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheReplacementPolicy;

/**
 * A thread-safe persistent cache implementation for storing cardinality entries. This
 * cache uses a {@link ChronicleMap}.
 *
 * @param <K> The key type for caching cardinality responses.
 */
public class ChronicleMapCardinalityCache implements PersistableCache<CardinalityCacheKey, CardinalityCacheEntry>
{
	protected final Map<CardinalityCacheKey, CardinalityCacheEntry> map;
	protected final static int defaultCapacity = 50_000;
	protected final static String defaultFilename = "cache/chronicle-map.dat";
	protected final int capacity;

	protected final CacheEntryFactory<CardinalityCacheEntry, Integer> entryFactory;
	protected final CacheInvalidationPolicy<CardinalityCacheEntry, Integer> invalidationPolicy;
	protected final CacheReplacementPolicy<CardinalityCacheKey, Integer, CardinalityCacheEntry> replacementPolicy;

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
		replacementPolicy = policies.getReplacementPolicyFactory().create();
		this.capacity = capacity;

		map = initializeMap( filename, capacity );
		initializeReplacementPolicy();
		evictExcessEntries();
	}
	
	/**
	 * Ensures that the cache file exists before initialization. If the file does
	 * not exist, it is created along with necessary directories.
	 * 
	 * @param filename The path of the file to ensure exists
	 * @return {@link File} object representing the ensured file
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
	 * Initializes and returns a persistent ChronicleMap for storing cardinality cache entries.
	 * If the specified file does not exist, it is created before the map is initialized.
	 *
	 * @param filename The path to the ChronicleMap file for persistent storage.
	 * @param capacity The maximum number of entries the cache can store.
	 * @return A {@link ChronicleMap} instance configured for storing cardinality cache entries.
	 * @throws IOException If an error occurs while creating or accessing the file.
	 */
	private static Map<CardinalityCacheKey, CardinalityCacheEntry> initializeMap(final String filename, final int capacity) throws IOException {
		ensureFileExists( filename );
		return ChronicleMap.of( CardinalityCacheKey.class, CardinalityCacheEntry.class )
			.name( "cardinality-map" )
			.entries( capacity )
			.averageKeySize( 512 )
			.averageValueSize( 64 )
			.maxBloatFactor( 5.0 )
			.createPersistedTo( new File( filename ) );
	}

	/**
	 * Populates the cache's replacement policy using existing entries.  Entries are sorted chronologically
	 * by their creation timestamp before being added to the replacement policy to maintain eviction order.
	 */
	private void initializeReplacementPolicy() {
		final List<Entry<CardinalityCacheKey, CardinalityCacheEntry>> entries = new ArrayList<>( map.entrySet() );
		entries.sort( Comparator.comparingLong( e -> e.getValue().createdAt() ) );
		entries.forEach( e -> replacementPolicy.entryWasAdded( e.getKey(), e.getValue() ) );
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
		put( key, entry );
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
		if ( map.containsKey( key ) )
			replacementPolicy.entryWasRewritten( key, entry );
		else {
			// Check if max capacity has been reached
			if ( map.size() == capacity )
				replacementPolicy.getEvictionCandidates( 1 ).forEach( this::evict );

			replacementPolicy.entryWasAdded( key, entry );
		}

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
		if ( entry == null )
			return null;

		// lazy evict
		if ( ! invalidationPolicy.isStillValid( entry ) ) {
			evict( key );
			return null;
		}

		replacementPolicy.entryWasRequested( key, entry );
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
		final boolean removed = map.remove( key ) != null;

		if ( removed )
			replacementPolicy.entryWasEvicted( key );

		return removed;
	}

	/**
	 * Removes the specified entry from the cache only if the current value matches.
	 *
	 * @param key   The key to remove.
	 * @param value The expected value to match before removal.
	 * @return {@link true} if the entry was removed, {@link false} otherwise.
	 */
	public boolean evict( final CardinalityCacheKey key, final Integer value ) {
		final CardinalityCacheEntry entry = entryFactory.createCacheEntry( value );
		return evict( key, entry );
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
		if ( map.containsKey( key ) && map.get( key ).equals( entry ) ) {
			evict( key );
			return true;
		}
		return false;
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
		replacementPolicy.clear();
	}

	/**
	 * Shrinks the cache to match its capacity.
	 */
	public void evictExcessEntries(){
		final int excess = map.size() - capacity;
		if (excess > 0)
			replacementPolicy.getEvictionCandidates( excess ).forEach( this::evict );
	}

	/**
	 * No-op since ChronicleMap persists changes automatically.
	 */
	@Override
	public void save() {
		// Do nothing
	}

	/**
	 * No-op since ChronicleMap automatically loads data.
	 */
	@Override
	public void load() {
		// Do nothing
	}
}
