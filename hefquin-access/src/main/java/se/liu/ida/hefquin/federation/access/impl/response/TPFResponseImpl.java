package se.liu.ida.hefquin.federation.access.impl.response;

import java.util.Date;
import java.util.List;

import se.liu.ida.hefquin.base.data.Triple;
import se.liu.ida.hefquin.base.utils.ConcatenatingIterable;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.federation.access.TPFResponse;
import se.liu.ida.hefquin.federation.access.UnsupportedOperationDueToRetrievalError;

public class TPFResponseImpl extends DataRetrievalResponseBase<Iterable<Triple>> implements TPFResponse
{
	protected final List<Triple> metadataTriples;
	protected final Integer cardEstimate;
	protected final String nextPageURL;

	/**
	 * Constructs a response with the given matching triples, metadata triples, next page URL, federation member,
	 * request, and request start time. The retrieval end time is automatically set to the current time at the moment of
	 * construction. This constructor assumes no error occurred.
	 *
	 * @param matchingTriples  the list of triples matching the request pattern (must not be null)
	 * @param metadataTriples  the list of metadata triples (must not be null)
	 * @param nextPageURL      the URL for the next page of results, or {@code null} if there is none
	 * @param fm               the federation member from which the data was retrieved
	 * @param request          the original data retrieval request
	 * @param requestStartTime the timestamp when the request started
	 */
	public TPFResponseImpl( final List<Triple> matchingTriples,
	                        final List<Triple> metadataTriples,
	                        final String nextPageURL,
	                        final FederationMember fm,
	                        final DataRetrievalRequest request,
	                        final Date requestStartTime ) {
		super( matchingTriples, fm, request, requestStartTime );

		assert metadataTriples != null;

		this.metadataTriples = metadataTriples;
		this.nextPageURL = nextPageURL; // may be null
		this.cardEstimate = null;
	}

	/**
	 * Constructs a response with the given matching triples, metadata triples, next page URL, federation member,
	 * request, request start time, and retrieval end time. This constructor assumes no error occurred.
	 *
	 * @param matchingTriples  the list of triples matching the request pattern (must not be null)
	 * @param metadataTriples  the list of metadata triples (must not be null)
	 * @param nextPageURL      the URL for the next page of results, or {@code null} if there is none
	 * @param fm               the federation member from which the data was retrieved
	 * @param request          the original data retrieval request
	 * @param requestStartTime the timestamp when the request started
	 * @param retrievalEndTime the time at which the retrieval of this response was completed (must not be {@code null})
	 */
	public TPFResponseImpl( final List<Triple> matchingTriples,
	                        final List<Triple> metadataTriples,
	                        final String nextPageURL,
	                        final FederationMember fm,
	                        final DataRetrievalRequest request,
	                        final Date requestStartTime,
	                        final Date retrievalEndTime ) {
		super( matchingTriples, fm, request, requestStartTime, retrievalEndTime );

		assert metadataTriples != null;

		this.metadataTriples = metadataTriples;
		this.nextPageURL = nextPageURL; // may be null
		this.cardEstimate = null;
	}

	/**
	 * Constructs a response with the given matching triples, metadata triples, next page URL, triple count, federation
	 * member, request, and request start time. The retrieval end time is automatically set to the current time at the moment of
	 * construction. This constructor assumes no error occurred.
	 *
	 * @param matchingTriples  the list of triples matching the request pattern (must not be null)
	 * @param metadataTriples  the list of metadata triples (must not be null)
	 * @param nextPageURL      the URL for the next page of results, or {@code null} if there is none
	 * @param tripleCount      the triple count
	 * @param fm               the federation member from which the data was retrieved
	 * @param request          the original data retrieval request
	 * @param requestStartTime the timestamp when the request started
	 */
	public TPFResponseImpl( final List<Triple> matchingTriples,
	                        final List<Triple> metadataTriples,
	                        final String nextPageURL,
	                        final int tripleCount,
	                        final FederationMember fm,
	                        final DataRetrievalRequest request,
	                        final Date requestStartTime ) {
		super( matchingTriples, fm, request, requestStartTime );

		assert metadataTriples != null;
		assert tripleCount >= 0;

		this.metadataTriples = metadataTriples;
		this.nextPageURL = nextPageURL; // may be null
		this.cardEstimate = Integer.valueOf( tripleCount );
	}

