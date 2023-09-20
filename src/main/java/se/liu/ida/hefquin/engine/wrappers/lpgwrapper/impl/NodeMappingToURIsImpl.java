package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.impl.LPGNode;

public class NodeMappingToURIsImpl implements NodeMapping{

    protected final String NS = "https://example.org/";
    protected final String NODE = "node/";

    @Override
    public Node mapNode(final LPGNode node) {
        return NodeFactory.createURI(NS + NODE + node.getId());
    }

    @Override
    public LPGNode unmapNode(final Node node) {
        if (!node.isURI())
            throw new IllegalArgumentException("LPG2RDF configuration only accepts URI Node mappings");
        if (!node.getURI().startsWith(NS + NODE))
            throw new IllegalArgumentException("The provided URI is not mapping a Node");
        final String id = node.getURI().replaceAll(NS + NODE, "");
        return new LPGNode(id, "", null);
    }
}
