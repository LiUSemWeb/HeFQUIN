package se.liu.ida.hefquin.federation.access.impl.cache.chroniclemap;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.openhft.chronicle.map.ChronicleMap;
import se.liu.ida.hefquin.base.datastructures.Cache;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CachePolicies;

/**
 * A thread-safe persistent cache implementation. This cache uses a
 * {@link ChronicleMap}.
 */
public class ChronicleMapCache implements Cache<ChronicleMapCacheKey, ChronicleMapCacheObject>, AutoCloseable
{
	private static Logger logger = LoggerFactory.getLogger( ChronicleMapCache.class );
	protected final ChronicleMap<ChronicleMapCacheKey, ChronicleMapCacheEntry> map;
	protected final CachePolicies<ChronicleMapCacheKey, ChronicleMapCacheObject, ChronicleMapCacheEntry> policies;
	protected static final int DEFAULT_CAPACITY = 1000;
	protected static final String DEFAULT_FILENAME = "cache/chronicle-map.dat";
	protected final int capacity;
	protected final String filename;

	/**
	 * Constructs a new {@link ChronicleMapCache} with the default cache file and
	 * the default capacity.
	 * 
	 * @throws IOException
	 */
	public ChronicleMapCache( final CachePolicies<ChronicleMapCacheKey, ChronicleMapCacheObject, ChronicleMapCacheEntry> policies )
			throws IOException {
		this(DEFAULT_CAPACITY, policies, DEFAULT_FILENAME);
	}

	/**
	 * Constructs a new {@link ChronicleMapCache} with the default cache file and a
	 * maximum capacity.
	 *
	 * @param capacity Maximum cache capacity.
	 * @throws IOException
	 */
	public ChronicleMapCache( final int capacity,
	                          final CachePolicies<ChronicleMapCacheKey, ChronicleMapCacheObject, ChronicleMapCacheEntry> policies )
			throws IOException {
		this(capacity, policies, DEFAULT_FILENAME);
	}

	/**
	 * Constructs a new {@link ChronicleMapCache} with a custom file path and a
	 * maximum capacity.
	 *
	 * @param filename Path to the cache file
	 * @throws IOException
	 */
	public ChronicleMapCache( final int capacity,
	                          final CachePolicies<ChronicleMapCacheKey, ChronicleMapCacheObject, ChronicleMapCacheEntry> policies,
	                          final String filename )
			throws IOException {
		assert capacity > 0;
		assert policies != null;
		assert filename != null && ! filename.isBlank();

		this.capacity = capacity;
		this.policies = policies;
		this.filename = filename;
		map = initializeMap(filename, capacity);

		logger.info( toString() );
	}
	
	protected static File ensureParentDirectoryExists( final String filename ) {
		final File file = new File( filename );
		final File parent = file.getParentFile();
		if ( parent != null && ! parent.exists() && ! parent.mkdirs() ) {
			throw new RuntimeException( "Failed to create directory: " + parent.getAbsolutePath() );
		}
		return file;
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
	protected ChronicleMap<ChronicleMapCacheKey, ChronicleMapCacheEntry> initializeMap( final String filename,
	                                                                                    final int capacity )
			throws IOException {
		final File file = ensureParentDirectoryExists(filename);
		return ChronicleMap.of( ChronicleMapCacheKey.class, ChronicleMapCacheEntry.class )
			.name("cache-map")
			.entries(capacity)
			.averageKeySize(512)
			.averageValueSize(1024)
			.maxBloatFactor(5.0)
			.createPersistedTo(file);
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public void put( final ChronicleMapCacheKey key, final ChronicleMapCacheObject value ) {
		final ChronicleMapCacheEntry entry = policies.getEntryFactory().createCacheEntry(value);
		map.put(key, entry);
	}

	@Override
	public ChronicleMapCacheObject get( final ChronicleMapCacheKey key ) {
		final ChronicleMapCacheEntry entry = map.get(key);
		return entry == null ? null : entry.getObject();
	}

	@Override
	public boolean evict( final ChronicleMapCacheKey key ) {
		return map.remove(key) != null;
	}

	@Override
	public boolean evict( final ChronicleMapCacheKey key, final ChronicleMapCacheObject value ) {
		final ChronicleMapCacheEntry entry = map.get(key);
		if ( entry != null && entry.getObject().equals(value) ) {
			return map.remove(key) != null;
		}
		return false;
	}

	public int size() {
		return map.size();
	}

	@Override
	public String toString() {
		return "ChronicleMapCache{filename=" + filename + ", capacity=" + capacity + ", size=" + map.size() + "}";
	}

	@Override
	public void close() {
		map.close();
	}
}
