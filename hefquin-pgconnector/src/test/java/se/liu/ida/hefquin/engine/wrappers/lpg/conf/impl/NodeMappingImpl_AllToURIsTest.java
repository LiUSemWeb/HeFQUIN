package se.liu.ida.hefquin.engine.wrappers.lpg.conf.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.junit.Test;

import se.liu.ida.hefquin.engine.wrappers.lpg.data.impl.LPGNode;

public class NodeMappingImpl_AllToURIsTest {
    protected final String NSNODE = "https://example.org/node/";

    protected final NodeMapping nodeMapping = new NodeMappingImpl_AllToURIs(NSNODE);

    @Test
    public void mapNode() {
        final LPGNode node = new LPGNode("0", null, null);
        final Node resultNode = nodeMapping.map(node);
        assertNotNull(resultNode);
        assertTrue(resultNode.isURI());
        assertEquals(resultNode.getURI(), NSNODE + "0");
    }

    @Test
    public void unmapURINode(){
        final Node node = NodeFactory.createURI(NSNODE + "0");
        final LPGNode resultNode = nodeMapping.unmap(node);
        assertNotNull(resultNode);
        assertEquals(resultNode.getId(), "0");
        assertEquals(resultNode.getLabel(), "");
        assertNull(resultNode.getProperties());
    }

    @Test(expected = IllegalArgumentException.class)
    public void unmapNonURINode(){
        final Node node = NodeFactory.createBlankNode();
        nodeMapping.unmap(node);
    }

    @Test(expected = IllegalArgumentException.class)
    public void unmapNodeWithInvalidURI(){
        final Node node = NodeFactory.createURI("https://example.com/node/3");
        nodeMapping.unmap(node);
    }
}
