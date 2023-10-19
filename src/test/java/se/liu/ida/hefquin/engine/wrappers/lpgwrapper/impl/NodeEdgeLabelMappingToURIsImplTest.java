package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.junit.Test;

import static org.junit.Assert.*;

public class NodeEdgeLabelMappingToURIsImplTest {
    protected final String NSRELATIONSHIP = "https://example.org/relationship/";

    protected final NodeEdgeLabelMapping nodeEdgeLabelMapping = new NodeEdgeLabelMappingToURIsImpl(NSRELATIONSHIP);

    @Test
    public void mapNodeEdgeLabel() {
        final String label = "0";
        final Node resultNode = nodeEdgeLabelMapping.map(label);
        assertNotNull(resultNode);
        assertTrue(resultNode.isURI());
        assertEquals(resultNode.getURI(), NSRELATIONSHIP + "0");
    }

    @Test
    public void unmapURINodeEdgeLabel(){
        final Node node = NodeFactory.createURI(NSRELATIONSHIP + "0");
        final String resultString = nodeEdgeLabelMapping.unmap(node);
        assertNotNull(resultString);
        assertEquals(resultString, "0");
    }

    @Test
    public void nodeEdgeLabelIsPossibleResult(){
        final Node IRINode = NodeFactory.createURI(NSRELATIONSHIP + "0");
        final boolean IRIIsPossible = nodeEdgeLabelMapping.isPossibleResult(IRINode);
        assertTrue(IRIIsPossible);
    }


    /*
     * In this test case, a node with an invalid URI is provided as an argument to the NodeEdgeLabelMappingToURIsImpl.
     */
    @Test(expected = IllegalArgumentException.class)
    public void unmapNodeEdgeLabelWithInvalidURI(){
        final Node node = NodeFactory.createURI("https://example.com/relationship/3");
        nodeEdgeLabelMapping.unmap(node);
    }
}
