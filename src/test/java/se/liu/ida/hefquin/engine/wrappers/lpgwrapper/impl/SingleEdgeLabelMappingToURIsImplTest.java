package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.junit.Test;

import static org.junit.Assert.*;

public class SingleEdgeLabelMappingToURIsImplTest {

    protected final String label="DIRECTED";
    protected final String iri="http://singleExample.org/directorOf";
    protected final EdgeLabelMapping singleEdgeLabelMapping = new SingleEdgeLabelMappingToURIsImpl(label,iri);

    @Test
    public void mapSingleEdgeLabel() {
        final Node resultNode = singleEdgeLabelMapping.map("DIRECTED");
        assertNotNull(resultNode);
        assertTrue(resultNode.isURI());
        assertEquals(resultNode.getURI(),"http://singleExample.org/directorOf");
    }

    @Test
    public void unmapSingleURIEdgeLabel(){
        final Node node = NodeFactory.createURI("http://singleExample.org/directorOf");
        final String resultString = singleEdgeLabelMapping.unmap(node);
        assertNotNull(resultString);
        assertEquals(resultString, "DIRECTED");
    }

    @Test
    public void edgeLabelIsPossibleResult(){
        final Node IRINode = NodeFactory.createURI("http://singleExample.org/directorOf");
        final boolean IRIIsPossible = singleEdgeLabelMapping.isPossibleResult(IRINode);
        assertTrue(IRIIsPossible);
    }



    /*
     * In this test case, a node with an invalid URI is provided as an argument to the SingleEdgeLabelMappingToURIsImpl.
     */
    @Test(expected = IllegalArgumentException.class)
    public void unmapEdgeLabelWithInvalidURI(){
        final Node node = NodeFactory.createURI("https://example.org/3");
        singleEdgeLabelMapping.unmap(node);
    }

    /*
     * In this test case, a label which is not equal with the given label in SingleEdgeLabelMappingToURIsImpl.
     */
    @Test(expected = IllegalArgumentException.class)
    public void mapEdgeLabelWithUnmatchedLabel(){
        final String label = "test";
        singleEdgeLabelMapping.map(label);
    }

}
