package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper;

import se.liu.ida.hefquin.engine.federation.GraphQLEndpoint;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessException;

/**
 * Used to initialize a GraphQL endpoint
 */
public interface GraphQLEndpointInitializer {
    
    public GraphQLEndpoint initializeEndpoint(final String url) throws FederationAccessException;
}
