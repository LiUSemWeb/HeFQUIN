package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.impl;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jena.vocabulary.RDF;

import se.liu.ida.hefquin.engine.federation.GraphQLEndpoint;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.GraphQL2RDFConfiguration;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.GraphQLProperty;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLFieldType;

public class DefaultGraphQL2RDFConfiguration implements GraphQL2RDFConfiguration {

    protected final String classPrefix;
    protected final String propertyPrefix;
    protected final String connectingText = "_of_";
    
    public DefaultGraphQL2RDFConfiguration(){
        this.classPrefix = "http://example.org/c/";
        this.propertyPrefix = "http://example.org/p/";
    }

    public DefaultGraphQL2RDFConfiguration(final String classPrefix, final String propertyPrefix){
        this.classPrefix = classPrefix;
        this.propertyPrefix = propertyPrefix;
    }

    @Override
    public boolean containsClassURI(final String uri, final GraphQLEndpoint endpoint) {
        if(uri.startsWith(classPrefix)){
            final String className = uri.substring(classPrefix.length());
            return endpoint.containsClass(className);
        }
        return false;
    }

    @Override
    public boolean containsPropertyURI(final String uri, final GraphQLEndpoint endpoint) {
        final String removedPrefix = removePropertyPrefix(uri);
        if(removedPrefix == null){
            return false;
        }

        final String propertyName = removePropertySuffix(removedPrefix);
        if(propertyName == null){
            return false;
        }

        final String className = getClassFromPropertyURI(uri);
        if(className == null){
            return false;
        }

        return endpoint.containsProperty(className, propertyName);
    }

    @Override
    public Set<String> getPropertyURIs(final String className, final GraphQLFieldType fieldType, 
            final GraphQLEndpoint endpoint) {
        final Set<String> propertyURIs = new HashSet<>();
        if(endpoint.containsClass(className)){
            final Map<String,GraphQLProperty> properties = endpoint.getClassProperties(className);
            for(final String propertyName : properties.keySet()){
                if(endpoint.getPropertyFieldType(className, propertyName) == fieldType){
                    final StringBuilder propertyURI = new StringBuilder();
                    propertyURI.append(propertyPrefix);
                    propertyURI.append(propertyName);
                    propertyURI.append(connectingText);
                    propertyURI.append(className);
                    propertyURIs.add(propertyURI.toString());
                }
            }
        }
        return propertyURIs;
    }

    @Override
    public String getPropertyPrefix() {
        return propertyPrefix;
    }

    @Override
    public String getClassPrefix() {
        return classPrefix;
    }

    @Override
    public String getClassMembershipURI(){
        return RDF.type.getURI();
    }

    @Override
    public String getClassFromPropertyURI(final String uri){
        final int splitIndex = uri.lastIndexOf(connectingText);
        return splitIndex > 0 ? uri.substring(splitIndex + 4) : null;
    }

    @Override
    public String removePropertyPrefix(final String uri){
        final int splitIndex = propertyPrefix.length();
        return uri.startsWith(propertyPrefix) ? uri.substring(splitIndex) : null;
    }

    @Override
    public String removePropertySuffix(final String uri){
        final int splitIndex = uri.lastIndexOf(connectingText);
        return splitIndex > 0 ? uri.substring(0, splitIndex) : null;
    }
}
