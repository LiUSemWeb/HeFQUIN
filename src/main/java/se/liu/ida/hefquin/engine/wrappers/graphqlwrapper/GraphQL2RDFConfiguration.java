package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper;

import java.util.Set;

import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.GraphQLEntrypoint;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLEntrypointType;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLFieldType;

/**
 * Used to represent and retrieve information about a virtual RDF view for a GraphQL endpoint
 */
public interface GraphQL2RDFConfiguration {

    /**
     * Verifies that @param className exists
     */
    public boolean containsClass(final String className);

    /**
     * Verifies that @param uri that correponds to a class exists
     */
    public boolean containsClassURI(final String uri);

    /**
     * Verifies that @param propertyName of @param className exists
     */    
    public boolean containsProperty(final String className, final String propertyName);

    /**
     * Verifies that @param uri that corresponds to a property exists
     */
    public boolean containsPropertyURI(final String uri);

    /**
     * @return the GraphQLFieldType of @param property of @param className ,
     * Otherwise return null if unable to find class or property
     */
    public GraphQLFieldType getPropertyFieldType(final String className, final String propertyName);

    /**
     * @return the value type for @param property of @param className ,
     * Otherwise return null if unable to find class or property
     */
    public String getPropertyValueType(final String className, final String propertyName);

    /**
     * @return a set of URI of all properties for @param className that links to a scalar value
     */
    public Set<String> getPropertyURIs(final String className, final GraphQLFieldType fieldType);

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
     * @return the prefix used for rdf
     */
    public String getRDFPrefix();

    /**
     * @return the class associated with the property URI
     * Otherwise return null if unable
     */
    public String getClassFromPropertyURI(final String uri);

    /**
     * Helper function to remove the prefix from @param uri
     * Otherwise return null if unable
     */
    public String removePropertyPrefix(final String uri);

    /**
     * Helper function to remove the suffix from @param uri
     * Otherwise return null if unable
     */
    public String removePropertySuffix(final String uri);

    /**
     * @return a GraphQLEntrypoint for @param className where @param type is used to select an entrypoint
     * for the chosen class. Otherwise return null if the class doesn't exist or if the GraphQLEntrypoint of 
     * the given type isn't mapped to anything.
     */
    public GraphQLEntrypoint getEntrypoint(final String className, final GraphQLEntrypointType type);  
}
