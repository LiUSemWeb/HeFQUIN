package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.junit.Test;

import static org.junit.Assert.*;

public class NodeLabelMappingToLiteralsImplTest {

    protected final String NSNODELABEL = "https://example.org/label/";

    protected final NodeLabelMapping nodeLabelMapping = new NodeLabelMappingToLiteralsImpl();

    @Test
    public void mapNodeLabel() {
        final String label = "0";
        final Node resultNode = nodeLabelMapping.map(label);
        assertNotNull(resultNode);
        assertTrue(resultNode.isLiteral());
        assertEquals(resultNode.getLiteral().toString(), "0");
    }

    @Test
    public void unmapLiteralNodeLabel(){
        final Node node = NodeFactory.createLiteral("0");
        final String resultString = nodeLabelMapping.unmap(node);
        assertNotNull(resultString);
        assertEquals(resultString, "0");
    }

    @Test
    public void nodeLabelIsPossibleResult(){
        final Node node = NodeFactory.createLiteral("0");
        final Node IRINode = NodeFactory.createURI(NSNODELABEL + "0");
        final boolean literalIsPossible = nodeLabelMapping.isPossibleResult(node);
        final boolean IRIIsPossible = nodeLabelMapping.isPossibleResult(IRINode);
        assertTrue(literalIsPossible);
        assertFalse(IRIIsPossible);
    }

    /*
     * In this test case, a non-Literal node is provided as an argument to the NodeLabelMappingToLiteralsImpl.
     */
    @Test(expected = IllegalArgumentException.class)
    public void unmapNonLiteralNodeLabel(){
        final Node IRINode = NodeFactory.createURI(NSNODELABEL + "0");
        nodeLabelMapping.unmap(IRINode);
    }
}
