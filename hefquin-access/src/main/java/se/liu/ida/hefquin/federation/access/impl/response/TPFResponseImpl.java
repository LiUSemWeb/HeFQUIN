package se.liu.ida.hefquin.federation.access.impl.response;

import java.util.Date;
import java.util.List;

import se.liu.ida.hefquin.base.data.Triple;
import se.liu.ida.hefquin.base.utils.ConcatenatingIterable;
import se.liu.ida.hefquin.federation.access.TPFResponse;
import se.liu.ida.hefquin.federation.access.UnsupportedOperationDueToRetrievalError;

public class TPFResponseImpl extends DataRetrievalResponseBase<Iterable<Triple>> implements TPFResponse
{
	protected final List<Triple> metadataTriples;
	protected final Integer cardEstimate;
	protected final String nextPageURL;

	/**
	 * Constructs a response with the given matching triples, metadata
	 * triples, next page URL, and request start time. The retrieval
	 * end time is automatically set to the current time at the moment
	 * of construction. This constructor assumes no error occurred.
	 *
	 * @param matchingTriples  the list of triples matching the request pattern (must not be null)
	 * @param metadataTriples  the list of metadata triples (must not be null)
	 * @param nextPageURL      the URL for the next page of results, or {@code null} if there is none
	 * @param requestStartTime the timestamp when the request started
	 */
	public TPFResponseImpl( final List<Triple> matchingTriples,
	                        final List<Triple> metadataTriples,
	                        final String nextPageURL,
	                        final Date requestStartTime ) {
		super(matchingTriples, requestStartTime);

		assert metadataTriples != null;

		this.metadataTriples = metadataTriples;
		this.nextPageURL = nextPageURL; // may be null
		this.cardEstimate = null;
	}

	/**
	 * Constructs a response with the given matching triples, metadata
	 * triples, next page URL, request start time, and retrieval end time.
	 * This constructor assumes no error occurred.
	 *
	 * @param matchingTriples  the list of triples matching the request pattern (must not be null)
	 * @param metadataTriples  the list of metadata triples (must not be null)
	 * @param nextPageURL      the URL for the next page of results, or {@code null} if there is none
	 * @param requestStartTime the timestamp when the request started
	 * @param retrievalEndTime the time at which the retrieval of this response was completed (must not be {@code null})
	 */
	public TPFResponseImpl( final List<Triple> matchingTriples,
	                        final List<Triple> metadataTriples,
	                        final String nextPageURL,
	                        final Date requestStartTime,
	                        final Date retrievalEndTime ) {
		super(matchingTriples, requestStartTime, retrievalEndTime);

		assert metadataTriples != null;

		this.metadataTriples = metadataTriples;
		this.nextPageURL = nextPageURL; // may be null
		this.cardEstimate = null;
	}

	/**
	 * Constructs a response with the given matching triples, metadata
	 * triples, next page URL, triple count, and request start time.
	 * The retrieval end time is automatically set to the current time
	 * at the moment of construction. This constructor assumes no error
	 * occurred.
	 *
	 * @param matchingTriples  the list of triples matching the request pattern (must not be null)
	 * @param metadataTriples  the list of metadata triples (must not be null)
	 * @param nextPageURL      the URL for the next page of results, or {@code null} if there is none
	 * @param tripleCount      the triple count
	 * @param requestStartTime the timestamp when the request started
	 */
	public TPFResponseImpl( final List<Triple> matchingTriples,
	                        final List<Triple> metadataTriples,
	                        final String nextPageURL,
	                        final int tripleCount,
	                        final Date requestStartTime ) {
		super(matchingTriples, requestStartTime);

		assert metadataTriples != null;
		assert tripleCount >= 0;

		this.metadataTriples = metadataTriples;
		this.nextPageURL = nextPageURL; // may be null
		this.cardEstimate = Integer.valueOf( tripleCount );
	}

	/**
	 * Constructs a response with the given matching triples, metadata
	 * triples, next page URL, triple count, request start time, and
	 * retrieval end time. This constructor assumes no error occurred.
	 *
	 * @param matchingTriples  the list of triples matching the request pattern (must not be null)
	 * @param metadataTriples  the list of metadata triples (must not be null)
	 * @param nextPageURL      the URL for the next page of results, or {@code null} if there is none
	 * @param tripleCount      the triple count
	 * @param requestStartTime the timestamp when the request started
	 * @param retrievalEndTime the time at which the retrieval of this response was completed (must not be {@code null})
	 */
	public TPFResponseImpl( final List<Triple> matchingTriples,
	                        final List<Triple> metadataTriples,
	                        final String nextPageURL,
	                        final int tripleCount,
	                        final Date requestStartTime,
	                        final Date retrievalEndTime ) {
		super(matchingTriples, requestStartTime, retrievalEndTime);

		assert metadataTriples != null;
		assert tripleCount >= 0;

		this.metadataTriples = metadataTriples;
		this.nextPageURL = nextPageURL; // may be null
		this.cardEstimate = Integer.valueOf( tripleCount );
	}

