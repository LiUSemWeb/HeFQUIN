package se.liu.ida.hefquin.base.query;

public interface SPARQLUnionPattern extends SPARQLGraphPattern
{
	/**
	 * Returns the number of graph patterns that are combined via this union operator.
	 */
	int getNumberOfSubPatterns();

	/**
	 * Returns all graph patterns that are combined via this union operator.
	 */
	Iterable<SPARQLGraphPattern> getSubPatterns();

	/**
	 * Returns the i-th of the graph patterns that are combined
	 * via this union operator, where i starts at index 0 (zero).
	 *
	 * If the union operator has fewer sub-patterns, then
	 * an {@link IndexOutOfBoundsException} will be thrown.
	 */
	SPARQLGraphPattern getSubPatterns( int i ) throws IndexOutOfBoundsException;
}
