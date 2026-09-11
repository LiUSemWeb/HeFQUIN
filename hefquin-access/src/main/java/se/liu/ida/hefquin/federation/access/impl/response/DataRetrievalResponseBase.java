package se.liu.ida.hefquin.federation.access.impl.response;

import java.util.Date;

import se.liu.ida.hefquin.federation.access.DataRetrievalResponse;
import se.liu.ida.hefquin.federation.access.UnsupportedOperationDueToRetrievalError;

public class DataRetrievalResponseBase<T> implements DataRetrievalResponse<T>
{
	protected final T data;
	protected final Integer errorStatusCode;
	protected final String errorDescription;
	protected final Exception exception;
	protected final Date requestStartTime;
	protected final Date retrievalEndTime;

	/**
	 * Constructs a response with the given data and request start time.
	 * The retrieval end time is automatically set to the current time at
	 * the moment of construction. This constructor assumes no error
	 * occurred.
	 *
	 * @param data             the data contained in this response
	 *                         (must not be {@code null})
	 * @param requestStartTime the time at which the request was initiated
	 *                         (must not be {@code null})
	 */
	protected DataRetrievalResponseBase( final T data,
	                                     final Date requestStartTime ) {
		this(data, requestStartTime, new Date());
	}

	/**
	 * Constructs a response with the given data, request start time, and
	 * request end time. This constructor assumes no error occurred.
	 *
	 * @param data             the data contained in this response
	 *                         (must not be {@code null})
	 * @param requestStartTime the time at which the request was initiated
	 *                         (must not be {@code null})
	 * @param retrievalEndTime the time at which the retrieval of this response
	 *                         was completed (must not be {@code null})
	 */
	protected DataRetrievalResponseBase( final T data,
	                                     final Date requestStartTime,
	                                     final Date retrievalEndTime ) {
		assert data != null;
		assert requestStartTime != null;
		assert retrievalEndTime != null;

		this.data = data;

		this.requestStartTime = requestStartTime;
		this.retrievalEndTime = retrievalEndTime;

		this.errorStatusCode = null;
		this.errorDescription = null;
		this.exception = null;
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
	protected DataRetrievalResponseBase( final int errorStatusCode,
	                                     final String errorDescription,
	                                     final Date requestStartTime ) {
		this( errorStatusCode, errorDescription, requestStartTime, new Date() );
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
	protected DataRetrievalResponseBase( final int errorStatusCode,
	                                     final String errorDescription,
	                                     final Date requestStartTime,
	                                     final Date retrievalEndTime ) {
		assert requestStartTime != null;
		assert retrievalEndTime != null;

		this.errorStatusCode = errorStatusCode;
		this.errorDescription = errorDescription;

		this.requestStartTime = requestStartTime;
		this.retrievalEndTime = retrievalEndTime;

		this.data = null;
		this.exception = null;
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
	protected DataRetrievalResponseBase( final Exception exception,
	                                     final Date requestStartTime ) {
		this( exception, requestStartTime, new Date() );
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
	protected DataRetrievalResponseBase( final Exception exception,
	                                     final Date requestStartTime,
	                                     final Date retrievalEndTime ) {
		assert exception != null;
		assert requestStartTime != null;
		assert retrievalEndTime != null;

		this.exception = exception;

		this.requestStartTime = requestStartTime;
		this.retrievalEndTime = retrievalEndTime;

		this.data = null;
		this.errorStatusCode = null;
		this.errorDescription = null;
	}

	@Override
	public T getResponseData() throws UnsupportedOperationDueToRetrievalError {
		throwExceptionIfNoResponseData();
		return data;
	}

	@Override
	public Integer getErrorStatusCode() {
		return errorStatusCode;
	}

	@Override
	public String getErrorDescription() {
		return errorDescription;
	}

	@Override
	public Exception getException() {
		return exception;
	}

	@Override
	public Date getRequestStartTime() {
		return requestStartTime;
	}

	@Override
	public Date getRetrievalEndTime() {
		return retrievalEndTime;
	}

	/**
	 * Throws an {@link UnsupportedOperationDueToRetrievalError} exception
	 * if ({@link #isError()} is {@code true} or {@link #isDefective()} is
	 * {@code true}. Hence, this method can be used as an initial check
	 * in all methods that assume this response contains the requested
	 * data (i.e., the response to the corresponding request was an not
	 * error message and the response is not defective.
	 *
	 * @throws UnsupportedOperationDueToRetrievalError
	 */
	protected void throwExceptionIfNoResponseData()
			throws UnsupportedOperationDueToRetrievalError {
		if ( isError() ) {
			throw new UnsupportedOperationDueToRetrievalError(
					getErrorStatusCode(),
					getErrorDescription() );
		}

		if ( isDefective() ) {
			throw new UnsupportedOperationDueToRetrievalError(
					getException() );
		}
	}
}
