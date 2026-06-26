package se.liu.ida.hefquin.federation.access.impl.cache;

import se.liu.ida.hefquin.base.datastructures.Cache;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheEntry;

/**
 * A cache composed of two cache layers arranged hierarchically.
 *
 * <p>
 * Entries are written to both layers. Lookup operations first consult the
 * primary cache layer (L1). If no entry is found, the secondary cache layer
 * (L2) is consulted. Entries retrieved from L2 are promoted to L1 to improve
 * access performance for subsequent requests.
 * </p>
 *
 * <p>
 * Eviction and clear operations are applied to both cache layers.
 * </p>
 *
 * <p>
 * All operations are synchronized on an internal lock, ensuring that
 * interactions between the two cache layers are performed atomically with
 * respect to other operations on the same {@code HierarchicalCache} instance.
 * </p>
 *
 * @param <IdType>     key type used to identify cached objects
 * @param <ObjectType> type of objects stored in the cache
 * @param <EntryType>  cache entry type used by underlying cache layers
 */
public class HierarchicalCache<IdType,
                               ObjectType,
                               EntryType extends CacheEntry<ObjectType>
							   > implements Cache<IdType,ObjectType>
{
	protected final Cache<IdType, ObjectType> l1;
	protected final Cache<IdType, ObjectType> l2;
	private final Object lock = new Object();

	/**
	 * Creates a hierarchical cache consisting of a primary and a secondary cache
	 * layer.
	 *
	 * @param l1 primary cache layer consulted first during lookups
	 * @param l2 secondary cache layer consulted when entries are not found in the
	 *           primary cache layer
	 */
	public HierarchicalCache( final Cache<IdType, ObjectType> l1,
	                          final Cache<IdType, ObjectType> l2 ) {
		this.l1 = l1;
		this.l2 = l2;
	}

	/**
	 * Stores the given key-value pair in both cache layers.
	 *
	 * <p>
	 * Existing entries associated with the key are replaced according to the
	 * behavior of the underlying cache layers.
	 * </p>
	 */
	@Override
	public void put( final IdType key, final ObjectType value ) {
		synchronized (lock) {
			l1.put(key, value);
			l2.put(key, value);
		}
	}

	/**
	 * Retrieves the value associated with the given key.
	 *
	 * <p>
	 * The primary cache layer is consulted first. If no entry is found, the
	 * secondary cache layer is consulted. Entries retrieved from the secondary
	 * cache layer are promoted to the primary cache layer before being returned.
	 * </p>
	 *
	 * @return the cached value, or {@code null} if no entry exists
	 */
	@Override
	public ObjectType get( final IdType key ) {
		synchronized (lock) {
			final ObjectType value = l1.get(key);

			if ( value != null ) {
				return value;
			}

			final ObjectType fallbackValue = l2.get(key);

			if ( fallbackValue != null ) {
				l1.put(key, fallbackValue);
			}

			return fallbackValue;
		}
	}

	/**
	 * Removes the entry associated with the given key from both cache layers.
	 *
	 * @return {@code true} if at least one cache layer removed an entry
	 */
	@Override
	public boolean evict( final IdType key ) {
		synchronized (lock) {
			return l1.evict(key) | l2.evict(key);
		}
	}

	/**
	 * Removes entries associated with the given key if they currently map to the
	 * specified value.
	 *
	 * <p>
	 * The operation is applied independently to each cache layer and behaves as if
	 * {@code evict(key, value)} were invoked directly on each layer. Consequently,
	 * an entry may be removed from some layers but not others if the layers use
	 * different representations or equality semantics for cached values.
	 * </p>
	 *
	 * @return {@code true} if at least one cache layer removed an entry
	 */
	@Override
	public boolean evict( final IdType key, final ObjectType value ) {
		synchronized (lock) {
			return l1.evict(key, value) | l2.evict(key, value);
		}
	}

	/**
	 * Returns {@code true} if both cache layers are empty.
	 *
	 * @return {@code true} if neither cache layer contains any entries
	 */
	@Override
	public boolean isEmpty() {
		synchronized (lock) {
			return l1.isEmpty() && l2.isEmpty();
		}
	}

	/**
	 * Removes all entries from both cache layers.
	 */
	@Override
		public void clear() {
		synchronized (lock) {
			l1.clear();
			l2.clear();
		}
	}

	/**
	 * Releases all resources associated with both cache layers.
	 *
	 * <p>
	 * The close operation is propagated to the primary and secondary cache layers.
	 * After this method returns, the cache should not be used anymore.
	 * </p>
	 *
	 * <p>
	 * If a cache layer does not manage any external resources, its implementation
	 * of {@code close()} may perform no action.
	 * </p>
	 */
	@Override
	public void close() {
		synchronized (lock) {
			l1.close();
			l2.close();
		}
	}
}