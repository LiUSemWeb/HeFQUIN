package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.junit.Test;

import static org.junit.Assert.*;

public class PropertyMappingToURIsImplTest {
    protected final String NSPROPERTY = "https://example.org/property/";

    protected final PropertyMapping propertyMapping = new PropertyMappingToURIsImpl(NSPROPERTY);

    @Test
    public void mapProperty() {
        final String property = "0";
        final Node resultNode = propertyMapping.map(property);
        assertNotNull(resultNode);
        assertTrue(resultNode.isURI());
        assertEquals(resultNode.getURI(), NSPROPERTY + "0");
    }

    @Test
    public void unmapURIProperty(){
        final Node node = NodeFactory.createURI(NSPROPERTY + "0");
        final String resultString = propertyMapping.unmap(node);
        assertNotNull(resultString);
        assertEquals(resultString, "0");
    }

    @Test
    public void propertyIsPossibleResult(){
        final Node IRINode = NodeFactory.createURI(NSPROPERTY + "0");
        final boolean IRIIsPossible = propertyMapping.isPossibleResult(IRINode);
        assertTrue(IRIIsPossible);
    }


    /*
     * In this test case, a node with an invalid URI is provided as an argument to the PropertyMappingToURIsImpl.
     */
    @Test(expected = IllegalArgumentException.class)
    public void unmapPropertyWithInvalidURI(){
        final Node node = NodeFactory.createURI("https://example.com/property/3");
        propertyMapping.unmap(node);
    }
}
