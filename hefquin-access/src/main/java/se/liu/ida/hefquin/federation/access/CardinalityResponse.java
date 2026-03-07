package se.liu.ida.hefquin.federation.access;

public interface CardinalityResponse extends DataRetrievalResponse<Integer>
{
	/**
	 * Returns the cardinality value associated with this response. The cardinality represents the number of result
	 * items obtained or estimated.
	 *
	 * @return the cardinality value
	 */
	default int getCardinality() throws UnsupportedOperationDueToRetrievalError {
		return getResponseData();
	}
}
