package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper;

import org.apache.jena.atlas.json.io.parserjavacc.javacc.ParseException;

import se.liu.ida.hefquin.engine.federation.GraphQLEndpoint;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessException;

/**
 * Used to initialize a GraphQL endpoint
 */
public interface GraphQLEndpointInitializer {
    
    public GraphQLEndpoint initializeEndpoint(  final String url,
                                                final int connectionTimeout,
                                                final int readTimeout ) 
                                                throws FederationAccessException,ParseException;
}