	/**
	 * Constructs a response with the given matching triples, metadata triples, next page URL, triple count, federation
	 * member, request, request start time, and retrieval end time. This constructor assumes no error occurred.
	 *
	 * @param matchingTriples  the list of triples matching the request pattern (must not be null)
	 * @param metadataTriples  the list of metadata triples (must not be null)
	 * @param nextPageURL      the URL for the next page of results, or {@code null} if there is none
	 * @param tripleCount      the triple count
	 * @param fm               the federation member from which the data was retrieved
	 * @param request          the original data retrieval request
	 * @param requestStartTime the timestamp when the request started
	 * @param retrievalEndTime the time at which the retrieval of this response was completed (must not be {@code null})
	 */
	public TPFResponseImpl( final List<Triple> matchingTriples,
	                        final List<Triple> metadataTriples,
	                        final String nextPageURL,
	                        final int tripleCount,
	                        final FederationMember fm,
	                        final DataRetrievalRequest request,
	                        final Date requestStartTime,
	                        final Date retrievalEndTime ) {
		super( matchingTriples, fm, request, requestStartTime, retrievalEndTime );

		assert metadataTriples != null;
		assert tripleCount >= 0;

		this.metadataTriples = metadataTriples;
		this.nextPageURL = nextPageURL; // may be null
		this.cardEstimate = Integer.valueOf( tripleCount );
	}

	/**
	 * Constructs a response with the given matching triples, metadata triples, next page URL, federation member,
	 * request, request start time, and error details. The retrieval end time is automatically set to the current time
	 * at the moment of construction.
	 *
	 * @param matchingTriples  the list of triples matching the request pattern (must not be null)
	 * @param metadataTriples  the list of metadata triples (must not be null)
	 * @param nextPageURL      the URL for the next page of results, or {@code null} if there is none
	 * @param fm               the federation member from which the data was retrieved
	 * @param request          the original data retrieval request
	 * @param requestStartTime the timestamp when the request started
	 * @param errorStatusCode  the HTTP status code representing an error, or {@code null} if no error occurred
	 * @param errorDescription a short description of the error, or {@code null} if no error occurred
	 */
	public TPFResponseImpl( final List<Triple> matchingTriples,
	                        final List<Triple> metadataTriples,
	                        final String nextPageURL,
	                        final FederationMember fm,
	                        final DataRetrievalRequest request,
	                        final Date requestStartTime,
	                        final int errorStatusCode,
	                        final String errorDescription ) {
		super( matchingTriples, fm, request, requestStartTime, errorStatusCode, errorDescription );

		assert metadataTriples != null;

		this.metadataTriples = metadataTriples;
		this.nextPageURL = nextPageURL; // may be null
		this.cardEstimate = null;
	}

	/**
	 * Constructs a response with the given matching triples, metadata triples, next page URL, federation member,
	 * request, request start time, retrieval end time, and error details.
	 *
	 * @param matchingTriples  the list of triples matching the request pattern (must not be null)
	 * @param metadataTriples  the list of metadata triples (must not be null)
	 * @param nextPageURL      the URL for the next page of results, or {@code null} if there is none
	 * @param fm               the federation member from which the data was retrieved
	 * @param request          the original data retrieval request
	 * @param requestStartTime the timestamp when the request started
	 * @param retrievalEndTime the time at which the retrieval of this response was completed (must not be {@code null})
	 * @param errorStatusCode  the HTTP status code representing an error, or {@code null} if no error occurred
	 * @param errorDescription a short description of the error, or {@code null} if no error occurred
	 */
	public TPFResponseImpl( final List<Triple> matchingTriples,
	                        final List<Triple> metadataTriples,
	                        final String nextPageURL,
	                        final FederationMember fm,
	                        final DataRetrievalRequest request,
	                        final Date requestStartTime,
	                        final Date retrievalEndTime,
	                        final Integer errorStatusCode,
	                        final String errorDescription ) {
		super( matchingTriples, fm, request, requestStartTime, retrievalEndTime, errorStatusCode, errorDescription );

		assert metadataTriples != null;

		this.metadataTriples = metadataTriples;
		this.nextPageURL = nextPageURL; // may be null
		this.cardEstimate = null;
	}

