package se.liu.ida.hefquin.engine.datastructures.impl.cache;

/**
 * An implementation of {@link CacheEntryFactory} for {@link CacheEntryBase} objects.
 */
public class CacheEntryBaseFactory<ObjectType> implements CacheEntryFactory<CacheEntryBase<ObjectType>, ObjectType>
{
	@Override
	public CacheEntryBase<ObjectType> createCacheEntry( final ObjectType obj ) {
		return new CacheEntryBase<>(obj);
	}

}
