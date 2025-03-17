package se.liu.ida.hefquin.engine.federation.access.impl.response;

import java.util.Date;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalResponse;

public abstract class DataRetrievalResponseBase implements DataRetrievalResponse
{
	private final FederationMember fm;
	private final DataRetrievalRequest request;
	private final Date requestStartTime;
	private final Date retrievalEndTime;
	protected final Integer errorStatusCode;
	protected final String errorDescription;

	/**
	 * Constructs a response with the given federation member, request, and request start time. The retrieval end time
	 * is automatically set to the current time at the moment of construction. This constructor assumes no error
	 * occurred.
	 *
	 * @param fm               the federation member from which this response originates (must not be {@code null})
	 * @param request          the request associated with this response (must not be {@code null})
	 * @param requestStartTime the time at which the request was initiated (must not be {@code null})
	 */
	protected DataRetrievalResponseBase( final FederationMember fm,
	                                     final DataRetrievalRequest request,
	                                     final Date requestStartTime ) {
		this( fm, request, requestStartTime, null, null );
	}

	/**
	 * Constructs a response with the given federation member, request, request start time, and request end time. This
	 * constructor assumes no error occurred.
	 *
	 * @param fm               the federation member from which this response originates (must not be {@code null})
	 * @param request          the request associated with this response (must not be {@code null})
	 * @param requestStartTime the time at which the request was initiated (must not be {@code null})
	 * @param retrievalEndTime the time at which the retrieval of this response was completed (must not be {@code null})
	 */
	protected DataRetrievalResponseBase( final FederationMember fm,
	                                     final DataRetrievalRequest request,
	                                     final Date requestStartTime,
	                                     final Date retrievalEndTime ) {
		this( fm, request, requestStartTime, retrievalEndTime, null, null );
	}

	/**
	 * Constructs a response with the given federation member, request, request start time, and error details. The
	 * retrieval end time is automatically set to the current time at the moment of construction.
	 *
	 * @param fm               the federation member from which this response originates (must not be {@code null})
	 * @param request          the request associated with this response (must not be {@code null})
	 * @param requestStartTime the time at which the request was initiated (must not be {@code null})
	 * @param errorStatusCode  the HTTP status code representing an error, or {@code null} if no error occurred
	 * @param errorDescription a short description of the error, or {@code null} if no error occurred
	 */
	protected DataRetrievalResponseBase( final FederationMember fm,
	                                     final DataRetrievalRequest request,
	                                     final Date requestStartTime,
	                                     final Integer errorStatusCode,
	                                     final String errorDescription ) {
		this( fm, request, requestStartTime, new Date(), errorStatusCode, errorDescription );
	}

	/**
	 * Constructs a response with fully specified timing and error information.
	 *
	 * @param fm               the federation member from which this response originates (must not be {@code null})
	 * @param request          the request associated with this response (must not be {@code null})
	 * @param requestStartTime the time at which the request was initiated (must not be {@code null})
	 * @param retrievalEndTime the time at which the retrieval of this response was completed (must not be {@code null})
	 * @param errorStatusCode  the HTTP status code representing an error, or {@code null} if no error occurred
	 * @param errorDescription a short description of the error, or {@code null} if no error occurred
	 */
	protected DataRetrievalResponseBase( final FederationMember fm,
	                                     final DataRetrievalRequest request,
	                                     final Date requestStartTime,
	                                     final Date retrievalEndTime,
	                                     final Integer errorStatusCode,
	                                     final String errorDescription ) {
		assert fm != null;
		assert request != null;
		assert requestStartTime != null;
		assert retrievalEndTime != null;

		this.fm = fm;
		this.request = request;
		this.requestStartTime = requestStartTime;
		this.retrievalEndTime = retrievalEndTime;
		this.errorStatusCode = errorStatusCode;
		this.errorDescription = errorDescription;
	}

	@Override
	public FederationMember getFederationMember() {
		return fm;
	}

	@Override
	public DataRetrievalRequest getRequest() {
		return request;
	}

	@Override
	public Date getRequestStartTime() {
		return requestStartTime;
	}

	@Override
	public Date getRetrievalEndTime() {
		return retrievalEndTime;
	}

	@Override
	public Integer getErrorStatusCode() {
		return errorStatusCode;
	}

	@Override
	public String getErrorDescription() {
		return errorDescription;
	}
}
