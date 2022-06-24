package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data;

import org.apache.jena.atlas.json.JsonValue;

/**
 * Represents an individual argument for a GraphQL query
 */
public interface GraphQLArgument {

    /**
     * Get the variable name used for the argument
     */
    public String getVariableName();

    /**
     * Get the name of the argument
     */
    public String getArgName();

    /**
     * @return the value used for the argument
     */
    public JsonValue getArgValue();

    /**
     * @return the definition (GraphQL object type name) for the argument
     */
    public String getArgDefinition();

    /**
     * @return a fieldpath string version of the argument
     */
    public String toString();
}
