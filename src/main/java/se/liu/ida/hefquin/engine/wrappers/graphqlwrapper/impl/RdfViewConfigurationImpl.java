package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.impl;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.RdfViewConfiguration;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.GraphQLEntrypoint;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.GraphQLViewProperty;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLEntrypointType;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLFieldType;

public class RdfViewConfigurationImpl implements RdfViewConfiguration {

    // Maps classnames to a map which in turn maps property names to a GraphQLViewProperty object
    protected final Map<String, Map<String,GraphQLViewProperty>> classToProperty;

    // Maps classnames to a map which in turn maps a GraphQLEntrypointType to a GraphQLEntrypoint
    protected final Map<String, Map<GraphQLEntrypointType,GraphQLEntrypoint>> classToEntrypoint;

    // The URI prefixes used by the virtual RDF view
    protected final String classPrefix = "c:";
    protected final String propertyPrefix = "p:";
    

    public RdfViewConfigurationImpl(final Map<String,Map<String,GraphQLViewProperty>> classToProperty, 
            final Map<String,Map<GraphQLEntrypointType,GraphQLEntrypoint>> classToEntrypoint){
        this.classToProperty = classToProperty;
        this.classToEntrypoint = classToEntrypoint;
    }

    @Override
    public boolean containsClass(String className) {
        return classToProperty.containsKey(className);
    }

    @Override
    public boolean containsClassURI(String uri) {
        if(uri.startsWith(classPrefix)){
            final String className = uri.substring(classPrefix.length());
            return classToProperty.containsKey(className);
        }
        return false;
    }

    @Override
    public boolean containsProperty(String className, String propertyName) {
        if(containsClass(className)){
            return classToProperty.get(className).containsKey(propertyName);
        }
        return false;
    }

    @Override
    public boolean containsPropertyURI(String className, String uri) {
        if(containsClass(className)){
            if(uri.startsWith(propertyPrefix)){
                final String withoutPrefix = uri.substring(propertyPrefix.length());
                final int suffixIndex = withoutPrefix.lastIndexOf("_of_");
                if(suffixIndex > 0){
                    final String propertyName = withoutPrefix.substring(0, suffixIndex);
                    return containsProperty(className, propertyName);
                }
            }
        }
        return false;
    }

    @Override
    public boolean isObjectProperty(String className, String propertyName) {
        if(containsClass(className) && containsProperty(className, propertyName)){
            GraphQLFieldType type = classToProperty.get(className).get(propertyName).getFieldType();
            return type.equals(GraphQLFieldType.OBJECT);
        }
        return false;
    }

    @Override
    public final Set<String> getObjectURIs(String className) {
        Set<String> objectURIs = new HashSet<>();
        if(containsClass(className)){
            Map<String,GraphQLViewProperty> properties = classToProperty.get(className);
            for(String propertyName : properties.keySet()){
                if(isObjectProperty(className, propertyName)){
                    objectURIs.add(propertyPrefix + propertyName + "_of_" + className);
                }
            }
        }
        return objectURIs;
    }

    @Override
    public final Set<String> getScalarURIs(String className) {
        Set<String> scalarURIs = new HashSet<>();
        if(containsClass(className)){
            Map<String,GraphQLViewProperty> properties = classToProperty.get(className);
            for(String propertyName : properties.keySet()){
                if(!isObjectProperty(className, propertyName)){
                    scalarURIs.add(propertyPrefix + propertyName + "_of_" + className);
                }
            }
        }
        return scalarURIs;
    }

    @Override
    public final String getPropertyValueType(String className, String propertyName) {
        if(containsClass(className) && containsProperty(className, propertyName)){
            return classToProperty.get(className).get(propertyName).getValueType();
        }
        return null;
    }

    @Override
    public final Set<String> getClasses() {
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
    public final GraphQLEntrypoint getEntrypoint(String className, GraphQLEntrypointType type) {
        if(containsClass(className) && classToEntrypoint.get(className).containsKey(type)){
            return classToEntrypoint.get(className).get(type);
        }
        return null;
    }

}
