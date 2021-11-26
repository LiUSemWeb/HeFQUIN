package se.liu.ida.hefquin.engine.datastructures.impl;

/**
 * @param <ObjectType> the type of the objects to be cached via a cache entry
 */
public interface CacheEntry<ObjectType>
{
	/**
	 * Returns the object that is cached via this cache entry.
	 */
	ObjectType getObject();
}
