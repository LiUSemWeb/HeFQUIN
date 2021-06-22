package se.liu.ida.hefquin.engine.data.utils;

import org.apache.jena.sparql.engine.binding.Binding;
import org.junit.Test;
import se.liu.ida.hefquin.engine.data.SolutionMapping;

import java.util.Iterator;

import static org.junit.Assert.*;

public class SolutionMappingsIterableWithTwoVarsFilterTest extends TestsForSolutionMappingsIterableWithFilter
{
    @Test
    public void solutionMappingsIterableWithTwoVarsFilter_filterWithTwoVars_subset() {
        // iterate over the subset of solution mappings that have (var2, y1, var3, z1)
        final Iterator<SolutionMapping> it = new SolutionMappingsIterableWithTwoVarsFilter( getSolMapListWithTwoVar(), var2, y1, var3, z1 ).iterator();

        assertTrue( it.hasNext() );
        final Binding b = it.next().asJenaBinding();
        assertEquals( 2, b.size() );
        assertEquals( "http://example.org/y1", b.get(var2).getURI() );
        assertEquals( "http://example.org/z1", b.get(var3).getURI() );

        assertFalse( it.hasNext() );
    }

    @Test
    public void solutionMappingsIterableWithTwoVarsFilter_filterWithTwoVar_empty() {
        // no solution mappings that having (var2, y2, var3, z1)
        final Iterator<SolutionMapping> it = new SolutionMappingsIterableWithTwoVarsFilter( getSolMapListWithTwoVar(), var2, y2, var3, z1 ).iterator();

        assertFalse( it.hasNext() );
    }

    @Test
    public void solutionMappingsIterableWithTwoVarsFilter_filterWithOneOfTwoVars() {
        // (var1, p, var2, y1): iterate over the subset of solution mappings that have y1 for var2
        final Iterator<SolutionMapping> it = new SolutionMappingsIterableWithTwoVarsFilter( getSolMapListWithTwoVar(), var1, p, var2, y1).iterator();

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
    public void solutionMappingsIterableWithTwoVarsFilter_filterWithNonOfTwoVar() {
        // return all solution mappings: no solution mappings that have value for var1, var4
        final Iterator<SolutionMapping> it = new SolutionMappingsIterableWithTwoVarsFilter( getSolMapListWithTwoVar(), var1, p, var4, p).iterator();

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
