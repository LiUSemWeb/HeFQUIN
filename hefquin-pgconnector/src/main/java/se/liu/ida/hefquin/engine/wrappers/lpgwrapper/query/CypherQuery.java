package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query;

import java.util.Set;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.expression.CypherVar;

/**
 * Represents a query written in the Cypher query language.
 */
public interface CypherQuery 
{
	/**
	 * Returns a Cypher expression representing this query.
	 */
	String toString();

	/**
	 * Returns the set of variables that are defined in the
	 * MATCH clauses of this query.
	 */
	Set<CypherVar> getMatchVars();

}
