package se.liu.ida.hefquin.engine.datastructures.impl;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.junit.Test;
import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.impl.SolutionMappingUtils;

import java.util.*;

import static org.junit.Assert.*;

public class SolutionMappingsHashTableTest {
    @Test
    public void hashTableWithThreeInputVariable_basic() {
        // test method: isEmpty(), contains(), add(), size(), clear()
        final TestsForSolutionMappingsIndex solMaps= new TestsForSolutionMappingsIndex();
        final SolutionMappingsIndexBase solMHashTable = solMaps.createHashTableBasedThreeVars();

        assertFalse(solMHashTable.isEmpty());
        assertEquals( 3, solMHashTable.size() );

        final SolutionMapping sm0 = SolutionMappingUtils.createSolutionMapping(solMaps.var1, solMaps.x1, solMaps.var2, solMaps.y1, solMaps.var3, solMaps.z1);
        assertTrue(solMHashTable.contains(sm0));

        solMHashTable.clear();
        assertTrue(solMHashTable.isEmpty());
        assertFalse(solMHashTable.contains(sm0));
    }

    @Test
    public void hashTableWithThreeInputVariable_getAllSolMaps() {
        final TestsForSolutionMappingsIndex solMaps= new TestsForSolutionMappingsIndex();
        final SolutionMappingsIndexBase solMHashTable = solMaps.createHashTableBasedThreeVars();

        // test getAllSolutionMappings()
        final Iterator<SolutionMapping> it = solMHashTable.iterator();
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
        final TestsForSolutionMappingsIndex solMaps= new TestsForSolutionMappingsIndex();
        final SolutionMappingsIndexBase solMHashTable = solMaps.createHashTableBasedThreeVars();

        // findSolutionMappings(var2, y2): one matching solution mapping
        final Iterable<SolutionMapping> solMapVar2= solMHashTable.findSolutionMappings(solMaps.var2, solMaps.y2);
        final Iterator<SolutionMapping> itVar2 = solMapVar2.iterator();

        assertTrue( itVar2.hasNext() );
        final Binding bItVar2 = itVar2.next().asJenaBinding();
        assertEquals( 3, bItVar2.size() );
        assertEquals( "http://example.org/x2", bItVar2.get(solMaps.var1).getURI() );
        assertEquals( "http://example.org/y2", bItVar2.get(solMaps.var2).getURI() );
        assertEquals( "http://example.org/z2", bItVar2.get(solMaps.var3).getURI() );

        assertFalse( itVar2.hasNext() );
    }

    @Test
    public void hashTableWithThreeInputVariable_findSolutionMappings2() {
        final TestsForSolutionMappingsIndex solMaps= new TestsForSolutionMappingsIndex();
        final SolutionMappingsIndexBase solMHashTable = solMaps.createHashTableBasedThreeVars();

        // findSolutionMappings(var4, p): return all solution mappings
        final Iterable<SolutionMapping> solMapVar4 = solMHashTable.findSolutionMappings(solMaps.var4, solMaps.p);
        final Iterator<SolutionMapping> itVar4 = solMapVar4.iterator();

        assertTrue( itVar4.hasNext() );
        final Binding bitVar41 = itVar4.next().asJenaBinding();
        assertEquals( 3, bitVar41.size() );

        assertTrue( itVar4.hasNext() );
        final Binding bitVar42 = itVar4.next().asJenaBinding();
        assertEquals( 3, bitVar42.size() );

        assertTrue( itVar4.hasNext() );
        final Binding bitVar43 = itVar4.next().asJenaBinding();
        assertEquals( 3, bitVar43.size() );

        assertFalse( itVar4.hasNext() );
    }

