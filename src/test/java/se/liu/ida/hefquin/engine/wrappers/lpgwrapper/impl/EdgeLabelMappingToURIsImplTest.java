package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.junit.Test;

import static org.junit.Assert.*;

public class EdgeLabelMappingToURIsImplTest {
    protected final String NSRELATIONSHIP = "https://example.org/relationship/";

    protected final EdgeLabelMapping edgeLabelMapping = new EdgeLabelMappingToURIsImpl(NSRELATIONSHIP);

    @Test
    public void mapEdgeLabel() {
        final String label = "0";
        final Node resultNode = edgeLabelMapping.map(label);
        assertNotNull(resultNode);
        assertTrue(resultNode.isURI());
        assertEquals(resultNode.getURI(), NSRELATIONSHIP + "0");
    }

    @Test
    public void unmapURIEdgeLabel(){
        final Node node = NodeFactory.createURI(NSRELATIONSHIP + "0");
        final String resultString = edgeLabelMapping.unmap(node);
        assertNotNull(resultString);
        assertEquals(resultString, "0");
    }

    @Test
    public void edgeLabelIsPossibleResult(){
        final Node IRINode = NodeFactory.createURI(NSRELATIONSHIP + "0");
        final boolean IRIIsPossible = edgeLabelMapping.isPossibleResult(IRINode);
        assertTrue(IRIIsPossible);
    }


    /*
     * In this test case, a node with an invalid URI is provided as an argument to the EdgeLabelMappingToURIsImpl.
     */
    @Test(expected = IllegalArgumentException.class)
    public void unmapEdgeLabelWithInvalidURI(){
        final Node node = NodeFactory.createURI("https://example.com/relationship/3");
        edgeLabelMapping.unmap(node);
    }
}
