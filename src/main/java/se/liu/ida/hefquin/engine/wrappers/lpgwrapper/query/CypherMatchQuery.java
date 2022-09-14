package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.expression.AliasedExpression;

import java.util.List;

/**
 * Represents a Cypher Query with a MATCH-WHERE-RETURN structure,
 * For example, the query:
 * MATCH (x)
 * MATCH (a)-[b]->(c)
 * WHERE a:CLASS AND b.property='value'
 * UNWIND KEYS(a) AS k
 * RETURN x AS n1, c AS n2, k AS key
 * can be represented with this interface.
 */
public interface CypherMatchQuery extends CypherQuery {

    /**
     * Obtains the list of the patterns to be matched in the query. For the example query,
     * this method returns a list with 2 elements, one representing each MATCH statement.
     * @return a list of MatchClause objects
     */
    List<MatchClause> getMatches();

    /**
     * Obtains a list of conditions, such that their conjunction represents the condition imposed
     * in the WHERE clause of the query. For the example query, this method returns a list of 2
     * conditions, representing a:CLASS and b.property='value'.
     * @return a list of WhereCondition objects
     */
    List<WhereCondition> getConditions();

    /**
     * Obtains a list of iterator expressions, of the form list AS var, present on the query.
     * For the example query, this method returns a list of one iterator: KEYS(a) AS k
     * @return a list of UnwindIterator objects
     */
    List<UnwindIterator> getIterators();

    /**
     * Obtains a list of expressions with optional aliases that represent the columns being returned by the query.
     * For the example query, this method returns a list with 2 elements, representing the 2 columns being returned
     * @return a list of ReturnStatement objects
     */
    List<AliasedExpression> getReturnExprs();

}
