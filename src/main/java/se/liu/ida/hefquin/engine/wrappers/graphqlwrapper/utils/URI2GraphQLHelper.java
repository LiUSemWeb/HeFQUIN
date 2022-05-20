package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.utils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import se.liu.ida.hefquin.engine.federation.GraphQLEndpoint;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.GraphQL2RDFConfiguration;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.GraphQLField;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLFieldType;

/**
 * Provides helper functions that focuses on conversions from URIs to GraphQL data and vice versa.
 */
public class URI2GraphQLHelper {

    /**
     * Takes a class URI ( @param uri ) and extracts the class name by using information 
     * from @param config , then checks if the name matches a GraphQL object type in @param endpoint
     */
    public static boolean containsClassURI(final String uri, final GraphQL2RDFConfiguration config, 
            final GraphQLEndpoint endpoint){

        final String classPrefix = config.getClassPrefix();
        if(uri.startsWith(classPrefix)){
            final String className = uri.substring(classPrefix.length());
            return endpoint.containsGraphQLObjectType(className);
        }
        return false;
    }

    /**
     * Takes a property URI ( @param uri ) and extracts the property and class names 
     * using @param config. A check whether the property name matches a field for a GraphQL object type
     * in @param endpoint is then performed (where the class name is assumed to be the GraphQL object type name).
     */
    public static boolean containsPropertyURI(final String uri, final GraphQL2RDFConfiguration config, 
            final GraphQLEndpoint endpoint){

        final String removedPrefix = config.removePropertyPrefix(uri);
        if(removedPrefix == null){
            return false;
        }

        final String propertyName = config.removePropertySuffix(removedPrefix);
        if(propertyName == null){
            return false;
        }

        final String className = config.getClassFromPropertyURI(uri);
        if(className == null){
            return false;
        }

        return endpoint.containsGraphQLField(className, propertyName);
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
                    final StringBuilder propertyURI = new StringBuilder();
                    propertyURI.append(config.getPropertyPrefix());
                    propertyURI.append(fieldName);
                    propertyURI.append(config.getConnectingText());
                    propertyURI.append(objectTypeName);
                    propertyURIs.add(propertyURI.toString());
                }
            }
        }
        return propertyURIs;
    }
}
