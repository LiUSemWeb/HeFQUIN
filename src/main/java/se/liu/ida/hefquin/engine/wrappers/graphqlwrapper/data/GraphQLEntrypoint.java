package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data;

import java.util.Map;

/**
 * Used to represent and contain information about a specific field
 * in the GraphQL query type
 */
public interface GraphQLEntrypoint {
    /**
     * @return the GraphQL query fieldname
     */
    public String getFieldName();

    /**
     * @return the parameter definitions for the entrypoint as a map
     * consisting of parameter names mapped to parameter value types.
     */
    public Map<String,String> getParameterDefinitions();

    /**
     * @return the GraphQL type the entrypoint represents
     */
    public String getType();
}
