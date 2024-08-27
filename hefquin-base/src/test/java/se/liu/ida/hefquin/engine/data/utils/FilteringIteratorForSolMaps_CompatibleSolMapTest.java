package se.liu.ida.hefquin.engine.data.utils;

import org.junit.Test;
import se.liu.ida.hefquin.engine.data.SolutionMapping;

import java.util.Iterator;

import static org.junit.Assert.*;

public class FilteringIteratorForSolMaps_CompatibleSolMapTest extends TestsForSolutionMappingsIterableWithFilter
{
    @Test
    public void solutionMappingsIterableWithSolMapFilter_sm1() {
        // iterate over the subset of solution mappings that are compatible with sm (var2, y1, var3, z1)
        final SolutionMapping sm = SolutionMappingUtils.createSolutionMapping(var2, y1, var3, z1);
        final Iterator<SolutionMapping> it = new FilteringIteratorForSolMaps_CompatibleSolMap( getSolMapListWithTwoVar(), sm );

        assertHasNext( it, "http://example.org/y1", var2, "http://example.org/z1", var3 );

        assertFalse( it.hasNext() );
    }

    @Test
    public void solutionMappingsIterableWithSolMapFilter_sm2() {
        // no solution mappings that are compatible with sm (var2, y2, var3, z1)
        final SolutionMapping sm = SolutionMappingUtils.createSolutionMapping(var2, y2, var3, z1);
        final Iterator<SolutionMapping> it = new FilteringIteratorForSolMaps_CompatibleSolMap( getSolMapListWithTwoVar(), sm );

        assertFalse( it.hasNext() );
    }

    @Test
    public void solutionMappingsIterableWithSolMapFilter_sm3() {
        // two solution mappings that are compatible with sm (var2, y1, var1, x1).
        final SolutionMapping sm = SolutionMappingUtils.createSolutionMapping(var2, y1, var1, x1);
        final Iterator<SolutionMapping> it = new FilteringIteratorForSolMaps_CompatibleSolMap( getSolMapListWithTwoVar(), sm );

        assertHasNext( it, "http://example.org/y1", var2, "http://example.org/z1", var3 );
        assertHasNext( it, "http://example.org/y1", var2, "http://example.org/z2", var3 );

        assertFalse( it.hasNext() );
    }

    @Test
    public void solutionMappingsIterableWithSolMapFilter_sm4() {
        // all solution mappings that are compatible with sm (var1, x1)
        final SolutionMapping sm = SolutionMappingUtils.createSolutionMapping(var1, x1);
        final Iterator<SolutionMapping> it = new FilteringIteratorForSolMaps_CompatibleSolMap( getSolMapListWithTwoVar(), sm );

        assertHasNext( it, "http://example.org/y1", var2, "http://example.org/z1", var3 );
        assertHasNext( it, "http://example.org/y1", var2, "http://example.org/z2", var3 );
        assertHasNext( it, "http://example.org/y2", var2, "http://example.org/z3", var3 );

        assertFalse( it.hasNext() );
    }
}
