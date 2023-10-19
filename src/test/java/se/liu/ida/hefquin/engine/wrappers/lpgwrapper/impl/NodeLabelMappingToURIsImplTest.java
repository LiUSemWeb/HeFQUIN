package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.junit.Test;

import static org.junit.Assert.*;

public class NodeLabelMappingToURIsImplTest {
    protected final String NSNODELABEL = "https://example.org/label/";

    protected final NodeLabelMapping nodeLabelMapping = new NodeLabelMappingToURIsImpl(NSNODELABEL);

    @Test
    public void mapNodeLabel() {
        final String label = "0";
        final Node resultNode = nodeLabelMapping.map(label);
        assertNotNull(resultNode);
        assertTrue(resultNode.isURI());
        assertEquals(resultNode.getURI(), NSNODELABEL + "0");
    }

    @Test
    public void unmapURINodeLabel(){
        final Node node = NodeFactory.createURI(NSNODELABEL + "0");
        final String resultString = nodeLabelMapping.unmap(node);
        assertNotNull(resultString);
        assertEquals(resultString, "0");
    }

    @Test
    public void nodeLabelIsPossibleResult(){
        final Node literalNode = NodeFactory.createLiteral("0");
        final Node IRINode = NodeFactory.createURI(NSNODELABEL + "0");
        final boolean literalIsPossible = nodeLabelMapping.isPossibleResult(literalNode);
        final boolean IRIIsPossible = nodeLabelMapping.isPossibleResult(IRINode);
        assertFalse(literalIsPossible);
        assertTrue(IRIIsPossible);
    }

    /*
     * In this test case, a non-URI node is provided as an argument to the NodeLabelMappingToURIsImpl.
     */
    @Test(expected = IllegalArgumentException.class)
    public void unmapNonURINodeLabel(){
        final Node literalNode = NodeFactory.createLiteral("literalnode");
        nodeLabelMapping.unmap(literalNode);
    }

    /*
     * In this test case, a node with an invalid URI is provided as an argument to the NodeLabelMappingToURIsImpl.
     */
    @Test(expected = IllegalArgumentException.class)
    public void unmapNodeLabelWithInvalidURI(){
        final Node node = NodeFactory.createURI("https://example.com/label/3");
        nodeLabelMapping.unmap(node);
    }
}
