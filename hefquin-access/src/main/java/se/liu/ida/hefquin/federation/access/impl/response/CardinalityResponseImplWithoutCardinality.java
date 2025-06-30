package se.liu.ida.hefquin.federation.access.impl.response;

import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.federation.access.DataRetrievalResponse;

/**
 * A subclass of {@link CardinalityResponseImpl} representing a response where the cardinality is not available.
 *
 * This implementation wraps a {@link DataRetrievalResponse} and a {@link DataRetrievalRequest} and captures the
 * exception that occurred, while assigning {@code Integer.MAX_VALUE} as a placeholder for the unknown cardinality.
 */
public class CardinalityResponseImplWithoutCardinality extends CardinalityResponseImpl
{
	protected final Exception exception;

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
