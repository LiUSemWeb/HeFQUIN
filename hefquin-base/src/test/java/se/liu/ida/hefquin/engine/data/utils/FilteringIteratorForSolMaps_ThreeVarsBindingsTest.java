package se.liu.ida.hefquin.engine.data.utils;

import org.junit.Test;
import se.liu.ida.hefquin.engine.data.SolutionMapping;
import java.util.Iterator;

import static org.junit.Assert.*;
import static se.liu.ida.hefquin.testutils.AssertExt.assertHasNext;

public class FilteringIteratorForSolMaps_ThreeVarsBindingsTest extends TestsForSolutionMappingsIterableWithFilter
{
    @Test
    public void filterWithThreeVars_subset() {
        // iterate over the subset of solution mappings that have (var3, z1, var1, x1, var2, y1)
        final Iterator<SolutionMapping> it = new FilteringIteratorForSolMaps_ThreeVarsBindings( getSolMapListWithThreeVar(), var3, z1, var1, x1, var2, y1 );

        assertHasNext( it, "http://example.org/x1", var1, "http://example.org/y1", var2, "http://example.org/z1", var3 );

        assertFalse( it.hasNext() );
    }

    @Test
    public void filterWithThreeVars_empty() {
        // no solution mappings that having (var1, x2, var2, y1, var3, z2)
        final Iterator<SolutionMapping> it = new FilteringIteratorForSolMaps_ThreeVarsBindings( getSolMapListWithThreeVar(), var1, x2, var2, y1, var3, z2 );

        assertFalse( it.hasNext() );
    }

    @Test
    public void filterWithTwoOfThreeVars_subset() {
        // (var2, y1, var1, x1, var4, p): filter based on subset of variables
        final Iterator<SolutionMapping> it = new FilteringIteratorForSolMaps_ThreeVarsBindings( getSolMapListWithThreeVar(), var2, y1, var1, x1, var4, p );

        assertHasNext( it, "http://example.org/x1", var1, "http://example.org/y1", var2, "http://example.org/z1", var3 );
        assertHasNext( it, "http://example.org/x1", var1, "http://example.org/y1", var2, "http://example.org/z2", var3 );

        assertFalse( it.hasNext() );
    }

    @Test
    public void filterWithTwoOfThreeVars_empty() {
        // (var2, y1, var1, x2, var4, p): no solution mappings that having (var2, y1, var1, x2)
        final Iterator<SolutionMapping> it = new FilteringIteratorForSolMaps_ThreeVarsBindings( getSolMapListWithThreeVar(), var2, y1, var1, x2, var4, p );

        assertFalse( it.hasNext() );
    }

    @Test
    public void filterWithNoneOfThreeVars_all() {
        // (var5, y1, var6, x2, var4, p): return all solution mappings: no solution mappings that have value for var4 or var5 or var6
        final Iterator<SolutionMapping> it = new FilteringIteratorForSolMaps_ThreeVarsBindings( getSolMapListWithThreeVar(), var5, y1, var6, x2, var4, p );

        assertHasNext( it, "http://example.org/x1", var1, "http://example.org/y1", var2, "http://example.org/z1", var3 );
        assertHasNext( it, "http://example.org/x1", var1, "http://example.org/y1", var2, "http://example.org/z2", var3 );
        assertHasNext( it, "http://example.org/x2", var1, "http://example.org/y2", var2, "http://example.org/z2", var3 );

        assertFalse( it.hasNext() );
    }
}
