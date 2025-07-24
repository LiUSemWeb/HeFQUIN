package se.liu.ida.hefquin.base.query;

import java.util.Set;

import org.apache.jena.sparql.engine.binding.Binding;

import se.liu.ida.hefquin.base.data.SolutionMapping;

/**
 * This interface represents basic graph patterns (BGPs).
 */
public interface BGP extends SPARQLGraphPattern
{
	/**
	 * Returns an unmodifiable set of triple patterns.
	 */
	Set<TriplePattern> getTriplePatterns();

	/**
	 * Returns a string representation of the BGP
	 */
	String toString();

	@Override
	BGP applySolMapToGraphPattern( Binding sm ) throws VariableByBlankNodeSubstitutionException;

	@Override
	default BGP applySolMapToGraphPattern( final SolutionMapping sm ) throws VariableByBlankNodeSubstitutionException {
		return applySolMapToGraphPattern( sm.asJenaBinding() );
	}

	/**
	 * Returns a BGP that contains all triple patterns of this BGP plus
	 * the given triple pattern. This method is a more specific version
	 * of {@link SPARQLGraphPattern#mergeWith(SPARQLGraphPattern)}.
	 */
	BGP mergeWith( TriplePattern tp );

	/**
	 * Return a BGP that contains all triple patterns of this BGP plus all
	 * triple patterns of the given BGP. This method is a more specific
	 * version of {@link SPARQLGraphPattern#mergeWith(SPARQLGraphPattern)}.
	 */
	BGP mergeWith( BGP otherBGP );

}
