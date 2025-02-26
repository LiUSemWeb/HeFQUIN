package se.liu.ida.hefquin.base.datastructures.impl.cache;

import java.time.Instant;

public class CacheInvalidationPolicyTimeToLive<EntryType extends CacheEntry<ObjectType>, ObjectType>
		implements CacheInvalidationPolicy<EntryType, ObjectType> {

	protected final long timeToLive;

	public CacheInvalidationPolicyTimeToLive( long timeToLive ){
		this.timeToLive = timeToLive;
	}

	/**
	 * Returns <code>true</code> if the given cache entry is not stale.
	 */
	public boolean isStillValid( EntryType e ) {
		return e.createdAt() + timeToLive > Instant.now().toEpochMilli();
	}
}
