package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.query;

import org.apache.jena.atlas.json.JsonValue;

import se.liu.ida.hefquin.engine.query.Query;

/**
 * Represents a query for GraphQL
 */
public interface GraphQLQuery extends Query
{
    /**
     * Adds @param fieldPath to the query.
     * Fieldpath includes aliasing and subfields are seperated by '/'
     */
    public void addFieldPath(String fieldPath);

    /**
     * Adds a variable parameter to the query. 
	 * @param parameterName within fieldpaths are exchanged for @param parameterValue
	 * @param graphqlType is the GraphQL type the variable represents.
     */
    public void addParameter(String parameterName, JsonValue parameterValue, String graphqlType);

    /**
     * @return a string url representation of the query
     */
    public String getURL();
}
