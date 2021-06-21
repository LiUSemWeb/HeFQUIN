package se.liu.ida.hefquin.engine.datastructures.impl;

import org.apache.jena.sparql.engine.binding.Binding;
import org.junit.Test;
import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.impl.SolutionMappingUtils;

import java.util.*;

import static org.junit.Assert.*;

public class SolutionMappingsHashTableBasedOnOneVarTest {
    @Test
    public void hashTableWithOneInputVariable_basic() {
        // test method: isEmpty(), contains(), add(), size(), clear()
        final TestsForSolutionMappingsIndex solMaps= new TestsForSolutionMappingsIndex();
        final SolutionMappingsIndexBase solMHashTable = solMaps.createHashTableBasedOneVar();

        assertFalse(solMHashTable.isEmpty());
        assertEquals( 3, solMHashTable.size() );

		final SolutionMapping sm0 = SolutionMappingUtils.createSolutionMapping(solMaps.var2, solMaps.y1, solMaps.var3, solMaps.z1);
        assertTrue(solMHashTable.contains(sm0));

        solMHashTable.clear();
        assertTrue(solMHashTable.isEmpty());
        assertFalse(solMHashTable.contains(sm0));
    }

    @Test
    public void hashTableWithOneInputVariable_getAllSolMaps() {
        final TestsForSolutionMappingsIndex solMaps= new TestsForSolutionMappingsIndex();
        final SolutionMappingsIndexBase solMHashTable = solMaps.createHashTableBasedOneVar();

        // test getAllSolutionMappings()
        final Iterator<SolutionMapping> it = solMHashTable.getAllSolutionMappings().iterator();
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
        final TestsForSolutionMappingsIndex solMaps= new TestsForSolutionMappingsIndex();
        final SolutionMappingsIndexBase solMHashTable = solMaps.createHashTableBasedOneVar();

        // findSolutionMappings(var2, y2): return one matching solution mapping (hash table is built based on var2)
        final Iterable<SolutionMapping> solMapVar2= solMHashTable.findSolutionMappings(solMaps.var2, solMaps.y2);
        for ( final SolutionMapping sm: solMapVar2 ){
            final Binding bsm = sm.asJenaBinding();
            if ( bsm.get(solMaps.var2).getURI().equals("http://example.org/y2") ) {
                assertEquals( "http://example.org/z3", bsm.get(solMaps.var3).getURI() );
            }
            else {
                fail( "Unexpected URI for ?v2: " + bsm.get(solMaps.var2).getURI() );
            }
        }
    }

    @Test
    public void hashTableWithOneInputVariable_findSolutionMappings2() {
        final TestsForSolutionMappingsIndex solMaps= new TestsForSolutionMappingsIndex();
        final SolutionMappingsIndexBase solMHashTable = solMaps.createHashTableBasedOneVar();

        // findSolutionMappings(var3, z3): return one matching solution mapping (hash table is not built based on var3)
        final Iterable<SolutionMapping> solMapVar3 = solMHashTable.findSolutionMappings(solMaps.var3, solMaps.z3);
        final Iterator<SolutionMapping> itVar3 = solMapVar3.iterator();

        assertTrue( itVar3.hasNext() );
        final Binding bItVar3 = itVar3.next().asJenaBinding();
        assertEquals( 2, bItVar3.size() );

        assertFalse( itVar3.hasNext() );
    }

    @Test
    public void hashTableWithOneInputVariable_findSolutionMappings3() {
        final TestsForSolutionMappingsIndex solMaps= new TestsForSolutionMappingsIndex();
        final SolutionMappingsIndexBase solMHashTable = solMaps.createHashTableBasedOneVar();

        // findSolutionMappings(var3, z3, var2, y2): matching one solution mapping
        final Iterable<SolutionMapping> solMapVar32= solMHashTable.findSolutionMappings(solMaps.var3, solMaps.z3, solMaps.var2, solMaps.y2);
        final Iterator<SolutionMapping> itVar32 = solMapVar32.iterator();

        assertTrue( itVar32.hasNext() );
        final Binding bItVar32 = itVar32.next().asJenaBinding();
        assertEquals( 2, bItVar32.size() );

        assertFalse( itVar32.hasNext() );
    }

    @Test
    public void hashTableWithOneInputVariable_findSolutionMappings4() {
        final TestsForSolutionMappingsIndex solMaps= new TestsForSolutionMappingsIndex();
        final SolutionMappingsIndexBase solMHashTable = solMaps.createHashTableBasedOneVar();

        // findSolutionMappings(var2, y2, var3, z1): no matching solution mappings
        final Iterable<SolutionMapping> solMapVar23= solMHashTable.findSolutionMappings(solMaps.var2, solMaps.y2, solMaps.var3, solMaps.z1);
        final Iterator<SolutionMapping> itVar23 = solMapVar23.iterator();

        assertFalse( itVar23.hasNext() );
    }

