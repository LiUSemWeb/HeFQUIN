package se.liu.ida.hefquin.federation.access;

import java.time.Duration;
import java.util.Date;

/**
 * Represents the response obtained for a {@link DataRetrievalRequest}.
 * <p>
 * We distinguish three cases for such responses:
 * <ol>
 *   <li>
 *   The response contains the requested data; hence, the corresponding
 *   request for which this response was obtained did not result in an
 *   error message and there was no exception related to the request. In
 *   this case, {@link #getResponseData()} can be used to get the data.
 *   <li>
 *   The response is an error. This would be the case if the corresponding
 *   request resulted in the retrieval of an error message (typically, an
 *   HTTP error). In this case, {@link #isError()} returns {@code true}
 *   and {@link #getResponseData()} throws an exception. Information about
 *   the error can be accessed via {@link #getErrorStatusCode()} and
 *   {@link #getErrorDescription()}.
 *   <li>
 *   The response is defective. This may be because of one of the following
 *   two reasons: i) The corresponding request could <em>not</em> be issued
 *   successfully but, instead, failed with an exception. ii) The request
 *   was issued successfully and resulted in retrieval of data (not an error
 *   message) but reading this data failed with an exception. In this case,
 *   {@link #isDefective()} returns {@code true}, {@link #getResponseData()}
 *   throws an exception, the exception that caused the response to be
 *   defective can be accessed via {@link #getException()}.
 * </ol>
 * Hence, code that consumes such a response should check {@link #isError()}
 * and {@link #isDefective()} before calling {@link #getResponseData()} (or
 * call {@link #getResponseData()} directly and be ready to deal with the
 * error and the defective case when handling the exception thrown by
 * {@link #getResponseData()}).
 *
 * @param <T> - the type of data obtained via the corresponding retrieval request
 */
public interface DataRetrievalResponse<T>
{
	/**
	 * Returns the data retrieved in this response, or throws an exception
	 * ({@link UnsupportedOperationDueToRetrievalError}) if either the
	 * response to the corresponding request was an error message (see
	 * {@link #isError()}) or the response is defective (see
	 * {@link #isDefective()}).
	 */
	T getResponseData() throws UnsupportedOperationDueToRetrievalError;

	/**
	 * Indicates whether an error message was retrieved. If this method
	 * returns {@code true}, then {@link #getErrorStatusCode()} and
	 * {@link #getErrorDescription()} can be used to get information
	 * about the error.
	 */
	default boolean isError() {
		return getErrorStatusCode() != null;
	};

	/**
	 * Indicates whether this response is defective because either issuing
	 * the request that was supposed to result in this response failed with
	 * an exception or reading the data obtained via the request failed with
	 * an exception. In both cases, the exception that caused the response
	 * to be defective can be accessed via {@link #getException()}.
	 */
	default boolean isDefective() {
		return getException() != null;
	}

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
	 * Returns the HTTP status code if the corresponding request
	 * resulted in an error, or {@code null} otherwise.
	 */
	Integer getErrorStatusCode();

	/**
	 * In case that {@link #isError()} is {@code true}, this method
	 * returns a short description of the error if available, or
	 * {@code null} otherwise.
	 */
	String getErrorDescription();

	/**
	 * In case that issuing the request that was supposed to result in this
	 * response failed with an exception or reading the data obtained via
	 * the request failed with an exception, this method returns that
	 * exception; otherwise it returns {@code null}.
	 */
	Exception getException();
}
