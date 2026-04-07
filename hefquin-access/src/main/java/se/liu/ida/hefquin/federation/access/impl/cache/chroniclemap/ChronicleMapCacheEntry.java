package se.liu.ida.hefquin.federation.access.impl.cache.chroniclemap;

import net.openhft.chronicle.bytes.BytesMarshallable;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheEntry;

/**
 * ChronicleMap-native ({@link BytesMarshallable}) cache entry storing a cached
 * object together with its creation timestamp.
 */
public class ChronicleMapCacheEntry implements CacheEntry<ChronicleMapCacheObject>
{
	protected final ChronicleMapCacheObject object;
	protected final long creationTime;


	public ChronicleMapCacheEntry( final ChronicleMapCacheObject object, final long creationTime ) {
		assert object != null;

		this.object = object;
		this.creationTime = creationTime;
	}

	@Override
	public String toString() {
		return "ChronicleMapCacheEntry{object=" + object + ", creationTime=" + creationTime + "}";
	}

	@Override
	public ChronicleMapCacheObject getObject() {
		return object;
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

		final ChronicleMapCacheEntry other = (ChronicleMapCacheEntry) obj;
		return object.equals(other.object);
	}

	@Override
	public int hashCode() {
		return object.hashCode();
	}
}
