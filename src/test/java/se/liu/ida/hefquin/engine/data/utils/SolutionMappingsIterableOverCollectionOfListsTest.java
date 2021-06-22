package se.liu.ida.hefquin.engine.data.utils;

import org.apache.jena.sparql.engine.binding.Binding;
import org.junit.Test;
import se.liu.ida.hefquin.engine.data.SolutionMapping;

import java.util.*;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class SolutionMappingsIterableOverCollectionOfListsTest extends TestsForSolutionMappingsIterableWithFilter
{
    @Test
    public void solMappingsIterableOverCollectionOfLists() {
        final Collection<List<SolutionMapping>> solMapCollection = new ArrayList<>();
        solMapCollection.add( getSolMapListWithTwoVar() );

        // Iterate over all solution mappings contained in a collection
        final Iterator<SolutionMapping> it = new SolutionMappingsIterableOverCollectionOfLists(solMapCollection).iterator();

        assertTrue( it.hasNext() );
        final Binding b1 = it.next().asJenaBinding();
        assertEquals( 2, b1.size() );
        assertEquals( "http://example.org/y1", b1.get(var2).getURI() );
        assertEquals( "http://example.org/z1", b1.get(var3).getURI() );

        assertTrue( it.hasNext() );
        final Binding b2 = it.next().asJenaBinding();
        assertEquals( 2, b2.size() );
        assertEquals("http://example.org/y1", b2.get(var2).getURI());
        assertEquals("http://example.org/z2", b2.get(var3).getURI());

        assertTrue( it.hasNext() );
        final Binding b3 = it.next().asJenaBinding();
        assertEquals( 2, b3.size() );
        assertEquals("http://example.org/y2", b3.get(var2).getURI());
        assertEquals("http://example.org/z3", b3.get(var3).getURI());

        assertFalse( it.hasNext() );
    }
}
