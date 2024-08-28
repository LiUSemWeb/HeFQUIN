package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl;

/**
 * Represents whether a GraphQLEntrypoint (a field in the query type) returns 
 * a single object, a list of filtered objects or a full list of all objects (of a certain type).
 */
public enum GraphQLEntrypointType {
    SINGLE,
    FILTERED,
    FULL
}
