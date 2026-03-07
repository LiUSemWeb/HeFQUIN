package se.liu.ida.hefquin.federation.access.impl.response;

import java.util.Date;

import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.federation.access.StringResponse;

public class StringResponseImpl extends DataRetrievalResponseBase<String> implements StringResponse
{
	/**
	 * Constructs a response with a string response, federation member, request, and request start time. The retrieval
	 * end time is automatically set to the current time at the moment of construction. This constructor assumes no
	 * error occurred.
	 *
	 * @param response         the response string (must not be {@code null})
	 * @param fm               the federation member from which this response originates (must not be {@code null})
	 * @param request          the data retrieval request associated with this response (must not be {@code null})
	 * @param requestStartTime the time at which the request was initiated (must not be {@code null})
	 */
	public StringResponseImpl( final String response,
	                           final FederationMember fm,
	                           final DataRetrievalRequest request,
	                           final Date requestStartTime ) {
		super( response, fm, request, requestStartTime );
	}

	/**
	 * Constructs a response with a string response, federation member, request, request start time, and retrieval end
	 * time. This constructor assumes no error occurred.
	 *
	 * @param response         the response string (must not be {@code null})
	 * @param fm               the federation member from which this response originates (must not be {@code null})
	 * @param request          the data retrieval request associated with this response (must not be {@code null})
	 * @param requestStartTime the time at which the request was initiated (must not be {@code null})
	 * @param retrievalEndTime the time at which the retrieval of this response was completed (must not be {@code null})
	 */
	public StringResponseImpl( final String response,
	                           final FederationMember fm,
	                           final DataRetrievalRequest request,
	                           final Date requestStartTime,
	                           final Date retrievalEndTime ) {
		super( response, fm, request, requestStartTime, retrievalEndTime );
	}

	/**
	 * Constructs a response with a string response, federation member, request, request start time, and error details.
	 * The retrieval end time is automatically set to the current time at the moment of construction.
	 *
	 * @param response         the response string (must not be {@code null})
	 * @param fm               the federation member from which this response originates (must not be {@code null})
	 * @param request          the data retrieval request associated with this response (must not be {@code null})
	 * @param requestStartTime the time at which the request was initiated (must not be {@code null})
	 * @param errorStatusCode  the HTTP status code representing an error, or {@code null} if no error occurred
	 * @param errorDescription a short description of the error, or {@code null} if no error occurred
	 */
	public StringResponseImpl( final String response,
	                           final FederationMember fm,
	                           final DataRetrievalRequest request,
	                           final Date requestStartTime,
	                           final Integer errorStatusCode,
	                           final String errorDescription ) {
		super( response, fm, request, requestStartTime, errorStatusCode, errorDescription );
	}

	/**
	 * Constructs a response with a string response, federation member, request, request start time, retrieval end time,
	 * and error details.
	 *
	 * @param response         the response string (must not be {@code null})
	 * @param fm               the federation member from which this response originates (must not be {@code null})
	 * @param request          the data retrieval request associated with this response (must not be {@code null})
	 * @param requestStartTime the time at which the request was initiated (must not be {@code null})
	 * @param retrievalEndTime the time at which the retrieval of this response was completed (must not be {@code null})
	 * @param errorStatusCode  the HTTP status code representing an error, or {@code null} if no error occurred
	 * @param errorDescription a short description of the error, or {@code null} if no error occurred
	 */
	public StringResponseImpl( final String response,
	                           final FederationMember fm,
	                           final DataRetrievalRequest request,
	                           final Date requestStartTime,
	                           final Date retrievalEndTime,
	                           final Integer errorStatusCode,
	                           final String errorDescription ) {
		super( response, fm, request, requestStartTime, retrievalEndTime, errorStatusCode, errorDescription );
	}
}
