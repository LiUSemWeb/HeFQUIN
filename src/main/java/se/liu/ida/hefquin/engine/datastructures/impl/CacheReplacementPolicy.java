package se.liu.ida.hefquin.engine.datastructures.impl;

/**
 * Interface for classes that implement a replacement policy for a cache.
 */
public interface CacheReplacementPolicy<IdType,
                                        ObjectType,
                                        EntryType extends CacheEntry<ObjectType>>
{
	/**
	 * Tries to determine the given number of cache entries that can be evicted
	 * from the cache. These cache entries are identified by the corresponding
	 * ID with which they are associated in the cache. The method may return
	 * fewer than the given number of candidates (e.g., if there are not enough
	 * entries).
	 */
	Iterable<IdType> getEvictionCandidates(int numberOfCandidates);

	/**
	 * Used by the cache to inform the replacement policy that
	 * the given entry has been added to the cache.
	 */
	void entryWasAdded(IdType id, EntryType e);

	/**
	 * Used by the cache to inform the replacement policy that
	 * the given entry has been requested from the cache.
	 */
	void entryWasRequested(IdType id, EntryType e);

	/**
	 * Used by the cache to inform the replacement policy that
	 * the given entry has been evicted from the cache.
	 */
	void entryWasEvicted(IdType id);

	/**
	 * Used by the cache to inform the replacement policy that
	 * the cache has been cleared.
	 */
	void clear();

}
