package se.liu.ida.hefquin.base.datastructures;

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
	 * Saves the current state of the cache to persistent storage.
	 * Implementations may choose different mechanisms for persistence, 
	 * such as writing to a file or a database.
	 * 
	 * This method should ensure that all cached data is synchronized with
	 * persistent storage. Depending on the implementation, synchronization
	 * may be automatic or explicitly controlled.
	 */
	void save();

	/**
	 * Loads the cache state from persistent storage.
	 * If persistent data exists, it should be restored into the cache.
	 * Implementations should handle cases where no prior state exists
	 * gracefully.
	 */
	void load();
}
