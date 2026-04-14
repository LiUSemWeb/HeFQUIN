package se.liu.ida.hefquin.federation.access.impl.response;

import java.util.Date;
import java.util.List;

import se.liu.ida.hefquin.base.data.Triple;
import se.liu.ida.hefquin.federation.access.TriplesResponse;

public class TriplesResponseImpl extends DataRetrievalResponseBase<Iterable<Triple>> implements TriplesResponse
{
	/**
	 * Constructs a response with the given triples and request start time.
	 * The retrieval end time is automatically set to the current time at
	 * the moment of construction. This constructor assumes no error occurred.
	 *
	 * @param triples          the list of triples contained in this response (must not be {@code null})
	 * @param requestStartTime the time at which the request was initiated (must not be {@code null})
	 */
	public TriplesResponseImpl( final List<Triple> triples,
	                            final Date requestStartTime ) {
		super(triples, requestStartTime);
	}

	/**
	 * Constructs a response with the given triples, request start time, and
	 * the retrieval end time. This constructor assumes no error occurred.
	 *
	 * @param triples          the list of triples contained in this response (must not be {@code null})
	 * @param requestStartTime the time at which the request was initiated (must not be {@code null})
	 * @param retrievalEndTime the time at which the retrieval of this response was completed (must not be {@code null})
	 */
	public TriplesResponseImpl( final List<Triple> triples,
	                            final Date requestStartTime,
	                            final Date retrievalEndTime ) {
		super(triples, requestStartTime, retrievalEndTime);
	}

	/**
	 * Constructs a response with the given triples, request start time,
	 * and error details. The retrieval end time is automatically set to
	 * the current time at the moment of construction.
	 *
	 * @param triples          the list of triples contained in this response (must not be {@code null})
	 * @param requestStartTime the time at which the request was initiated (must not be {@code null})
	 * @param errorStatusCode  the HTTP status code representing an error, or {@code null} if no error occurred
	 * @param errorDescription a short description of the error, or {@code null} if no error occurred
	 */
	public TriplesResponseImpl( final List<Triple> triples,
	                            final Date requestStartTime,
	                            final Integer errorStatusCode,
	                            final String errorDescription ) {
		super(triples, requestStartTime, errorStatusCode, errorDescription);
	}

	/**
	 * Constructs a response with the given triples, request start time,
	 * retrieval end time, and error details.
	 *
	 * @param triples          the list of triples contained in this response (must not be {@code null})
	 * @param requestStartTime the time at which the request was initiated (must not be {@code null})
	 * @param retrievalEndTime the time at which the retrieval of this response was completed (must not be {@code null})
	 * @param errorStatusCode  the HTTP status code representing an error, or {@code null} if no error occurred
	 * @param errorDescription a short description of the error, or {@code null} if no error occurred
	 */
	public TriplesResponseImpl( final List<Triple> triples,
	                            final Date requestStartTime,
	                            final Date retrievalEndTime,
	                            final Integer errorStatusCode,
	                            final String errorDescription ) {
		super(triples, requestStartTime, retrievalEndTime, errorStatusCode, errorDescription);
	}
}
