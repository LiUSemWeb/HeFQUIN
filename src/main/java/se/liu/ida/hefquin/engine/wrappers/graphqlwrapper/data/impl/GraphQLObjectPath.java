package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl;

import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.GraphQLFieldPath;

/**
 * GraphQLFieldPath segment used to represent GraphQL object types in the query.
 */
public class GraphQLObjectPath implements GraphQLFieldPath {

    protected final String alias;
    protected final String fieldName;

    public GraphQLObjectPath(final String alias, final String fieldName){
        assert alias != null;
        assert fieldName != null;
        this.alias = alias;
        this.fieldName = fieldName;
    }

    @Override
    public String toString() {
        return "object_" + alias + ":" + fieldName + "/";
    }
}
