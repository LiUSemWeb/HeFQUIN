package se.liu.ida.hefquin.federation.access.impl.cache;

import java.util.Map;

import se.liu.ida.hefquin.base.datastructures.Cache;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheEntry;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheEntryFactory;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheInvalidationPolicy;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CachePolicies;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheReplacementPolicy;

/**
 * Generic implementation of a cache layer.
 *
 * <p>
 * A cache layer stores objects identified by keys and delegates cache
 * management behavior to configurable policies.
 * </p>
 *
 * <ul>
 * <li>A {@link CacheReplacementPolicy} determines which entries should be
 * evicted when the cache exceeds its capacity.</li>
 * <li>A {@link CacheInvalidationPolicy} determines whether entries are still
 * valid when accessed.</li>
 * <li>A {@link CacheEntryFactory} creates cache entry instances for
 * objects.</li>
 * </ul>
 *
 * <p>
 * This class is designed to support thread-safe cache operations by
 * synchronizing access to the underlying cache map. As a result, updates to the
 * cache contents and interactions with the configured policies are performed
 * atomically with respect to other cache operations.
 * </p>
 *
 * <p>
 * Thread safety depends on subclasses providing a map instance that is used
 * consistently as the synchronization monitor and on the configured policies
 * behaving correctly when invoked under this synchronization scheme.
 * </p>
 *
 * @param <IdType>     key type used to identify cached objects
 * @param <ObjectType> type of objects stored in the cache
 * @param <EntryType>  cache entry type used internally
 */
public class CacheLayer<IdType,
                        ObjectType,
                        EntryType extends CacheEntry<ObjectType>
                        > implements Cache<IdType, ObjectType>
{
	protected final Object lock = new Object();
	protected final Map<IdType, EntryType> map;
	protected final int capacity;

	protected final CacheEntryFactory<EntryType, ObjectType> entryFactory;
	protected final CacheReplacementPolicy<IdType, ObjectType, EntryType> replacementPolicy;
	protected final CacheInvalidationPolicy<EntryType, ObjectType> invalidationPolicy;

	/**
	 * Creates a cache layer with the specified capacity and cache policies.
	 *
	 * <p>
	 * The provided policies define how cache entries are created, invalidated, and
	 * selected for eviction. The cache capacity represents the maximum number of
	 * entries that may be stored simultaneously.
	 * </p>
	 * 
	 * @param map      backing map used to store cache entries and synchronize cache
	 *                 operations
	 * @param capacity maximum number of entries that can be stored
	 * @param policies cache policies used by this cache layer
	 */
	public CacheLayer( final Map<IdType, EntryType> map,
	                   final int capacity,
	                   final CachePolicies<IdType, ObjectType, EntryType> policies ) {
		assert map != null;
		assert capacity > 0;
		assert policies != null;

		this.map = map;
		this.capacity = capacity;
		entryFactory = policies.getEntryFactory();
		replacementPolicy = policies.getReplacementPolicyFactory().create();
		invalidationPolicy = policies.getInvalidationPolicy();
	}

	/**
	 * Inserts or updates a cache entry.
	 *
	 * <p>
	 * If the key is not present, a new entry is added and the replacement policy is
	 * notified via {@code entryWasAdded}. If the key already exists, its value is
	 * replaced and the replacement policy is notified via
	 * {@code entryWasRewritten}.
	 * </p>
	 *
	 * <p>
	 * After insertion, the cache capacity is enforced. If the number of entries
	 * exceeds the configured capacity, one or more entries are evicted according to
	 * the replacement policy.
	 * </p>
	 *
	 * @param key   cache key, must not be {@code null}
	 * @param value object to cache, must not be {@code null}
	 *
	 * @throws IllegalArgumentException if {@code key} or {@code value} is
	 *                                  {@code null}
	 */
	@Override
	public void put( final IdType key, final ObjectType value ) {
		if ( key == null )
			throw new IllegalArgumentException("Cache key must not be null");

		if ( value == null )
			throw new IllegalArgumentException("Cache value must not be null");

		final EntryType entry = entryFactory.createCacheEntry(value);

		synchronized (lock) {
			final EntryType oldEntry = map.get(key);
			map.put(key, entry);

			if ( oldEntry == null )
				replacementPolicy.entryWasAdded(key, entry);
			else
				replacementPolicy.entryWasRewritten(key, entry);

			// Enforce max capacity
			if ( map.size() > capacity ) {
				final Iterable<IdType> evictionCandidates = replacementPolicy
						.getEvictionCandidates( map.size() - capacity );
				evictionCandidates.forEach(this::evict);
			}
		}
	}

	/**
	 * Retrieves the cached object associated with the given key.
	 *
	 * <p>
	 * If the key is present, the corresponding entry is first validated using the
	 * configured invalidation policy. Invalid entries are automatically evicted and
	 * treated as cache misses.
	 * </p>
	 *
	 * <p>
	 * Successful lookups notify the replacement policy via
	 * {@code entryWasRequested}, allowing policies such as LRU to update their
	 * internal state.
	 * </p>
	 *
	 * @param key cache key
	 *
	 * @return the cached object if a valid entry exists, otherwise {@code null}
	 */
	@Override
	public final ObjectType get( final IdType key ) {
		synchronized (lock) {
			final EntryType entry = map.get(key);

			if ( entry == null ) {
				return null;
			}

			if ( ! invalidationPolicy.isStillValid(entry) ) {
				evict(key);
				return null;
			}

			replacementPolicy.entryWasRequested(key, entry);
			return entry.getObject();
		}
	}

	/**
	 * Removes the cache entry associated with the given key.
	 *
	 * <p>
	 * If an entry is removed, the replacement policy is notified via
	 * {@code entryWasEvicted} so that any policy-specific state can be updated.
	 * </p>
	 *
	 * @param key key of the entry to remove
	 *
	 * @return {@code true} if an entry was present and removed, {@code false}
	 *         otherwise
	 */
	@Override
	public boolean evict( final IdType key ) {
		System.err.println("Evict: " + key);
		synchronized (lock) {
			if( map.remove(key) != null ) {
				replacementPolicy.entryWasEvicted(key);
				return true;
			}
			return false;
		}
	}

	/**
	 * Removes the cache entry associated with the given key if the cached object
	 * equals the provided value.
	 *
	 * <p>
	 * If an entry is removed, the replacement policy is notified via
	 * {@code entryWasEvicted} so that any policy-specific state can be updated.
	 * </p>
	 *
	 * @param key   key of the entry to remove
	 * @param value expected value associated with the key
	 *
	 * @return {@code true} if an entry was present and removed, {@code false}
	 *         otherwise
	 */
	@Override
	public boolean evict( final IdType key, final ObjectType value ) {
		synchronized (lock) {
			final EntryType entry = map.get(key);
			if ( entry != null && entry.getObject().equals(value) ) {
				return evict(key);
			}
			return false;
		}
	}

	/**
	 * Returns whether the cache currently contains any entries.
	 *
	 * @return {@code true} if the cache contains no entries, {@code false}
	 *         otherwise
	 */
	@Override
	public boolean isEmpty() {
		synchronized (lock) {
			return map.isEmpty();
		}
	}

	/**
	 * Removes all cache entries and resets replacement policy state.
	 *
	 * <p>
	 * After this method returns, the cache contains no entries and the replacement
	 * policy behaves as if it were newly created.
	 * </p>
	 */
	@Override
	public void clear() {
		synchronized (lock) {
			map.clear();
			replacementPolicy.clear();
		}
	}
}