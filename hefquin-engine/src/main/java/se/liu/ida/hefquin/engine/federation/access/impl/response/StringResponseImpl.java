package se.liu.ida.hefquin.engine.federation.access.impl.response;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.StringResponse;

import java.util.Date;

public class StringResponseImpl extends DataRetrievalResponseBase implements StringResponse
{
	protected final String response;

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
		super( fm, request, requestStartTime );

		assert response != null;
		this.response = response;
	}

	/**
	 * Constructs a response with a string response, federation member, request, request start time, and retrieval end
	 * time. This constructor assumes no error occurred.
	 *
	 * @param response         the response string (must not be {@code null})
	 * @param fm               the federation member from which this response originates (must not be {@code null})
	 * @param request          the data retrieval request associated with this response (must not be {@code null})
	 * @param requestStartTime the time at which the request was initiated (must not be {@code null})
	 */
	public StringResponseImpl( final String response,
	                           final FederationMember fm,
	                           final DataRetrievalRequest request,
	                           final Date requestStartTime,
	                           final Date retrievalEndTime ) {
		super( fm, request, requestStartTime, retrievalEndTime );

		assert response != null;
		this.response = response;
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
		super( fm, request, requestStartTime, errorStatusCode, errorDescription );

		assert response != null;
		this.response = response;
	}

	/**
	 * Constructs a response with a string response, federation member, request, and request start time. The retrieval
	 * end time is automatically set to the current time at the moment of construction. This constructor assumes no
	 * error occurred.
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
		super( fm, request, requestStartTime, retrievalEndTime, errorStatusCode, errorDescription );

		assert response != null;
		this.response = response;
	}

	@Override
	public String getResponse() {
		return this.response;
	}
}
