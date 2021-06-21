package se.liu.ida.hefquin.engine.data.utils;

import org.apache.jena.sparql.engine.binding.Binding;
import org.junit.Test;
import se.liu.ida.hefquin.engine.data.SolutionMapping;

import java.util.Iterator;

import static org.junit.Assert.*;

public class SolutionMappingsIterableWithTwoVarsFilterTest {
    @Test
    public void solutionMappingsIterableWithTwoVarsFilter_filterWithTwoVars_subset() {
        final TestsForSolutionMappingsIterableWithFilter solMaps= new TestsForSolutionMappingsIterableWithFilter();

        // iterate over the subset of solution mappings that have (var2, y1, var3, z1)
        final Iterable<SolutionMapping> solMapAfterFilter1 = new SolutionMappingsIterableWithTwoVarsFilter( solMaps.getSolMapListWithTwoVar(), solMaps.var2, solMaps.y1, solMaps.var3, solMaps.z1 );
        final Iterator<SolutionMapping> it1 = solMapAfterFilter1.iterator();
        assertTrue( it1.hasNext() );
        final Binding bIt1 = it1.next().asJenaBinding();
        assertEquals( 2, bIt1.size() );
        assertEquals( "http://example.org/y1", bIt1.get(solMaps.var2).getURI() );
        assertEquals( "http://example.org/z1", bIt1.get(solMaps.var3).getURI() );

        assertFalse( it1.hasNext() );
    }

    @Test
    public void solutionMappingsIterableWithTwoVarsFilter_filterWithTwoVar_empty() {
        final TestsForSolutionMappingsIterableWithFilter solMaps= new TestsForSolutionMappingsIterableWithFilter();

        // no solution mappings that having (var2, y2, var3, z1)
        final Iterable<SolutionMapping> solMapAfterFilter2 = new SolutionMappingsIterableWithTwoVarsFilter( solMaps.getSolMapListWithTwoVar(), solMaps.var2, solMaps.y2, solMaps.var3, solMaps.z1 );
        final Iterator<SolutionMapping> it2 = solMapAfterFilter2.iterator();

        assertFalse( it2.hasNext() );
    }

    @Test
    public void solutionMappingsIterableWithTwoVarsFilter_filterWithOneOfTwoVars() {
        final TestsForSolutionMappingsIterableWithFilter solMaps= new TestsForSolutionMappingsIterableWithFilter();

        // (var1, p, var2, y1): iterate over the subset of solution mappings that have y1 for var2
        final Iterable<SolutionMapping> solMapAfterFilter3 = new SolutionMappingsIterableWithTwoVarsFilter( solMaps.getSolMapListWithTwoVar(), solMaps.var1, solMaps.p, solMaps.var2, solMaps.y1);
        final Iterator<SolutionMapping> it3 = solMapAfterFilter3.iterator();
        assertTrue( it3.hasNext() );
        final Binding bIt31 = it3.next().asJenaBinding();
        assertEquals( 2, bIt31.size() );
        assertEquals( "http://example.org/y1", bIt31.get(solMaps.var2).getURI() );
        assertEquals( "http://example.org/z1", bIt31.get(solMaps.var3).getURI() );

        assertTrue( it3.hasNext() );
        final Binding bIt32 = it3.next().asJenaBinding();
        assertEquals( 2, bIt32.size() );
        assertEquals("http://example.org/y1", bIt32.get(solMaps.var2).getURI());
        assertEquals("http://example.org/z2", bIt32.get(solMaps.var3).getURI());

        assertFalse( it3.hasNext() );
    }

    @Test
    public void solutionMappingsIterableWithTwoVarsFilter_filterWithNonOfTwoVar() {
        final TestsForSolutionMappingsIterableWithFilter solMaps= new TestsForSolutionMappingsIterableWithFilter();

        // return all solution mappings: no solution mappings that have value for var1, var4
        final Iterable<SolutionMapping> solMapAfterFilter4 = new SolutionMappingsIterableWithTwoVarsFilter( solMaps.getSolMapListWithTwoVar(), solMaps.var1, solMaps.p, solMaps.var4, solMaps.p);
        final Iterator<SolutionMapping> it4 = solMapAfterFilter4.iterator();
        assertTrue( it4.hasNext() );
        final Binding bIt41 = it4.next().asJenaBinding();
        assertEquals( 2, bIt41.size() );
        assertEquals( "http://example.org/y1", bIt41.get(solMaps.var2).getURI() );
        assertEquals( "http://example.org/z1", bIt41.get(solMaps.var3).getURI() );

        assertTrue( it4.hasNext() );
        final Binding bIt42 = it4.next().asJenaBinding();
        assertEquals( 2, bIt42.size() );
        assertEquals("http://example.org/y1", bIt42.get(solMaps.var2).getURI());
        assertEquals("http://example.org/z2", bIt42.get(solMaps.var3).getURI());

        assertTrue( it4.hasNext() );
        final Binding bIt43 = it4.next().asJenaBinding();
        assertEquals( 2, bIt43.size() );
        assertEquals("http://example.org/y2", bIt43.get(solMaps.var2).getURI());
        assertEquals("http://example.org/z3", bIt43.get(solMaps.var3).getURI());

        assertFalse( it4.hasNext() );
    }
}
