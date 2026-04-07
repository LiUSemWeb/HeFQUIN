package se.liu.ida.hefquin.federation.access.impl.cache.chroniclemap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import net.openhft.chronicle.bytes.BytesMarshallable;
import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.Triple;
import se.liu.ida.hefquin.federation.access.DataRetrievalResponse;
import se.liu.ida.hefquin.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.federation.access.TPFResponse;
import se.liu.ida.hefquin.federation.access.UnsupportedOperationDueToRetrievalError;

/**
 * Cache value stored in ChronicleMap-native {@link BytesMarshallable} form.
 *
 * This object can represent solution mappings, TPF response data, or a cached
 * count result.
 */
public class ChronicleMapCacheObject implements BytesMarshallable
{
	private static final List<Triple> EMPTY_TRIPLES = List.of();
	private static final List<SolutionMapping> EMPTY_SOLUTION_MAPPINGS = List.of();
	private static final String NO_NEXT_PAGE_URL = null;
	private static final int NO_COUNT = -1;

	protected final List<SolutionMapping> solutionMappings;
	protected final List<Triple> matchingTriples;
	protected final List<Triple> metadataTriples;
	protected final String nextPageURL;
	protected final int count;

	public ChronicleMapCacheObject( final Iterable<SolutionMapping> solutionMappings,
	                                final Iterable<Triple> matchingTriples,
	                                final Iterable<Triple> metadataTriples,
	                                final String nextPageURL,
	                                final int count ) {
		assert solutionMappings != null;
		assert matchingTriples != null;
		assert metadataTriples != null;

		this.solutionMappings = new ArrayList<>();
		solutionMappings.forEach(this.solutionMappings::add);

		this.matchingTriples = new ArrayList<>();
		matchingTriples.forEach(this.matchingTriples::add);
		this.metadataTriples = new ArrayList<>();
		metadataTriples.forEach(this.metadataTriples::add);
		this.nextPageURL = nextPageURL;

		this.count = count;
	}

	/**
	 * Creates a cache object containing the given solution mappings.
	 *
	 * @param solutionMappings the solution mappings to cache
	 */
	public ChronicleMapCacheObject( final Iterable<SolutionMapping> solutionMappings ) {
		assert solutionMappings != null;

		this.solutionMappings = new ArrayList<>();
		solutionMappings.forEach(this.solutionMappings::add);

		this.matchingTriples = EMPTY_TRIPLES;
		this.metadataTriples = EMPTY_TRIPLES;
		nextPageURL = NO_NEXT_PAGE_URL;

		count = NO_COUNT;
	}

	/**
	 * Creates a cache object containing TPF response data.
	 *
	 * @param matchingTriples the matching triples
	 * @param metadataTriples the metadata triples
	 * @param nextPageURL     the next-page URL, or an empty string if none exists
	 */
	public ChronicleMapCacheObject( final Iterable<Triple> matchingTriples,
	                                final Iterable<Triple> metadataTriples,
	                                final String nextPageURL ) {
		assert matchingTriples != null;
		assert metadataTriples != null;

		this.solutionMappings = EMPTY_SOLUTION_MAPPINGS;

		this.matchingTriples = new ArrayList<>();
		matchingTriples.forEach(this.matchingTriples::add);
		this.metadataTriples = new ArrayList<>();
		metadataTriples.forEach(this.metadataTriples::add);
		this.nextPageURL = nextPageURL;

		count = NO_COUNT;
	}

	/**
	 * Creates a cache object containing only a cached count.
	 *
	 * @param count the cached result count
	 */
	public ChronicleMapCacheObject( final int count ) {
		this.solutionMappings = EMPTY_SOLUTION_MAPPINGS;

		matchingTriples = EMPTY_TRIPLES;
		metadataTriples = EMPTY_TRIPLES;
		this.nextPageURL = NO_NEXT_PAGE_URL;

		this.count = count;
	}

	/**
	 * Creates a cache object from the given retrieval response.
	 *
	 * Supported response types are {@link TPFResponse} and {@link SolMapsResponse}.
	 *
	 * @param response the response to convert
	 * @return the corresponding cache object
	 * @throws IllegalStateException if the response type is unsupported
	 * @throws UnsupportedOperationDueToRetrievalError if extracting response data fails
	 */
	public static ChronicleMapCacheObject create( final DataRetrievalResponse<?> response )
			throws UnsupportedOperationDueToRetrievalError, IllegalStateException
	{
		if( response instanceof TPFResponse tpfResponse ) {
			final Iterable<Triple> matchingTriples = tpfResponse.getPayload();
			final Iterable<Triple> metadataTriples = tpfResponse.getMetadata();
			final String nextPageURL = tpfResponse.getNextPageURL();
			return new ChronicleMapCacheObject( matchingTriples, metadataTriples, nextPageURL );
		}
		else if( response instanceof SolMapsResponse solMapResponse )
			return new ChronicleMapCacheObject( solMapResponse.getResponseData() );
		else
			throw new IllegalStateException( "Unsupported response type: " + response.getClass().getName() );
	}

	/**
	 * Returns the cached solution mappings.
	 * 
	 * @return a new list containing the cached solution mappings
	 */
	public List<SolutionMapping> getSolutionMappings() {
		return Collections.unmodifiableList(solutionMappings);
	}

	/**
	 * Returns the cached matching triples.
	 *
	 * @return a new list containing the cached matching triples
	 */
	public List<Triple> getMatchingTriples() {
		return Collections.unmodifiableList(matchingTriples);
	}

	/**
	 * Returns the cached metadata triples.
	 *
	 * @return a new list containing the cached metadata triples
	 */
	public List<Triple> getMetadataTriples() {
		return Collections.unmodifiableList(metadataTriples);
	}

	/**
	 * Returns the cached next-page URL.
	 *
	 * @return the next-page URL, or an empty string if none exists
	 */
	public String getNextPageURL() {
		return nextPageURL;
	}

	/**
	 * Returns the cached count of results for this request.
	 *
	 * @return the cached count, or {@code -1} if this object does not represent a
	 *         count
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
		return    solutionMappings.equals( other.solutionMappings )
		       && matchingTriples.equals( other.matchingTriples )
		       && metadataTriples.equals( other.metadataTriples )
		       && Objects.equals(nextPageURL, other.nextPageURL)
		       && count == other.count;
	}

	@Override
	public int hashCode() {
		return Objects.hash(matchingTriples, metadataTriples, nextPageURL, solutionMappings, count);
	}

	@Override
	public String toString() {
		return "ChronicleMapCacheObject{" +
		       "solutionMappings=" + solutionMappings.size() + ", " +
		       "matchingTriples=" + matchingTriples.size() + ", " +
		       "metadataTriples=" + metadataTriples.size() + ", " +
		       "nextPageURL=" + nextPageURL + ", " +
		       "count=" + count + "}";
	}
}
