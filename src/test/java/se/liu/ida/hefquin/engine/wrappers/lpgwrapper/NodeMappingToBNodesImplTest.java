package se.liu.ida.hefquin.engine.wrappers.lpgwrapper;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.junit.Test;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.impl.LPGNode;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl.NodeMapping;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl.NodeMappingToBNodesImpl;

import static org.junit.Assert.*;

public class NodeMappingToBNodesImplTest {
    protected final String NS = "https://example.org/";
    protected final String NODE = "node/";
    protected final NodeMapping nodeMapping = new NodeMappingToBNodesImpl();

    @Test
    public void mapNode() {
        final LPGNode node = new LPGNode("0", "", null);
        final Node resultNode = nodeMapping.mapNode(node);
        assertNotNull(resultNode);
        assertTrue(resultNode.isBlank());
        assertEquals(resultNode.getBlankNodeId().toString(), "0");
    }

    @Test
    public void unmapNode(){
        final Node node = NodeFactory.createBlankNode("0");
        final LPGNode resultNode = nodeMapping.unmapNode(node);
        assertNotNull(resultNode);
        assertEquals(resultNode.getId(), "0");
        assertEquals(resultNode.getLabel(), "");
        assertNull(resultNode.getProperties());
    }

    @Test(expected = IllegalArgumentException.class)
    public void unmapNonBlankNode(){
        final Node node = NodeFactory.createURI(NS + NODE + "0");
        nodeMapping.unmapNode(node);
    }

}
