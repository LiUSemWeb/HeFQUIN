package se.liu.ida.hefquin.base.data.utils;

import static org.junit.Assert.assertFalse;
import static se.liu.ida.hefquin.testutils.AssertExt.assertHasNext;

import java.util.Iterator;

import org.junit.Test;

import se.liu.ida.hefquin.base.data.SolutionMapping;

public class FilteringIteratorForSolMaps_OneVarBindingTest extends TestsForSolutionMappingsIterableWithFilter
{
    @Test
    public void subset() {
        // iterate over the subset of solution mappings that y1 for var2
        final Iterator<SolutionMapping> it = new FilteringIteratorForSolMaps_OneVarBinding( getSolMapListWithTwoVar(), var2, y1 );

        assertHasNext( it, "http://example.org/y1", var2, "http://example.org/z1", var3 );
        assertHasNext( it, "http://example.org/y1", var2, "http://example.org/z2", var3 );

        assertFalse( it.hasNext() );
    }

    @Test
    public void noMatch() {
        // no solution mappings that have p for var3
        final Iterator<SolutionMapping> it = new FilteringIteratorForSolMaps_OneVarBinding( getSolMapListWithTwoVar(), var3, p );

        assertFalse( it.hasNext() );
    }

    @Test
    public void all() {
        // return all solution mappings: no solution mappings that have value for var1
        final Iterator<SolutionMapping> it = new FilteringIteratorForSolMaps_OneVarBinding( getSolMapListWithTwoVar(), var1, y1 );

        assertHasNext( it, "http://example.org/y1", var2, "http://example.org/z1", var3 );
        assertHasNext( it, "http://example.org/y1", var2, "http://example.org/z2", var3 );
        assertHasNext( it, "http://example.org/y2", var2, "http://example.org/z3", var3 );

        assertFalse( it.hasNext() );
    }
}
