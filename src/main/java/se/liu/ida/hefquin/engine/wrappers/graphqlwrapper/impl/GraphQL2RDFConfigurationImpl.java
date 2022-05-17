package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.impl;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.GraphQL2RDFConfiguration;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.GraphQLEntrypoint;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.GraphQLProperty;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLEntrypointType;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLFieldType;

public class GraphQL2RDFConfigurationImpl implements GraphQL2RDFConfiguration {

    // Maps classnames to a map which in turn maps property names to a GraphQLProperty object
    protected final Map<String, Map<String,GraphQLProperty>> classToProperty;

    // Maps classnames to a map which in turn maps a GraphQLEntrypointType to a GraphQLEntrypoint
    protected final Map<String, Map<GraphQLEntrypointType,GraphQLEntrypoint>> classToEntrypoint;

    // The URI prefixes used by the virtual RDF view
    protected final String classPrefix;
    protected final String propertyPrefix;
    protected final String rdfPrefix = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    protected final String connectingText = "_of_";
    

    public GraphQL2RDFConfigurationImpl(final Map<String,Map<String,GraphQLProperty>> classToProperty, 
            final Map<String,Map<GraphQLEntrypointType,GraphQLEntrypoint>> classToEntrypoint){
        this.classToProperty = classToProperty;
        this.classToEntrypoint = classToEntrypoint;
        this.classPrefix = "http://example.org/c/";
        this.propertyPrefix = "http://example.org/p/";
    }

    public GraphQL2RDFConfigurationImpl(final Map<String,Map<String,GraphQLProperty>> classToProperty, 
            final Map<String,Map<GraphQLEntrypointType,GraphQLEntrypoint>> classToEntrypoint,
            final String classPrefix, final String propertyPrefix){
        this.classToProperty = classToProperty;
        this.classToEntrypoint = classToEntrypoint;
        this.classPrefix = classPrefix;
        this.propertyPrefix = propertyPrefix;
    }

    @Override
    public boolean containsClass(final String className) {
        return classToProperty.containsKey(className);
    }

    @Override
    public boolean containsClassURI(final String uri) {
        if(uri.startsWith(classPrefix)){
            final String className = uri.substring(classPrefix.length());
            return classToProperty.containsKey(className);
        }
        return false;
    }

    @Override
    public boolean containsProperty(final String className, final String propertyName) {
        if(containsClass(className)){
            return classToProperty.get(className).containsKey(propertyName);
        }
        return false;
    }

    @Override
    public boolean containsPropertyURI(final String uri) {
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

        return containsProperty(className, propertyName);
    }

    @Override
    public GraphQLFieldType getPropertyFieldType(final String className, final String propertyName) {
        if(containsClass(className) && containsProperty(className, propertyName)){
            return classToProperty.get(className).get(propertyName).getFieldType();
        }
        return null;
    }

    @Override
    public String getPropertyValueType(final String className, final String propertyName) {
        if(containsClass(className) && containsProperty(className, propertyName)){
            return classToProperty.get(className).get(propertyName).getValueType();
        }
        return null;
    }

    @Override
    public Set<String> getPropertyURIs(final String className, final GraphQLFieldType fieldType) {
        final Set<String> propertyURIs = new HashSet<>();
        if(containsClass(className)){
            final Map<String,GraphQLProperty> properties = classToProperty.get(className);
            for(final String propertyName : properties.keySet()){
                if(getPropertyFieldType(className, propertyName) == fieldType){
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
    public Set<String> getClasses() {
        return classToProperty.keySet();
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
    public String getRDFPrefix(){
        return rdfPrefix;
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

    @Override
    public GraphQLEntrypoint getEntrypoint(final String className, final GraphQLEntrypointType type) {
        if(containsClass(className) && classToEntrypoint.get(className).containsKey(type)){
            return classToEntrypoint.get(className).get(type);
        }
        return null;
    }
}
