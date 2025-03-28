package se.liu.ida.hefquin.engine.federation.access.impl.response;

import java.util.Date;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.CardinalityResponse;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;

public class CachedCardinalityResponseImpl extends DataRetrievalResponseBase<Integer> implements CardinalityResponse
{
	/**
	 * Constructs a response with the given cardinality, federation member, request, and request start time. The
	 * retrieval end time is automatically set to the current time at the moment of construction. This constructor
	 * assumes no error occurred.
	 *
	 * @param cardinality      the data contained in this response (must not be {@code null})
	 * @param fm               the federation member from which this response originates (must not be {@code null})
	 * @param request          the request associated with this response (must not be {@code null})
	 * @param requestStartTime the time at which the request was initiated (must not be {@code null})
	 */
	public CachedCardinalityResponseImpl( final int cardinality,
	                                      final FederationMember fm,
	                                      final DataRetrievalRequest request,
	                                      final Date requestStartTime ) {
		super( cardinality, fm, request, requestStartTime );
	}

	/**
	 * Constructs a response with the given cardinality, federation member, request, request start time, and request end
	 * time. This constructor assumes no error occurred.
	 *
	 * @param cardinality      the data contained in this response (must not be {@code null})
	 * @param fm               the federation member from which this response originates (must not be {@code null})
	 * @param request          the request associated with this response (must not be {@code null})
	 * @param requestStartTime the time at which the request was initiated (must not be {@code null})
	 * @param retrievalEndTime the time at which the retrieval of this response was completed (must not be {@code null})
	 */
	public CachedCardinalityResponseImpl( final int cardinality,
	                                      final FederationMember fm,
	                                      final DataRetrievalRequest request,
	                                      final Date requestStartTime,
	                                      final Date retrievalEndTime ) {
		super( cardinality, fm, request, requestStartTime, retrievalEndTime );
	}

	/**
	 * Constructs a response with the given cardinality, federation member, request, request start time, and error
	 * details. The retrieval end time is automatically set to the current time at the moment of construction.
	 *
	 * @param cardinality      the data contained in this response (must not be {@code null})
	 * @param fm               the federation member from which this response originates (must not be {@code null})
	 * @param request          the request associated with this response (must not be {@code null})
	 * @param requestStartTime the time at which the request was initiated (must not be {@code null})
	 * @param errorStatusCode  the HTTP status code representing an error, or {@code null} if no error occurred
	 * @param errorDescription a short description of the error, or {@code null} if no error occurred
	 */
	public CachedCardinalityResponseImpl( final int cardinality,
	                                      final FederationMember fm,
	                                      final DataRetrievalRequest request,
	                                      final Date requestStartTime,
	                                      final Integer errorStatusCode,
	                                      final String errorDescription ) {
		super( cardinality, fm, request, requestStartTime, errorStatusCode, errorDescription );
	}

	/**
	 * Constructs a response with the given cardinality, federation member, request, request start time, retrieval end
	 * time, and error details.
	 *
	 * @param cardinality      the data contained in this response (must not be {@code null})
	 * @param fm               the federation member from which this response originates (must not be {@code null})
	 * @param request          the request associated with this response (must not be {@code null})
	 * @param requestStartTime the time at which the request was initiated (must not be {@code null})
	 * @param retrievalEndTime the time at which the retrieval of this response was completed (must not be {@code null})
	 * @param errorStatusCode  the HTTP status code representing an error, or {@code null} if no error occurred
	 * @param errorDescription a short description of the error, or {@code null} if no error occurred
	 */
	public CachedCardinalityResponseImpl( final int cardinality,
	                                      final FederationMember fm,
	                                      final DataRetrievalRequest request,
	                                      final Date requestStartTime,
	                                      final Date retrievalEndTime,
	                                      final Integer errorStatusCode,
	                                      final String errorDescription ) {
		super( cardinality, fm, request, requestStartTime, retrievalEndTime, errorStatusCode, errorDescription );
	}
}
