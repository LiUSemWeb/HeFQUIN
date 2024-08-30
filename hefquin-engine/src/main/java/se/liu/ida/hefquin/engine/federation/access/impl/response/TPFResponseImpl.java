package se.liu.ida.hefquin.engine.federation.access.impl.response;

import java.util.Date;
import java.util.List;

import se.liu.ida.hefquin.engine.data.Triple;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.TPFResponse;
import se.liu.ida.hefquin.engine.utils.ConcatenatingIterable;

public class TPFResponseImpl
                    extends DataRetrievalResponseBase
                    implements TPFResponse
{
	protected final List<Triple> matchingTriples;
	protected final List<Triple> metadataTriples;
	protected final Integer cardEstimate;
	protected final String nextPageURL;

	/**
	 * Initializes the retrievalEndTime to the time when this object is created.
	 */
	public TPFResponseImpl( final List<Triple> matchingTriples,
	                        final List<Triple> metadataTriples,
	                        final String nextPageURL,
	                        final FederationMember fm,
	                        final DataRetrievalRequest request,
	                        final Date requestStartTime ) {
		super(fm, request, requestStartTime);

		assert matchingTriples != null;
		assert metadataTriples != null;

		this.matchingTriples = matchingTriples;
		this.metadataTriples = metadataTriples;
		this.nextPageURL = nextPageURL; // may be null
		this.cardEstimate = null;
	}

	public TPFResponseImpl( final List<Triple> matchingTriples,
	                        final List<Triple> metadataTriples,
	                        final String nextPageURL,
	                        final FederationMember fm,
	                        final DataRetrievalRequest request,
	                        final Date requestStartTime,
	                        final Date retrievalEndTime ) {
		super(fm, request, requestStartTime, retrievalEndTime);

		assert matchingTriples != null;
		assert metadataTriples != null;

		this.matchingTriples = matchingTriples;
		this.metadataTriples = metadataTriples;
		this.nextPageURL = nextPageURL; // may be null
		this.cardEstimate = null;
	}

	/**
	 * Initializes the retrievalEndTime to the time when this object is created.
	 */
	public TPFResponseImpl( final List<Triple> matchingTriples,
	                        final List<Triple> metadataTriples,
	                        final String nextPageURL,
	                        final int tripleCount,
	                        final FederationMember fm,
	                        final DataRetrievalRequest request,
	                        final Date requestStartTime ) {
		super(fm, request, requestStartTime);

		assert matchingTriples != null;
		assert metadataTriples != null;
		assert tripleCount >= 0;

		this.matchingTriples = matchingTriples;
		this.metadataTriples = metadataTriples;
		this.nextPageURL = nextPageURL; // may be null
		this.cardEstimate = Integer.valueOf(tripleCount);
	}

	public TPFResponseImpl( final List<Triple> matchingTriples,
	                        final List<Triple> metadataTriples,
	                        final String nextPageURL,
	                        final int tripleCount,
	                        final FederationMember fm,
	                        final DataRetrievalRequest request,
	                        final Date requestStartTime,
	                        final Date retrievalEndTime ) {
		super(fm, request, requestStartTime, retrievalEndTime);

		assert matchingTriples != null;
		assert metadataTriples != null;
		assert tripleCount >= 0;

		this.matchingTriples = matchingTriples;
		this.metadataTriples = metadataTriples;
		this.nextPageURL = nextPageURL; // may be null
		this.cardEstimate = Integer.valueOf(tripleCount);
	}

	@Override
	public Iterable<Triple> getTriples() {
		return new ConcatenatingIterable<Triple>(matchingTriples, metadataTriples);
	}

	@Override
	public int getSize() {
		return matchingTriples.size() + metadataTriples.size();
	}

	@Override
	public Iterable<Triple> getPayload() {
		return matchingTriples;
	}

	@Override
	public int getPayloadSize() {
		return matchingTriples.size();
	}

	@Override
	public Iterable<Triple> getMetadata() {
		return metadataTriples;
	}

	@Override
	public int getMetadataSize() {
		return metadataTriples.size();
	}

	@Override
	public Boolean isLastPage() {
		return ( nextPageURL == null );
	}

	@Override
	public String getNextPageURL() {
		return nextPageURL;
	}

	@Override
	public Integer getCardinalityEstimate() {
		return cardEstimate;
	}

}
