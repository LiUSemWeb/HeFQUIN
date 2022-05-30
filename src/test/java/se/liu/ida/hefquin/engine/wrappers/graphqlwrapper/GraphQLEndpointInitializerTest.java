package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import se.liu.ida.hefquin.engine.EngineTestBase;
import se.liu.ida.hefquin.engine.federation.GraphQLEndpoint;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessException;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.impl.GraphQLEndpointInitializerImpl;

public class GraphQLEndpointInitializerTest extends EngineTestBase {
    
    final GraphQLEndpointInitializer initializer = new GraphQLEndpointInitializerImpl();

    @Test
    public void testEndpointSetup(){
        //if(!skipLocalGraphQLTests){
        if(skipLocalGraphQLTests){
            try{
                final GraphQLEndpoint e = initializer.initializeEndpoint("http://localhost:4000/graphql");
                System.out.print(e.getGraphQLObjectTypes().toString());
                assert(!e.getGraphQLObjectTypes().isEmpty());
            }
            catch(final FederationAccessException e){
                e.printStackTrace();
            }
        }
    }
}
