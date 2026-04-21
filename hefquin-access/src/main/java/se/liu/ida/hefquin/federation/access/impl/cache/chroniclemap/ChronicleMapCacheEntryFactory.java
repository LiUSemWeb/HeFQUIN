package se.liu.ida.hefquin.federation.access.impl.cache.chroniclemap;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheEntryFactory;
import se.liu.ida.hefquin.federation.access.DataRetrievalResponse;

/**
 * Factory for creating {@link ChronicleMapCacheEntry} instances with the
 * current system time as creation timestamp.
 */
public class ChronicleMapCacheEntryFactory implements CacheEntryFactory<ChronicleMapCacheEntry, CompletableFuture<? extends DataRetrievalResponse<?>>>
{
	/**
	 * Creates a new cache entry for the given object, using the current time
	 * (in milliseconds since the epoch) as the creation timestamp.
	 *
	 * @param object the cached object, represented as a {@link CompletableFuture}
	 *               providing a {@link DataRetrievalResponse}
	 * @return a new {@link ChronicleMapCacheEntry} wrapping the given object
	 */
	@Override
	public ChronicleMapCacheEntry createCacheEntry( final CompletableFuture<? extends DataRetrievalResponse<?>> object ) {
		return new ChronicleMapCacheEntry( object, Instant.now().toEpochMilli() );
	}
}
