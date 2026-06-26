package se.liu.ida.hefquin.federation.access.impl.cache.chroniclemap;

import java.io.IOException;
import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.hash.serialization.BytesReader;
import net.openhft.chronicle.hash.serialization.BytesWriter;
import se.liu.ida.hefquin.federation.access.DataRetrievalResponse;
import se.liu.ida.hefquin.federation.access.impl.cache.CacheEntryCodec;
import se.liu.ida.hefquin.federation.access.impl.cache.PersistentCacheEntry;

/**
 * Serializer/deserializer for {@link PersistentCacheEntry}.
 *
 * <p>
 * Writes the entry's creation timestamp followed by a serialized form of the
 * cached {@link DataRetrievalResponse}, including a type discriminator. Reading
 * reconstructs a new entry instance.
 * </p>
 */
public class ChronicleMapCacheEntryMarshaller
		implements BytesWriter<PersistentCacheEntry>, BytesReader<PersistentCacheEntry>
{
	public final static ChronicleMapCacheEntryMarshaller INSTANCE = new ChronicleMapCacheEntryMarshaller();

	@Override
	public void write( final Bytes<?> out, final PersistentCacheEntry entry ) {
		try {
			CacheEntryCodec.write( new ChronicleCacheEntryOutput(out), entry );
		} catch ( final IOException e ) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public PersistentCacheEntry read( final Bytes<?> in, final PersistentCacheEntry using ) {
		try {
			return CacheEntryCodec.read( new ChronicleCacheEntryInput(in) );
		} catch ( final IOException e ) {
			throw new IllegalStateException(e);
		}
	}
}
