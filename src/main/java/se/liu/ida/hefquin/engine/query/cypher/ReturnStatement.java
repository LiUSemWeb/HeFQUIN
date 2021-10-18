package se.liu.ida.hefquin.engine.query.cypher;

import java.util.Set;

/**
 * Represents a RETURN statement of a Cypher Query
 * For example, RETURN n AS s, RETURN labels(x), etc.
 */
public interface ReturnStatement {
    /**
     * Obtains the set of variables used in the expression being returned
     * @return a Set of CypherVar
     * For instance, this method for RETURN label(x) AS l will return {x}
     */
    Set<CypherVar> getVars();

    /**
     * @return the optional alias variable of the return statement
     * For instance, this method for RETURN label(x) as l returns l
     */
    CypherVar getAlias();
}