    @Test
    public void hashTableWithOneInputVariable_findSolutionMappings5() {
        final TestsForSolutionMappingsIndex solMaps= new TestsForSolutionMappingsIndex();
        final SolutionMappingsIndexBase solMHashTable = solMaps.createHashTableBasedOneVar();

        // findSolutionMappings(var1, x1, var2, y2, var3, z3): one matching solution mapping
        final Iterable<SolutionMapping> solMapVar123= solMHashTable.findSolutionMappings(solMaps.var1, solMaps.x1, solMaps.var2, solMaps.y2, solMaps.var3, solMaps.z3);
        final Iterator<SolutionMapping> itVar123 = solMapVar123.iterator();

        assertTrue( itVar123.hasNext() );
        final Binding bItVar132 = itVar123.next().asJenaBinding();
        assertEquals( 2, bItVar132.size() );

        assertFalse( itVar123.hasNext() );
    }

    @Test
    public void hashTableWithOneInputVariable_getJoinPartners1() {
        final TestsForSolutionMappingsIndex solMaps= new TestsForSolutionMappingsIndex();
        final SolutionMappingsIndexBase solMHashTable = solMaps.createHashTableBasedOneVar();

        // getJoinPartners(): one join variable with two join partners
        final SolutionMapping sm1 = SolutionMappingUtils.createSolutionMapping(solMaps.var1, solMaps.x1, solMaps.var2, solMaps.y1);
        final Iterable<SolutionMapping> matchSolMap1 = solMHashTable.getJoinPartners(sm1);

        final Set<Binding> result = new HashSet<>();
        final Iterator<SolutionMapping> it1 = matchSolMap1.iterator();
        assertTrue( it1.hasNext() );
        result.add( it1.next().asJenaBinding() );
        assertTrue( it1.hasNext() );
        result.add( it1.next().asJenaBinding() );
        assertFalse( it1.hasNext() );

        for ( final Binding b: result ) {
            assertEquals( 2, b.size() );
            if ( b.get(solMaps.var3).getURI().equals("http://example.org/z1") ) {
                assertEquals( "http://example.org/y1", b.get(solMaps.var2).getURI() );
            }
            else if ( b.get(solMaps.var3).getURI().equals("http://example.org/z2") ) {
                assertEquals( "http://example.org/y1", b.get(solMaps.var2).getURI() );
            }
            else {
                fail( "Unexpected URI for ?v3: " + b.get(solMaps.var3).getURI() );
            }
        }
    }

    @Test
    public void hashTableWithOneInputVariable_getJoinPartners2() {
        final TestsForSolutionMappingsIndex solMaps= new TestsForSolutionMappingsIndex();
        final SolutionMappingsIndexBase solMHashTable = solMaps.createHashTableBasedOneVar();

        // getJoinPartners(): one join variable. Return two solution mappings after filtering
        final SolutionMapping sm2 = SolutionMappingUtils.createSolutionMapping(solMaps.var2, solMaps.y1, solMaps.var3, solMaps.z2);
        final Iterable<SolutionMapping> matchSolMap2 = solMHashTable.getJoinPartners(sm2);
        final Iterator<SolutionMapping> itVar2 = matchSolMap2.iterator();

        assertTrue( itVar2.hasNext() );
        final Binding bItVar21 = itVar2.next().asJenaBinding();
        assertEquals( 2, bItVar21.size() );

        assertTrue( itVar2.hasNext() );
        final Binding bItVar22 = itVar2.next().asJenaBinding();
        assertEquals( 2, bItVar22.size() );

        assertFalse( itVar2.hasNext() );
    }

    @Test
    public void hashTableWithOneInputVariable_getJoinPartners3() {
        final TestsForSolutionMappingsIndex solMaps= new TestsForSolutionMappingsIndex();
        final SolutionMappingsIndexBase solMHashTable = solMaps.createHashTableBasedOneVar();

        // getJoinPartners(): do not contain the join variable. Return all solution mappings if no postingMatching is applied.
        final SolutionMapping sm3 = SolutionMappingUtils.createSolutionMapping(solMaps.var3, solMaps.z3);
        final Iterable<SolutionMapping> matchSolMap3 = solMHashTable.getJoinPartners(sm3);
        final Iterator<SolutionMapping> it3 = matchSolMap3.iterator();

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

    @Test
    public void hashTableWithOneInputVariable_getJoinPartners4() {
        final TestsForSolutionMappingsIndex solMaps= new TestsForSolutionMappingsIndex();
        final SolutionMappingsIndexBase solMHashTable = solMaps.createHashTableBasedOneVar();

        // getJoinPartners(): one join variable but without join partner
        final SolutionMapping sm4 = SolutionMappingUtils.createSolutionMapping(solMaps.var1, solMaps.x2, solMaps.var2, solMaps.y3);
        final Iterable<SolutionMapping> matchSolMap4 = solMHashTable.getJoinPartners(sm4);
        final Iterator<SolutionMapping> it4 = matchSolMap4.iterator();
        assertFalse( it4.hasNext() );
    }
}
