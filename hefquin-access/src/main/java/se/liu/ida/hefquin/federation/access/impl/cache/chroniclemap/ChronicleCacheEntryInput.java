package se.liu.ida.hefquin.federation.access.impl.cache.chroniclemap;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;

import net.openhft.chronicle.bytes.Bytes;

public class ChronicleCacheEntryInput implements DataInput
{
	private final DataInputStream in;

	public ChronicleCacheEntryInput( final Bytes<?> bytes ) {
		final byte[] data = new byte[(int) bytes.readRemaining()];
		bytes.read( data );
		in = new DataInputStream( new ByteArrayInputStream(data) );
	}

	@Override
	public long readLong() throws IOException {
		return in.readLong();
	}

	@Override
	public int readInt() throws IOException {
		return in.readInt();
	}

	@Override
	public String readUTF() throws IOException {
		return in.readUTF();
	}

	@Override
	public boolean readBoolean() throws IOException {
		return in.readBoolean();
	}

	// Unsupported operations

	@Override
	public void readFully( byte[] b ) throws IOException {
		throw new UnsupportedOperationException( "Unimplemented method 'readFully'" );
	}

	@Override
	public void readFully( byte[] b, int off, int len ) throws IOException {
		throw new UnsupportedOperationException( "Unimplemented method 'readFully'" );
	}

	@Override
	public int skipBytes( int n ) throws IOException {
		throw new UnsupportedOperationException( "Unimplemented method 'skipBytes'" );
	}

	@Override
	public byte readByte() throws IOException {
		throw new UnsupportedOperationException( "Unimplemented method 'readByte'" );
	}

	@Override
	public int readUnsignedByte() throws IOException {
		throw new UnsupportedOperationException( "Unimplemented method 'readUnsignedByte'" );
	}

	@Override
	public short readShort() throws IOException {
		throw new UnsupportedOperationException( "Unimplemented method 'readShort'" );
	}

	@Override
	public int readUnsignedShort() throws IOException {
		throw new UnsupportedOperationException( "Unimplemented method 'readUnsignedShort'" );
	}

	@Override
	public char readChar() throws IOException {
		throw new UnsupportedOperationException( "Unimplemented method 'readChar'" );
	}

	@Override
	public float readFloat() throws IOException {
		throw new UnsupportedOperationException( "Unimplemented method 'readFloat'" );
	}

	@Override
	public double readDouble() throws IOException {
		throw new UnsupportedOperationException( "Unimplemented method 'readDouble'" );
	}

	@Override
	public String readLine() throws IOException {
		throw new UnsupportedOperationException( "Unimplemented method 'readLine'" );
	}
}