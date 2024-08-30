package se.liu.ida.hefquin.engine.wrappers.lpg.conf.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.junit.Test;

import se.liu.ida.hefquin.engine.wrappers.lpg.data.impl.LPGNode;

public class NodeMappingImpl_AllToBNodesTest {
    protected final String NSNODE = "https://example.org/node/";
    protected final NodeMapping nodeMapping = new NodeMappingImpl_AllToBNodes();

    @Test
    public void mapNode() {
        final LPGNode node = new LPGNode("0", "", null);
        final Node resultNode = nodeMapping.map(node);
        assertNotNull(resultNode);
        assertTrue(resultNode.isBlank());
        assertEquals(resultNode.getBlankNodeId().toString(), "0");
    }

    @Test
    public void unmapNode(){
        final Node node = NodeFactory.createBlankNode("0");
        final LPGNode resultNode = nodeMapping.unmap(node);
        assertNotNull(resultNode);
        assertEquals(resultNode.getId(), "0");
        assertEquals(resultNode.getLabel(), "");
        assertNull(resultNode.getProperties());
    }

    @Test(expected = IllegalArgumentException.class)
    public void unmapNonBlankNode(){
        final Node node = NodeFactory.createURI(NSNODE + "0");
        nodeMapping.unmap(node);
    }

}
