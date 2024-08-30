package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl;

import org.apache.jena.graph.Node;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl.exceptions.UnSupportedPropertyNameException;

import java.util.List;

public class CompositePropertyNameMappingImpl implements PropertyNameMapping {

    protected final List<PropertyNameMapping> propertyNameMappings;

    public CompositePropertyNameMappingImpl(final List<PropertyNameMapping> propertyNameMappings){
        this.propertyNameMappings = propertyNameMappings;
    }

    @Override
    public Node map(final String propertyName) {
        for (final PropertyNameMapping propertyNameMapping : propertyNameMappings) {
            try {
                return propertyNameMapping.map(propertyName);
            }
            catch (final UnSupportedPropertyNameException e) {}
        }
        throw new UnSupportedPropertyNameException("The given property name (" + propertyName + ") is not a supported property name in the image of this property name mapping.");
    }

    public String unmap(final Node node) {
        for (final PropertyNameMapping propertyNameMapping : propertyNameMappings) {
            try {
                return propertyNameMapping.unmap(node);
            }
            catch (final UnSupportedPropertyNameException e) {}
        }
        throw new UnSupportedPropertyNameException("The given RDF term (" + node.toString() + ") is not an URI node or not in the image of this property name mapping.");
    }

    @Override
    public boolean isPossibleResult(final Node node) {
        for (final PropertyNameMapping propertyNameMapping : propertyNameMappings) {
                if(propertyNameMapping.isPossibleResult(node)){
                    return true;
                }
        }
        return false;
    }
}
