package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.query;

import org.apache.jena.atlas.json.JsonObject;

import se.liu.ida.hefquin.engine.query.Query;

/**
 * Represents a query for GraphQL
 */
public interface GraphQLQuery extends Query
{
    /**
     * @return a string version of the query
     */
    public String toString();

    /**
     * @return a jsonobject with the parameter values for the query
     */
    public JsonObject getParameterValues();
}
