package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data;

import java.util.TreeMap;

public interface GraphQLEntrypoint {
    /**
     * @return the GraphQL query fieldname
     */
    public String getFieldName();

    /**
     * @return the parameter definitions for the entrypoint as a map
     * consisting of parameternames mapped to parameter value types.
     */
    public TreeMap<String,String> getParameterDefinitions();

    /**
     * @return the sgp type of the entrypoint
     */
    public String getType();
}
