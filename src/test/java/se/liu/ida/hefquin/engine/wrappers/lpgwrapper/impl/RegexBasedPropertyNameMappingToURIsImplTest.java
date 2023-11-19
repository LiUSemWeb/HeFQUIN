package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.junit.Test;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl.exceptions.UnSupportedPropertyNameException;

import static org.junit.Assert.*;

public class RegexBasedPropertyNameMappingToURIsImplTest {
    protected final String NSPROPERTY = "https://example2.org/test/";
    protected final String regex = "^[0-9]+";

    protected final PropertyNameMapping propertyNameMapping = new RegexBasedPropertyNameMappingToURIsImpl(regex, NSPROPERTY);

    @Test
    public void mapPropertyName() {
        final String propertyName = "0";
        final Node resultNode = propertyNameMapping.map(propertyName);
        assertNotNull(resultNode);
        assertTrue(resultNode.isURI());
        assertEquals(resultNode.getURI(), NSPROPERTY + "0");
    }

    @Test
    public void unmapURIPropertyName(){
        final Node node = NodeFactory.createURI(NSPROPERTY + "0");
        final String resultString = propertyNameMapping.unmap(node);
        assertNotNull(resultString);
        assertEquals(resultString, "0");
    }

    @Test
    public void propertyNameIsPossibleResult(){
        final Node IRINode = NodeFactory.createURI(NSPROPERTY + "0");
        final boolean IRIIsPossible = propertyNameMapping.isPossibleResult(IRINode);
        assertTrue(IRIIsPossible);
    }


    /*
     * In this test case, a node with an invalid URI is provided as an argument to the RegexBasedPropertyNameMappingToURIsImpl.
     */
    @Test(expected = UnSupportedPropertyNameException.class)
    public void unmapPropertyNameWithInvalidURI(){
        final Node node = NodeFactory.createURI("https://example.org/3");
        propertyNameMapping.unmap(node);
    }

    /*
     * In this test case, a propertyName which is not match with provided regex in the RegexBasedPropertyNameMappingToURIsImpl.
     */
    @Test(expected = UnSupportedPropertyNameException.class)
    public void mapPropertyNameWithUnmatchedPropertyName(){
        final String propertyName = "test";
        propertyNameMapping.map(propertyName);
    }
}
