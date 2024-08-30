package se.liu.ida.hefquin.engine.wrappers.graphql.data.impl;

import se.liu.ida.hefquin.engine.wrappers.graphql.data.GraphQLFieldPath;

/**
 * GraphQLFieldPath segment used to represent GraphQL object types in the query.
 */
public class GraphQLObjectPath implements GraphQLFieldPath {

    protected final String fieldName;
    protected final String prefix;

    public GraphQLObjectPath(final String fieldName, final String prefix){
        assert fieldName != null;
        assert prefix != null;
        this.fieldName = fieldName;
        this.prefix = prefix;
    }

    @Override
    public String toString() {
        return prefix + fieldName + ":" + fieldName + "/";
    }
}
