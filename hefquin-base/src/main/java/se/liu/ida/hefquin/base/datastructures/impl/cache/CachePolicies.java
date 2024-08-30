package se.liu.ida.hefquin.base.datastructures.impl.cache;

public interface CachePolicies<IdType,
                               ObjectType,
                               EntryType extends CacheEntry<ObjectType>>
{
	CacheEntryFactory<EntryType, ObjectType> getEntryFactory();
	CacheReplacementPolicyFactory<IdType,ObjectType,EntryType> getReplacementPolicyFactory();
	CacheInvalidationPolicy<EntryType,ObjectType> getInvalidationPolicy();
}
