package se.liu.ida.hefquin.engine.wrappers.graphql.data.impl;

import se.liu.ida.hefquin.engine.wrappers.graphql.data.GraphQLFieldPath;

/**
 * GraphQLFieldPath segment used to represent GraphQL scalar values in the query.
 */
public class GraphQLScalarPath implements GraphQLFieldPath {

    protected final String fieldName;
    protected final String prefix;

    public GraphQLScalarPath(final String fieldName, final String prefix){
        assert fieldName != null;
        assert prefix != null;
        this.fieldName = fieldName;
        this.prefix = prefix;
    }

    @Override
    public String toString() {
        return prefix + fieldName + ":" + fieldName;
    }
}
