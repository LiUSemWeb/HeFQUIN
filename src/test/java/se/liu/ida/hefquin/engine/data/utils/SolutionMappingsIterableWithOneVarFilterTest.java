package se.liu.ida.hefquin.engine.data.utils;

import org.apache.jena.sparql.engine.binding.Binding;
import org.junit.Test;
import se.liu.ida.hefquin.engine.data.SolutionMapping;

import java.util.*;
import static org.junit.Assert.*;

public class SolutionMappingsIterableWithOneVarFilterTest {
    @Test
    public void solMappingsIterableWithOneVarFilter_subset() {
        final TestsForSolutionMappingsIterableWithFilter solMaps= new TestsForSolutionMappingsIterableWithFilter();

        // iterate over the subset of solution mappings that y1 for var2
        final Iterable<SolutionMapping> solMapAfterFilter1 = new SolutionMappingsIterableWithOneVarFilter( solMaps.getSolMapListWithTwoVar(), solMaps.var2, solMaps.y1 );
        final Iterator<SolutionMapping> it1 = solMapAfterFilter1.iterator();
        assertTrue( it1.hasNext() );
        final Binding bIt1 = it1.next().asJenaBinding();
        assertEquals( 2, bIt1.size() );

        assertTrue( it1.hasNext() );
        final Binding bIt2 = it1.next().asJenaBinding();
        assertEquals( 2, bIt2.size() );

        assertFalse( it1.hasNext() );
    }

    @Test
    public void solMappingsIterableWithOneVarFilter_noMatching() {
        final TestsForSolutionMappingsIterableWithFilter solMaps= new TestsForSolutionMappingsIterableWithFilter();

        // no solution mappings that have p for var3
        final Iterable<SolutionMapping> solMapAfterFilter2 = new SolutionMappingsIterableWithOneVarFilter( solMaps.getSolMapListWithTwoVar(), solMaps.var3, solMaps.p );
        final Iterator<SolutionMapping> it2 = solMapAfterFilter2.iterator();

        assertFalse( it2.hasNext() );
    }

    @Test
    public void solMappingsIterableWithOneVarFilter_all() {
        final TestsForSolutionMappingsIterableWithFilter solMaps= new TestsForSolutionMappingsIterableWithFilter();

        // return all solution mappings: no solution mappings that have value for var1
        final Iterable<SolutionMapping> solMapAfterFilter3 = new SolutionMappingsIterableWithOneVarFilter( solMaps.getSolMapListWithTwoVar(), solMaps.var1, solMaps.y1 );
        final Iterator<SolutionMapping> it3 = solMapAfterFilter3.iterator();
        assertTrue( it3.hasNext() );
        final Binding bIt31 = it3.next().asJenaBinding();
        assertEquals( 2, bIt31.size() );

        assertTrue( it3.hasNext() );
        final Binding bIt32 = it3.next().asJenaBinding();
        assertEquals( 2, bIt32.size() );

        assertTrue( it3.hasNext() );
        final Binding bIt33 = it3.next().asJenaBinding();
        assertEquals( 2, bIt33.size() );

        assertFalse( it3.hasNext() );
    }
}
