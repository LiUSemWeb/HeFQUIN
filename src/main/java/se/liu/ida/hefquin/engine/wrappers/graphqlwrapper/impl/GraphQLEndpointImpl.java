package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.impl;

import java.util.Map;
import java.util.Set;

import se.liu.ida.hefquin.engine.data.VocabularyMapping;
import se.liu.ida.hefquin.engine.federation.GraphQLEndpoint;
import se.liu.ida.hefquin.engine.federation.access.GraphQLInterface;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.GraphQLEntrypoint;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.GraphQLProperty;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLEntrypointType;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLFieldType;

public class GraphQLEndpointImpl implements GraphQLEndpoint {

    // Maps classnames to a map which in turn maps property names to a GraphQLProperty object
    protected final Map<String, Map<String,GraphQLProperty>> classToProperty;

    // Maps classnames to a map which in turn maps a GraphQLEntrypointType to a GraphQLEntrypoint
    protected final Map<String, Map<GraphQLEntrypointType,GraphQLEntrypoint>> classToEntrypoint;

    // GraphQL Interface
    protected final GraphQLInterface graphqlInterface;

    public GraphQLEndpointImpl(final Map<String, Map<String,GraphQLProperty>> classToProperty,
            final Map<String, Map<GraphQLEntrypointType,GraphQLEntrypoint>> classToEntrypoint, 
            final GraphQLInterface graphqlInterface){
        this.classToProperty = classToProperty;
        this.classToEntrypoint = classToEntrypoint;
        this.graphqlInterface = graphqlInterface;
    }

    @Override
    public VocabularyMapping getVocabularyMapping() {
        return null;
    }

    @Override
    public GraphQLInterface getInterface() {
        return graphqlInterface;
    }

    @Override
    public boolean containsClass(final String className) {
        return classToProperty.containsKey(className);
    }

    @Override
    public boolean containsProperty(final String className, final String propertyName) {
        if(containsClass(className)){
            return classToProperty.get(className).containsKey(propertyName);
        }
        return false;
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
    public Set<String> getClasses() {
        return classToProperty.keySet();
    }

    @Override
    public GraphQLEntrypoint getEntrypoint(final String className, final GraphQLEntrypointType type) {
        if(containsClass(className) && classToEntrypoint.get(className).containsKey(type)){
            return classToEntrypoint.get(className).get(type);
        }
        return null;
    }

    @Override
    public Map<String, GraphQLProperty> getClassProperties(final String className) {
        if(containsClass(className)){
            return classToProperty.get(className);
        }
        return null;
    }
}
