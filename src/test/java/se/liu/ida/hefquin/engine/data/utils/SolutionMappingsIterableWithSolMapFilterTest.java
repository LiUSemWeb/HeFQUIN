package se.liu.ida.hefquin.engine.data.utils;

import org.apache.jena.sparql.engine.binding.Binding;
import org.junit.Test;
import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.impl.SolutionMappingUtils;

import java.util.Iterator;

import static org.junit.Assert.*;

public class SolutionMappingsIterableWithSolMapFilterTest {
    @Test
    public void solutionMappingsIterableWithSolMapFilter_sm1() {
        final TestsForSolutionMappingsIterableWithFilter solMaps= new TestsForSolutionMappingsIterableWithFilter();
        final SolutionMapping sm1 = SolutionMappingUtils.createSolutionMapping(solMaps.var2, solMaps.y1, solMaps.var3, solMaps.z1);

        // iterate over the subset of solution mappings that are compatible with sm1 (var2, y1, var3, z1)
        final Iterable<SolutionMapping> solMapAfterFilter1 = new SolutionMappingsIterableWithSolMapFilter( solMaps.getSolMapListWithTwoVar(), sm1 );
        final Iterator<SolutionMapping> it1 = solMapAfterFilter1.iterator();
        assertTrue( it1.hasNext() );
        final Binding bIt1 = it1.next().asJenaBinding();
        assertEquals( 2, bIt1.size() );

        assertFalse( it1.hasNext() );
    }

    @Test
    public void solutionMappingsIterableWithSolMapFilter_sm2() {
        final TestsForSolutionMappingsIterableWithFilter solMaps= new TestsForSolutionMappingsIterableWithFilter();
        final SolutionMapping sm2 = SolutionMappingUtils.createSolutionMapping(solMaps.var2, solMaps.y2, solMaps.var3, solMaps.z1);

        // no solution mappings that are compatible with sm00 (var2, y2, var3, z1)
        final Iterable<SolutionMapping> solMapAfterFilter2 = new SolutionMappingsIterableWithSolMapFilter( solMaps.getSolMapListWithTwoVar(), sm2 );
        final Iterator<SolutionMapping> it2 = solMapAfterFilter2.iterator();

        assertFalse( it2.hasNext() );
    }

    @Test
    public void solutionMappingsIterableWithSolMapFilter_sm3() {
        final TestsForSolutionMappingsIterableWithFilter solMaps= new TestsForSolutionMappingsIterableWithFilter();
        final SolutionMapping sm3 = SolutionMappingUtils.createSolutionMapping(solMaps.var2, solMaps.y1, solMaps.var1, solMaps.x1);

        // two solution mappings that are compatible with sm3 (var2, y1, var1, x1)
        final Iterable<SolutionMapping> solMapAfterFilter3 = new SolutionMappingsIterableWithSolMapFilter( solMaps.getSolMapListWithTwoVar(), sm3 );
        final Iterator<SolutionMapping> it3 = solMapAfterFilter3.iterator();

        assertTrue( it3.hasNext() );
        final Binding bIt31 = it3.next().asJenaBinding();
        assertEquals( 2, bIt31.size() );

        assertTrue( it3.hasNext() );
        final Binding bIt32 = it3.next().asJenaBinding();
        assertEquals( 2, bIt32.size() );

        assertFalse( it3.hasNext() );
    }

    @Test
    public void solutionMappingsIterableWithSolMapFilter_sm4() {
        final TestsForSolutionMappingsIterableWithFilter solMaps= new TestsForSolutionMappingsIterableWithFilter();
        final SolutionMapping sm4 = SolutionMappingUtils.createSolutionMapping(solMaps.var1, solMaps.x1);

        // all solution mappings that are compatible with sm4 (var1, x1)
        final Iterable<SolutionMapping> solMapAfterFilter4 = new SolutionMappingsIterableWithSolMapFilter( solMaps.getSolMapListWithTwoVar(), sm4 );
        final Iterator<SolutionMapping> it4 = solMapAfterFilter4.iterator();

        assertTrue( it4.hasNext() );
        final Binding bIt41 = it4.next().asJenaBinding();
        assertEquals( 2, bIt41.size() );

        assertTrue( it4.hasNext() );
        final Binding bIt42 = it4.next().asJenaBinding();
        assertEquals( 2, bIt42.size() );

        assertTrue( it4.hasNext() );
        final Binding bIt43 = it4.next().asJenaBinding();
        assertEquals( 2, bIt43.size() );

        assertFalse( it4.hasNext() );
    }
}
