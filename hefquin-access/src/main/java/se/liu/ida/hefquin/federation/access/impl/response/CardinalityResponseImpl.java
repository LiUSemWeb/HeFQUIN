package se.liu.ida.hefquin.federation.access.impl.response;

import java.time.Duration;
import java.util.Date;

import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.CardinalityResponse;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.federation.access.DataRetrievalResponse;
import se.liu.ida.hefquin.federation.access.UnsupportedOperationDueToRetrievalError;

public class CardinalityResponseImpl implements CardinalityResponse
{
	protected final DataRetrievalResponse<?> wrappedResponse;
	protected final DataRetrievalRequest request;
	protected final int cardinality;

	/**
	 * Constructs a cardinality response that wraps the given data retrieval response and associates it with a request
	 * and a cardinality value.
	 *
	 * @param wrappedResponse the wrapped data retrieval response (must not be null)
	 * @param request         the original data retrieval request (must not be null)
	 * @param cardinality     the cardinality of the request
	 */
	public CardinalityResponseImpl( final DataRetrievalResponse<?> wrappedResponse,
	                                final DataRetrievalRequest request,
	                                final int cardinality ) {
		assert wrappedResponse != null;
		assert request != null;

		this.wrappedResponse = wrappedResponse;
		this.request = request;
		this.cardinality = cardinality;
	}

	/**
	 * Returns the original data retrieval response that this object wraps.
	 *
	 * @return the wrapped {@link DataRetrievalResponse} instance
	 */
	public DataRetrievalResponse<?> getWrappedResponse() {
		return wrappedResponse;
	}

	/**
	 * Returns the federation member associated with the wrapped response.
	 *
	 * @return the corresponding federation member
	 */
	@Override
	public FederationMember getFederationMember() {
		return wrappedResponse.getFederationMember();
	}

	/**
	 * Returns the data retrieval request associated with this response.
	 *
	 * @return the associated {@link DataRetrievalRequest}
	 */
	@Override
	public DataRetrievalRequest getRequest() {
		return request;
	}

	/**
	 * Returns the timestamp indicating when the original request was initiated.
	 *
	 * @return the request start time
	 */
	@Override
	public Date getRequestStartTime() {
		return wrappedResponse.getRequestStartTime();
	}

	/**
	 * Returns the timestamp indicating when the retrieval of the wrapped response completed.
	 *
	 * @return the retrieval end time
	 */
	@Override
	public Date getRetrievalEndTime() {
		return wrappedResponse.getRetrievalEndTime();
	}

	/**
	 * Returns the duration between the request start and the retrieval end time, as reported by the wrapped response.
	 *
	 * @return the total duration of the request
	 */
	@Override
	public Duration getRequestDuration() {
		return wrappedResponse.getRequestDuration();
	}

	/**
	 * Indicates whether an error occurred during data retrieval, based on the wrapped response.
	 *
	 * @return {@code true} if an error occurred. otherwise {@code false}
	 */
	@Override
	public boolean isError() {
		return wrappedResponse.getErrorStatusCode() != null;
	}

	/**
	 * Returns the error status code from the wrapped response, if any.
	 *
	 * @return the HTTP status code representing the error, or {@code null} if no error occurred
	 */
	@Override
	public Integer getErrorStatusCode() {
		return wrappedResponse.getErrorStatusCode();
	}

	/**
	 * Returns a short description of the error from the wrapped response, if available.
	 *
	 * @return the error description, or {@code null} if no error occurred
	 */
	@Override
	public String getErrorDescription() {
		return wrappedResponse.getErrorDescription();
	}

	/**
	 * Returns the cardinality value as the response data. If the wrapped response indicates an error, this method
	 * throws an exception instead of returning a value.
	 *
	 * @return the cardinality value
	 * @throws UnsupportedOperationDueToRetrievalError if the wrapped response indicates an error
	 */
	@Override
	public Integer getResponseData() throws UnsupportedOperationDueToRetrievalError {
		if ( wrappedResponse.isError() ) {
			throw new UnsupportedOperationDueToRetrievalError(
				getErrorStatusCode(),
				getErrorDescription(),
				getRequest(),
				getFederationMember()
			);
		}
		return cardinality;
	}
}
