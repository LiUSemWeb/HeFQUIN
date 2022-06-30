package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl;

import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.GraphQLFieldPath;

/**
 * GraphQLFieldPath segment used to represent GraphQL scalar values in the query.
 */
public class GraphQLScalarPath implements GraphQLFieldPath {

    protected final String fieldName;

    public GraphQLScalarPath(final String fieldName){
        assert fieldName != null;
        this.fieldName = fieldName;
    }

    @Override
    public String toString() {
        return "scalar_" + fieldName + ":" + fieldName;
    }
}
