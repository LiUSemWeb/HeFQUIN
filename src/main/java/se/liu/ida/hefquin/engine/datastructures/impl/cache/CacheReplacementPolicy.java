package se.liu.ida.hefquin.engine.datastructures.impl.cache;

/**
 * Interface for classes that implement a replacement policy for a cache.
 */
public interface CacheReplacementPolicy<IdType,
                                        ObjectType,
                                        EntryType extends CacheEntry<ObjectType>>
{
	/**
	 * Tries to determine the given number of cache entries as next
	 * candidates to be evicted from the cache. The candidates are
	 * identified by the corresponding ID with which these entries
	 * are associated in the cache. While the identified entries are
	 * considered as candidates, they are not yet considered to be
	 * evicted (once they are, the cache will inform the replacement
	 * policy by calling {@link #entryWasEvicted(Object)}.
	 * 
	 * This method may not return as many candidates as requests,
	 * (e.g., if there are not enough entries).
	 */
	Iterable<IdType> getEvictionCandidates(int numberOfCandidates);

	/**
	 * Used by the cache to inform the replacement policy that the
	 * given entry has been added to the cache (which includes that
	 * the given ID is new).
	 */
	void entryWasAdded(IdType id, EntryType e);

	/**
	 * Used by the cache to inform the replacement policy that the
	 * given entry has been added to the cache as a new entry for
	 * the given ID.
	 */
	void entryWasRewritten(IdType id, EntryType e);

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
