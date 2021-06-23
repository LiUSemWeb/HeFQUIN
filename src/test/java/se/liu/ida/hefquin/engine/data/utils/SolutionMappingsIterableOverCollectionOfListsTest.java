package se.liu.ida.hefquin.engine.data.utils;

import org.junit.Test;
import se.liu.ida.hefquin.engine.data.SolutionMapping;

import java.util.*;

import static org.junit.Assert.assertFalse;

public class SolutionMappingsIterableOverCollectionOfListsTest extends TestsForSolutionMappingsIterableWithFilter
{
    @Test
    public void oneList() {
        final Collection<List<SolutionMapping>> solMapCollection = new ArrayList<>();
        solMapCollection.add( getSolMapListWithTwoVar() );

        // Iterate over all solution mappings contained in a collection
        final Iterator<SolutionMapping> it = new SolutionMappingsIterableOverCollectionOfLists(solMapCollection).iterator();

        assertHasNext( it, "http://example.org/y1", var2, "http://example.org/z1", var3 );
        assertHasNext( it, "http://example.org/y1", var2, "http://example.org/z2", var3 );
        assertHasNext( it, "http://example.org/y2", var2, "http://example.org/z3", var3 );
        assertFalse( it.hasNext() );
    }

    @Test
    public void twoListsSame() {
        final Collection<List<SolutionMapping>> solMapCollection = new ArrayList<>();
        solMapCollection.add( getSolMapListWithTwoVar() );
        solMapCollection.add( getSolMapListWithTwoVar() );

        // Iterate over all solution mappings contained in a collection
        final Iterator<SolutionMapping> it = new SolutionMappingsIterableOverCollectionOfLists(solMapCollection).iterator();

        assertHasNext( it, "http://example.org/y1", var2, "http://example.org/z1", var3 );
        assertHasNext( it, "http://example.org/y1", var2, "http://example.org/z2", var3 );
        assertHasNext( it, "http://example.org/y2", var2, "http://example.org/z3", var3 );

        assertHasNext( it, "http://example.org/y1", var2, "http://example.org/z1", var3 );
        assertHasNext( it, "http://example.org/y1", var2, "http://example.org/z2", var3 );
        assertHasNext( it, "http://example.org/y2", var2, "http://example.org/z3", var3 );

        assertFalse( it.hasNext() );
    }

    @Test
    public void twoListsDifferent() {
        final Collection<List<SolutionMapping>> solMapCollection = new ArrayList<>();
        solMapCollection.add( getSolMapListWithTwoVar() );
        solMapCollection.add( getSolMapListWithThreeVar() );

        // Iterate over all solution mappings contained in a collection
        final Iterator<SolutionMapping> it = new SolutionMappingsIterableOverCollectionOfLists(solMapCollection).iterator();

        assertHasNext( it, "http://example.org/y1", var2, "http://example.org/z1", var3 );
        assertHasNext( it, "http://example.org/y1", var2, "http://example.org/z2", var3 );
        assertHasNext( it, "http://example.org/y2", var2, "http://example.org/z3", var3 );

        assertHasNext( it, "http://example.org/y1", var2, "http://example.org/z1", var3, "http://example.org/x1", var1 );
        assertHasNext( it, "http://example.org/y1", var2, "http://example.org/z2", var3, "http://example.org/x1", var1 );
        assertHasNext( it, "http://example.org/y2", var2, "http://example.org/z2", var3, "http://example.org/x2", var1 );

        assertFalse( it.hasNext() );
    }

    @Test
    public void twoListsOneEmpty1() {
        final Collection<List<SolutionMapping>> solMapCollection = new ArrayList<>();
        solMapCollection.add( getSolMapListWithTwoVar() );
        solMapCollection.add( new ArrayList<>() );  // <--- empty list second

        // Iterate over all solution mappings contained in a collection
        final Iterator<SolutionMapping> it = new SolutionMappingsIterableOverCollectionOfLists(solMapCollection).iterator();

        assertHasNext( it, "http://example.org/y1", var2, "http://example.org/z1", var3 );
        assertHasNext( it, "http://example.org/y1", var2, "http://example.org/z2", var3 );
        assertHasNext( it, "http://example.org/y2", var2, "http://example.org/z3", var3 );

        assertFalse( it.hasNext() );
    }

    @Test
    public void twoListsOneEmpty2() {
        final Collection<List<SolutionMapping>> solMapCollection = new ArrayList<>();
        solMapCollection.add( new ArrayList<>() );  // <--- empty list first
        solMapCollection.add( getSolMapListWithTwoVar() );

        // Iterate over all solution mappings contained in a collection
        final Iterator<SolutionMapping> it = new SolutionMappingsIterableOverCollectionOfLists(solMapCollection).iterator();

        assertHasNext( it, "http://example.org/y1", var2, "http://example.org/z1", var3 );
        assertHasNext( it, "http://example.org/y1", var2, "http://example.org/z2", var3 );
        assertHasNext( it, "http://example.org/y2", var2, "http://example.org/z3", var3 );

        assertFalse( it.hasNext() );
    }

    @Test
    public void twoEmptyLists() {
        final Collection<List<SolutionMapping>> solMapCollection = new ArrayList<>();
        solMapCollection.add( new ArrayList<>() );
        solMapCollection.add( new ArrayList<>() );

        // Iterate over all solution mappings contained in a collection
        final Iterator<SolutionMapping> it = new SolutionMappingsIterableOverCollectionOfLists(solMapCollection).iterator();

        assertFalse( it.hasNext() );
    }

}
