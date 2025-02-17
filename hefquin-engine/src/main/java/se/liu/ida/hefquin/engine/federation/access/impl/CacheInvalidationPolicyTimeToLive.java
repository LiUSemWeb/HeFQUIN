package se.liu.ida.hefquin.engine.federation.access.impl;

import java.time.Instant;

import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheEntry;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheInvalidationPolicy;

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
		if( e instanceof CardinalityCacheEntry entry ){
			return entry.getCacheEntryCreatedAt() + timeToLive > Instant.now().toEpochMilli();
		}
		return true;
	}
}
