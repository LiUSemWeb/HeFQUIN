package se.liu.ida.hefquin.engine.wrappers.graphql.data;

import se.liu.ida.hefquin.engine.wrappers.graphql.data.impl.GraphQLFieldType;

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
     * Note: does not include List or Non-nullable identifiers in the
     * type information.
     */
    public String getValueType();

    /**
     * @return a GraphQLFieldType enum describing if the field
     * fetches scalar value(s) or object(s).
     */
    public GraphQLFieldType getFieldType();
}
