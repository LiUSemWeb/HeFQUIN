package se.liu.ida.hefquin.engine.wrappers.lpg.conf.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.junit.Test;

import se.liu.ida.hefquin.engine.wrappers.lpg.impl.exceptions.UnSupportedNodeLabelException;

public class CompositeNodeLabelMappingImplTest {

    protected final List<NodeLabelMapping> nodeLabelMappings = new ArrayList<NodeLabelMapping>(Arrays.asList(
            new NodeLabelMappingImpl_SingleMatchToURI("0","http://singleExample.org/zero"),
            new NodeLabelMappingImpl_SingleMatchToLiteral("3","three"),
            new NodeLabelMappingImpl_RegexMatchToURIs("^[0-9]+", "https://example2.org/test/"),
            new NodeLabelMappingImpl_AllToURIs("https://example.org/label/")
    ));

    protected final NodeLabelMapping nodeLabelMapping = new CompositeNodeLabelMappingImpl(nodeLabelMappings);

    @Test
    public void mapSingleNodeLabel() {
        Node resultNode = nodeLabelMapping.map("0");
        assertNotNull(resultNode);
        assertTrue(resultNode.isURI());
        assertEquals(resultNode.getURI(),"http://singleExample.org/zero");

        resultNode = nodeLabelMapping.map("3");
        assertNotNull(resultNode);
        assertTrue(resultNode.isLiteral());
        assertEquals(resultNode.getLiteralLexicalForm(), "three");

        resultNode = nodeLabelMapping.map("100");
        assertNotNull(resultNode);
        assertTrue(resultNode.isURI());
        assertEquals(resultNode.getURI(),"https://example2.org/test/100");

        resultNode = nodeLabelMapping.map("DIRECTED");
        assertNotNull(resultNode);
        assertTrue(resultNode.isURI());
        assertEquals(resultNode.getURI(),"https://example.org/label/DIRECTED");
    }

    @Test
    public void unmapSingleURINodeLabel(){
        Node node = NodeFactory.createURI("http://singleExample.org/zero");
        String resultString = nodeLabelMapping.unmap(node);
        assertNotNull(resultString);
        assertEquals(resultString, "0");

        node = NodeFactory.createLiteral("three");
        resultString = nodeLabelMapping.unmap(node);
        assertNotNull(resultString);
        assertEquals(resultString, "3");

        node = NodeFactory.createURI("https://example2.org/test/100");
        resultString = nodeLabelMapping.unmap(node);
        assertNotNull(resultString);
        assertEquals(resultString, "100");

        node = NodeFactory.createURI("https://example.org/label/DIRECTED");
        resultString = nodeLabelMapping.unmap(node);
        assertNotNull(resultString);
        assertEquals(resultString, "DIRECTED");
    }

    @Test
    public void nodeLabelIsPossibleResult(){

        Node node = NodeFactory.createURI("http://singleExample.org/zero");
        boolean IRIIsPossible = nodeLabelMapping.isPossibleResult(node);
        assertTrue(IRIIsPossible);

        node = NodeFactory.createLiteral("three");
        IRIIsPossible = nodeLabelMapping.isPossibleResult(node);
        assertTrue(IRIIsPossible);

        node = NodeFactory.createURI("https://example2.org/test/100");
        IRIIsPossible = nodeLabelMapping.isPossibleResult(node);
        assertTrue(IRIIsPossible);

        node = NodeFactory.createURI("https://example.org/label/DIRECTED");
        IRIIsPossible = nodeLabelMapping.isPossibleResult(node);
        assertTrue(IRIIsPossible);

        node = NodeFactory.createURI("https://invalid.url.org/label/DIRECTED");
        IRIIsPossible = nodeLabelMapping.isPossibleResult(node);
        assertFalse(IRIIsPossible);
    }



    /*
     * In this test case, a node with an invalid URI is provided as an argument to the CombinedNodeLabelMappingImpl.
     */
    @Test(expected = UnSupportedNodeLabelException.class)
    public void unmapNodeLabelWithInvalidURI(){
        final Node node = NodeFactory.createURI("https://invalid.url.org/label/DIRECTED");
        nodeLabelMapping.unmap(node);
    }

    /*
     * In this test case, an unsupported Literal node is provided as an argument to the CombinedNodeLabelMappingImpl.
     */
    @Test(expected = UnSupportedNodeLabelException.class)
    public void unmapNodeLabelWithInvalidLiteral(){
        final Node node = NodeFactory.createLiteral("test");
        nodeLabelMapping.unmap(node);
    }


}
