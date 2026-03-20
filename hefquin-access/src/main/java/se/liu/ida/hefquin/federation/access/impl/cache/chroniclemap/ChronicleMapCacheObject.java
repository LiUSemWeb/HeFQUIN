package se.liu.ida.hefquin.federation.access.impl.cache.chroniclemap;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.openhft.chronicle.bytes.BytesIn;
import net.openhft.chronicle.bytes.BytesMarshallable;
import net.openhft.chronicle.bytes.BytesOut;
import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.Triple;
import se.liu.ida.hefquin.federation.access.DataRetrievalResponse;
import se.liu.ida.hefquin.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.federation.access.TPFResponse;
import se.liu.ida.hefquin.federation.access.UnsupportedOperationDueToRetrievalError;

/**
 * Object to stores solutions in a ChronicleMap-native
 * ({@link BytesMarshallable}) form.
 */
public class ChronicleMapCacheObject implements BytesMarshallable
{
	private List<MarshallableSolutionMapping> solutionMappings = new ArrayList<>();
	private List<MarshallableTriple> matchingTriples = new ArrayList<>();
	private List<MarshallableTriple> metadataTriples = new ArrayList<>();
	private String nextPageURL = "";
	private Integer count = -1;

	public ChronicleMapCacheObject() {}

	public ChronicleMapCacheObject( final Iterable<SolutionMapping> solutionMappings ) {
		assert solutionMappings != null;

		for ( final SolutionMapping sm : solutionMappings ) {
			this.solutionMappings.add( new MarshallableSolutionMapping(sm) );
		}
	}

	public ChronicleMapCacheObject( final Iterable<Triple> matchingTriples,
	                                final Iterable<Triple> metadataTriples,
	                                final String nextPageURL ) {
		assert matchingTriples != null;
		assert metadataTriples != null;

		for ( final Triple t : matchingTriples ) {
			this.matchingTriples.add( new MarshallableTriple(t) );
		}

		for ( final Triple t : metadataTriples ) {
			this.metadataTriples.add( new MarshallableTriple(t) );
		}

		this.nextPageURL = nextPageURL;
	}

	public ChronicleMapCacheObject( final int count ) {
		this.count = count;
	}

	public static ChronicleMapCacheObject create( final DataRetrievalResponse<?> response )
			throws UnsupportedOperationDueToRetrievalError
	{
		if( response instanceof TPFResponse tpfResponse ) {
			final Iterable<Triple> matchingTriples = tpfResponse.getPayload();
			final Iterable<Triple> metadataTriples = tpfResponse.getMetadata();
			final String nextPageURL = tpfResponse.getNextPageURL();
			return new ChronicleMapCacheObject( matchingTriples, metadataTriples, nextPageURL );
		}
		else if( response instanceof SolMapsResponse solMapResponse ) {
			return new ChronicleMapCacheObject( solMapResponse.getResponseData() );
		}
		return null;
	}

	@Override
	public void writeMarshallable( final BytesOut<?> out ) {
		// Matching triples
		out.writeInt( matchingTriples.size() );
		for ( final MarshallableTriple t : matchingTriples ) {
			t.writeMarshallable(out);
		}
		// Metadata triples
		out.writeInt( metadataTriples.size() );
		for ( final MarshallableTriple t : metadataTriples ) {
			t.writeMarshallable(out);
		}
		// Next page URL
		out.writeUtf8(nextPageURL);
		// Solution mappings
		out.writeInt( solutionMappings.size() );
		for ( final MarshallableSolutionMapping sm : solutionMappings ) {
			sm.writeMarshallable(out);
		}
		// Count
		out.writeInt(count);
	}

	@Override
	public void readMarshallable( final BytesIn<?> in ) {
		// Matching triples
		final int matchingTriplesSize = in.readInt();
		for ( int i = 0; i < matchingTriplesSize; i++ ) {
			final MarshallableTriple t = new MarshallableTriple();
			t.readMarshallable(in);
			matchingTriples.add(t);
		}
		// Metadata triples
		final int metadataTriplesSize = in.readInt();
		for ( int i = 0; i < metadataTriplesSize; i++ ) {
			final MarshallableTriple t = new MarshallableTriple();
			t.readMarshallable(in);
			metadataTriples.add(t);
		}
		// Next page URL
		nextPageURL = in.readUtf8();
		// Solution mappings
		final int solutionMappingsSize = in.readInt();
		solutionMappings = new ArrayList<>( solutionMappingsSize );
		for ( int i = 0; i < solutionMappingsSize; i++ ) {
			final MarshallableSolutionMapping sm = new MarshallableSolutionMapping();
			sm.readMarshallable(in);
			solutionMappings.add(sm);
		}
		// Count
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

	public List<Triple> getMatchingTriples() {
		return new ArrayList<>(matchingTriples);
	}

	public List<Triple> getMetadataTriples() {
		return new ArrayList<>(metadataTriples);
	}

	public String getNextPageURL() {
		return nextPageURL;
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
	public boolean equals( Object obj ) {
		if ( this == obj )
			return true;
		if ( obj == null || getClass() != obj.getClass() )
			return false;
		final ChronicleMapCacheObject other = (ChronicleMapCacheObject) obj;
		return    matchingTriples.equals( other.matchingTriples )
		       && metadataTriples.equals( other.metadataTriples )
		       && nextPageURL.equals( other.nextPageURL )
		       && solutionMappings.equals( other.solutionMappings )
		       && count.equals( other.count );
	}

	@Override
	public int hashCode() {
		return Objects.hash(matchingTriples, metadataTriples, solutionMappings, count);
	}

	@Override
	public String toString() {
		return "ChronicleMapCacheObject{" +
		       "matchingTriples=" + matchingTriples.size() + ", " +
		       "metadataTriples=" + metadataTriples.size() + ", " +
		       "nextPageURL=" + nextPageURL + ", " +
		       "solutionMappings=" + solutionMappings.size() + ", " +
			   "count=" + count + "}";
	}
}
