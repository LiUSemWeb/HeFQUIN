package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.impl;

import org.apache.jena.vocabulary.RDF;

import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.GraphQL2RDFConfiguration;

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