    @Test
    public void hashTableWithThreeInputVariable_findSolutionMappings3() {
        final TestsForSolutionMappingsIndex solMaps= new TestsForSolutionMappingsIndex();
        final SolutionMappingsIndexBase solMHashTable = solMaps.createHashTableBasedThreeVars();

        // findSolutionMappings(var1, x1, var2, y1)
        final Iterable<SolutionMapping> solMap12= solMHashTable.findSolutionMappings(solMaps.var1, solMaps.x1, solMaps.var2, solMaps.y1);

        final Set<Binding> result = new HashSet<>();
        final Iterator<SolutionMapping> it1 = solMap12.iterator();
        assertTrue( it1.hasNext() );
        result.add( it1.next().asJenaBinding() );

        assertTrue( it1.hasNext() );
        result.add( it1.next().asJenaBinding() );

        assertFalse( it1.hasNext() );

        for ( final Binding b: result ) {
            assertEquals( 3, b.size() );
            if ( b.get(solMaps.var3).getURI().equals("http://example.org/z1") ) {
                assertEquals( "http://example.org/x1", b.get(solMaps.var1).getURI() );
                assertEquals( "http://example.org/y1", b.get(solMaps.var2).getURI() );

            }
            else if ( b.get(solMaps.var3).getURI().equals("http://example.org/z2") ) {
                assertEquals( "http://example.org/x1", b.get(solMaps.var1).getURI() );
                assertEquals( "http://example.org/y1", b.get(solMaps.var2).getURI() );
            }
            else {
                fail( "Unexpected URI for ?v3: " + b.get(solMaps.var3).getURI() );
            }
        }
    }

    @Test
    public void hashTableWithThreeInputVariable_findSolutionMappings4() {
        final TestsForSolutionMappingsIndex solMaps= new TestsForSolutionMappingsIndex();
        final SolutionMappingsIndexBase solMHashTable = solMaps.createHashTableBasedThreeVars();

        // findSolutionMappings(var2, y2, var1, x2), change order of var1 and var2
        final Iterable<SolutionMapping> solMap21= solMHashTable.findSolutionMappings(solMaps.var2, solMaps.y2, solMaps.var1, solMaps.x2);
        final Iterator<SolutionMapping> itVar21 = solMap21.iterator();
        assertTrue( itVar21.hasNext() );
        final Binding bItVar21 = itVar21.next().asJenaBinding();
        assertEquals( 3, bItVar21.size() );
        assertEquals( "http://example.org/x2", bItVar21.get(solMaps.var1).getURI() );
        assertEquals( "http://example.org/y2", bItVar21.get(solMaps.var2).getURI() );
        assertEquals( "http://example.org/z2", bItVar21.get(solMaps.var3).getURI() );

        assertFalse( itVar21.hasNext() );
    }

    @Test
    public void hashTableWithThreeInputVariable_findSolutionMappings5() {
        final TestsForSolutionMappingsIndex solMaps= new TestsForSolutionMappingsIndex();
        final SolutionMappingsIndexBase solMHashTable = solMaps.createHashTableBasedThreeVars();

        // findSolutionMappings(var2, y2, var4, p)
        final Iterable<SolutionMapping> solMap24= solMHashTable.findSolutionMappings(solMaps.var2, solMaps.y2, solMaps.var4, solMaps.p);
        final Iterator<SolutionMapping> itVar24 = solMap24.iterator();
        assertTrue( itVar24.hasNext() );
        final Binding bItVar24 = itVar24.next().asJenaBinding();
        assertEquals( 3, bItVar24.size() );
        assertEquals( "http://example.org/x2", bItVar24.get(solMaps.var1).getURI() );
        assertEquals( "http://example.org/y2", bItVar24.get(solMaps.var2).getURI() );
        assertEquals( "http://example.org/z2", bItVar24.get(solMaps.var3).getURI() );

        assertFalse( itVar24.hasNext() );
    }

