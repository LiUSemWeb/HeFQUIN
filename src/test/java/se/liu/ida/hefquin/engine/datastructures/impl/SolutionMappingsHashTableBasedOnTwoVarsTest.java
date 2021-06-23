package se.liu.ida.hefquin.engine.datastructures.impl;

import org.apache.jena.sparql.engine.binding.Binding;
import org.junit.Test;
import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.engine.datastructures.SolutionMappingsIndex;

import java.util.*;

import static org.junit.Assert.*;

public class SolutionMappingsHashTableBasedOnTwoVarsTest extends TestsForSolutionMappingsIndex {
    @Test
    public void basic() {
        // test method: isEmpty(), contains(), add(), size(), clear()
        final SolutionMappingsIndex solMHashTable = createHashTableBasedTwoVars();

        assertFalse(solMHashTable.isEmpty());
        assertEquals( 3, solMHashTable.size() );

        final SolutionMapping sm = SolutionMappingUtils.createSolutionMapping(var1, x1, var2, y1, var3, z1);
        assertTrue(solMHashTable.contains(sm));

        solMHashTable.clear();
        assertTrue(solMHashTable.isEmpty());
        assertFalse(solMHashTable.contains(sm));
    }

    @Test
    public void getAllSolMaps() {
        // test getAllSolutionMappings()
        final Iterator<SolutionMapping> it = createHashTableBasedTwoVars().getAllSolutionMappings().iterator();

        assertTrue( it.hasNext() );
        final Binding b1 = it.next().asJenaBinding();
        assertEquals( 3, b1.size() );

        assertTrue( it.hasNext() );
        final Binding b2 = it.next().asJenaBinding();
        assertEquals( 3, b2.size() );

        assertTrue( it.hasNext() );
        final Binding b3 = it.next().asJenaBinding();
        assertEquals( 3, b3.size() );

        assertFalse( it.hasNext() );
    }

    @Test
    public void findSolutionMappings1() {
        // findSolutionMappings(var2, y2): one matching solution mapping
        final Iterator<SolutionMapping> it = createHashTableBasedTwoVars().findSolutionMappings(var2, y2).iterator();

        assertHasNext( it, "http://example.org/x2", var1, "http://example.org/y2", var2, "http://example.org/z2", var3 );

        assertFalse( it.hasNext() );
    }

    @Test
    public void findSolutionMappings2() {
        // findSolutionMappings(var3, z1): return one matching solution mapping (hash table is not built based on var3)
        final Iterator<SolutionMapping> it = createHashTableBasedTwoVars().findSolutionMappings(var3, z1).iterator();

        assertHasNext( it, "http://example.org/x1", var1, "http://example.org/y1", var2, "http://example.org/z1", var3 );

        assertFalse( it.hasNext() );
    }

    @Test
    public void findSolutionMappings3() {
        // findSolutionMappings(var1, x2, var2, y2): return one matching solution mapping (hash table is built based on (var1, var2))
        final Iterator<SolutionMapping> it = createHashTableBasedTwoVars().findSolutionMappings(var1, x2, var2, y2).iterator();

        assertHasNext( it, "http://example.org/x2", var1, "http://example.org/y2", var2, "http://example.org/z2", var3 );

        assertFalse( it.hasNext() );
    }

    @Test
    public void hashTableWithTwoInputVariable_findSolutionMappings4() {
        // findSolutionMappings(var2, y2, var1, x2), change order of var1 and var2
        final Iterator<SolutionMapping> it = createHashTableBasedTwoVars().findSolutionMappings(var2, y2, var1, x2).iterator();

        assertHasNext( it, "http://example.org/x2", var1, "http://example.org/y2", var2, "http://example.org/z2", var3 );

        assertFalse( it.hasNext() );
    }

    @Test
    public void findSolutionMappings5() {
        // findSolutionMappings(var1, x1, var3, z1): return one matching solution mapping
        final Iterator<SolutionMapping> it = createHashTableBasedTwoVars().findSolutionMappings(var1, x1, var3, z1).iterator();

        assertHasNext( it, "http://example.org/x1", var1, "http://example.org/y1", var2, "http://example.org/z1", var3 );

        assertFalse( it.hasNext() );
    }

    @Test
    public void findSolutionMappings6() {
        // findSolutionMappings(var1, x1, var2, y1, var3, z1)
        final Iterator<SolutionMapping> it = createHashTableBasedTwoVars().findSolutionMappings(var1, x1, var2, y1, var3, z1).iterator();

        assertHasNext( it, "http://example.org/x1", var1, "http://example.org/y1", var2, "http://example.org/z1", var3 );

        assertFalse( it.hasNext() );
    }