	/**
	 * Constructs a response for the case that the response was an
	 * error message. The retrieval end time is automatically set
	 * to the current time at the moment of construction.
	 *
	 * *@param errorStatusCode  the HTTP status code of the error message
	 *                         (must not be {@code null})
	 * @param errorDescription a short description of the error (may be
	 *                         {@code null})
	 * @param requestStartTime the time at which the request was initiated
	 *                         (must not be {@code null})
	 */
	public TPFResponseImpl( final int errorStatusCode,
	                        final String errorDescription,
	                        final Date requestStartTime ) {
		super(errorStatusCode, errorDescription, requestStartTime);

		this.metadataTriples = null;
		this.nextPageURL = null;
		this.cardEstimate = null;
	}

	/**
	 * Constructs a response for the case that the response was an
	 * error message.
	 *
	 * *@param errorStatusCode  the HTTP status code of the error message
	 *                         (must not be {@code null})
	 * @param errorDescription a short description of the error (may be
	 *                         {@code null})
	 * @param requestStartTime the time at which the request was initiated
	 *                         (must not be {@code null})
	 * @param retrievalEndTime the time at which the retrieval of this response
	 *                         was completed (must not be {@code null})
	 */
	public TPFResponseImpl( final int errorStatusCode,
	                        final String errorDescription,
	                        final Date requestStartTime,
	                        final Date retrievalEndTime ) {
		super(errorStatusCode, errorDescription, requestStartTime, retrievalEndTime);

		this.metadataTriples = null;
		this.nextPageURL = null;
		this.cardEstimate = null;
	}

	/**
	 * Constructs a defective response for the case that an exception
	 * was thrown when producing the response. The retrieval end time
	 * is automatically set to the current time at the moment of
	 * construction.
	 *
	 * @param exception  the exception (must not be {@code null})
	 * @param requestStartTime the time at which the request was initiated
	 *                         (must not be {@code null})
	 */
	public TPFResponseImpl( final Exception exception,
	                        final Date requestStartTime ) {
		super(exception, requestStartTime);

		this.metadataTriples = null;
		this.nextPageURL = null;
		this.cardEstimate = null;
	}

	/**
	 * Constructs a defective response for the case that an exception
	 * was thrown when producing the response.
	 *
	 * @param exception  the exception (must not be {@code null})
	 * @param requestStartTime the time at which the request was initiated
	 *                         (must not be {@code null})
	 * @param retrievalEndTime the time at which the creation of this response
	 *                         resulted in an exception (must not be {@code null})
	 */
	public TPFResponseImpl( final Exception exception,
	                        final Date requestStartTime,
	                        final Date retrievalEndTime ) {
		super(exception, requestStartTime, retrievalEndTime);

		this.metadataTriples = null;
		this.nextPageURL = null;
		this.cardEstimate = null;
	}

	@Override
	public Iterable<Triple> getMetadata() throws UnsupportedOperationDueToRetrievalError {
		throwExceptionIfNoResponseData();
		return metadataTriples;
	}

	@Override
	public Boolean isLastPage() throws UnsupportedOperationDueToRetrievalError {
		throwExceptionIfNoResponseData();
		return nextPageURL == null;
	}

	@Override
	public String getNextPageURL() throws UnsupportedOperationDueToRetrievalError {
		throwExceptionIfNoResponseData();
		return nextPageURL;
	}

	@Override
	public Integer getCardinalityEstimate() throws UnsupportedOperationDueToRetrievalError {
		throwExceptionIfNoResponseData();
		return cardEstimate;
	}

	/**
	 * Returns an iterator over all triples contained in the TPF response,
	 * concatenating the payload and metadata into a single iterable.
	 *
	 * @throws UnsupportedOperationDueToRetrievalError if the response to the
	 * corresponding request was an error message (see {@link #isError()}) or
	 * the response is defective (see {@link #isDefective()}).
	 */
	@Override
	public Iterable<Triple> getResponseData() throws UnsupportedOperationDueToRetrievalError {
		throwExceptionIfNoResponseData();
		return new ConcatenatingIterable<Triple>( getPayload(), getMetadata() );
	}

	@Override
	public Iterable<Triple> getPayload() throws UnsupportedOperationDueToRetrievalError {
		return super.getResponseData();
	}
}
