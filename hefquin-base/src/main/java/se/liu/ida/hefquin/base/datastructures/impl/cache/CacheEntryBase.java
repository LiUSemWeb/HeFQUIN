package se.liu.ida.hefquin.base.datastructures.impl.cache;

import java.io.Serializable;

public class CacheEntryBase<ObjectType> implements CacheEntry<ObjectType>, Serializable
{
	private static final long serialVersionUID = 1L;
	protected final ObjectType obj;

	public CacheEntryBase( final ObjectType obj ) {
		assert obj != null;
		this.obj = obj;
	}

	@Override
	public ObjectType getObject() {
		return obj;
	}

}
