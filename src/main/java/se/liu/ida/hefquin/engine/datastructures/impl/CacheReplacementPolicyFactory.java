package se.liu.ida.hefquin.engine.datastructures.impl;

public interface CacheReplacementPolicyFactory<IdType,
                                               ObjectType,
                                               EntryType extends CacheEntry<ObjectType>>
{
	CacheReplacementPolicy<IdType,ObjectType,EntryType> create();
}
