package se.liu.ida.hefquin.engine.wrappers.graphql;

import org.apache.jena.atlas.json.io.parserjavacc.javacc.ParseException;

import se.liu.ida.hefquin.engine.wrappers.graphql.data.GraphQLSchema;

/**
 * Used to initialize a GraphQL schema from a GraphQL endpoint
 */
public interface GraphQLSchemaInitializer {
    
    public GraphQLSchema initializeSchema(  final String url,
                                            final int connectionTimeout,
                                            final int readTimeout ) 
                                            throws GraphQLException,ParseException;
}
