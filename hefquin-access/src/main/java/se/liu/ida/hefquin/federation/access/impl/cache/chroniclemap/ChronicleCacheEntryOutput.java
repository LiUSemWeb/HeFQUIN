package se.liu.ida.hefquin.federation.access.impl.cache.chroniclemap;

import java.io.DataOutput;
import java.io.IOException;

import net.openhft.chronicle.bytes.Bytes;

public class ChronicleCacheEntryOutput implements DataOutput {
	protected final Bytes<?> bytes;

	public ChronicleCacheEntryOutput( final Bytes<?> bytes ) {
		this.bytes = bytes;
	}

	@Override
	public void write( final byte[] b ) throws IOException {
		bytes.write(b);
	}

	// Unsupported operations

	@Override
	public void write( int b ) throws IOException {
		throw new UnsupportedOperationException( "Unimplemented method 'write'" );
	}

	@Override
	public void write( byte[] b, int off, int len ) throws IOException {
		throw new UnsupportedOperationException( "Unimplemented method 'write'" );
	}

	@Override
	public void writeBoolean( boolean v ) throws IOException {
		throw new UnsupportedOperationException( "Unimplemented method 'writeBoolean'" );
	}

	@Override
	public void writeByte( int v ) throws IOException {
		throw new UnsupportedOperationException( "Unimplemented method 'writeByte'" );
	}

	@Override
	public void writeShort( int v ) throws IOException {
		throw new UnsupportedOperationException( "Unimplemented method 'writeShort'" );
	}

	@Override
	public void writeChar( int v ) throws IOException {
		throw new UnsupportedOperationException( "Unimplemented method 'writeChar'" );
	}

	@Override
	public void writeInt( int v ) throws IOException {
		throw new UnsupportedOperationException( "Unimplemented method 'writeInt'" );
	}

	@Override
	public void writeLong( long v ) throws IOException {
		throw new UnsupportedOperationException( "Unimplemented method 'writeLong'" );
	}

	@Override
	public void writeFloat( float v ) throws IOException {
		throw new UnsupportedOperationException( "Unimplemented method 'writeFloat'" );
	}

	@Override
	public void writeDouble( double v ) throws IOException {
		throw new UnsupportedOperationException( "Unimplemented method 'writeDouble'" );
	}

	@Override
	public void writeBytes( String s ) throws IOException {
		throw new UnsupportedOperationException( "Unimplemented method 'writeBytes'" );
	}

	@Override
	public void writeChars( String s ) throws IOException {
		throw new UnsupportedOperationException( "Unimplemented method 'writeChars'" );
	}

	@Override
	public void writeUTF( String s ) throws IOException {
		throw new UnsupportedOperationException( "Unimplemented method 'writeUTF'" );
	}
}