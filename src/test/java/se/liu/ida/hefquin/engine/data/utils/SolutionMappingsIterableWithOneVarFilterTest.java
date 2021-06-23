package se.liu.ida.hefquin.engine.data.utils;

import org.junit.Test;
import se.liu.ida.hefquin.engine.data.SolutionMapping;

import java.util.*;
import static org.junit.Assert.*;

public class SolutionMappingsIterableWithOneVarFilterTest extends TestsForSolutionMappingsIterableWithFilter
{
    @Test
    public void solMappingsIterableWithOneVarFilter_subset() {
        // iterate over the subset of solution mappings that y1 for var2
        final Iterator<SolutionMapping> it = new SolutionMappingsIterableWithOneVarFilter( getSolMapListWithTwoVar(), var2, y1 ).iterator();

        assertHasNext( it, "http://example.org/y1", var2, "http://example.org/z1", var3 );
        assertHasNext( it, "http://example.org/y1", var2, "http://example.org/z2", var3 );

        assertFalse( it.hasNext() );
    }

    @Test
    public void solMappingsIterableWithOneVarFilter_noMatching() {
        // no solution mappings that have p for var3
        final Iterator<SolutionMapping> it = new SolutionMappingsIterableWithOneVarFilter( getSolMapListWithTwoVar(), var3, p ).iterator();

        assertFalse( it.hasNext() );
    }

    @Test
    public void solMappingsIterableWithOneVarFilter_all() {
        // return all solution mappings: no solution mappings that have value for var1
        final Iterator<SolutionMapping> it = new SolutionMappingsIterableWithOneVarFilter( getSolMapListWithTwoVar(), var1, y1 ).iterator();

        assertHasNext( it, "http://example.org/y1", var2, "http://example.org/z1", var3 );
        assertHasNext( it, "http://example.org/y1", var2, "http://example.org/z2", var3 );
        assertHasNext( it, "http://example.org/y2", var2, "http://example.org/z3", var3 );

        assertFalse( it.hasNext() );
    }
}
