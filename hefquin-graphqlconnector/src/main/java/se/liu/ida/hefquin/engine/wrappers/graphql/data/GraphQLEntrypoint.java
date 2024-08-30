package se.liu.ida.hefquin.engine.wrappers.graphql.data;

import java.util.Map;

/**
 * Used to represent and contain information about a specific field
 * in the GraphQL "query" type
 */
public interface GraphQLEntrypoint {
    /**
     * @return the query field name
     */
    public String getFieldName();

    /**
     * @return the argument definitions for the entrypoint as a map
     * consisting of argument names mapped to GraphQL value types. 
     * (argument value types can include non-null exclamations)
     */
    public Map<String,String> getArgumentDefinitions();

    /**
     * @return the name of the GraphQL object type that the entrypoint field fetches.
     */
    public String getTypeName();

    /**
     * @return an aliased version of the entrypoint where @param counter is used
     * to make the entrypoint alias unique.
     */
    public String getEntrypointAlias(int counter);
}
