package se.liu.ida.hefquin.federation.access.impl.cache.chroniclemap;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheEntryFactory;
import se.liu.ida.hefquin.federation.access.DataRetrievalResponse;
import se.liu.ida.hefquin.federation.access.impl.cache.PersistentCacheEntry;

/**
 * Factory for creating {@link PersistentCacheEntry} instances with the
 * current system time as creation timestamp.
 */
public class ChronicleMapCacheEntryFactory implements CacheEntryFactory<PersistentCacheEntry, CompletableFuture<? extends DataRetrievalResponse<?>>>
{
	/**
	 * Creates a new cache entry for the given object, using the current time
	 * (in milliseconds since the epoch) as the creation timestamp.
	 *
	 * @param object the cached object, represented as a {@link CompletableFuture}
	 *               providing a {@link DataRetrievalResponse}
	 * @return a new {@link PersistentCacheEntry} wrapping the given object
	 */
	@Override
	public PersistentCacheEntry createCacheEntry( final CompletableFuture<? extends DataRetrievalResponse<?>> object ) {
		return new PersistentCacheEntry( object, Instant.now().toEpochMilli() );
	}
}
