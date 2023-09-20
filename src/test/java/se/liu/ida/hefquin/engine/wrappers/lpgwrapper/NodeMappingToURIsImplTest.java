package se.liu.ida.hefquin.engine.wrappers.lpgwrapper;

import org.apache.jena.graph.NodeFactory;
import org.junit.Test;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.impl.LPGNode;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl.NodeMapping;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl.NodeMappingToURIsImpl;
import org.apache.jena.graph.Node;

import static org.junit.Assert.*;

public class NodeMappingToURIsImplTest {
    protected final String NS = "https://example.org/";
    protected final String NODE = "node/";

    protected final NodeMapping nodeMapping = new NodeMappingToURIsImpl();

    @Test
    public void mapNode() {
        final LPGNode node = new LPGNode("0", null, null);
        final Node resultNode = nodeMapping.mapNode(node);
        assertNotNull(resultNode);
        assertTrue(resultNode.isURI());
        assertEquals(resultNode.getURI(), NS + NODE + "0");
    }

    @Test
    public void unmapURINode(){
        final Node node = NodeFactory.createURI(NS + NODE + "0");
        final LPGNode resultNode = nodeMapping.unmapNode(node);
        assertNotNull(resultNode);
        assertEquals(resultNode.getId(), "0");
        assertEquals(resultNode.getLabel(), "");
        assertNull(resultNode.getProperties());
    }

    @Test(expected = IllegalArgumentException.class)
    public void unmapNonURINode(){
        final Node node = NodeFactory.createURI("0");
        nodeMapping.unmapNode(node);
    }

    @Test(expected = IllegalArgumentException.class)
    public void unmapNodeWithInvalidURI(){
        final Node node = NodeFactory.createURI("https://example.com/node/3");
        nodeMapping.unmapNode(node);
    }
}
