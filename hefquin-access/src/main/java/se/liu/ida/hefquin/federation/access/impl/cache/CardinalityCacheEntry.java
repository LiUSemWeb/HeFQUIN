package se.liu.ida.hefquin.federation.access.impl.cache;

import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheEntryBase;

/**
 * An entry used when caching cardinality requests.
 */
public class CardinalityCacheEntry extends CacheEntryBase<Integer>
{
	private static final long serialVersionUID = 1L;

	public CardinalityCacheEntry( final Integer cardinality, final long entryCreatedAt ) {
		super( cardinality, entryCreatedAt );
	}

	@Override
	public String toString() {
		return "CardinalityCacheEntry{cardinality='" + getObject() + "'}";
	}
}
