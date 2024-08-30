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

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl.exceptions.UnSupportedPropertyNameException;

public class CompositePropertyNameMappingImplTest {

    protected final List<PropertyNameMapping> propertyNameMappings = new ArrayList<PropertyNameMapping>(Arrays.asList(
            new PropertyNameMappingImpl_SingleMatchToURI("0","http://singleExample.org/zero"),
            new PropertyNameMappingImpl_SingleMatchToURI("3","http://singleExample.org/three"),
            new PropertyNameMappingImpl_RegexMatchToURIs("^[0-9]+", "https://example2.org/test/"),
            new PropertyNameMappingImpl_AllToURIs("https://example.org/property/")
    ));

    protected final PropertyNameMapping propertyNameMapping = new CompositePropertyNameMappingImpl(propertyNameMappings);

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
