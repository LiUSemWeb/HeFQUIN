package se.liu.ida.hefquin.federation.access.impl.cache.chroniclemap;

import java.util.ArrayList;
import java.util.List;

import net.openhft.chronicle.bytes.BytesIn;
import net.openhft.chronicle.bytes.BytesMarshallable;
import net.openhft.chronicle.bytes.BytesOut;
import se.liu.ida.hefquin.base.data.SolutionMapping;

/**
 * Object to stores solutions in a ChronicleMap-native
 * ({@link BytesMarshallable}) form.
 *
 * TODO: Implement support for TPF/brTPF requests
 */
public class ChronicleMapCacheObject implements BytesMarshallable
{
	private List<MarshallableSolutionMapping> solutionMappings = new ArrayList<>();
	private Integer count = -1;

	public ChronicleMapCacheObject() {}

	public ChronicleMapCacheObject( final Iterable<SolutionMapping> solutionMappings ) {
		if ( solutionMappings != null ) {
			for ( final SolutionMapping sm : solutionMappings ) {
				this.solutionMappings.add( new MarshallableSolutionMapping(sm) );
			}
		}
	}

	public ChronicleMapCacheObject( final int count ) {
		this.count = count;
	}

	@Override
	public void writeMarshallable( final BytesOut<?> out ) {
		out.writeInt( solutionMappings.size() );
		for ( final MarshallableSolutionMapping sm : solutionMappings ) {
			sm.writeMarshallable(out);
		}
		out.writeInt(count);
	}

	@Override
	public void readMarshallable( final BytesIn<?> in ) {
		final int solutionMappingsSize = in.readInt();
		solutionMappings = new ArrayList<>( solutionMappingsSize );
		for ( int i = 0; i < solutionMappingsSize; i++ ) {
			final MarshallableSolutionMapping sm = new MarshallableSolutionMapping();
			sm.readMarshallable(in);
			solutionMappings.add(sm);
		}
		count = in.readInt();
	}

	/**
	 * Returns the cached solution mappings as interface-typed objects. The returned
	 * list is a shallow copy and the contained mapping instances are shared.
	 * 
	 * @return a new list containing the cached solution mappings
	 */
	public List<SolutionMapping> getSolutionMappings() {
		return new ArrayList<>(solutionMappings);
	}

	/**
	 * Returns the cached count of results for this request.
	 *
	 * @return the number of matching results
	 */
	public int getCount() {
		return count;
	}

	@Override
	public String toString() {
		return "ChronicleMapCacheObject{solutionMappings=" + solutionMappings.size() + ", count=" + count + "}";
	}
}
