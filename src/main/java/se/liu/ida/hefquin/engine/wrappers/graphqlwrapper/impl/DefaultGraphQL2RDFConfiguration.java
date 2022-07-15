package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.impl;

import org.apache.jena.atlas.json.JsonException;
import org.apache.jena.vocabulary.RDF;

import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.GraphQL2RDFConfiguration;

public class DefaultGraphQL2RDFConfiguration implements GraphQL2RDFConfiguration {

    // RDF prefixes and configurations
    protected final String classPrefix;
    protected final String propertyPrefix;
    protected final String connectingText = "_of_";
    protected final String membershipURI = RDF.type.getURI();

    // Prefixes used by the expected JSON keys
    protected final String jsonIDKeyPrefix = "id_";
    protected final String jsonObjectKeyPrefix = "object_";
    protected final String jsonScalarKeyPrefix = "scalar_";
    
    public DefaultGraphQL2RDFConfiguration(){
        this.classPrefix = "http://example.org/c/";
        this.propertyPrefix = "http://example.org/p/";
    }

    public DefaultGraphQL2RDFConfiguration(final String classPrefix, final String propertyPrefix){
        this.classPrefix = classPrefix;
        this.propertyPrefix = propertyPrefix;
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
    public String getConnectingText() {
        return connectingText;
    }

    @Override
    public String getClassMembershipURI(){
        return membershipURI;
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
    public boolean isValidClassURI(final String uri) {
        return uri.startsWith(classPrefix) && uri.length() > classPrefix.length();
    }

    @Override
    public boolean isValidPropertyURI(final String uri) {
        if(!uri.startsWith(propertyPrefix) || uri.length() <= propertyPrefix.length()){
            return false;
        }

        final String uriSubString = uri.substring(propertyPrefix.length());
        final int i = uriSubString.indexOf(connectingText);

        // Checks that connectingText is not at the beginning or the end of the substring
        if(i <= 0 || i+connectingText.length() >= uriSubString.length() ){
            return false;
        }

        return true;
    }

    @Override
    public boolean isValidMembershipURI(final String uri) {
        return uri.equals(membershipURI);
    }

    @Override
    public String mapClassToType(final String uri) {
        if(isValidClassURI(uri)){
            return uri.substring(classPrefix.length());
        }
        return null;
    }

    @Override
    public String mapTypeToClass(final String type) {
        return classPrefix + type;
    }

    @Override
    public String mapPropertyToField(final String uri) {
        if(isValidPropertyURI(uri)){
            final String substr = removePropertyPrefix(uri);
            return removePropertySuffix(substr);
        }
        return null;
    }

    @Override
    public String mapPropertyToType(final String uri) {
        if(isValidPropertyURI(uri)){
            final int splitIndex = uri.lastIndexOf(connectingText);
            return uri.substring(splitIndex + 4);
        }
        return null;
    }

    @Override
    public String mapFieldToProperty(final String type, final String field) {
        return propertyPrefix + field + connectingText + type;
    }

    @Override
    public String getJsonIDKeyPrefix() {
        return jsonIDKeyPrefix;
    }

    @Override
    public String getJsonObjectKeyPrefix() {
        return jsonObjectKeyPrefix;
    }

    @Override
    public String getJsonScalarKeyPrefix() {
        return jsonScalarKeyPrefix;
    }

    @Override
    public String removeJsonKeyPrefix(final String key) {
        if(key.startsWith(jsonIDKeyPrefix)){
            return key.substring(jsonIDKeyPrefix.length());
        }
        else if(key.startsWith(jsonObjectKeyPrefix)){
            return key.substring(jsonObjectKeyPrefix.length());
        }
        else if(key.startsWith(jsonScalarKeyPrefix)){
            return key.substring(jsonScalarKeyPrefix.length());
        }
        else{
            throw new JsonException("The provided key did not have one of the pre-defined prefixes from this configuration.");
        }
    }
}