    @Test
    public void hashTableWithThreeInputVariable_findSolutionMappings6() {
        final TestsForSolutionMappingsIndex solMaps= new TestsForSolutionMappingsIndex();
        final SolutionMappingsIndexBase solMHashTable = solMaps.createHashTableBasedThreeVars();

        // findSolutionMappings(var2, y1, var1, x1, var3, z1)
        final Iterable<SolutionMapping> solMap213= solMHashTable.findSolutionMappings(solMaps.var2, solMaps.y1, solMaps.var1, solMaps.x1, solMaps.var3, solMaps.z1);
        final Iterator<SolutionMapping> itVar213 = solMap213.iterator();
        assertTrue( itVar213.hasNext() );
        final Binding bitVar213 = itVar213.next().asJenaBinding();
        assertEquals( 3, bitVar213.size() );
        assertEquals( "http://example.org/x1", bitVar213.get(solMaps.var1).getURI() );
        assertEquals( "http://example.org/y1", bitVar213.get(solMaps.var2).getURI() );
        assertEquals( "http://example.org/z1", bitVar213.get(solMaps.var3).getURI() );

        assertFalse( itVar213.hasNext() );
    }

    @Test
    public void hashTableWithThreeInputVariable_findSolutionMappings7() {
        final TestsForSolutionMappingsIndex solMaps= new TestsForSolutionMappingsIndex();
        final SolutionMappingsIndexBase solMHashTable = solMaps.createHashTableBasedThreeVars();

        // findSolutionMappings(var2, y2, var1, x2, var4, p)
        final Iterable<SolutionMapping> solMap214= solMHashTable.findSolutionMappings(solMaps.var2, solMaps.y2, solMaps.var1, solMaps.x2, solMaps.var4, solMaps.p);
        final Iterator<SolutionMapping> itVar214 = solMap214.iterator();

        assertTrue( itVar214.hasNext() );
        final Binding bitVar214 = itVar214.next().asJenaBinding();
        assertEquals( 3, bitVar214.size() );
        assertEquals( "http://example.org/x2", bitVar214.get(solMaps.var1).getURI() );
        assertEquals( "http://example.org/y2", bitVar214.get(solMaps.var2).getURI() );
        assertEquals( "http://example.org/z2", bitVar214.get(solMaps.var3).getURI() );

        assertFalse( itVar214.hasNext() );
    }

    @Test
    public void hashTableWithThreeInputVariable_findSolutionMappings8() {
        final TestsForSolutionMappingsIndex solMaps= new TestsForSolutionMappingsIndex();
        final SolutionMappingsIndexBase solMHashTable = solMaps.createHashTableBasedThreeVars();

        // findSolutionMappings(var2, y1, var1, x1, var3, z1, var4, p)
        final Iterable<SolutionMapping> solMap2134= solMHashTable.findSolutionMappings(solMaps.var2, solMaps.y1, solMaps.var1, solMaps.x1, solMaps.var3, solMaps.z1);
        final Iterator<SolutionMapping> itVar2134 = solMap2134.iterator();

        assertTrue( itVar2134.hasNext() );
        final Binding bitVar2134 = itVar2134.next().asJenaBinding();
        assertEquals( 3, bitVar2134.size() );
        assertEquals( "http://example.org/x1", bitVar2134.get(solMaps.var1).getURI() );
        assertEquals( "http://example.org/y1", bitVar2134.get(solMaps.var2).getURI() );
        assertEquals( "http://example.org/z1", bitVar2134.get(solMaps.var3).getURI() );

        assertFalse( itVar2134.hasNext() );
    }

    @Test
    public void hashTableWithThreeInputVariable_getJoinPartners1() {
        final TestsForSolutionMappingsIndex solMaps= new TestsForSolutionMappingsIndex();
        final SolutionMappingsIndexBase solMHashTable = solMaps.createHashTableBasedThreeVars();

        // getJoinPartners(var2, y1, var1, x1, var3, z1): one join partner
        final SolutionMapping sm3 = SolutionMappingUtils.createSolutionMapping(solMaps.var2, solMaps.y1, solMaps.var1, solMaps.x1, solMaps.var3, solMaps.z1);
        final Iterable<SolutionMapping> matchSolMap3 = solMHashTable.getJoinPartners(sm3);
        final Iterator<SolutionMapping> it3 = matchSolMap3.iterator();

        assertTrue( it3.hasNext() );
        final Binding bit3 = it3.next().asJenaBinding();
        assertEquals( 3, bit3.size() );
        assertEquals( "http://example.org/x1", bit3.get(solMaps.var1).getURI() );
        assertEquals( "http://example.org/y1", bit3.get(solMaps.var2).getURI() );
        assertEquals( "http://example.org/z1", bit3.get(solMaps.var3).getURI() );

        assertFalse( it3.hasNext() );
    }

