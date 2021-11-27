package se.liu.ida.hefquin.engine.datastructures.impl.cache;

public class CacheEntryBase<ObjectType> implements CacheEntry<ObjectType>
{
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
