package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.impl.LPGNode;

public class NodeMappingToBNodesImpl implements NodeMapping{

    @Override
    public Node mapNode(final LPGNode node) {
        return NodeFactory.createBlankNode(node.getId());
    }

    @Override
    public LPGNode unmapNode(final Node node) {
        if (!node.isBlank())
            throw new IllegalArgumentException("LPG2RDF configuration accepts Blank Node");
        final String id = node.getBlankNodeId().toString();
        return new LPGNode(id, "", null);
    }
}
