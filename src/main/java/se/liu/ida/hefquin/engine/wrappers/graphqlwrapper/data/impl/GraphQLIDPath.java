package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl;

import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.GraphQLFieldPath;

/**
 * GraphQLFieldPath segment used to represent the "id" field for objects in a GraphQL query
 */
public class GraphQLIDPath implements GraphQLFieldPath {
    
    protected final String type;

    public GraphQLIDPath(final String type){
        assert type != null;
        this.type = type;
    }

    @Override
    public String toString() {
        return "id_" + type + ":id";
    }
}
