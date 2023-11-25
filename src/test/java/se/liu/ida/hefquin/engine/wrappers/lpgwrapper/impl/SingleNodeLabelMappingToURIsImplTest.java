package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.junit.Test;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl.exceptions.UnSupportedNodeLabelException;

import static org.junit.Assert.*;

public class SingleNodeLabelMappingToURIsImplTest {

    protected final String label="DIRECTED";
    protected final String iri="http://singleExample.org/directorOf";
    protected final NodeLabelMapping nodeLabelMapping = new SingleNodeLabelMappingToURIsImpl(label,iri);

    @Test
    public void mapSingleURINodeLabel() {
        final Node resultNode = nodeLabelMapping.map("DIRECTED");
        assertNotNull(resultNode);
        assertTrue(resultNode.isURI());
        assertEquals(resultNode.getURI(),"http://singleExample.org/directorOf");
    }

    @Test
    public void unmapSingleURINodeLabel(){
        final Node node = NodeFactory.createURI("http://singleExample.org/directorOf");
        final String resultString = nodeLabelMapping.unmap(node);
        assertNotNull(resultString);
        assertEquals(resultString, "DIRECTED");
    }

    @Test
    public void singleURINodeLabelIsPossibleResult(){
        final Node IRINode = NodeFactory.createURI("http://singleExample.org/directorOf");
        final boolean IRIIsPossible = nodeLabelMapping.isPossibleResult(IRINode);
        assertTrue(IRIIsPossible);
    }



    /*
     * In this test case, a node with an invalid URI is provided as an argument to the SingleNodeLabelMappingToURIsImpl.
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
