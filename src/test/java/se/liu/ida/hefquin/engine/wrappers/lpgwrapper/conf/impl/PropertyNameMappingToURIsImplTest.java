package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.junit.Test;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl.PropertyNameMapping;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl.PropertyNameMappingToURIsImpl;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl.exceptions.UnSupportedPropertyNameException;

import static org.junit.Assert.*;

public class PropertyNameMappingToURIsImplTest {
    protected final String NSPROPERTY = "https://example.org/property/";

    protected final PropertyNameMapping propertyNameMapping = new PropertyNameMappingToURIsImpl(NSPROPERTY);

    @Test
    public void mapProperty() {
        final String propertyName = "0";
        final Node resultNode = propertyNameMapping.map(propertyName);
        assertNotNull(resultNode);
        assertTrue(resultNode.isURI());
        assertEquals(resultNode.getURI(), NSPROPERTY + "0");
    }

    @Test
    public void unmapURIProperty(){
        final Node node = NodeFactory.createURI(NSPROPERTY + "0");
        final String resultString = propertyNameMapping.unmap(node);
        assertNotNull(resultString);
        assertEquals(resultString, "0");
    }

    @Test
    public void propertyIsPossibleResult(){
        final Node IRINode = NodeFactory.createURI(NSPROPERTY + "0");
        final boolean IRIIsPossible = propertyNameMapping.isPossibleResult(IRINode);
        assertTrue(IRIIsPossible);
    }


    /*
     * In this test case, a node with an invalid URI is provided as an argument to the PropertyNameMappingToURIsImpl.
     */
    @Test(expected = UnSupportedPropertyNameException.class)
    public void unmapPropertyNameWithInvalidURI(){
        final Node node = NodeFactory.createURI("https://example.com/property/3");
        propertyNameMapping.unmap(node);
    }
}
