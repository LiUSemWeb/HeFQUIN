package se.liu.ida.hefquin.engine.datastructures.impl;

import org.apache.jena.sparql.engine.binding.Binding;
import org.junit.Test;
import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.impl.SolutionMappingUtils;

import java.util.*;

import static org.junit.Assert.*;

public class SolutionMappingsHashTableBasedOnOneVarTest extends TestsForSolutionMappingsIndex {
    @Test
    public void hashTableWithOneInputVariable_basic() {
        // test method: isEmpty(), contains(), add(), size(), clear()
        final SolutionMappingsIndexBase solMHashTable = createHashTableBasedOneVar();

        assertFalse(solMHashTable.isEmpty());
        assertEquals( 3, solMHashTable.size() );

		final SolutionMapping sm = SolutionMappingUtils.createSolutionMapping(var2, y1, var3, z1);
        assertTrue(solMHashTable.contains(sm));

        solMHashTable.clear();
        assertTrue(solMHashTable.isEmpty());
        assertFalse(solMHashTable.contains(sm));
    }

    @Test
    public void hashTableWithOneInputVariable_getAllSolMaps() {
        // test getAllSolutionMappings()
        final Iterator<SolutionMapping> it = createHashTableBasedOneVar().getAllSolutionMappings().iterator();

        assertTrue( it.hasNext() );
        final Binding b1 = it.next().asJenaBinding();
        assertEquals( 2, b1.size() );

        assertTrue( it.hasNext() );
        final Binding b2 = it.next().asJenaBinding();
        assertEquals( 2, b2.size() );

        assertTrue( it.hasNext() );
        final Binding b3 = it.next().asJenaBinding();
        assertEquals( 2, b3.size() );

        assertFalse( it.hasNext() );
    }

    @Test
    public void hashTableWithOneInputVariable_findSolutionMappings1() {
        // findSolutionMappings(var2, y2): return one matching solution mapping (hash table is built based on var2)
        final Iterator<SolutionMapping> it = createHashTableBasedOneVar().findSolutionMappings(var2, y2).iterator();

        assertHasNext( it, "http://example.org/y2", var2, "http://example.org/z3", var3 );

        assertFalse( it.hasNext() );
    }

    @Test
    public void hashTableWithOneInputVariable_findSolutionMappings2() {
        //findSolutionMappings(var3, z3): return one matching solution mapping (hash table is not built based on var3)
        final Iterator<SolutionMapping> it = createHashTableBasedOneVar().findSolutionMappings(var3, z3).iterator();

        assertHasNext( it, "http://example.org/y2", var2, "http://example.org/z3", var3 );

        assertFalse( it.hasNext() );
    }

    @Test
    public void hashTableWithOneInputVariable_findSolutionMappings3() {
        // findSolutionMappings(var3, z3, var2, y2): matching one solution mapping
        final Iterator<SolutionMapping> it = createHashTableBasedOneVar().findSolutionMappings(var3, z3, var2, y2).iterator();

        assertHasNext( it, "http://example.org/y2", var2, "http://example.org/z3", var3 );

        assertFalse( it.hasNext() );
    }

    @Test
    public void hashTableWithOneInputVariable_findSolutionMappings4() {
        // findSolutionMappings(var2, y2, var3, z1): no matching solution mappings
        final Iterator<SolutionMapping> it = createHashTableBasedOneVar().findSolutionMappings(var2, y2, var3, z1).iterator();

        assertFalse( it.hasNext() );
    }

    @Test
    public void hashTableWithOneInputVariable_findSolutionMappings5() {
        // findSolutionMappings(var1, x1, var2, y2, var3, z3)
        final Iterator<SolutionMapping> it = createHashTableBasedOneVar().findSolutionMappings(var1, x1, var2, y2, var3, z3).iterator();

        assertHasNext( it, "http://example.org/y2", var2, "http://example.org/z3", var3 );

        assertFalse( it.hasNext() );
    }

    @Test
    public void hashTableWithOneInputVariable_getJoinPartners1() {
        // getJoinPartners(): one join variable with two join partners
        final SolutionMapping sm = SolutionMappingUtils.createSolutionMapping(var1, x1, var2, y1);
        final Iterator<SolutionMapping> it = createHashTableBasedOneVar().getJoinPartners(sm).iterator();

        final Set<Binding> result = new HashSet<>();
        //final Iterator<SolutionMapping> it1 = matchSolMap1.iterator();
        assertTrue( it.hasNext() );
        result.add( it.next().asJenaBinding() );
        assertTrue( it.hasNext() );
        result.add( it.next().asJenaBinding() );
        assertFalse( it.hasNext() );

        for ( final Binding b: result ) {
            assertEquals( 2, b.size() );
            if ( b.get(var3).getURI().equals("http://example.org/z1") ) {
                assertEquals( "http://example.org/y1", b.get(var2).getURI() );
            }
            else if ( b.get(var3).getURI().equals("http://example.org/z2") ) {
                assertEquals( "http://example.org/y1", b.get(var2).getURI() );
            }
            else {
                fail( "Unexpected URI for ?v3: " + b.get(var3).getURI() );
            }
        }
    }

    @Test
    public void hashTableWithOneInputVariable_getJoinPartners2() {
        // getJoinPartners(): one join variable. Return two solution mappings without post matching
        final SolutionMapping sm = SolutionMappingUtils.createSolutionMapping(var2, y1, var3, z2);
        final Iterator<SolutionMapping> it = createHashTableBasedOneVar().getJoinPartners(sm).iterator();

        final Set<Binding> result = new HashSet<>();
        assertTrue( it.hasNext() );
        result.add( it.next().asJenaBinding() );
        assertTrue( it.hasNext() );
        result.add( it.next().asJenaBinding() );
        assertFalse( it.hasNext() );

        for ( final Binding b: result ) {
            assertEquals( 2, b.size() );
            if ( b.get(var3).getURI().equals("http://example.org/z1") ) {
                assertEquals( "http://example.org/y1", b.get(var2).getURI() );
            }
            else if ( b.get(var3).getURI().equals("http://example.org/z2") ) {
                assertEquals( "http://example.org/y1", b.get(var2).getURI() );
            }
            else {
                fail( "Unexpected URI for ?v3: " + b.get(var3).getURI() );
            }
        }

    }

    @Test
    public void hashTableWithOneInputVariable_getJoinPartners3() {
        // getJoinPartners(): do not contain the join variable. Return all solution mappings if no postingMatching is applied.
        final SolutionMapping sm = SolutionMappingUtils.createSolutionMapping(var3, z3);
        final Iterator<SolutionMapping> it = createHashTableBasedOneVar().getJoinPartners(sm).iterator();

        assertTrue( it.hasNext() );
        final Binding b1 = it.next().asJenaBinding();
        assertEquals( 2, b1.size() );

        assertTrue( it.hasNext() );
        final Binding b2 = it.next().asJenaBinding();
        assertEquals( 2, b2.size() );

        assertTrue( it.hasNext() );
        final Binding b3 = it.next().asJenaBinding();
        assertEquals( 2, b3.size() );

        assertFalse( it.hasNext() );
    }

    @Test
    public void hashTableWithOneInputVariable_getJoinPartners4() {
        // getJoinPartners(): one join variable but without join partner
        final SolutionMapping sm = SolutionMappingUtils.createSolutionMapping(var1, x2, var2, y3);
        final Iterator<SolutionMapping> it = createHashTableBasedOneVar().getJoinPartners(sm).iterator();

        assertFalse( it.hasNext() );
    }
}
