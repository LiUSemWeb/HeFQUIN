package se.liu.ida.hefquin.engine.data.utils;

import org.apache.jena.sparql.engine.binding.Binding;
import org.junit.Test;
import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.impl.SolutionMappingUtils;

import java.util.Iterator;

import static org.junit.Assert.*;

public class SolutionMappingsIterableWithSolMapFilterTest extends TestsForSolutionMappingsIterableWithFilter
{
    @Test
    public void solutionMappingsIterableWithSolMapFilter_sm1() {
        final SolutionMapping sm = SolutionMappingUtils.createSolutionMapping(var2, y1, var3, z1);

        // iterate over the subset of solution mappings that are compatible with sm (var2, y1, var3, z1)
        final Iterator<SolutionMapping> it = new SolutionMappingsIterableWithSolMapFilter( getSolMapListWithTwoVar(), sm ).iterator();

        assertTrue( it.hasNext() );
        final Binding b = it.next().asJenaBinding();
        assertEquals( 2, b.size() );
        assertEquals( "http://example.org/y1", b.get(var2).getURI() );
        assertEquals( "http://example.org/z1", b.get(var3).getURI() );

        assertFalse( it.hasNext() );
    }

    @Test
    public void solutionMappingsIterableWithSolMapFilter_sm2() {
        final SolutionMapping sm = SolutionMappingUtils.createSolutionMapping(var2, y2, var3, z1);

        // no solution mappings that are compatible with sm (var2, y2, var3, z1)
        final Iterator<SolutionMapping> it = new SolutionMappingsIterableWithSolMapFilter( getSolMapListWithTwoVar(), sm ).iterator();

        assertFalse( it.hasNext() );
    }

    @Test
    public void solutionMappingsIterableWithSolMapFilter_sm3() {
        final SolutionMapping sm = SolutionMappingUtils.createSolutionMapping(var2, y1, var1, x1);

        // two solution mappings that are compatible with sm (var2, y1, var1, x1).
        final Iterator<SolutionMapping> it = new SolutionMappingsIterableWithSolMapFilter( getSolMapListWithTwoVar(), sm ).iterator();

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
    public void solutionMappingsIterableWithSolMapFilter_sm4() {
        final SolutionMapping sm = SolutionMappingUtils.createSolutionMapping(var1, x1);

        // all solution mappings that are compatible with sm (var1, x1)
        final Iterator<SolutionMapping> it = new SolutionMappingsIterableWithSolMapFilter( getSolMapListWithTwoVar(), sm ).iterator();

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