    @Test
    public void findSolutionMappings7() {
        // findSolutionMappings(var1, x1, var2, y2, var3, z1): no matching solution mapping
        final Iterator<SolutionMapping> it = createHashTableBasedTwoVars().findSolutionMappings(var1, x1, var2, y2, var3, z1).iterator();

        assertFalse( it.hasNext() );
    }

    @Test
    public void getJoinPartners1() {
        // getJoinPartners(): two join variables with two join partners
        final SolutionMapping sm = SolutionMappingUtils.createSolutionMapping(var1, x1, var2, y1);
        final Iterator<SolutionMapping> it = createHashTableBasedTwoVars().getJoinPartners(sm).iterator();

        final Set<Binding> result = new HashSet<>();
        assertTrue( it.hasNext() );
        result.add( it.next().asJenaBinding() );

        assertTrue( it.hasNext() );
        result.add( it.next().asJenaBinding() );

        assertFalse( it.hasNext() );

        for ( final Binding b: result ) {
            assertEquals( 3, b.size() );
            if ( b.get(var3).getURI().equals("http://example.org/z1") ) {
                assertEquals( "http://example.org/x1", b.get(var1).getURI() );
                assertEquals( "http://example.org/y1", b.get(var2).getURI() );

            }
            else if ( b.get(var3).getURI().equals("http://example.org/z2") ) {
                assertEquals( "http://example.org/x1", b.get(var1).getURI() );
                assertEquals( "http://example.org/y1", b.get(var2).getURI() );
            }
            else {
                fail( "Unexpected URI for ?v3: " + b.get(var3).getURI() );
            }
        }
    }

    @Test
    public void getJoinPartners2() {
        // getJoinPartners(): two join variables but without join partner (case 1: subset matching)
        final SolutionMapping sm = SolutionMappingUtils.createSolutionMapping(var1, x1, var2, y2);
        final Iterator<SolutionMapping> it = createHashTableBasedTwoVars().getJoinPartners(sm).iterator();

        assertFalse( it.hasNext() );

    }

    @Test
    public void getJoinPartners3() {
        // getJoinPartners(): two join variables but without join partner (case 2: no matching)
        final SolutionMapping sm = SolutionMappingUtils.createSolutionMapping(var1, x3, var2, y3);
        final Iterator<SolutionMapping> it = createHashTableBasedTwoVars().getJoinPartners(sm).iterator();

        assertFalse( it.hasNext() );
    }

    @Test
    public void getJoinPartners4() {
        // getJoinPartners(): do not contain complete join variables.
        final SolutionMapping sm = SolutionMappingUtils.createSolutionMapping(var2, y2);
        final Iterator<SolutionMapping> it = createHashTableBasedTwoVars().getJoinPartners(sm).iterator();

        assertHasNext( it, "http://example.org/x2", var1, "http://example.org/y2", var2, "http://example.org/z2", var3 );

        assertFalse( it.hasNext() );
    }

    @Test
    public void getJoinPartners5() {
        // getJoinPartners(): do not contain complete join variables. Find one join partner if not post matching
        final SolutionMapping sm = SolutionMappingUtils.createSolutionMapping(var2, y2, var3, z1);
        final Iterator<SolutionMapping> it = createHashTableBasedTwoVars().getJoinPartners(sm).iterator();

        // (var1, x2, var2, y2, var3, z2) is not an 'actual' join partner, which will be removed after post-matching
        assertHasNext( it, "http://example.org/x2", var1, "http://example.org/y2", var2, "http://example.org/z2", var3 );

        assertFalse( it.hasNext() );
    }

    @Test
    public void getJoinPartners6() {
        // getJoinPartners(): do not contain any join variable. Return all solution mappings if no post matching
        final SolutionMapping sm = SolutionMappingUtils.createSolutionMapping(var3, z1);
        final Iterator<SolutionMapping> it = createHashTableBasedTwoVars().getJoinPartners(sm).iterator();

        assertTrue( it.hasNext() );
        final Binding bIt61 = it.next().asJenaBinding();
        assertEquals( 3, bIt61.size() );

        assertTrue( it.hasNext() );
        final Binding bIt62 = it.next().asJenaBinding();
        assertEquals( 3, bIt62.size() );

        assertTrue( it.hasNext() );
        final Binding bIt63 = it.next().asJenaBinding();
        assertEquals( 3, bIt63.size() );

        assertFalse( it.hasNext() );
    }
}
