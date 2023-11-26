package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.junit.Test;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl.CombinedPropertyNameMappingToURIsImpl;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl.PropertyNameMapping;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl.PropertyNameMappingToURIsImpl;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl.RegexBasedPropertyNameMappingToURIsImpl;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl.SinglePropertyNameMappingToURIsImpl;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl.exceptions.UnSupportedPropertyNameException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class CombinedPropertyNameMappingToURIsImplTest {

    protected final List<PropertyNameMapping> propertyNameMappings = new ArrayList<PropertyNameMapping>(Arrays.asList(
            new SinglePropertyNameMappingToURIsImpl("0","http://singleExample.org/zero"),
            new SinglePropertyNameMappingToURIsImpl("3","http://singleExample.org/three"),
            new RegexBasedPropertyNameMappingToURIsImpl("^[0-9]+", "https://example2.org/test/"),
            new PropertyNameMappingToURIsImpl("https://example.org/property/")
    ));

    protected final PropertyNameMapping propertyNameMapping = new CombinedPropertyNameMappingToURIsImpl(propertyNameMappings);

    @Test
    public void mapSinglePropertyName() {
        Node resultNode = propertyNameMapping.map("0");
        assertNotNull(resultNode);
        assertTrue(resultNode.isURI());
        assertEquals(resultNode.getURI(),"http://singleExample.org/zero");

        resultNode = propertyNameMapping.map("3");
        assertNotNull(resultNode);
        assertTrue(resultNode.isURI());
        assertEquals(resultNode.getURI(),"http://singleExample.org/three");

        resultNode = propertyNameMapping.map("100");
        assertNotNull(resultNode);
        assertTrue(resultNode.isURI());
        assertEquals(resultNode.getURI(),"https://example2.org/test/100");

        resultNode = propertyNameMapping.map("DIRECTED");
        assertNotNull(resultNode);
        assertTrue(resultNode.isURI());
        assertEquals(resultNode.getURI(),"https://example.org/property/DIRECTED");
    }

    @Test
    public void unmapSingleURIPropertyName(){
        Node node = NodeFactory.createURI("http://singleExample.org/zero");
        String resultString = propertyNameMapping.unmap(node);
        assertNotNull(resultString);
        assertEquals(resultString, "0");

        node = NodeFactory.createURI("http://singleExample.org/three");
        resultString = propertyNameMapping.unmap(node);
        assertNotNull(resultString);
        assertEquals(resultString, "3");

        node = NodeFactory.createURI("https://example2.org/test/100");
        resultString = propertyNameMapping.unmap(node);
        assertNotNull(resultString);
        assertEquals(resultString, "100");

        node = NodeFactory.createURI("https://example.org/property/DIRECTED");
        resultString = propertyNameMapping.unmap(node);
        assertNotNull(resultString);
        assertEquals(resultString, "DIRECTED");
    }

    @Test
    public void propertyNameIsPossibleResult(){

        Node node = NodeFactory.createURI("http://singleExample.org/zero");
        boolean IRIIsPossible = propertyNameMapping.isPossibleResult(node);
        assertTrue(IRIIsPossible);

        node = NodeFactory.createURI("http://singleExample.org/three");
        IRIIsPossible = propertyNameMapping.isPossibleResult(node);
        assertTrue(IRIIsPossible);

        node = NodeFactory.createURI("https://example2.org/test/100");
        IRIIsPossible = propertyNameMapping.isPossibleResult(node);
        assertTrue(IRIIsPossible);

        node = NodeFactory.createURI("https://example.org/property/DIRECTED");
        IRIIsPossible = propertyNameMapping.isPossibleResult(node);
        assertTrue(IRIIsPossible);

        node = NodeFactory.createURI("https://invalid.url.org/property/DIRECTED");
        IRIIsPossible = propertyNameMapping.isPossibleResult(node);
        assertFalse(IRIIsPossible);
    }



    /*
     * In this test case, a node with an invalid URI is provided as an argument to the CombinedPropertyNameMappingToURIsImpl.
     */
    @Test(expected = UnSupportedPropertyNameException.class)
    public void unmapPropertyNameWithInvalidURI(){
        final Node node = NodeFactory.createURI("https://invalid.url.org/property/DIRECTED");
        propertyNameMapping.unmap(node);
    }


}
