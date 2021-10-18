package se.liu.ida.hefquin.engine.query.cypher;

import java.util.Set;

/**
 * Represents path matching statements of Cypher queries
 * For example, MATCH (x), MATCH (x)-[e]->(y)
 */
public interface MatchClause {
    /**
     * Checks if two match statements are redundant
     * @param match the other match clause to test
     * @return true if the clauses are redundant, i.e.,
     * if both patterns share nodes
     */
    boolean isRedundantWith(final MatchClause match);

    /**
     * Obtains the set of variables defined in the pattern
     * @return a set of CypherVar with the variables defined in the pattern
     * For example, the object that represents the pattern MATCH (x)-[e]->(y)
     * returns the set {x, e, y}.
     */
    Set<CypherVar> getVars();
}
