package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl;

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

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl.exceptions.UnSupportedEdgeLabelException;

public class CompositeEdgeLabelMappingImplTest {

    protected final List<EdgeLabelMapping> edgeLabelMappings = new ArrayList<EdgeLabelMapping>(Arrays.asList(
            new EdgeLabelMappingImpl_SingleMatchToURI("0","http://singleExample.org/zero"),
            new EdgeLabelMappingImpl_SingleMatchToURI("3","http://singleExample.org/three"),
            new EdgeLabelMappingImpl_RegexMatchToURIs("^[0-9]+", "https://example2.org/test/"),
            new EdgeLabelMappingImpl_AllToURIs("https://example.org/relationship/")
    ));

    protected final EdgeLabelMapping edgeLabelMapping = new CompositeEdgeLabelMappingImpl(edgeLabelMappings);

    @Test
    public void mapSingleEdgeLabel() {
        Node resultNode = edgeLabelMapping.map("0");
        assertNotNull(resultNode);
        assertTrue(resultNode.isURI());
        assertEquals(resultNode.getURI(),"http://singleExample.org/zero");

        resultNode = edgeLabelMapping.map("3");
        assertNotNull(resultNode);
        assertTrue(resultNode.isURI());
        assertEquals(resultNode.getURI(),"http://singleExample.org/three");

        resultNode = edgeLabelMapping.map("100");
        assertNotNull(resultNode);
        assertTrue(resultNode.isURI());
        assertEquals(resultNode.getURI(),"https://example2.org/test/100");

        resultNode = edgeLabelMapping.map("DIRECTED");
        assertNotNull(resultNode);
        assertTrue(resultNode.isURI());
        assertEquals(resultNode.getURI(),"https://example.org/relationship/DIRECTED");
    }

    @Test
    public void unmapSingleURIEdgeLabel(){
        Node node = NodeFactory.createURI("http://singleExample.org/zero");
        String resultString = edgeLabelMapping.unmap(node);
        assertNotNull(resultString);
        assertEquals(resultString, "0");

        node = NodeFactory.createURI("http://singleExample.org/three");
        resultString = edgeLabelMapping.unmap(node);
        assertNotNull(resultString);
        assertEquals(resultString, "3");

        node = NodeFactory.createURI("https://example2.org/test/100");
        resultString = edgeLabelMapping.unmap(node);
        assertNotNull(resultString);
        assertEquals(resultString, "100");

        node = NodeFactory.createURI("https://example.org/relationship/DIRECTED");
        resultString = edgeLabelMapping.unmap(node);
        assertNotNull(resultString);
        assertEquals(resultString, "DIRECTED");
    }

    @Test
    public void edgeLabelIsPossibleResult(){

        Node node = NodeFactory.createURI("http://singleExample.org/zero");
        boolean IRIIsPossible = edgeLabelMapping.isPossibleResult(node);
        assertTrue(IRIIsPossible);

        node = NodeFactory.createURI("http://singleExample.org/three");
        IRIIsPossible = edgeLabelMapping.isPossibleResult(node);
        assertTrue(IRIIsPossible);

        node = NodeFactory.createURI("https://example2.org/test/100");
        IRIIsPossible = edgeLabelMapping.isPossibleResult(node);
        assertTrue(IRIIsPossible);

        node = NodeFactory.createURI("https://example.org/relationship/DIRECTED");
        IRIIsPossible = edgeLabelMapping.isPossibleResult(node);
        assertTrue(IRIIsPossible);

        node = NodeFactory.createURI("https://invalid.url.org/relationship/DIRECTED");
        IRIIsPossible = edgeLabelMapping.isPossibleResult(node);
        assertFalse(IRIIsPossible);
    }



    /*
     * In this test case, a node with an invalid URI is provided as an argument to the CombinedEdgeLabelMappingToURIsImpl.
     */
    @Test(expected = UnSupportedEdgeLabelException.class)
    public void unmapEdgeLabelWithInvalidURI(){
        final Node node = NodeFactory.createURI("https://invalid.url.org/relationship/DIRECTED");
        edgeLabelMapping.unmap(node);
    }


}