	/**
	 * Constructs a response with the given matching triples, metadata triples, next page URL, triple count, federation
	 * member, request, request start time, and error details. The retrieval end time is automatically set to the
	 * current time at the moment of construction.
	 *
	 * @param matchingTriples  the list of triples matching the request pattern (must not be null)
	 * @param metadataTriples  the list of metadata triples (must not be null)
	 * @param nextPageURL      the URL for the next page of results, or {@code null} if there is none
	 * @param tripleCount      the triple count
	 * @param fm               the federation member from which the data was retrieved
	 * @param request          the original data retrieval request
	 * @param requestStartTime the timestamp when the request started
	 * @param errorStatusCode  the HTTP status code representing an error, or {@code null} if no error occurred
	 * @param errorDescription a short description of the error, or {@code null} if no error occurred
	 */
	public TPFResponseImpl( final List<Triple> matchingTriples,
	                        final List<Triple> metadataTriples,
	                        final String nextPageURL,
	                        final int tripleCount,
	                        final FederationMember fm,
	                        final DataRetrievalRequest request,
	                        final Date requestStartTime,
	                        final Integer errorStatusCode,
	                        final String errorDescription ) {
		super( matchingTriples, fm, request, requestStartTime, errorStatusCode, errorDescription );

		assert metadataTriples != null;
		assert tripleCount >= 0;

		this.metadataTriples = metadataTriples;
		this.nextPageURL = nextPageURL; // may be null
		this.cardEstimate = Integer.valueOf( tripleCount );
	}

	/**
	 * Constructs a response with the given matching triples, metadata triples, next page URL, triple count, federation
	 * member, request, request start time, retrieval end time, and error details.
	 *
	 * @param matchingTriples  the list of triples matching the request pattern (must not be null)
	 * @param metadataTriples  the list of metadata triples (must not be null)
	 * @param nextPageURL      the URL for the next page of results, or {@code null} if there is none
	 * @param tripleCount      the triple count
	 * @param fm               the federation member from which the data was retrieved
	 * @param request          the original data retrieval request
	 * @param requestStartTime the timestamp when the request started
	 * @param retrievalEndTime the time at which the retrieval of this response was completed (must not be {@code null})
	 * @param errorStatusCode  the HTTP status code representing an error, or {@code null} if no error occurred
	 * @param errorDescription a short description of the error, or {@code null} if no error occurred
	 */
	public TPFResponseImpl( final List<Triple> matchingTriples,
	                        final List<Triple> metadataTriples,
	                        final String nextPageURL,
	                        final int tripleCount,
	                        final FederationMember fm,
	                        final DataRetrievalRequest request,
	                        final Date requestStartTime,
	                        final Date retrievalEndTime,
	                        final Integer errorStatusCode,
	                        final String errorDescription ) {
		super( matchingTriples, fm, request, requestStartTime, retrievalEndTime, errorStatusCode, errorDescription );

		assert metadataTriples != null;
		assert tripleCount >= 0;

		this.metadataTriples = metadataTriples;
		this.nextPageURL = nextPageURL; // may be null
		this.cardEstimate = Integer.valueOf( tripleCount );
	}

	@Override
	public Iterable<Triple> getMetadata() {
		return metadataTriples;
	}

	@Override
	public Boolean isLastPage() {
		return nextPageURL == null;
	}

	@Override
	public String getNextPageURL() {
		return nextPageURL;
	}

	@Override
	public Integer getCardinalityEstimate() {
		return cardEstimate;
	}

	/**
	 * Returns an iterator over all triples contained in the TPF response, concatenating the payload
	 * and metadata into a single iterable.
	 *
	 * @throws UnsupportedOperationDueToRetrievalError
	 */
	@Override
	public Iterable<Triple> getResponseData() throws UnsupportedOperationDueToRetrievalError {
		if ( isError() ) {
			throw new UnsupportedOperationDueToRetrievalError(
				getErrorStatusCode(),
				getErrorDescription(),
				getRequest(),
				getFederationMember()
			);
		}
		return new ConcatenatingIterable<Triple>( getPayload(), getMetadata() );
	}

	/**
	 * Returns an iterator over the matched triples in the TPF response.
	 *
	 * @throws UnsupportedOperationDueToRetrievalError
	 */
	@Override
	public Iterable<Triple> getPayload() throws UnsupportedOperationDueToRetrievalError {
		return super.getResponseData();
	}
}
