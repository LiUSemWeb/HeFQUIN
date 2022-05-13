package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data;

import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLFieldType;

/**
 * Used to represent information about a field in a GraphQL object
 */
public interface GraphQLProperty {
    
    /**
     * @return the name of the property
     */
    public String getName();

    /**
     * @return the GraphQL value type of the property
     */
    public String getValueType();

    /**
     * @return the GraphQLFieldType of the property
     */
    public GraphQLFieldType getFieldType();
}
