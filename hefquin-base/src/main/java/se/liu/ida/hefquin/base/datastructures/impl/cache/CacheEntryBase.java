package se.liu.ida.hefquin.base.datastructures.impl.cache;

import java.io.Serializable;
import java.time.Instant;

public class CacheEntryBase<ObjectType> implements CacheEntry<ObjectType>, Serializable
{
	private static final long serialVersionUID = 1L;
	protected final ObjectType obj;
	protected final long creationTime;

	public CacheEntryBase( final ObjectType obj ) {
		this( obj, Instant.now().toEpochMilli() );
	}

	public CacheEntryBase( final ObjectType obj, final long creationTime ) {
		assert obj != null;
		this.obj = obj;
		this.creationTime = creationTime;
	}

	@Override
	public ObjectType getObject() {
		return obj;
	}

	@Override
	public long createdAt() {
		return creationTime;
	}

	@Override
	public boolean equals( final Object obj ) {
		if ( this == obj )
			return true;
		if ( obj == null || getClass() != obj.getClass() )
			return false;
		return this.getObject().equals(((CacheEntryBase<?>) obj).getObject());
	}

	@Override
	public int hashCode() {
		return getObject().hashCode();
	}
}
