package se.liu.ida.hefquin.base.data.utils;

import static org.junit.Assert.assertFalse;
import static se.liu.ida.hefquin.testutils.AssertExt.assertHasNext;

import java.util.Iterator;

import org.junit.Test;

import se.liu.ida.hefquin.base.data.SolutionMapping;

public class FilteringIteratorForSolMaps_TwoVarsBindingsTest extends TestsForSolutionMappingsIterableWithFilter
{
    @Test
    public void filterWithTwoVars_subset() {
        // iterate over the subset of solution mappings that have (var2, y1, var3, z1)
        final Iterator<SolutionMapping> it = new FilteringIteratorForSolMaps_TwoVarsBindings( getSolMapListWithTwoVar(), var2, y1, var3, z1 );

        assertHasNext( it, "http://example.org/y1", var2, "http://example.org/z1", var3 );

        assertFalse( it.hasNext() );
    }

    @Test
    public void filterWithTwoVar_empty() {
        // no solution mappings that having (var2, y2, var3, z1)
        final Iterator<SolutionMapping> it = new FilteringIteratorForSolMaps_TwoVarsBindings( getSolMapListWithTwoVar(), var2, y2, var3, z1 );

        assertFalse( it.hasNext() );
    }

    @Test
    public void filterWithOneOfTwoVars() {
        // (var1, p, var2, y1): iterate over the subset of solution mappings that have y1 for var2
        final Iterator<SolutionMapping> it = new FilteringIteratorForSolMaps_TwoVarsBindings( getSolMapListWithTwoVar(), var1, p, var2, y1 );

        assertHasNext( it, "http://example.org/y1", var2, "http://example.org/z1", var3 );
        assertHasNext( it, "http://example.org/y1", var2, "http://example.org/z2", var3 );

        assertFalse( it.hasNext() );
    }

    @Test
    public void filterWithNonOfTwoVar() {
        // return all solution mappings: no solution mappings that have value for var1, var4
        final Iterator<SolutionMapping> it = new FilteringIteratorForSolMaps_TwoVarsBindings( getSolMapListWithTwoVar(), var1, p, var4, p );

        assertHasNext( it, "http://example.org/y1", var2, "http://example.org/z1", var3 );
        assertHasNext( it, "http://example.org/y1", var2, "http://example.org/z2", var3 );
        assertHasNext( it, "http://example.org/y2", var2, "http://example.org/z3", var3 );

        assertFalse( it.hasNext() );
    }
}
