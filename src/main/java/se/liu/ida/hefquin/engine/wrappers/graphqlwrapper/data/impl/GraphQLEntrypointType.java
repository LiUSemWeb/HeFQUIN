package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl;

/**
 * Represents whether a GraphQLEntrypoint returns a single object,
 * a list of filtered objects or a list of all objects.
 */
public enum GraphQLEntrypointType {
    SINGLE,
    FILTERED,
    FULL
}
