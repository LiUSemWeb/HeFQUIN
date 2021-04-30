package se.liu.ida.hefquin.federation.access.impl.response;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.atlas.iterator.Iter;

import se.liu.ida.hefquin.data.Triple;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.federation.access.TPFResponse;

public class TPFResponseImpl
                    extends DataRetrievalResponseBase
                    implements TPFResponse
{
	protected final List<Triple> matchingTriples;
	protected final List<Triple> metadataTriples;

	/**
	 * Initializes the retrievalEndTime to the time when this object is created.
	 */
	public TPFResponseImpl( final List<Triple> matchingTriples,
	                        final List<Triple> metadataTriples,
	                        final FederationMember fm,
	                        final DataRetrievalRequest request,
	                        final Date requestStartTime ) {
		super(fm, request, requestStartTime);

		assert matchingTriples != null;
		assert metadataTriples != null;

		this.matchingTriples = matchingTriples;
		this.metadataTriples = metadataTriples;
	}

	public TPFResponseImpl( final List<Triple> matchingTriples,
	                        final List<Triple> metadataTriples,
	                        final FederationMember fm,
	                        final DataRetrievalRequest request,
	                        final Date requestStartTime,
	                        final Date retrievalEndTime ) {
		super(fm, request, requestStartTime, retrievalEndTime);

		assert matchingTriples != null;
		assert metadataTriples != null;

		this.matchingTriples = matchingTriples;
		this.metadataTriples = metadataTriples;
	}

	@Override
	public Iterator<Triple> getIterator() {
		return Iter.concat( matchingTriples.iterator(), metadataTriples.iterator() );
	}

	@Override
	public int getSize() {
		return matchingTriples.size() + metadataTriples.size();
	}

	@Override
	public Iterator<Triple> getPayloadIterator() {
		return matchingTriples.iterator();
	}

	@Override
	public int getPayloadSize() {
		return matchingTriples.size();
	}

	@Override
	public Iterator<Triple> getMetadataIterator() {
		return metadataTriples.iterator();
	}

	@Override
	public int getMetadataSize() {
		return metadataTriples.size();
	}

	@Override
	public Boolean isLastPage() {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public Integer getCardinalityEstimate() {
		throw new UnsupportedOperationException("TODO");
	}

}
