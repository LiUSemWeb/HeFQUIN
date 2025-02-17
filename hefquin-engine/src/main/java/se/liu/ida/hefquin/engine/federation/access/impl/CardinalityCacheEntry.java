package se.liu.ida.hefquin.engine.federation.access.impl;

import java.time.Instant;

import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheEntryBase;

/**
 * An entry used when caching cardinality requests.
 */
public class CardinalityCacheEntry extends CacheEntryBase<Integer> {
	private static final long serialVersionUID = 1L;
	private final long expirationTime;
	private final long defaultTTL = 5 * 60 * 1000; // ms

	public CardinalityCacheEntry( final Integer cardinality ){
		this( cardinality, null );
	}
	
	public CardinalityCacheEntry( final Integer cardinality, final Long expirationTime ){
		super( cardinality );
        this.expirationTime = (expirationTime != null) ? expirationTime : Instant.now().getEpochSecond() + defaultTTL;
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


	public boolean isValid(){
		return expirationTime > Instant.now().toEpochMilli();
	}
}
