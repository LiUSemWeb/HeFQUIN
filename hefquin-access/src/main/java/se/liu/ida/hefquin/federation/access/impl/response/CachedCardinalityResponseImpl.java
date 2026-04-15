package se.liu.ida.hefquin.federation.access.impl.response;

import java.util.Date;

import se.liu.ida.hefquin.federation.access.CardinalityResponse;

public class CachedCardinalityResponseImpl extends DataRetrievalResponseBase<Integer> implements CardinalityResponse
{
	/**
	 * Constructs a response with the given cardinality and request start
	 * time. The retrieval end time is automatically set to the current
	 * time at the moment of construction. This constructor assumes no
	 * error occurred.
	 *
	 * @param cardinality      the data contained in this response (must not be {@code null})
	 * @param requestStartTime the time at which the request was initiated (must not be {@code null})
	 */
	public CachedCardinalityResponseImpl( final int cardinality,
	                                      final Date requestStartTime ) {
		super(cardinality, requestStartTime);
	}

	/**
	 * Constructs a response with the given cardinality, request start time,
	 * and request end time. This constructor assumes no error occurred.
	 *
	 * @param cardinality      the data contained in this response (must not be {@code null})
	 * @param requestStartTime the time at which the request was initiated (must not be {@code null})
	 * @param retrievalEndTime the time at which the retrieval of this response was completed (must not be {@code null})
	 */
	public CachedCardinalityResponseImpl( final int cardinality,
	                                      final Date requestStartTime,
	                                      final Date retrievalEndTime ) {
		super(cardinality, requestStartTime, retrievalEndTime);
	}

	/**
	 * Constructs a response with the given cardinality, request start time,
	 * and error details. The retrieval end time is automatically set to the
	 * current time at the moment of construction.
	 *
	 * @param cardinality      the data contained in this response (must not be {@code null})
	 * @param requestStartTime the time at which the request was initiated (must not be {@code null})
	 * @param errorStatusCode  the HTTP status code representing an error, or {@code null} if no error occurred
	 * @param errorDescription a short description of the error, or {@code null} if no error occurred
	 */
	public CachedCardinalityResponseImpl( final int cardinality,
	                                      final Date requestStartTime,
	                                      final Integer errorStatusCode,
	                                      final String errorDescription ) {
		super(cardinality, requestStartTime, errorStatusCode, errorDescription);
	}

	/**
	 * Constructs a response with the given cardinality, request start time,
	 * retrieval end time, and error details.
	 *
	 * @param cardinality      the data contained in this response (must not be {@code null})
	 * @param requestStartTime the time at which the request was initiated (must not be {@code null})
	 * @param retrievalEndTime the time at which the retrieval of this response was completed (must not be {@code null})
	 * @param errorStatusCode  the HTTP status code representing an error, or {@code null} if no error occurred
	 * @param errorDescription a short description of the error, or {@code null} if no error occurred
	 */
	public CachedCardinalityResponseImpl( final int cardinality,
	                                      final Date requestStartTime,
	                                      final Date retrievalEndTime,
	                                      final Integer errorStatusCode,
	                                      final String errorDescription ) {
		super(cardinality, requestStartTime, retrievalEndTime, errorStatusCode, errorDescription);
	}
}
