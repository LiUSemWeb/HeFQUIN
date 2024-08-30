package se.liu.ida.hefquin.engine.wrappers.lpg.conf.impl;

import org.apache.jena.graph.Node;

import se.liu.ida.hefquin.engine.wrappers.lpg.impl.exceptions.UnSupportedPropertyNameException;

public class PropertyNameMappingImpl_RegexMatchToURIs extends PropertyNameMappingImpl_AllToURIs {

    protected final String regex;

    public PropertyNameMappingImpl_RegexMatchToURIs(final String regex, final String NSPROPERTY){
        super(NSPROPERTY);
        this.regex = regex;
    }
    @Override
    public Node map(final String propertyName) {
        if (propertyName.matches(regex)) {
            return super.map(propertyName);
        }
        else {
            throw new UnSupportedPropertyNameException("The given property name (" + propertyName + ") is not a supported property name in the image of this property name mapping.");
        }
    }
}
