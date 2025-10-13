package se.liu.ida.hefquin.engine.wrappers.lpg.conf.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.junit.Test;

public class NodeLabelMappingImpl_AllToLiteralsTest {

    protected final String NSNODELABEL = "https://example.org/label/";

    protected final NodeLabelMapping nodeLabelMapping = new NodeLabelMappingImpl_AllToLiterals();

    @Test
    public void mapNodeLabel() {
        final String label = "0";
        final Node resultNode = nodeLabelMapping.map(label);
        assertNotNull(resultNode);
        assertTrue(resultNode.isLiteral());
        assertEquals(resultNode.getLiteralLexicalForm(), "0");
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
