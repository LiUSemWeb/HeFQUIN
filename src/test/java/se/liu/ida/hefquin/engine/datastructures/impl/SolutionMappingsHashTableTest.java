package se.liu.ida.hefquin.engine.datastructures.impl;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.junit.Test;
import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.impl.SolutionMappingUtils;

import java.util.*;

import static org.junit.Assert.*;

public class SolutionMappingsHashTableTest extends TestsForSolutionMappingsIndex {
    @Test
    public void hashTableWithThreeInputVariable_basic() {
        // test method: isEmpty(), contains(), add(), size(), clear()
        final SolutionMappingsIndexBase solMHashTable = createHashTableBasedThreeVars();

        assertFalse(solMHashTable.isEmpty());
        assertEquals( 3, solMHashTable.size() );

        final SolutionMapping sm = SolutionMappingUtils.createSolutionMapping(var1, x1, var2, y1, var3, z1);
        assertTrue(solMHashTable.contains(sm));

        solMHashTable.clear();
        assertTrue(solMHashTable.isEmpty());
        assertFalse(solMHashTable.contains(sm));
    }

    @Test
    public void hashTableWithThreeInputVariable_getAllSolMaps() {
        // test getAllSolutionMappings()
        final Iterator<SolutionMapping> it = createHashTableBasedThreeVars().getAllSolutionMappings().iterator();

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
    public void hashTableWithThreeInputVariable_findSolutionMappings1() {
        // findSolutionMappings(var2, y2): one matching solution mapping
        final Iterator<SolutionMapping> it = createHashTableBasedThreeVars().findSolutionMappings(var2, y2).iterator();

        assertHasNext( it, "http://example.org/x2", var1, "http://example.org/y2", var2, "http://example.org/z2", var3 );

        assertFalse( it.hasNext() );
    }

    @Test
    public void hashTableWithThreeInputVariable_findSolutionMappings2() {
        // findSolutionMappings(var4, p): return all solution mappings
        final Iterator<SolutionMapping> it = createHashTableBasedThreeVars().findSolutionMappings(var4, p).iterator();

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
    public void hashTableWithThreeInputVariable_findSolutionMappings3() {
        // findSolutionMappings(var1, x1, var2, y1)
        final Iterator<SolutionMapping> it = createHashTableBasedThreeVars().findSolutionMappings(var1, x1, var2, y1).iterator();

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
    public void hashTableWithThreeInputVariable_findSolutionMappings4() {
        // findSolutionMappings(var2, y2, var1, x2), change order of var1 and var2
        final Iterator<SolutionMapping> it = createHashTableBasedThreeVars().findSolutionMappings(var2, y2, var1, x2).iterator();

        assertHasNext( it, "http://example.org/x2", var1, "http://example.org/y2", var2, "http://example.org/z2", var3 );

        assertFalse( it.hasNext() );
    }

    @Test
    public void hashTableWithThreeInputVariable_findSolutionMappings5() {
        // findSolutionMappings(var2, y2, var4, p)
        final Iterator<SolutionMapping> it = createHashTableBasedThreeVars().findSolutionMappings(var2, y2, var4, p).iterator();
        assertHasNext( it, "http://example.org/x2", var1, "http://example.org/y2", var2, "http://example.org/z2", var3 );

        assertFalse( it.hasNext() );
    }

    @Test
    public void hashTableWithThreeInputVariable_findSolutionMappings6() {
        // findSolutionMappings(var2, y1, var1, x1, var3, z1)
        final Iterator<SolutionMapping> it = createHashTableBasedThreeVars().findSolutionMappings(var2, y1, var1, x1, var3, z1).iterator();
        assertHasNext( it, "http://example.org/x1", var1, "http://example.org/y1", var2, "http://example.org/z1", var3 );

        assertFalse( it.hasNext() );
    }

    @Test
    public void hashTableWithThreeInputVariable_findSolutionMappings7() {
        // findSolutionMappings(var2, y2, var1, x2, var4, p)
        final Iterator<SolutionMapping> it = createHashTableBasedThreeVars().findSolutionMappings(var2, y2, var1, x2, var4, p).iterator();

        assertHasNext( it, "http://example.org/x2", var1, "http://example.org/y2", var2, "http://example.org/z2", var3 );

        assertFalse( it.hasNext() );
    }

    @Test
    public void hashTableWithThreeInputVariable_findSolutionMappings8() {
        // findSolutionMappings(var2, y1, var1, x1, var3, z1, var4, p)
        final Iterator<SolutionMapping> it = createHashTableBasedThreeVars().findSolutionMappings(var2, y1, var1, x1, var3, z1).iterator();

        assertHasNext( it, "http://example.org/x1", var1, "http://example.org/y1", var2, "http://example.org/z1", var3 );

        assertFalse( it.hasNext() );
    }

    @Test
    public void hashTableWithThreeInputVariable_getJoinPartners1() {
        // getJoinPartners(var2, y1, var1, x1, var3, z1): one join partner
        final SolutionMapping sm = SolutionMappingUtils.createSolutionMapping(var2, y1, var1, x1, var3, z1);
        final Iterator<SolutionMapping> it = createHashTableBasedThreeVars().getJoinPartners(sm).iterator();

        assertHasNext( it, "http://example.org/x1", var1, "http://example.org/y1", var2, "http://example.org/z1", var3 );

        assertFalse( it.hasNext() );
    }

    @Test
    public void hashTableWithThreeInputVariable_getJoinPartners2() {
        // getJoinPartners(var2, y2, var1, x1, var3, z1): no join partner
        final SolutionMapping sm = SolutionMappingUtils.createSolutionMapping(var2, y2, var1, x1, var3, z1);
        final Iterator<SolutionMapping> it = createHashTableBasedThreeVars().getJoinPartners(sm).iterator();

        assertFalse( it.hasNext() );
    }

    @Test
    public void hashTableWithThreeInputVariable_getJoinPartners3() {
        // getJoinPartners(): do not contain complete join variables. Return all solution mappings (if no post-matching)
        final SolutionMapping sm = SolutionMappingUtils.createSolutionMapping(var1, x2);
        final Iterator<SolutionMapping> it = createHashTableBasedThreeVars().getJoinPartners(sm).iterator();

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
    public void hashTableWithThreeInputVariable_getJoinPartners4() {
        // getJoinPartners(): do not contain any join variable. Return all solution mappings (if no post-matching)
        final SolutionMapping sm = SolutionMappingUtils.createSolutionMapping(var4, z2);
        final Iterator<SolutionMapping> it = createHashTableBasedThreeVars().getJoinPartners(sm).iterator();

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
    public void hashTableWithEmptyInputVariable() {
        final List<Var> inputVars = Arrays.asList(); // create an empty list
        assertThrows(AssertionError.class,
                ()->{ new SolutionMappingsHashTable(inputVars); });
    }
}
