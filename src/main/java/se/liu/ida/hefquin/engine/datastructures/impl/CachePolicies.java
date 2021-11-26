package se.liu.ida.hefquin.engine.datastructures.impl;

public interface CachePolicies<EntryType extends CacheEntry<ObjectType>,ObjectType>
{
	CacheInvalidationPolicy<EntryType,ObjectType> getInvalidationPolicy();
	CacheReplacementPolicy<EntryType,ObjectType> getReplacementPolicy();
}
