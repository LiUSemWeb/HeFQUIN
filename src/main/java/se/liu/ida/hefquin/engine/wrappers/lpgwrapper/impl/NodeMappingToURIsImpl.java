package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.impl.LPGNode;

public class NodeMappingToURIsImpl implements NodeMapping{

    protected final String NSNODE;

    public NodeMappingToURIsImpl (final String NSNODE){
        this.NSNODE= NSNODE;
    }

    @Override
    public Node map(final LPGNode node) {
        return NodeFactory.createURI(NSNODE + node.getId());
    }

    @Override
    public LPGNode unmap(final Node node) {
        if (!node.isURI())
            throw new IllegalArgumentException("The given RDF term (" + node.toString() + ") is not an URI node.");
        if (!node.getURI().startsWith(NSNODE))
            throw new IllegalArgumentException("The given IRI (" + node.getURI() + ") is not in the image of this node mapping.");
        final String id = node.getURI().replaceAll(NSNODE, "");
        return new LPGNode(id, "", null);
    }

    @Override
    public boolean isPossibleResult(final Node node) {
        return node.isURI() && node.getURI().startsWith(NSNODE);
    }
}