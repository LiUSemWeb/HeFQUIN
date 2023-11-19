package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.junit.Test;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl.exceptions.UnSupportedPropertyNameException;

import static org.junit.Assert.*;

public class SinglePropertyNameMappingToURIsImplTest {

    protected final String propertyName="DIRECTED";
    protected final String iri="http://singleExample.org/directorOf";
    protected final PropertyNameMapping singlePropertyNameMapping = new SinglePropertyNameMappingToURIsImpl(propertyName,iri);

    @Test
    public void mapSinglePropertyName() {
        final Node resultNode = singlePropertyNameMapping.map("DIRECTED");
        assertNotNull(resultNode);
        assertTrue(resultNode.isURI());
        assertEquals(resultNode.getURI(),"http://singleExample.org/directorOf");
    }

    @Test
    public void unmapSingleURIPropertyName(){
        final Node node = NodeFactory.createURI("http://singleExample.org/directorOf");
        final String resultString = singlePropertyNameMapping.unmap(node);
        assertNotNull(resultString);
        assertEquals(resultString, "DIRECTED");
    }

    @Test
    public void propertyNameIsPossibleResult(){
        final Node IRINode = NodeFactory.createURI("http://singleExample.org/directorOf");
        final boolean IRIIsPossible = singlePropertyNameMapping.isPossibleResult(IRINode);
        assertTrue(IRIIsPossible);
    }



    /*
     * In this test case, a node with an invalid URI is provided as an argument to the SinglePropertyNameMappingToURIsImpl.
     */
    @Test(expected = UnSupportedPropertyNameException.class)
    public void unmapPropertyNameWithInvalidURI(){
        final Node node = NodeFactory.createURI("https://example.org/3");
        singlePropertyNameMapping.unmap(node);
    }

    /*
     * In this test case, a property name which is not equal with the given property name in SinglePropertyNameMappingToURIsImpl.
     */
    @Test(expected = UnSupportedPropertyNameException.class)
    public void mapPropertyNameWithUnmatchedPropertyName(){
        final String propertyName = "test";
        singlePropertyNameMapping.map(propertyName);
    }

}