    @Test
    public void hashTableWithThreeInputVariable_getJoinPartners2() {
        final TestsForSolutionMappingsIndex solMaps= new TestsForSolutionMappingsIndex();
        final SolutionMappingsIndexBase solMHashTable = solMaps.createHashTableBasedThreeVars();

        // getJoinPartners(var2, y2, var1, x1, var3, z1): no join partner
        final SolutionMapping sm = SolutionMappingUtils.createSolutionMapping(solMaps.var2, solMaps.y2, solMaps.var1, solMaps.x1, solMaps.var3, solMaps.z1);
        final Iterable<SolutionMapping> matchSolMap = solMHashTable.getJoinPartners(sm);
        final Iterator<SolutionMapping> it = matchSolMap.iterator();

        assertFalse( it.hasNext() );
    }

    @Test
    public void hashTableWithThreeInputVariable_getJoinPartners3() {
        final TestsForSolutionMappingsIndex solMaps= new TestsForSolutionMappingsIndex();
        final SolutionMappingsIndexBase solMHashTable = solMaps.createHashTableBasedThreeVars();

        // getJoinPartners(): do not contain complete join variables. Return all solution mappings (if no post-matching)
        final SolutionMapping sm1 = SolutionMappingUtils.createSolutionMapping(solMaps.var1, solMaps.x2);
        final Iterable<SolutionMapping> matchSolMap1 = solMHashTable.getJoinPartners(sm1);
        final Iterator<SolutionMapping> it1 = matchSolMap1.iterator();

        assertTrue( it1.hasNext() );
        final Binding bIt11 = it1.next().asJenaBinding();
        assertEquals( 3, bIt11.size() );

        assertTrue( it1.hasNext() );
        final Binding bIt12 = it1.next().asJenaBinding();
        assertEquals( 3, bIt12.size() );

        assertTrue( it1.hasNext() );
        final Binding bIt13 = it1.next().asJenaBinding();
        assertEquals( 3, bIt13.size() );

        assertFalse( it1.hasNext() );
    }

    @Test
    public void hashTableWithThreeInputVariable_getJoinPartners4() {
        final TestsForSolutionMappingsIndex solMaps= new TestsForSolutionMappingsIndex();
        final SolutionMappingsIndexBase solMHashTable = solMaps.createHashTableBasedThreeVars();

        // getJoinPartners(): do not contain any join variable. Return all solution mappings (if no post-matching)
        final SolutionMapping sm2 = SolutionMappingUtils.createSolutionMapping(solMaps.var4, solMaps.z2);
        final Iterable<SolutionMapping> matchSolMap2 = solMHashTable.getJoinPartners(sm2);
        final Iterator<SolutionMapping> it2 = matchSolMap2.iterator();
        assertTrue( it2.hasNext() );
        final Binding bIt21 = it2.next().asJenaBinding();
        assertEquals( 3, bIt21.size() );

        assertTrue( it2.hasNext() );
        final Binding bIt42 = it2.next().asJenaBinding();
        assertEquals( 3, bIt42.size() );

        assertTrue( it2.hasNext() );
        final Binding bIt23 = it2.next().asJenaBinding();
        assertEquals( 3, bIt23.size() );

        assertFalse( it2.hasNext() );
    }

    @Test
    public void hashTableWithEmptyInputVariable() {
        final List<Var> inputVars = Arrays.asList(); // create an empty list
        assertThrows(AssertionError.class,
                ()->{ new SolutionMappingsHashTable(inputVars); });
    }
}
