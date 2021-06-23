package se.liu.ida.hefquin.engine.data.utils;

import org.junit.Test;
import se.liu.ida.hefquin.engine.data.SolutionMapping;

import java.util.Iterator;

import static org.junit.Assert.*;

public class SolutionMappingsIterableWithTwoVarsFilterTest extends TestsForSolutionMappingsIterableWithFilter
{
    @Test
    public void filterWithTwoVars_subset() {
        // iterate over the subset of solution mappings that have (var2, y1, var3, z1)
        final Iterator<SolutionMapping> it = new SolutionMappingsIterableWithTwoVarsFilter( getSolMapListWithTwoVar(), var2, y1, var3, z1 ).iterator();

        assertHasNext( it, "http://example.org/y1", var2, "http://example.org/z1", var3 );

        assertFalse( it.hasNext() );
    }

    @Test
    public void filterWithTwoVar_empty() {
        // no solution mappings that having (var2, y2, var3, z1)
        final Iterator<SolutionMapping> it = new SolutionMappingsIterableWithTwoVarsFilter( getSolMapListWithTwoVar(), var2, y2, var3, z1 ).iterator();

        assertFalse( it.hasNext() );
    }

    @Test
    public void filterWithOneOfTwoVars() {
        // (var1, p, var2, y1): iterate over the subset of solution mappings that have y1 for var2
        final Iterator<SolutionMapping> it = new SolutionMappingsIterableWithTwoVarsFilter( getSolMapListWithTwoVar(), var1, p, var2, y1).iterator();

        assertHasNext( it, "http://example.org/y1", var2, "http://example.org/z1", var3 );
        assertHasNext( it, "http://example.org/y1", var2, "http://example.org/z2", var3 );

        assertFalse( it.hasNext() );
    }

    @Test
    public void filterWithNonOfTwoVar() {
        // return all solution mappings: no solution mappings that have value for var1, var4
        final Iterator<SolutionMapping> it = new SolutionMappingsIterableWithTwoVarsFilter( getSolMapListWithTwoVar(), var1, p, var4, p).iterator();

        assertHasNext( it, "http://example.org/y1", var2, "http://example.org/z1", var3 );
        assertHasNext( it, "http://example.org/y1", var2, "http://example.org/z2", var3 );
        assertHasNext( it, "http://example.org/y2", var2, "http://example.org/z3", var3 );

        assertFalse( it.hasNext() );
    }
}
