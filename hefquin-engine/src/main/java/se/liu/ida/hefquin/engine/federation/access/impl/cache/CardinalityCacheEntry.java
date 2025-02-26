package se.liu.ida.hefquin.engine.federation.access.impl;

import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheEntryBase;

/**
 * An entry used when caching cardinality requests.
 */
public class CardinalityCacheEntry extends CacheEntryBase<Integer> {
	private static final long serialVersionUID = 1L;
	private final long entryCreatedAt;
	
	public CardinalityCacheEntry( final Integer cardinality, final long entryCreatedAt ){
		super( cardinality );
        this.entryCreatedAt = entryCreatedAt;
	}

	@Override
	public boolean equals( Object obj ) {
		if ( this == obj )
			return true;
		if ( obj == null || getClass() != obj.getClass() )
			return false;
		return this.getObject() == ((CardinalityCacheEntry) obj).getObject();
	}

	@Override
	public int hashCode() {
		return getObject();
	}

	@Override
	public String toString() {
		return "CardinalityCacheEntry{cardinality='" + getObject() + "'}";
	}

	public long getCacheEntryCreatedAt(){
		return entryCreatedAt;
	}
}
