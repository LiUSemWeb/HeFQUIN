package se.liu.ida.hefquin.federation.access.impl.cache.chroniclemap;

import java.time.Instant;

import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheEntryFactory;

/**
 * Factory for creating {@link ChronicleMapCacheEntry} instances with the
 * current timestamp.
 */
public class ChronicleMapCacheEntryFactory implements CacheEntryFactory<ChronicleMapCacheEntry, ChronicleMapCacheObject>
{
	/**
	 * Creates a new cache entry for the given object, using the current time as the
	 * creation timestamp.
	 *
	 * @param object the cached object
	 * @return a new cache entry wrapping the object
	 */
	@Override
	public ChronicleMapCacheEntry createCacheEntry( final ChronicleMapCacheObject object ) {
		return new ChronicleMapCacheEntry( object, Instant.now().toEpochMilli() );
	}
}
