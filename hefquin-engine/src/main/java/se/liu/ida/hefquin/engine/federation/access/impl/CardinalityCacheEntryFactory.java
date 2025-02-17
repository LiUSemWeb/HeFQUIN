package se.liu.ida.hefquin.engine.federation.access.impl;

import java.time.Instant;

/**
 * An entry used when caching cardinality requests.
 */
public class CardinalityCacheEntryFactory  {
	private final long defaultTimeToLive = 5 * 60 * 1000; // ms
	private final long timeToLive;

	public CardinalityCacheEntryFactory(){
		timeToLive = defaultTimeToLive;
	}

	public CardinalityCacheEntryFactory( final long timeToLive ){
		this.timeToLive = timeToLive;
	}

	public CardinalityCacheEntry createEntry( int cardinality ){
		final long expirationTime = Instant.now().toEpochMilli() + timeToLive;
		return new CardinalityCacheEntry( cardinality, expirationTime );
	}
}
