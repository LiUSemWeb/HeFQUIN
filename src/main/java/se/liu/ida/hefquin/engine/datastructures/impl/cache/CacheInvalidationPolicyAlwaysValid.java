package se.liu.ida.hefquin.engine.datastructures.impl.cache;

/**
 * A generic implementation of {@link CacheInvalidationPolicy} that always
 * returns <code>true</code>. In other words, when using this invalidation
 * policy, cache entries never become stale.
 */
public class CacheInvalidationPolicyAlwaysValid<EntryType extends CacheEntry<ObjectType>,ObjectType>
       implements CacheInvalidationPolicy<EntryType, ObjectType>
{
	@Override
	public boolean isStillValid( final EntryType e ) {
		return true;
	}

}
