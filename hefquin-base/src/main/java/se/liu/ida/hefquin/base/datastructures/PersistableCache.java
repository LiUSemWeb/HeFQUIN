package se.liu.ida.hefquin.base.datastructures;

import java.util.Map;

/**
 * A generic interface for data structures that can be used as a persisted cache
 * for objects of a specific type. Implementations of this interface may employ
 * their particular policies for cache replacement and for cache invalidation.
 *
 * @param <IdType> the type of the values by which the cached objects can be identified
 * @param <ObjectType> the type of the objects to be maintained in this cache
 */
public interface PersistableCache<IdType, ObjectType> extends Cache<IdType, ObjectType> {

	/**
	 * Adds all entries in the incoming map to this cache, associating each object
	 * with its corresponding ID. If an object is already associated with a given
	 * ID, it will be replaced by the new one.
	 * 
	 * Updating the cache may also lead to the eviction of other cached objects,
	 * depending on, whether the cache has reached its capacity. In such a case, the
	 * object(s) that are evicted are determined based on the cache replacement
	 * policy of this cache.
	 * 
	 * @param entries a map of IDs to objects to be added to the cache
	 */
	void putAll( Map<IdType, ObjectType> map );
}
