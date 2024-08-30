package se.liu.ida.hefquin.engine.wrappers.graphql.data.impl;

/**
 * Used to represent whether a field in a GraphQL object type returns
 * an object / list of objects OR a scalar value / list of scalar values.
 */
public enum GraphQLFieldType {
    SCALAR,
    OBJECT
}
