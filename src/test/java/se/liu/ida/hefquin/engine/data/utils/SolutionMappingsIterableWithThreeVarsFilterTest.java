package se.liu.ida.hefquin.engine.data.utils;

import org.apache.jena.sparql.engine.binding.Binding;
import org.junit.Test;
import se.liu.ida.hefquin.engine.data.SolutionMapping;
import java.util.Iterator;

import static org.junit.Assert.*;

public class SolutionMappingsIterableWithThreeVarsFilterTest extends TestsForSolutionMappingsIterableWithFilter
{
    @Test
    public void solutionMappingsIterableWithThreeVarsFilter_filterWithThreeVars_subset() {
        // iterate over the subset of solution mappings that have (var3, z1, var1, x1, var2, y1)
        final Iterator<SolutionMapping> it = new SolutionMappingsIterableWithThreeVarsFilter( getSolMapListWithThreeVar(), var3, z1, var1, x1, var2, y1 ).iterator();

        assertTrue( it.hasNext() );
        final Binding b = it.next().asJenaBinding();
        assertEquals( 3, b.size() );
        assertEquals( "http://example.org/x1", b.get(var1).getURI() );
        assertEquals( "http://example.org/y1", b.get(var2).getURI() );
        assertEquals( "http://example.org/z1", b.get(var3).getURI() );

        assertFalse( it.hasNext() );
    }

    @Test
    public void solutionMappingsIterableWithThreeVarsFilter_filterWithThreeVars_empty() {
        // no solution mappings that having (var1, x2, var2, y1, var3, z2)
        final Iterator<SolutionMapping> it = new SolutionMappingsIterableWithThreeVarsFilter( getSolMapListWithThreeVar(), var1, x2, var2, y1, var3, z2 ).iterator();

        assertFalse( it.hasNext() );
    }

    @Test
    public void solutionMappingsIterableWithThreeVarsFilter_filterWithTwoOfThreeVars_subset() {
        // (var2, y1, var1, x1, var4, p): filter based on subset of variables
        final Iterator<SolutionMapping> it = new SolutionMappingsIterableWithThreeVarsFilter( getSolMapListWithThreeVar(), var2, y1, var1, x1, var4, p ).iterator();

        assertTrue( it.hasNext() );
        final Binding b1 = it.next().asJenaBinding();
        assertEquals( 3, b1.size() );
        assertEquals( "http://example.org/x1", b1.get(var1).getURI() );
        assertEquals( "http://example.org/y1", b1.get(var2).getURI() );
        assertEquals( "http://example.org/z1", b1.get(var3).getURI() );

        assertTrue( it.hasNext() );
        final Binding b2 = it.next().asJenaBinding();
        assertEquals( 3, b2.size() );
        assertEquals( "http://example.org/x1", b2.get(var1).getURI() );
        assertEquals( "http://example.org/y1", b2.get(var2).getURI() );
        assertEquals( "http://example.org/z2", b2.get(var3).getURI() );

        assertFalse( it.hasNext() );
    }

    @Test
    public void solutionMappingsIterableWithThreeVarsFilter_filterWithTwoOfThreeVars_empty() {
        final TestsForSolutionMappingsIterableWithFilter solMaps= new TestsForSolutionMappingsIterableWithFilter();

        // (var2, y1, var1, x2, var4, p): no solution mappings that having (var2, y1, var1, x2)
        final Iterable<SolutionMapping> solMapAfterFilter4 = new SolutionMappingsIterableWithThreeVarsFilter( solMaps.getSolMapListWithThreeVar(), solMaps.var2, solMaps.y1, solMaps.var1, solMaps.x2, solMaps.var4, solMaps.p );
        final Iterator<SolutionMapping> it4 = solMapAfterFilter4.iterator();

        assertFalse( it4.hasNext() );
    }

    @Test
    public void solutionMappingsIterableWithThreeVarsFilter_filterWithNoneOfThreeVars_all() {
        // (var5, y1, var6, x2, var4, p): return all solution mappings: no solution mappings that have value for var4 or var5 or var6
        final Iterator<SolutionMapping> it = new SolutionMappingsIterableWithThreeVarsFilter( getSolMapListWithThreeVar(), var5, y1, var6, x2, var4, p ).iterator();

        assertTrue( it.hasNext() );
        final Binding b1 = it.next().asJenaBinding();
        assertEquals( 3, b1.size() );
        assertEquals( "http://example.org/x1", b1.get(var1).getURI() );
        assertEquals( "http://example.org/y1", b1.get(var2).getURI() );
        assertEquals( "http://example.org/z1", b1.get(var3).getURI() );

        assertTrue( it.hasNext() );
        final Binding b2 = it.next().asJenaBinding();
        assertEquals( 3, b2.size() );
        assertEquals( "http://example.org/x1", b2.get(var1).getURI() );
        assertEquals( "http://example.org/y1", b2.get(var2).getURI() );
        assertEquals( "http://example.org/z2", b2.get(var3).getURI() );

        assertTrue( it.hasNext() );
        final Binding b3 = it.next().asJenaBinding();
        assertEquals( 3, b3.size() );
        assertEquals( "http://example.org/x2", b3.get(var1).getURI() );
        assertEquals( "http://example.org/y2", b3.get(var2).getURI() );
        assertEquals( "http://example.org/z2", b3.get(var3).getURI() );

        assertFalse( it.hasNext() );
    }
}
