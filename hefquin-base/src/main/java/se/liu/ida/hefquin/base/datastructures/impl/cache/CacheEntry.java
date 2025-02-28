package se.liu.ida.hefquin.base.datastructures.impl.cache;

/**
 * @param <ObjectType> the type of the objects to be cached via a cache entry
 */
public interface CacheEntry<ObjectType>
{
	/**
	 * Returns the object that is cached via this cache entry.
	 */
	ObjectType getObject();

	/**
	 * Returns the time at which this cache entry was created.
	 */
	long createdAt();
}


