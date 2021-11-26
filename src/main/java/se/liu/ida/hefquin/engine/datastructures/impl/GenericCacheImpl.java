package se.liu.ida.hefquin.engine.datastructures.impl;

import java.util.Map;

import se.liu.ida.hefquin.engine.datastructures.Cache;

/**
 * A generic, thread-safe implementation of {@link Cache}.
 */
public class GenericCacheImpl<IdType,
                              ObjectType,
                              EntryType extends CacheEntry<ObjectType>
                             > implements Cache<IdType, ObjectType>
{
	protected final Map<IdType,EntryType> index;
	protected final CacheEntryFactory<EntryType,ObjectType> entryFactory;
	protected final CachePolicies<EntryType,ObjectType> policies;

	public GenericCacheImpl( final Map<IdType,EntryType> index,
	                         final CacheEntryFactory<EntryType,ObjectType> entryFactory,
	                         final CachePolicies<EntryType,ObjectType> policies ) {
		assert index != null;
		assert entryFactory != null;
		assert policies != null;

		this.index = index;
		this.entryFactory = entryFactory;
		this.policies = policies;
	}

	@Override
	public void put( final IdType id, final ObjectType obj ) {
		if ( obj == null )
			throw new IllegalArgumentException();

		final EntryType e = entryFactory.createCacheEntry(obj);
		synchronized (index) {
			index.put(id, e);
		}
	}

	@Override
	public ObjectType get( final IdType id ) {
		final EntryType e;
		synchronized (index) {
			e = index.get(id);
			if ( e == null )
				return null;

			if ( ! policies.getInvalidationPolicy().isStillValid(e) ) {
				index.remove(id);
				return null;
			}
		}

		policies.getReplacementPolicy().entryWasRequested(e);
		return e.getObject();
	}

	@Override
	public boolean evict( final IdType id ) {
		final EntryType e;
		synchronized (index) {
			e = index.remove(id);
		}

		return e != null;
	}

	@Override
	public boolean evict( final IdType id, final ObjectType obj ) {
		synchronized (index) {
			return index.remove(id, obj);
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
			index.clear();
		}
	}

}
