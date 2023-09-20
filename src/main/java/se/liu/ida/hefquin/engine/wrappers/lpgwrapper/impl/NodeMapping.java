package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl;

import org.apache.jena.graph.Node;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.impl.LPGNode;

public interface NodeMapping {

    public Node mapNode(final LPGNode node);

    public LPGNode unmapNode(final Node node);
}
