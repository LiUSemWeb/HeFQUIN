package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.expression.CypherVar;

import java.util.Set;

/**
 * Represents a RETURN statement of a Cypher Query. This means that each column returned by a query is modelled
 * by one of the classes that implements this interface.
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
     * @return the optional alias variable of the return statement, or null if it is not defined
     * For instance, this method for RETURN labels(x) as l returns l
     */
    CypherVar getAlias();

    /**
     * Returns the Cypher expression being returned by the statement.
     * For instance, this method for RETURN labels(x) as l returns the String "labels(x)"
     */
    String getExpression();
}
