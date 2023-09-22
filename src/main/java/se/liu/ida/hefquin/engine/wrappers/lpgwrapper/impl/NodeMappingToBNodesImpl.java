package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.impl.LPGNode;

public class NodeMappingToBNodesImpl implements NodeMapping{

    @Override
    public Node map(final LPGNode node) {
        return NodeFactory.createBlankNode(node.getId());
    }

    @Override
    public LPGNode unmap(final Node node) {
        if (!node.isBlank())
            throw new IllegalArgumentException("The given RDF term (" + node.toString() + ") is not a blank node.");
        final String id = node.getBlankNodeId().toString();
        return new LPGNode(id, "", null);
    }

    @Override
    public boolean isPossibleResult(Node node) {
        return node.isBlank();
    }
}
