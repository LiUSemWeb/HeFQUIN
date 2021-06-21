package se.liu.ida.hefquin.engine.data.utils;

import org.apache.jena.sparql.engine.binding.Binding;
import org.junit.Test;
import se.liu.ida.hefquin.engine.data.SolutionMapping;

import java.util.*;

import static org.junit.Assert.*;

public class SolutionMappingsIterableOverCollectionOfListsTest {
    @Test
    public void solMappingsIterableOverCollectionOfLists() {
        final Collection<List<SolutionMapping>> solMapCollection = new ArrayList<>();
        final TestsForSolutionMappingsIterableWithFilter solMaps= new TestsForSolutionMappingsIterableWithFilter();
        solMapCollection.add(solMaps.getSolMapListWithTwoVar());

        // Iterate over all solution mappings contained in a collection
        final Iterable<SolutionMapping> allSolMap = new SolutionMappingsIterableOverCollectionOfLists(solMapCollection);
        final Iterator<SolutionMapping> it3 = allSolMap.iterator();

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
