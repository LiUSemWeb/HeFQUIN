package se.liu.ida.hefquin.base.datastructures.impl.cache;

public interface CacheReplacementPolicyFactory<IdType,
                                               ObjectType,
                                               EntryType extends CacheEntry<ObjectType>>
{
	CacheReplacementPolicy<IdType,ObjectType,EntryType> create();
}
