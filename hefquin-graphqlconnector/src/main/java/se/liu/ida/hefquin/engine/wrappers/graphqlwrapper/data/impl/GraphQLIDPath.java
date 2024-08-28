package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl;

import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.GraphQLFieldPath;

/**
 * GraphQLFieldPath segment used to represent the "id" field for objects in a GraphQL query
 */
public class GraphQLIDPath implements GraphQLFieldPath {
    
    protected final String type;
    protected final String prefix;

    public GraphQLIDPath(final String type, final String prefix){
        assert type != null;
        assert prefix != null;
        this.type = type;
        this.prefix = prefix;
    }

    @Override
    public String toString() {
        return prefix + type + ":id";
    }
}
