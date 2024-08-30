package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query;

import se.liu.ida.hefquin.base.query.Query;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.expression.CypherVar;

import java.util.Set;

/**
 * Represents a Query written in the Cypher Query Language
 */
public interface CypherQuery extends Query {
    /**
     * Creates the string representation of the modelled Cypher query
     * @return a Cypher query string
     */
    String toString();

    /**
     * Obtains the set of variables that are defined in the MATCH clauses of the query
     * @return a Set of CypherVar objects
     */
    Set<CypherVar> getMatchVars();
}
