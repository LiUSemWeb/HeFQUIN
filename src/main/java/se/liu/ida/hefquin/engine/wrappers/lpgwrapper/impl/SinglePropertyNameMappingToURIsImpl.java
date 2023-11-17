package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl.exceptions.UnSupportedPropertyNameException;

public class SinglePropertyNameMappingToURIsImpl implements PropertyNameMapping {

    protected final String propertyName;
    protected final Node node;

    public SinglePropertyNameMappingToURIsImpl(final String propertyName, final String iri){
        this.propertyName=propertyName;
        this.node = NodeFactory.createURI(iri);
    }
    @Override
    public Node map(final String propertyName) {
        if (propertyName.equals(this.propertyName)) {
            return this.node;
        }
        else {
            throw new UnSupportedPropertyNameException("The given property name (" + propertyName + ") is not a supported property name in the image of this property name mapping.");
        }
    }

    public String unmap(final Node node) {
        if (!isPossibleResult(node))
            throw new UnSupportedPropertyNameException("The given RDF term (" + node.toString() + ") is not an URI node or not in the image of this property name mapping.");
        return this.propertyName;
    }

    @Override
    public boolean isPossibleResult(final Node node) {
        return node.isURI() && node.getURI().equals(this.node.getURI());
    }

}
