package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper;

/**
 * Used to represent a URI configuration.
 */
public interface GraphQL2RDFConfiguration {

    /**
     * @return the prefix used for property URIs.
     * A property URI is defined by its prefix followed by a property name,
     * a connecting text and finally a class name of which that property belongs to.
     * Example: "http://example.org/p/book_of_Author"
     */
    public String getPropertyPrefix();

    /**
     * @return the prefix used for class URIs.
     * A class URI is defined by its prefix followed by a class name.
     * Example: "http://example.org/c/Author"
     */
    public String getClassPrefix();

    /**
     * @return the text that connects the property name and the class name in a property URI.
     * Example: _of_
     */
    public String getConnectingText();

    /**
     * @return a URI that signify a membership relation. 
     * Example: https://www.w3.org/1999/02/22-rdf-syntax-ns#type
     */
    public String getClassMembershipURI();

    /**
     * @return the class name associated with the property URI: @param uri
     * Otherwise return null if unable (invalid property URI)
     */
    public String getClassFromPropertyURI(final String uri);

    /**
     * Function to remove the prefix from the property URI: @param uri
     * Otherwise return null if unable (invalid property URI)
     */
    public String removePropertyPrefix(final String uri);

    /**
     * Function to remove the suffix (connecting text and class name) 
     * from the property URI: @param uri.
     * Otherwise return null if unable (invalid property URI)
     */
    public String removePropertySuffix(final String uri);
}
