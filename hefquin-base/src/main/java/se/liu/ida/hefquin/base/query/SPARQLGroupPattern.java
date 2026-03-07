package se.liu.ida.hefquin.base.query;

import org.apache.jena.sparql.engine.binding.Binding;

import se.liu.ida.hefquin.base.data.SolutionMapping;

/**
 * A SPARQL group pattern represents a collection of SPARQL graph patterns
 * for which the results are meant to be joined. Hence, when considering an
 * algebraic representation of SPARQL queries, the algebraic operator that
 * corresponds to such a group pattern is a multiway join.
 */
public interface SPARQLGroupPattern extends SPARQLGraphPattern
{
	/**
	 * Returns the number of graph patterns that are combined in this group pattern.
	 */
	int getNumberOfSubPatterns();

	/**
	 * Returns all graph patterns that are combined in this group pattern.
	 */
	Iterable<SPARQLGraphPattern> getSubPatterns();

	/**
	 * Returns the i-th of the graph patterns that are combined
	 * in this group pattern, where i starts at index 0 (zero).
	 *
	 * If this group pattern has fewer sub-patterns, then
	 * an {@link IndexOutOfBoundsException} will be thrown.
	 */
	SPARQLGraphPattern getSubPatterns( int i ) throws IndexOutOfBoundsException;

	@Override
	SPARQLGroupPattern applySolMapToGraphPattern( Binding sm ) throws VariableByBlankNodeSubstitutionException;

	@Override
	default SPARQLGroupPattern applySolMapToGraphPattern( final SolutionMapping sm )
			throws VariableByBlankNodeSubstitutionException {
		return applySolMapToGraphPattern( sm.asJenaBinding() );
	}

}
