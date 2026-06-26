package se.liu.ida.hefquin.federation.access.impl.cache;

import java.util.concurrent.CompletableFuture;

import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheEntryBase;
import se.liu.ida.hefquin.federation.access.DataRetrievalResponse;

/**
 * Cache entry implementation for persistent caching.
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
public class PersistentCacheEntry extends CacheEntryBase<CompletableFuture<? extends DataRetrievalResponse<?>>>
{
	/**
	 * Creates a new cache entry.
	 *
	 * @param object       a future providing the cached {@link DataRetrievalResponse}
	 * @param creationTime the timestamp representing when this entry was created
	 */
	public PersistentCacheEntry( final CompletableFuture<? extends DataRetrievalResponse<?>> object,
	                               final long creationTime ) {
		super(object, creationTime);
	}

	@Override
	public String toString() {
		return "PersistentCacheEntry{object=" + getObject() + ", creationTime=" + creationTime + "}";
	}
}
