package se.liu.ida.hefquin.engine.datastructures.impl;

public interface CacheReplacementPolicy<EntryType extends CacheEntry<ObjectType>,ObjectType>
{

	void entryWasRequested(EntryType e);

	// question: I believe that this class needs to be combined with the
	// index in order to be able to actually find the replacement candidate
	// entries efficiently -- the question is how? 
}
