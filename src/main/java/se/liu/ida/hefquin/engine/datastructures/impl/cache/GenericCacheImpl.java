package se.liu.ida.hefquin.engine.datastructures.impl.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import se.liu.ida.hefquin.engine.datastructures.Cache;

/**
 * A generic, thread-safe implementation of {@link Cache}.
 */
public class GenericCacheImpl<IdType,
                              ObjectType,
                              EntryType extends CacheEntry<ObjectType>
                             > implements Cache<IdType, ObjectType>
{
	protected final CachePolicies<IdType,ObjectType,EntryType> policies;
	protected final CacheReplacementPolicy<IdType,ObjectType,EntryType> replacementPolicy;

	final int capacity;

	/** The slots of this cache. */
	protected final List<EntryType> slots;

	/** Maps keys to slot indexes. */
	protected final Map<IdType,Integer> index = new HashMap<>();

	/**
	 * Indicates that we have completed the initial cache population
	 * phase during which the cache has not yet reached its capacity.
	 */
	protected boolean initialPhaseCompleted = false;

	/**
	 * Indexes of empty/unused slots; will be used have the initial
	 * cache population phase.
	 * */
	protected Queue<Integer> availableSlotIndexes = new LinkedList<>();

	public GenericCacheImpl( final int capacity,
	                         final CachePolicies<IdType,ObjectType,EntryType> policies ) {
		assert capacity > 0;
		this.capacity = capacity;

		assert policies != null;
		this.policies = policies;
		replacementPolicy = policies.getReplacementPolicyFactory().create();

		slots = new ArrayList<>(capacity);
	}

	@Override
	public void put( final IdType id, final ObjectType obj ) {
		if ( id == null )
			throw new IllegalArgumentException();

		if ( obj == null )
			throw new IllegalArgumentException();

		synchronized (index) {
			final int slotNr;
			final Integer existingSlotNr = index.get(id);
			if ( existingSlotNr != null ) {
				// If we already have an object for the given ID in the cache,
				// then we can simply replace that object with the given object.
				slotNr = existingSlotNr.intValue();
			}
			else if ( ! initialPhaseCompleted ) {
				// If the given ID is a new ID and we are still in the initial
				// cache population phase, we can use the next available slot.
				slotNr = slots.size();
				if ( slotNr + 1 == capacity ) {
					initialPhaseCompleted = true;
				}
			}
			else {
				// If the given ID is a new ID and we are already past the initial
				// cache population phase, we have to replace some object (with some
				// other ID) from the cache.
				final Iterable<IdType> eids = replacementPolicy.getEvictionCandidates(1);
				final IdType eid = eids.iterator().next();
				slotNr = index.get(eid).intValue();
				replacementPolicy.entryWasEvicted(eid);
			}

			final EntryType e = policies.getEntryFactory().createCacheEntry(obj);
			index.put(id, slotNr);
			slots.set(slotNr, e);
			replacementPolicy.entryWasAdded(id, e);
		}
	}

	@Override
	public ObjectType get( final IdType id ) {
		synchronized (index) {
			final Integer slotNr = index.get(id);
			if ( slotNr == null )
				return null;

			final EntryType e = slots.get(slotNr);
			if ( ! policies.getInvalidationPolicy().isStillValid(e) ) {
				index.remove(id);
				slots.set(slotNr, null);
				availableSlotIndexes.add(slotNr);
				replacementPolicy.entryWasEvicted(id);
				return null;
			}

			replacementPolicy.entryWasRequested(id, e);
			return e.getObject();
		}
	}

	@Override
	public boolean evict( final IdType id ) {
		synchronized (index) {
			final Integer slotNr = index.get(id);
			if ( slotNr == null )
				return false;

			index.remove(id);
			slots.set(slotNr, null);
			availableSlotIndexes.add(slotNr);
			replacementPolicy.entryWasEvicted(id);
			return true;
		}
	}

	@SuppressWarnings("unlikely-arg-type")
	@Override
	public boolean evict( final IdType id, final ObjectType obj ) {
		synchronized (index) {
			final Integer slotNr = index.get(id);
			if ( slotNr == null )
				return false;

			if ( ! slots.get(slotNr).equals(obj) )
				return false;

			index.remove(id);
			slots.set(slotNr, null);
			availableSlotIndexes.add(slotNr);
			replacementPolicy.entryWasEvicted(id);
			return true;
		}
	}

	@Override
	public boolean isEmpty() {
		synchronized (index) {
			return index.isEmpty();
		}
	}

	@Override
	public void clear() {
		synchronized (index) {
			for ( int i = 0; i < capacity; ++i )
				slots.set(i, null);

			index.clear();
			availableSlotIndexes.clear();
			replacementPolicy.clear();
		}
	}

}
