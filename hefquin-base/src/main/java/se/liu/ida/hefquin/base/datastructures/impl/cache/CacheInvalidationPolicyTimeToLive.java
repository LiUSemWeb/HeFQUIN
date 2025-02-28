package se.liu.ida.hefquin.base.datastructures.impl.cache;

import java.time.Instant;

public class CacheInvalidationPolicyTimeToLive<EntryType extends CacheEntry<ObjectType>, ObjectType>
		implements CacheInvalidationPolicy<EntryType, ObjectType>
{
	protected final long timeToLive;

	public CacheInvalidationPolicyTimeToLive( final long timeToLive ){
		this.timeToLive = timeToLive;
	}

	/**
	 * Returns <code>true</code> if the given cache entry has not reached
	 * the time to live considered by this policy.
	 */
	public boolean isStillValid( final EntryType e ) {
		return e.createdAt() + timeToLive > Instant.now().toEpochMilli();
	}
}
