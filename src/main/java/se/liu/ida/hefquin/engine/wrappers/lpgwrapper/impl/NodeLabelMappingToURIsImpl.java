package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl.exceptions.UnSupportedNodeLabelException;

public class NodeLabelMappingToURIsImpl implements NodeLabelMapping{

    protected final String NSNODELABEL;


    public NodeLabelMappingToURIsImpl (final String NSNODELABEL){
        this.NSNODELABEL= NSNODELABEL;
    }

    @Override
    public Node map(final String label) {
        return NodeFactory.createURI(NSNODELABEL + label);
    }

    @Override
    public String unmap(final Node node) {
        if (!isPossibleResult(node))
            throw new UnSupportedNodeLabelException("The given RDF term (" + node.toString() + ") is not an URI node or not in the image of this node label mapping.");
        return node.getURI().replaceAll(NSNODELABEL, "");
    }

    @Override
    public boolean isPossibleResult(final Node node) {
        return node.isURI() && node.getURI().startsWith(NSNODELABEL);
    }
}
