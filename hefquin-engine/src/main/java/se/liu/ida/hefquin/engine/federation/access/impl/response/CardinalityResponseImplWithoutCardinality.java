package se.liu.ida.hefquin.engine.federation.access.impl.response;

import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalResponse;

/**
 * A subclass of {@link CardinalityResponseImpl} representing a response where the cardinality is not available due to a
 * possible error.
 *
 * This implementation wraps a {@link DataRetrievalResponse} and a {@link DataRetrievalRequest} and captures the
 * exception that occurred, while assigning {@code Integer.MAX_VALUE} as a placeholder for the unknown cardinality.
 */
public class CardinalityResponseImplWithoutCardinality extends CardinalityResponseImpl
{
	protected Exception exception;

	/**
	 * Constructs a {@code CardinalityResponseImplWithoutCardinality} with no specific exception.
	 *
	 * @param wrappedResponse the original data retrieval response
	 * @param request         the original data retrieval request
	 */
	public CardinalityResponseImplWithoutCardinality( final DataRetrievalResponse<?> wrappedResponse,
	                                                  final DataRetrievalRequest request ) {
		this( null, wrappedResponse, request );
	}

	/**
	 * Constructs a {@code CardinalityResponseImplWithoutCardinality} with the given exception.
	 *
	 * @param exception       the exception that caused the absence of cardinality/cardinality estimate
	 * @param wrappedResponse the original data retrieval response
	 * @param request         the original data retrieval request
	 */
	public CardinalityResponseImplWithoutCardinality( final Exception exception,
		                                              final DataRetrievalResponse<?> wrappedResponse,
		                                              final DataRetrievalRequest request ) {
		super( wrappedResponse, request, Integer.MAX_VALUE );
		this.exception = exception;
	}

	/**
	 * Returns the exception that caused the absence of cardinality/cardinality estimate.
	 *
	 * @return the associated exception
	 */
	public Exception getException() {
		return exception;
	}
}
