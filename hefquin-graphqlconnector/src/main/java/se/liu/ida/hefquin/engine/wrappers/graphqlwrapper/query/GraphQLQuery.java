package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.query;

import java.util.Map;
import java.util.Set;

import org.apache.jena.atlas.json.JsonObject;

import se.liu.ida.hefquin.engine.query.Query;

/**
 * Represents a GraphQL query
 */
public interface GraphQLQuery extends Query
{
    /**
     * @return a string version of the query
     */
    public String toString();

    /**
     * @return a Set with the fieldpaths for each field in the query 
     * (objects seperated by /)
     */
    public Set<String> getFieldPaths();

    /**
     * @return a jsonobject with the argument names to argument values for the query
     */
    public JsonObject getArgumentValues();

    /**
     * @return a mapping of argument names to argument types for the query.
     */
    public Map<String,String> getArgumentDefinitions();

    
}
