package se.liu.ida.hefquin.base.datastructures.impl.cache;

/**
 * Interface for classes that determine whether cache entries are
 * still valid. Entries that are not valid anymore can be evicted
 * from the corresponding cache for which this policy is used.
 */
public interface CacheInvalidationPolicy<EntryType extends CacheEntry<ObjectType>,ObjectType>
{
	/**
	 * Returns <code>true</code> if the given cache entry is still
	 * valid according to this cache invalidation policy.
	 */
	boolean isStillValid( EntryType e );
}
