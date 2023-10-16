package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

public class NodeLabelMappingToLiteralsImpl implements NodeLabelMapping{

    @Override
    public Node map(final String label) {
        return NodeFactory.createLiteral(label);
    }

    @Override
    public String unmap(Node node) {
        if (!node.isLiteral())
            throw new IllegalArgumentException("The given RDF term (" + node.toString() + ") is not a literal.");
        return node.getLiteral().toString();
    }

    @Override
    public boolean isPossibleResult(Node node) {
        return node.isLiteral();
    }
}
