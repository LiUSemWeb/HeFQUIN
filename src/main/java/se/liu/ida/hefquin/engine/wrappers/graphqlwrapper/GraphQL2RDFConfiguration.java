package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper;

import java.util.Set;

import se.liu.ida.hefquin.engine.federation.GraphQLEndpoint;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLFieldType;

/**
 * Used to represent and retrieve information about a virtual RDF view for a GraphQL endpoint
 */
public interface GraphQL2RDFConfiguration {

    /**
     * Verifies that @param uri that correponds to a class exists for @param endpoint
     */
    public boolean containsClassURI(final String uri, final GraphQLEndpoint endpoint);

    /**
     * Verifies that @param uri that corresponds to a property exists for @param endpoint
     */
    public boolean containsPropertyURI(final String uri, final GraphQLEndpoint endpoint);

    /**
     * @return a set of URI of all properties for @param className that links to a scalar value
     * for @param endpoint
     */
    public Set<String> getPropertyURIs(final String className, final GraphQLFieldType fieldType, 
        final GraphQLEndpoint endpoint);

    /**
     * @return the prefix used for properties
     */
    public String getPropertyPrefix();

    /**
     * @return the prefix used for classes
     */
    public String getClassPrefix();

    /**
     * @return the rdf:type URI
     */
    public String getClassMembershipURI();

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
}
