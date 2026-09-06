package se.liu.ida.hefquin.federation.access.impl.response;

import java.util.Date;
import java.util.List;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.federation.access.SolMapsResponse;

public class SolMapsResponseImpl extends DataRetrievalResponseBase<Iterable<SolutionMapping>> implements SolMapsResponse
{
	/**
	 * Constructs a response with the given solution mappings and request
	 * start time. The retrieval end time is automatically set to the
	 * current time at the moment of construction. This constructor
	 * assumes no error occurred.
	 *
	 * @param solMaps          the list of solution mappings contained in this response (must not be {@code null})
	 * @param requestStartTime the time at which the request was initiated (must not be {@code null})
	 */
	public SolMapsResponseImpl( final List<SolutionMapping> solMaps,
	                            final Date requestStartTime ) {
		super(solMaps, requestStartTime);
	}

	/**
	 * Constructs a response with the given solution mappings, request
	 * start time, and retrieval end time. This constructor assumes no
	 * error occurred.
	 *
	 * @param solMaps          the list of solution mappings contained in this response (must not be {@code null})
	 * @param requestStartTime the time at which the request was initiated (must not be {@code null})
	 * @param retrievalEndTime the time at which the retrieval of this response was completed (must not be {@code null})
	 */
	public SolMapsResponseImpl( final List<SolutionMapping> solMaps,
	                            final Date requestStartTime,
	                            final Date retrievalEndTime ) {
		super(solMaps, requestStartTime, retrievalEndTime);
	}

	/**
	 * Constructs a response for the case that the response was an error
	 * message. The retrieval end time is automatically set to the
	 * current time at the moment of construction.
	 *
	 * @param errorStatusCode  the HTTP status code of the error message
	 *                         (must not be {@code null})
	 * @param errorDescription a short description of the error (may be
	 *                         {@code null})
	 * @param requestStartTime the time at which the request was initiated
	 *                         (must not be {@code null})
	 */
	public SolMapsResponseImpl( final int errorStatusCode,
	                            final String errorDescription,
	                            final Date requestStartTime ) {
		super(errorStatusCode, errorDescription, requestStartTime);
	}

	/**
	 * Constructs a response for the case that the response was an error
	 * message.
	 *
	 * @param errorStatusCode  the HTTP status code of the error message
	 *                         (must not be {@code null})
	 * @param errorDescription a short description of the error (may be
	 *                         {@code null})
	 * @param requestStartTime the time at which the request was initiated
	 *                         (must not be {@code null})
	 * @param retrievalEndTime the time at which the retrieval of this response
	 *                         was completed (must not be {@code null})
	 */
	public SolMapsResponseImpl( final int errorStatusCode,
	                            final String errorDescription,
	                            final Date requestStartTime,
	                            final Date retrievalEndTime ) {
		super(errorStatusCode, errorDescription, requestStartTime, retrievalEndTime);
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
	public SolMapsResponseImpl( final Exception exception,
	                            final Date requestStartTime ) {
		super(exception, requestStartTime);
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
	public SolMapsResponseImpl( final Exception exception,
	                            final Date requestStartTime,
	                            final Date retrievalEndTime ) {
		super(exception, requestStartTime, retrievalEndTime);
	}
}
