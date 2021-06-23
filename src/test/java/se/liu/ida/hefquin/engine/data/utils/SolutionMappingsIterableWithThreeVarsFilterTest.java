package se.liu.ida.hefquin.engine.data.utils;

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

        assertHasNext( it, "http://example.org/x1", var1, "http://example.org/y1", var2, "http://example.org/z1", var3 );

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

        assertHasNext( it, "http://example.org/x1", var1, "http://example.org/y1", var2, "http://example.org/z1", var3 );
        assertHasNext( it, "http://example.org/x1", var1, "http://example.org/y1", var2, "http://example.org/z2", var3 );

        assertFalse( it.hasNext() );
    }

    @Test
    public void solutionMappingsIterableWithThreeVarsFilter_filterWithTwoOfThreeVars_empty() {
        // (var2, y1, var1, x2, var4, p): no solution mappings that having (var2, y1, var1, x2)
        final Iterator<SolutionMapping> it = new SolutionMappingsIterableWithThreeVarsFilter( getSolMapListWithThreeVar(), var2, y1, var1, x2, var4, p).iterator();

        assertFalse( it.hasNext() );
    }

    @Test
    public void solutionMappingsIterableWithThreeVarsFilter_filterWithNoneOfThreeVars_all() {
        // (var5, y1, var6, x2, var4, p): return all solution mappings: no solution mappings that have value for var4 or var5 or var6
        final Iterator<SolutionMapping> it = new SolutionMappingsIterableWithThreeVarsFilter( getSolMapListWithThreeVar(), var5, y1, var6, x2, var4, p ).iterator();

        assertHasNext( it, "http://example.org/x1", var1, "http://example.org/y1", var2, "http://example.org/z1", var3 );
        assertHasNext( it, "http://example.org/x1", var1, "http://example.org/y1", var2, "http://example.org/z2", var3 );
        assertHasNext( it, "http://example.org/x2", var1, "http://example.org/y2", var2, "http://example.org/z2", var3 );

        assertFalse( it.hasNext() );
    }
}
