package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.utils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import se.liu.ida.hefquin.engine.federation.GraphQLEndpoint;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.GraphQL2RDFConfiguration;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.GraphQLField;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLFieldType;

/**
 * Provides helper functions that focuses on conversions from URIs to GraphQL data 
 * that is then used at a GraphQLEndpoint.
 */
public class URI2GraphQLHelper {

    /**
     * Takes a class URI ( @param uri ) and checks that its valid before getting the GraphQL type name 
     * from it using @param config, then checks if the name matches a GraphQL object type in @param endpoint
     */
    public static boolean containsClassURI(final String uri, final GraphQL2RDFConfiguration config, 
            final GraphQLEndpoint endpoint){
        if(config.isValidClassURI(uri)){
            final String typeName = config.mapClassToType(uri);
            return endpoint.containsGraphQLObjectType(typeName);
        }
        return false;
    }

    /**
     * Takes a property URI ( @param uri ) and checks that its valid before getting the GraphQL type and field name
     * using @param config, then checks whether they matches a field for a GraphQL object type in @param endpoint
     */
    public static boolean containsPropertyURI(final String uri, final GraphQL2RDFConfiguration config, 
            final GraphQLEndpoint endpoint){

        if(!config.isValidPropertyURI(uri)){
            return false;
        }

        final String typeName = config.mapPropertyToType(uri);
        final String fieldName = config.mapPropertyToField(uri);

        return endpoint.containsGraphQLField(typeName, fieldName);
    }

    /**
     * Takes @param objectTypeName and retrieves all fields for the GraphQL object type of that name in 
     * @param endpoint that matches the @param fieldType. Then, by using @param config creates a 
     * corresponding property URI for each field fetched. @return the set of all property URIs created this way.
     */
    public static Set<String> getPropertyURIs(final String objectTypeName, final GraphQLFieldType fieldType, 
            final GraphQL2RDFConfiguration config, final GraphQLEndpoint endpoint){
                
        final Set<String> propertyURIs = new HashSet<>();
        if(endpoint.containsGraphQLObjectType(objectTypeName)){
            final Map<String,GraphQLField> fields = endpoint.getGraphQLObjectFields(objectTypeName);
            for(final String fieldName : fields.keySet()){
                if(endpoint.getGraphQLFieldType(objectTypeName, fieldName) == fieldType){
                    propertyURIs.add(config.mapFieldToProperty(objectTypeName, fieldName));
                }
            }
        }
        return propertyURIs;
    }
}
