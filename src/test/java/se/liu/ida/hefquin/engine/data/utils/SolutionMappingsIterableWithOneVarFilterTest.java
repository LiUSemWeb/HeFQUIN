package se.liu.ida.hefquin.engine.data.utils;

import org.apache.jena.sparql.engine.binding.Binding;
import org.junit.Test;
import se.liu.ida.hefquin.engine.data.SolutionMapping;

import java.util.*;
import static org.junit.Assert.*;

public class SolutionMappingsIterableWithOneVarFilterTest extends TestsForSolutionMappingsIterableWithFilter
{
    @Test
    public void solMappingsIterableWithOneVarFilter_subset() {
        // iterate over the subset of solution mappings that y1 for var2
        final Iterator<SolutionMapping> it = new SolutionMappingsIterableWithOneVarFilter( getSolMapListWithTwoVar(), var2, y1 ).iterator();

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

        assertFalse( it.hasNext() );
    }

    @Test
    public void solMappingsIterableWithOneVarFilter_noMatching() {
        // no solution mappings that have p for var3
        final Iterator<SolutionMapping> it = new SolutionMappingsIterableWithOneVarFilter( getSolMapListWithTwoVar(), var3, p ).iterator();

        assertFalse( it.hasNext() );
    }

    @Test
    public void solMappingsIterableWithOneVarFilter_all() {
        // return all solution mappings: no solution mappings that have value for var1
        final Iterator<SolutionMapping> it = new SolutionMappingsIterableWithOneVarFilter( getSolMapListWithTwoVar(), var1, y1 ).iterator();
        
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
