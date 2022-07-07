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

    /**
     * Verifies that @param uri is valid
     * (Has correct class prefix)
     */
    public boolean isValidClassURI(final String uri);

    /**
     * Verifies that @param uri is valid
     * (Has correct property prefix, and connecting text)
     */
    public boolean isValidPropertyURI(final String uri);

    /**
     * Verifies that @param uri is valid
     * (Has correct membership uri)
     */
    public boolean isValidMembershipURI(final String uri);

    /**
     * Maps an rdfs class uri to a GraphQL type
     * Return null if URI is invalid (not a class URI)
     */
    public String mapClassToType(final String uri);

    /**
     * Maps a GraphQL type to an rdfs class uri
     */
    public String mapTypeToClass(final String type);

    /**
     * Maps an rdf property URI to a GraphQL field
     * Return null if URI is invalid (not a property URI)
     */
    public String mapPropertyToField(final String uri);

    /**
     * Maps an rdf property URI to a GraphQL type
     * Return null if URI is invalid (not a property URI)
     */
    public String mapPropertyToType(final String uri);

    /**
     * Maps a GraphQL object type and field to an rdf property URI
     */
    public String mapFieldToProperty(final String type, final String field);

    /**
     * @return the prefix used by the id key in the expected json.
     */
    public String getJsonIDKeyPrefix();

    /**
     * @return the prefix used by object keys in the expected json.
     */
    public String getJsonObjectKeyPrefix();

    /**
     * @return the prefix used by scalar keys in the expected json.
     */
    public String getJsonScalarKeyPrefix();
}
