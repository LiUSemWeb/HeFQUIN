package se.liu.ida.hefquin.federation.access.impl.cache.mapdb;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.Serializer;

import se.liu.ida.hefquin.federation.access.impl.cache.CacheEntryCodec;
import se.liu.ida.hefquin.federation.access.impl.cache.PersistentCacheEntry;

public class MapDBCacheEntrySerializer implements Serializer<PersistentCacheEntry>
{
	public static MapDBCacheEntrySerializer INSTANCE = new MapDBCacheEntrySerializer();

	@Override
	public void serialize( final DataOutput2 out, final PersistentCacheEntry value ) throws IOException {
		CacheEntryCodec.write(out, value);
	}
	
	@Override
	public PersistentCacheEntry deserialize( final DataInput2 in, final int available ) throws IOException {
		final byte[] data = new byte[available];
		in.readFully( data );

		try ( DataInputStream din = new DataInputStream( new ByteArrayInputStream(data) ) ) {
			return CacheEntryCodec.read( din );
		}
	}
}