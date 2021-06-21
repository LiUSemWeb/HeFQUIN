package se.liu.ida.hefquin.engine.data.utils;

import org.apache.jena.sparql.engine.binding.Binding;
import org.junit.Test;
import se.liu.ida.hefquin.engine.data.SolutionMapping;
import java.util.Iterator;

import static org.junit.Assert.*;

public class SolutionMappingsIterableWithThreeVarsFilterTest {
    @Test
    public void solutionMappingsIterableWithThreeVarsFilter_filterWithThreeVars_subset() {
        final TestsForSolutionMappingsIterableWithFilter solMaps= new TestsForSolutionMappingsIterableWithFilter();

        // iterate over the subset of solution mappings that have (var3, z1, var1, x1, var2, y1)
        final Iterable<SolutionMapping> solMapAfterFilter1 = new SolutionMappingsIterableWithThreeVarsFilter( solMaps.getSolMapListWithThreeVar(), solMaps.var3, solMaps.z1, solMaps.var1, solMaps.x1, solMaps.var2, solMaps.y1 );
        final Iterator<SolutionMapping> it1 = solMapAfterFilter1.iterator();
        assertTrue( it1.hasNext() );
        final Binding bIt1 = it1.next().asJenaBinding();
        assertEquals( 3, bIt1.size() );
        assertEquals( "http://example.org/x1", bIt1.get(solMaps.var1).getURI() );
        assertEquals( "http://example.org/y1", bIt1.get(solMaps.var2).getURI() );
        assertEquals( "http://example.org/z1", bIt1.get(solMaps.var3).getURI() );

        assertFalse( it1.hasNext() );
    }

    @Test
    public void solutionMappingsIterableWithThreeVarsFilter_filterWithThreeVars_empty() {
        final TestsForSolutionMappingsIterableWithFilter solMaps= new TestsForSolutionMappingsIterableWithFilter();

        // no solution mappings that having (var1, x2, var2, y1, var3, z2)
        final Iterable<SolutionMapping> solMapAfterFilter3 = new SolutionMappingsIterableWithThreeVarsFilter( solMaps.getSolMapListWithThreeVar(), solMaps.var1, solMaps.x2, solMaps.var2, solMaps.y1, solMaps.var3, solMaps.z2 );
        final Iterator<SolutionMapping> it3 = solMapAfterFilter3.iterator();

        assertFalse( it3.hasNext() );
    }

    @Test
    public void solutionMappingsIterableWithThreeVarsFilter_filterWithTwoOfThreeVars_subset() {
        final TestsForSolutionMappingsIterableWithFilter solMaps= new TestsForSolutionMappingsIterableWithFilter();

        // (var2, y1, var1, x1, var4, p): filter based on subset of variables
        final Iterable<SolutionMapping> solMapAfterFilter2 = new SolutionMappingsIterableWithThreeVarsFilter( solMaps.getSolMapListWithThreeVar(), solMaps.var2, solMaps.y1, solMaps.var1, solMaps.x1, solMaps.var4, solMaps.p );
        final Iterator<SolutionMapping> it2 = solMapAfterFilter2.iterator();
        assertTrue( it2.hasNext() );
        final Binding bIt21 = it2.next().asJenaBinding();
        assertEquals( 3, bIt21.size() );
        assertEquals( "http://example.org/x1", bIt21.get(solMaps.var1).getURI() );
        assertEquals( "http://example.org/y1", bIt21.get(solMaps.var2).getURI() );
        assertEquals( "http://example.org/z1", bIt21.get(solMaps.var3).getURI() );

        assertTrue( it2.hasNext() );
        final Binding bIt22 = it2.next().asJenaBinding();
        assertEquals( 3, bIt22.size() );
        assertEquals( "http://example.org/x1", bIt22.get(solMaps.var1).getURI() );
        assertEquals( "http://example.org/y1", bIt22.get(solMaps.var2).getURI() );
        assertEquals( "http://example.org/z2", bIt22.get(solMaps.var3).getURI() );

        assertFalse( it2.hasNext() );
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
        final TestsForSolutionMappingsIterableWithFilter solMaps= new TestsForSolutionMappingsIterableWithFilter();

        // (var5, y1, var6, x2, var4, p): return all solution mappings: no solution mappings that have value for var4 or var5 or var6
        final Iterable<SolutionMapping> solMapAfterFilter5 = new SolutionMappingsIterableWithThreeVarsFilter( solMaps.getSolMapListWithThreeVar(), solMaps.var5, solMaps.y1, solMaps.var6, solMaps.x2, solMaps.var4, solMaps.p );
        final Iterator<SolutionMapping> it5 = solMapAfterFilter5.iterator();
        assertTrue( it5.hasNext() );
        final Binding bIt51 = it5.next().asJenaBinding();
        assertEquals( 3, bIt51.size() );
        assertEquals( "http://example.org/x1", bIt51.get(solMaps.var1).getURI() );
        assertEquals( "http://example.org/y1", bIt51.get(solMaps.var2).getURI() );
        assertEquals( "http://example.org/z1", bIt51.get(solMaps.var3).getURI() );

        assertTrue( it5.hasNext() );
        final Binding bIt52 = it5.next().asJenaBinding();
        assertEquals( 3, bIt52.size() );
        assertEquals( "http://example.org/x1", bIt52.get(solMaps.var1).getURI() );
        assertEquals( "http://example.org/y1", bIt52.get(solMaps.var2).getURI() );
        assertEquals( "http://example.org/z2", bIt52.get(solMaps.var3).getURI() );

        assertTrue( it5.hasNext() );
        final Binding bIt53 = it5.next().asJenaBinding();
        assertEquals( 3, bIt53.size() );
        assertEquals( "http://example.org/x2", bIt53.get(solMaps.var1).getURI() );
        assertEquals( "http://example.org/y2", bIt53.get(solMaps.var2).getURI() );
        assertEquals( "http://example.org/z2", bIt53.get(solMaps.var3).getURI() );

        assertFalse( it5.hasNext() );
    }
}
