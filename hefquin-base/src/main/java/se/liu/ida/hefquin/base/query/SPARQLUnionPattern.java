package se.liu.ida.hefquin.base.query;

import org.apache.jena.sparql.engine.binding.Binding;

import se.liu.ida.hefquin.base.data.SolutionMapping;

/**
 * A SPARQL union pattern represents a collection of SPARQL graph patterns
 * for which the results are meant to be combined via union. Hence, when
 * considering an algebraic representation of SPARQL queries, the algebraic
 * operator that corresponds to such a union pattern is a multiway union.
 */
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

	@Override
	SPARQLUnionPattern applySolMapToGraphPattern( Binding sm ) throws VariableByBlankNodeSubstitutionException;

	@Override
	default SPARQLUnionPattern applySolMapToGraphPattern( final SolutionMapping sm )
			throws VariableByBlankNodeSubstitutionException {
		return applySolMapToGraphPattern( sm.asJenaBinding() );
	}

}
