package se.liu.ida.hefquin.engine.wrappers.lpg.conf.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.junit.Test;

import se.liu.ida.hefquin.engine.wrappers.lpg.impl.exceptions.UnSupportedNodeLabelException;

public class NodeLabelMappingImpl_SingleMatchToLiteralTest {

    protected final String label="DIRECTED";
    protected final String literal="directorOf";
    protected final NodeLabelMapping nodeLabelMapping = new NodeLabelMappingImpl_SingleMatchToLiteral(label,literal);

    @Test
    public void mapSingleLiteralNodeLabel() {
        final Node resultNode = nodeLabelMapping.map("DIRECTED");
        assertNotNull(resultNode);
        assertTrue(resultNode.isLiteral());
        assertEquals(resultNode.getLiteralLexicalForm(), "directorOf");
    }

    @Test
    public void unmapSingleLiteralNodeLabel(){
        final Node node = NodeFactory.createLiteral("directorOf");
        final String resultString = nodeLabelMapping.unmap(node);
        assertNotNull(resultString);
        assertEquals(resultString, "DIRECTED");
    }

    @Test
    public void singleLiteralNodeLabelIsPossibleResult(){
        final Node literalNode = NodeFactory.createLiteral("directorOf");
        final Node IRINode = NodeFactory.createURI("http://singleExample.org/directorOf");
        final boolean literalIsPossible = nodeLabelMapping.isPossibleResult(literalNode);
        final boolean IRIIsPossible = nodeLabelMapping.isPossibleResult(IRINode);
        assertTrue(literalIsPossible);
        assertFalse(IRIIsPossible);
    }



    /*
     * In this test case, a non-Literal node is provided as an argument to the SingleNodeLabelMappingToURIsImpl.
    */
    @Test(expected = UnSupportedNodeLabelException.class)
    public void unmapNodeLabelWithInvalidURI(){
        final Node node = NodeFactory.createURI("https://example.org/3");
        nodeLabelMapping.unmap(node);
    }

    /*
     * In this test case, a label which is not equal with the given label in SingleNodeLabelMappingToURIsImpl.
     */
    @Test(expected = UnSupportedNodeLabelException.class)
    public void mapNodeLabelWithUnmatchedLabel(){
        final String label = "test";
        nodeLabelMapping.map(label);
    }

}
