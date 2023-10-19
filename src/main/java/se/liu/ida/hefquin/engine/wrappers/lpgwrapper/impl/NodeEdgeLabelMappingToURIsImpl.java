package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

public class NodeEdgeLabelMappingToURIsImpl implements NodeEdgeLabelMapping{

    protected final String NSRELATIONSHIP;


    public NodeEdgeLabelMappingToURIsImpl(final String NSRELATIONSHIP){
        this.NSRELATIONSHIP=NSRELATIONSHIP ;
    }

    @Override
    public Node map(final String label) {
        return NodeFactory.createURI(NSRELATIONSHIP + label);
    }

    @Override
    public String unmap(final Node node) {
        if (!isPossibleResult(node))
            throw new IllegalArgumentException("The given RDF term (" + node.toString() + ") is not an URI node or not in the image of this node edge label mapping.");
        return node.getURI().replaceAll(NSRELATIONSHIP, "");
    }

    @Override
    public boolean isPossibleResult(final Node node) {
        return node.isURI() && node.getURI().startsWith(NSRELATIONSHIP);
    }
}
