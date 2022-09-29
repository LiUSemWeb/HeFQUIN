package se.liu.ida.hefquin.engine.data.mappings;

import java.util.Set;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.query.TriplePattern;

/**
 * Maps from the global schema (considered RDF vocabularies) to
 * a local schema of some data source(s) / federation member(s).
 */
public interface SchemaMapping
{
	/**
	 * Applies this mapping to the given triple pattern and returns the
	 * resulting graph pattern. If this mapping is not relevant for the
	 * the given triple pattern (i.e., applying the mapping to the triple
	 * pattern does not have any effect), then the result of this function
	 * is simply the given triple pattern itself.
	 */
	SPARQLGraphPattern applyToTriplePattern( TriplePattern tp );

	/**
	 * Applies this schema mapping to the given solution mapping, which
	 * is assumed to use the global schema for the vocabulary terms that
	 * it binds to its query variables. If this schema mapping is not
	 * relevant for anything mentioned in the given solution mapping
	 * (i.e., applying this schema mapping to the solution mapping does
	 * not have any effect), then the result of this function is simply
	 * a singleton set that contains the given solution mapping without
	 * any changes.
	 */
	Set<SolutionMapping> applyToSolutionMapping( SolutionMapping solmap );

	/**
	 * Applies the inverse of this schema mapping to the given solution
	 * mapping, which is assumed to use the global schema for the vocabulary
	 * terms that it binds to its query variables. If this schema mapping is
	 * not relevant for anything mentioned in the given solution mapping (i.e.,
	 * applying this schema mapping to the solution mapping does not have any
	 * effect), then the result of this function is simply a singleton set that
	 * contains the given solution mapping without any changes.
	 */
	Set<SolutionMapping> applyInverseToSolutionMapping( SolutionMapping solmap );
}
