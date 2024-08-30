package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.junit.Test;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl.exceptions.UnSupportedEdgeLabelException;

public class EdgeLabelMappingImpl_RegexMatchToURIsTest {
    protected final String NSRELATIONSHIP = "https://example2.org/test/";
    protected final String regex = "^[0-9]+";

    protected final EdgeLabelMapping edgeLabelMapping = new EdgeLabelMappingImpl_RegexMatchToURIs(regex, NSRELATIONSHIP);

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
     * In this test case, a node with an invalid URI is provided as an argument to the RegexBasedEdgeLabelMappingToURIsImpl.
     */
    @Test(expected = UnSupportedEdgeLabelException.class)
    public void unmapEdgeLabelWithInvalidURI(){
        final Node node = NodeFactory.createURI("https://example.org/3");
        edgeLabelMapping.unmap(node);
    }

    /*
     * In this test case, a label which is not match with provided regex in the RegexBasedEdgeLabelMappingToURIsImpl.
     */
    @Test(expected = UnSupportedEdgeLabelException.class)
    public void mapEdgeLabelWithUnmatchedLabel(){
        final String label = "test";
        edgeLabelMapping.map(label);
    }
}
