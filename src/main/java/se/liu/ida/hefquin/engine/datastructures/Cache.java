package se.liu.ida.hefquin.engine.datastructures;

/**
 * A generic interface for data structures that can be used as a cache for
 * objects of a specific type. Implementations of this interface may employ
 * their particular policies for cache replacement and for cache invalidation.
 *
 * @param <IdType> the type of the values by which the cached objects can be identified
 * @param <ObjectType> the type of the objects to be maintained in this cache
 */
public interface Cache<IdType,ObjectType>
{
	/**
	 * Adds the given object to this cache, associated with the given ID.
	 * If there is another object currently associated with the given ID,
	 * that object will be replaced by the given one.
	 * 
	 * Updating the cache may also lead to the eviction of other cached
	 * objects, depending on whether the cache has reached its capacity.
	 * In such a case, the object(s) that are evicted are determined
	 * based on the cache replacement policy of this cache.
	 */
	void put(IdType id, ObjectType obj);

	/**
	 * Returns the object associated with the given ID in this cache, or
	 * <code>null</code> if there is no such object in the cache.
	 * 
	 * Implementations may also return <code>null</code> even if there is
	 * such an object but that object is not valid anymore, according to
	 * a cache invalidation policy employed by the cache. In such a case,
	 * the object may also be evicted altogether from the cache.
	 */
	ObjectType get(IdType id);

	/**
	 * If the cache contains an object that is associated with the given ID,
	 * then this object is evicted from the cache and the method returns
	 * <code>true</true>. If there is no such cached object, the method
	 * returns <code>false</true>.
	 * 
	 * Calling this method may also have the side effect that other objects
	 * are evicted from the cache, depending on the invalidation policy of
	 * this cache.
	 */
	boolean evict(IdType id);

	/**
	 * If the given object is associated with the given ID in this cache,
	 * then this object is evicted from the cache and the method returns
	 * <code>true</true>. If there is another object associated with the
	 * ID, or none at all, the method returns <code>false</true>.
	 * 
	 * Calling this method may also have the side effect that other objects
	 * are evicted from the cache, depending on the invalidation policy of
	 * this cache.
	 */
	boolean evict(IdType id, ObjectType obj);

	/**
	 * Returns <code>true</code> if the cache does not contain any objects
	 * at the moment, and <code>false</code> otherwise.
	 */
	boolean isEmpty();

	/**
	 * Removes all objects from this cache.
	 */
	void clear();
}
