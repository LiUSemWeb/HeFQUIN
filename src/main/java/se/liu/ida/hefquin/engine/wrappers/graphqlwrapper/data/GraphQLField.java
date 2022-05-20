package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data;

import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLFieldType;

/**
 * Used to represent information about a field in a GraphQL object type
 */
public interface GraphQLField {
    
    /**
     * @return the name of the field
     */
    public String getName();

    /**
     * @return the GraphQL value type of the field (String,Int,... etc.)
     */
    public String getValueType();

    /**
     * @return a GraphQLFieldType enum describing if the field
     * fetches scalar value(s) or object(s).
     */
    public GraphQLFieldType getFieldType();
}
