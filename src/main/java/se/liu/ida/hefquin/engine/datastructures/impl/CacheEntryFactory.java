package se.liu.ida.hefquin.engine.datastructures.impl;

/**
 * Creates EntryType objects that wrap ObjectType objects.
 */
public interface CacheEntryFactory<EntryType extends CacheEntry<ObjectType>, ObjectType>
{
	/**
	 * Creates and returns a new EntryType object that wraps the given object.
	 */
	EntryType createCacheEntry(ObjectType obj);
}
