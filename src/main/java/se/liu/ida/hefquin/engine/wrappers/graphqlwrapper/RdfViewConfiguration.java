package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper;

import java.util.Set;

import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.GraphQLEntrypoint;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLEntrypointType;

/**
 * Used to represent and retrieve information about a virtual RDF view for a GraphQL endpoint
 */
public interface RdfViewConfiguration {

    /**
     * Verify that @param className exist
     */
    public boolean containsClass(String className);

    /**
     * Verify that @param uri that correponds to a class exists
     */
    public boolean containsClassURI(String uri);

    /**
     * Verify that @param propertyName of @param className exist
     */    
    public boolean containsProperty(String className, String propertyName);

    /**
     * Verify that @param uri that corresponds to a property exist for @param className
     */
    public boolean containsPropertyURI(String className, String uri);

    /**
     * Verify that @param property of @param className links to a nested object
     */
    public boolean isObjectProperty(String className, String propertyName);

    /**
     * @return a set of URI of all properties for @param className that links to a nested object
     */
    public Set<String> getObjectURIs(String className);

    /**
     * @return a set of URI of all properties for @param className that links to a scalar value
     */
    public Set<String> getScalarURIs(String className);

    /**
     * @return the value type for @param property of @param className 
     */
    public String getPropertyValueType(String className, String propertyName);

    /**
     * @return a set with the names of all the defined classes
     */
    public Set<String> getClasses();

    /**
     * @return the prefix used for properties
     */
    public String getPropertyPrefix();

    /**
     * @return the prefix used for classes
     */
    public String getClassPrefix();

    /**
     * @return a GraphQLEntrypoint for @param className where @param type is used to select an entrypoint
     * for the chosen class
     */
    public GraphQLEntrypoint getEntrypoint(String className, GraphQLEntrypointType type);  
}
