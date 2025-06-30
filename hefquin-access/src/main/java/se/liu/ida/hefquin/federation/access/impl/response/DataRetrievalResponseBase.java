package se.liu.ida.hefquin.federation.access.impl.response;

import java.util.Date;

import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.federation.access.DataRetrievalResponse;
import se.liu.ida.hefquin.federation.access.UnsupportedOperationDueToRetrievalError;

public abstract class DataRetrievalResponseBase<T> implements DataRetrievalResponse<T>
{
	protected final T data;
	private final FederationMember fm;
	private final DataRetrievalRequest request;
	private final Date requestStartTime;
	private final Date retrievalEndTime;
	protected final Integer errorStatusCode;
	protected final String errorDescription;

	/**
	 * Constructs a response with the given data, federation member, request, and request start time. The retrieval end
	 * time is automatically set to the current time at the moment of construction. This constructor assumes no error
	 * occurred.
	 *
	 * @param data             the data contained in this response (must not be {@code null})
	 * @param fm               the federation member from which this response originates (must not be {@code null})
	 * @param request          the request associated with this response (must not be {@code null})
	 * @param requestStartTime the time at which the request was initiated (must not be {@code null})
	 */
	protected DataRetrievalResponseBase( final T data,
	                                     final FederationMember fm,
	                                     final DataRetrievalRequest request,
	                                     final Date requestStartTime ) {
		this( data, fm, request, requestStartTime, null, null );
	}

	/**
	 * Constructs a response with the given data, federation member, request, request start time, and request end time.
	 * This constructor assumes no error occurred.
	 *
	 * @param data             the data contained in this response (must not be {@code null})
	 * @param fm               the federation member from which this response originates (must not be {@code null})
	 * @param request          the request associated with this response (must not be {@code null})
	 * @param requestStartTime the time at which the request was initiated (must not be {@code null})
	 * @param retrievalEndTime the time at which the retrieval of this response was completed (must not be {@code null})
	 */
	protected DataRetrievalResponseBase( final T data,
	                                     final FederationMember fm,
	                                     final DataRetrievalRequest request,
	                                     final Date requestStartTime,
	                                     final Date retrievalEndTime ) {
		this( data, fm, request, requestStartTime, retrievalEndTime, null, null );
	}

	/**
	 * Constructs a response with the given data, federation member, request, request start time, and error details. The
	 * retrieval end time is automatically set to the current time at the moment of construction.
	 *
	 * @param data             the data contained in this response (must not be {@code null})
	 * @param fm               the federation member from which this response originates (must not be {@code null})
	 * @param request          the request associated with this response (must not be {@code null})
	 * @param requestStartTime the time at which the request was initiated (must not be {@code null})
	 * @param errorStatusCode  the HTTP status code representing an error, or {@code null} if no error occurred
	 * @param errorDescription a short description of the error, or {@code null} if no error occurred
	 */
	protected DataRetrievalResponseBase( final T data,
	                                     final FederationMember fm,
	                                     final DataRetrievalRequest request,
	                                     final Date requestStartTime,
	                                     final Integer errorStatusCode,
	                                     final String errorDescription ) {
		this( data, fm, request, requestStartTime, new Date(), errorStatusCode, errorDescription );
	}

	/**
	 * Constructs a response with the given data, federation member, request, request start time, retrieval end time,
	 * and error details.
	 *
	 * @param data             the data contained in this response (must not be {@code null})
	 * @param fm               the federation member from which this response originates (must not be {@code null})
	 * @param request          the request associated with this response (must not be {@code null})
	 * @param requestStartTime the time at which the request was initiated (must not be {@code null})
	 * @param retrievalEndTime the time at which the retrieval of this response was completed (must not be {@code null})
	 * @param errorStatusCode  the HTTP status code representing an error, or {@code null} if no error occurred
	 * @param errorDescription a short description of the error, or {@code null} if no error occurred
	 */
	protected DataRetrievalResponseBase( final T data,
	                                     final FederationMember fm,
	                                     final DataRetrievalRequest request,
	                                     final Date requestStartTime,
	                                     final Date retrievalEndTime,
	                                     final Integer errorStatusCode,
	                                     final String errorDescription ) {
		assert data != null;
		assert fm != null;
		assert request != null;
		assert requestStartTime != null;
		assert retrievalEndTime != null;

		this.data = data;
		this.fm = fm;
		this.request = request;
		this.requestStartTime = requestStartTime;
		this.retrievalEndTime = retrievalEndTime;
		this.errorStatusCode = errorStatusCode;
		this.errorDescription = errorDescription;
	}

	/**
	 * Returns the federation member from which this response originates.
	 *
	 * @return the corresponding federation member
	 */
	@Override
	public FederationMember getFederationMember() {
		return fm;
	}

	/**
	 * Returns the data retrieval request that led to this response.
	 *
	 * @return the associated data retrieval request
	 */
	@Override
	public DataRetrievalRequest getRequest() {
		return request;
	}

	/**
	 * Returns the timestamp indicating when the data retrieval request was initiated.
	 *
	 * @return the request start time
	 */
	@Override
	public Date getRequestStartTime() {
		return requestStartTime;
	}

	/**
	 * Returns the timestamp indicating when the data retrieval was completed.
	 *
	 * @return the retrieval end time
	 */
	@Override
	public Date getRetrievalEndTime() {
		return retrievalEndTime;
	}

	/**
	 * Returns the HTTP status code associated with an error, if any.
	 *
	 * @return the error status code, or {@code null} if no error occurred
	 */
	@Override
	public Integer getErrorStatusCode() {
		return errorStatusCode;
	}

	/**
	 * Returns a short textual description of the error, if available.
	 *
	 * @return the error description, or {@code null} if no error occurred
	 */
	@Override
	public String getErrorDescription() {
		return errorDescription;
	}

	/**
	 * Returns the data retrieved in response to the corresponding request,
	 * or throws UnsupportedOperationDueToRetrievalError if an error occurred
	 * during data retrieval.
	 * 
	 * @return the data retrieved in this response
	 */
	@Override
	public T getResponseData() throws UnsupportedOperationDueToRetrievalError {
		if ( isError() ) {
			throw new UnsupportedOperationDueToRetrievalError(
				getErrorStatusCode(),
				getErrorDescription(),
				getRequest(),
				getFederationMember()
			);
		}
		return data;
	}
}
