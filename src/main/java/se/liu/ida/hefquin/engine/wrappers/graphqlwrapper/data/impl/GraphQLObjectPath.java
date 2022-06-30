package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl;

import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.GraphQLFieldPath;

/**
 * GraphQLFieldPath segment used to represent GraphQL object types in the query.
 */
public class GraphQLObjectPath implements GraphQLFieldPath {

    protected final String fieldName;

    public GraphQLObjectPath(final String fieldName){
        assert fieldName != null;
        this.fieldName = fieldName;
    }

    @Override
    public String toString() {
        return "object_" + fieldName + ":" + fieldName + "/";
    }
}
