package se.liu.ida.hefquin.federation.access.impl.cache.chroniclemap;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.hash.serialization.BytesReader;
import net.openhft.chronicle.hash.serialization.BytesWriter;

/**
 * Serializer/deserializer for {@link ChronicleMapCacheEntry}.
 *
 * <p>
 * Writes the entry's creation timestamp followed by its
 * {@link ChronicleMapCacheObject}. Reading reconstructs a new entry instance.
 * </p>
 */
public class ChronicleMapCacheEntryMarshaller
		implements BytesWriter<ChronicleMapCacheEntry>, BytesReader<ChronicleMapCacheEntry>
{
	protected final static ChronicleMapCacheEntryMarshaller INSTANCE = new ChronicleMapCacheEntryMarshaller();

	@Override
	public void write( final Bytes<?> out, final ChronicleMapCacheEntry entry ) {
		out.writeLong( entry.createdAt() );
		ChronicleMapCacheObjectMarshaller.INSTANCE.write( out, entry.getObject() );
	}

	@Override
	public ChronicleMapCacheEntry read( final Bytes<?> in, final ChronicleMapCacheEntry using ) {
		final long creationTime = in.readLong();
		final ChronicleMapCacheObject object = ChronicleMapCacheObjectMarshaller.INSTANCE.read(in, null);
		return new ChronicleMapCacheEntry(object, creationTime);
	}
}
