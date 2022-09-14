package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query;

import java.util.List;

/**
 * Represents a UNION Cypher query. This means, a finite collection of compatible MATCH Cypher queries (modelled by
 * CypherMatchQuery).
 *
 * Two CypherMatchQuery objects are compatible if they return the same number of columns and said columns have the same
 * names or aliases.
 *
 * For instance, the query:
 * MATCH (x)
 * RETURN labels(x) AS l, x AS n
 * UNION
 * MATCH (y)-[e]->(z)
 * RETURN labels(z) AS l, y AS n
 *
 * can be represented with this interface
 */
public interface CypherUnionQuery extends CypherQuery {

    /**
     * Returns the list of MATCH Cypher queries that are part of the union of the modelled query. For the example
     * query, this method returns a list with two elements, one representing each MATCH query.
     * @return a List of CypherMatchQuery objects
     */
    List<CypherMatchQuery> getSubqueries();
}
