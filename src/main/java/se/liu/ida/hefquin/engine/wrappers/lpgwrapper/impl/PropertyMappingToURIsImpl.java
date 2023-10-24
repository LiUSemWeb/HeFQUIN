package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

public class PropertyMappingToURIsImpl implements PropertyMapping {

    protected final String NSPROPERTY;


    public PropertyMappingToURIsImpl(final String NSPROPERTY){
        this.NSPROPERTY=NSPROPERTY ;
    }

    @Override
    public Node map(final String property) {
        return NodeFactory.createURI(NSPROPERTY + property);
    }

    @Override
    public String unmap(final Node node) {
        if (!isPossibleResult(node))
            throw new IllegalArgumentException("The given RDF term (" + node.toString() + ") is not an URI node or not in the image of this property mapping.");
        return node.getURI().replaceAll(NSPROPERTY, "");
    }

    @Override
    public boolean isPossibleResult(final Node node) {
        return node.isURI() && node.getURI().startsWith(NSPROPERTY);
    }
}
