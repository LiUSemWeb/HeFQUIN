package se.liu.ida.hefquin.engine.data.mappings;

import java.util.Set;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.query.TriplePattern;

/**
 * Maps from the global representation of entities to a local representation
 * that is specific to a particular data source (federation member).
 */
public interface EntityMapping
{
	/**
	 * Applies this mapping to the given triple pattern and returns
	 * the resulting set of triple patterns that is meant to be used
	 * as a union. If this mapping is not relevant for any of the
	 * entities mentioned in the given triple pattern (i.e., applying
	 * the mapping to the triple pattern does not have any effect),
	 * then the result of this function is simply a singleton set that
	 * contains the given triple pattern without any changes.
	 */
	Set<TriplePattern> applyToTriplePattern( TriplePattern tp );

	/**
	 * Applies this entity mapping to the given solution mapping, which
	 * is assumed to use the global representation of the entities that
	 * it binds to its query variables. If this entity mapping is not
	 * relevant for any of the entities mentioned in the given solution
	 * mapping (i.e., applying this entity mapping to the solution mapping
	 * does not have any effect), then the result of this function is simply
	 * a singleton set that contains the given solution mapping without any
	 * changes.
	 */
	Set<SolutionMapping> applyToSolutionMapping( SolutionMapping solmap );

	/**
	 * Applies the inverse of this entity mapping to the given solution
	 * mapping, which is assumed to use the local representation of the
	 * entities that it binds to its query variables. If this entity
	 * mapping is not relevant for any of the entities mentioned in the
	 * given solution mapping (i.e., applying this entity mapping to the
	 * solution mapping does not have any effect), then the result of this
	 * function is simply a singleton set that contains the given solution
	 * mapping without any changes.
	 */
	Set<SolutionMapping> applyInverseToSolutionMapping( SolutionMapping solmap );
}
