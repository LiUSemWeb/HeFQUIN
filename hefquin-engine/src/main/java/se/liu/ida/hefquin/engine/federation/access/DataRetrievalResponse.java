package se.liu.ida.hefquin.engine.federation.access;

import java.time.Duration;
import java.util.Date;

import se.liu.ida.hefquin.engine.federation.FederationMember;

public interface DataRetrievalResponse<T>
{
	/**
	 * Returns the federation member where this response comes from.
	 */
	FederationMember getFederationMember();

	/**
	 * Returns the request that has been issued to obtain this response.
	 */
	DataRetrievalRequest getRequest();

	/**
	 * Returns the time at which the corresponding data retrieval
	 * request (see {@link #getRequest()}) was started.
	 */
	Date getRequestStartTime();

	/**
	 * Returns the time at which the retrieval of this response was completed.
	 */
	Date getRetrievalEndTime();

	/**
	 * Returns the total duration between request start and request end.
	 */
	default Duration getRequestDuration() {
		return Duration.between( getRequestStartTime().toInstant(), getRetrievalEndTime().toInstant() );
	}

	/**
	 * Indicates whether an error occurred during data retrieval.
	 */
	default boolean isError() {
		return getErrorStatusCode() != null;
	};

	/**
	 * Returns the data retrieved in response to the corresponding request, or throws
	 * UnsupportedOperationDueToRetrievalError if an error occurred during data retrieval.
	 */
	T getResponseData() throws UnsupportedOperationDueToRetrievalError;

	/**
	 * Returns the HTTP status code if the response resulted in an error, or empty
	 * otherwise.
	 */
	default Integer getErrorStatusCode() {
		return null;
	};

	/**
	 * Returns a short description of the error if available, or empty otherwise.
	 */
	default String getErrorDescription() {
		return null;
	};
}
