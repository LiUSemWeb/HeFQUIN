package se.liu.ida.hefquin.federation.access.impl.cache;

import java.time.Instant;

import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheEntryFactory;

/**
 * An entry used when caching cardinality requests.
 */
public class CardinalityCacheEntryFactory implements CacheEntryFactory<CardinalityCacheEntry, Integer>
{
	@Override
	public CardinalityCacheEntry createCacheEntry( final Integer cardinality ) {
		return new CardinalityCacheEntry( cardinality, Instant.now().toEpochMilli() );
	}
}
