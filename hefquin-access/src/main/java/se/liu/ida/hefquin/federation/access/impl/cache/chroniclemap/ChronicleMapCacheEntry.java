package se.liu.ida.hefquin.federation.access.impl.cache.chroniclemap;

import java.util.concurrent.CompletableFuture;

import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheEntryBase;
import se.liu.ida.hefquin.federation.access.DataRetrievalResponse;

/**
 * Cache entry implementation for ChronicleMap-based caching.
 *
 * <p>
 * Wraps a {@link CompletableFuture} holding a {@link DataRetrievalResponse}
 * together with its creation timestamp.
 * </p>
 *
 * <p>
 * The response is represented as a future to allow asynchronous retrieval, but
 * may be already completed when stored in or retrieved from the cache.
 * </p>
 */
public class ChronicleMapCacheEntry extends CacheEntryBase<CompletableFuture<? extends DataRetrievalResponse<?>>>
{
	/**
	 * Creates a new cache entry.
	 *
	 * @param object       a future providing the cached {@link DataRetrievalResponse}
	 * @param creationTime the timestamp representing when this entry was created
	 */
	public ChronicleMapCacheEntry( final CompletableFuture<? extends DataRetrievalResponse<?>> object,
	                               final long creationTime ) {
		super(object, creationTime);
	}

	@Override
	public String toString() {
		return "ChronicleMapCacheEntry{object=" + getObject() + ", creationTime=" + creationTime + "}";
	}
}
