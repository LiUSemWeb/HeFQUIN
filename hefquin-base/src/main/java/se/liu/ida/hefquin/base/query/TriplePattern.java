package se.liu.ida.hefquin.base.query;

import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.engine.binding.Binding;

import se.liu.ida.hefquin.base.data.SolutionMapping;

/**
 * This interface represents triple patterns.
 */
public interface TriplePattern extends SPARQLGraphPattern
{
	/**
	 * Returns a representation of this triple pattern as an object of
	 * the class {@link org.apache.jena.graph.Triple} of the Jena API.
	 */
	Triple asJenaTriple();
	
	/**
	 * Returns the number of distinct variables in this triple pattern.
	 */
	int numberOfVars();
	
	/**
	 * Returns a string representation of the triple
	 */
	String toString();

	@Override
	TriplePattern applySolMapToGraphPattern( Binding sm ) throws VariableByBlankNodeSubstitutionException;

	@Override
	default TriplePattern applySolMapToGraphPattern( final SolutionMapping sm ) throws VariableByBlankNodeSubstitutionException {
		return applySolMapToGraphPattern( sm.asJenaBinding() );
	}

	/**
	 * Returns a BGP that contains this triple pattern plus all triple
	 * patterns of the given BGP. This method is a more specific version
	 * of {@link SPARQLGraphPattern#mergeWith(SPARQLGraphPattern)}.
	 */
	BGP mergeWith( TriplePattern other );

	/**
	 * Return a BGP that contains this triple pattern plus all triple
	 * patterns of the given BGP. This method is a more specific version
	 * of {@link SPARQLGraphPattern#mergeWith(SPARQLGraphPattern)}.
	 */
	BGP mergeWith( BGP bgp );
}
