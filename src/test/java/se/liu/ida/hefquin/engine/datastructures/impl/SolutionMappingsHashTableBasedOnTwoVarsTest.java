package se.liu.ida.hefquin.engine.datastructures.impl;

import org.apache.jena.sparql.engine.binding.Binding;
import org.junit.Test;
import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.impl.SolutionMappingUtils;

import java.util.*;

import static org.junit.Assert.*;

public class SolutionMappingsHashTableBasedOnTwoVarsTest {
    @Test
    public void hashTableWithTwoInputVariable_basic() {
        // test method: isEmpty(), contains(), add(), size(), clear()
        final TestsForSolutionMappingsIndex solMaps= new TestsForSolutionMappingsIndex();
        final SolutionMappingsIndexBase solMHashTable = solMaps.createHashTableBasedTwoVars();

        assertFalse(solMHashTable.isEmpty());
        assertEquals( 3, solMHashTable.size() );

        final SolutionMapping sm0 = SolutionMappingUtils.createSolutionMapping(solMaps.var1, solMaps.x1, solMaps.var2, solMaps.y1, solMaps.var3, solMaps.z1);
        assertTrue(solMHashTable.contains(sm0));

        solMHashTable.clear();
        assertTrue(solMHashTable.isEmpty());
        assertFalse(solMHashTable.contains(sm0));
    }

    @Test
    public void hashTableWithTwoInputVariable_getAllSolMaps() {
        final TestsForSolutionMappingsIndex solMaps= new TestsForSolutionMappingsIndex();
        final SolutionMappingsIndexBase solMHashTable = solMaps.createHashTableBasedTwoVars();

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
    public void hashTableWithTwoInputVariable_findSolutionMappings1() {
        final TestsForSolutionMappingsIndex solMaps= new TestsForSolutionMappingsIndex();
        final SolutionMappingsIndexBase solMHashTable = solMaps.createHashTableBasedTwoVars();

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
    public void hashTableWithTwoInputVariable_findSolutionMappings2() {
        final TestsForSolutionMappingsIndex solMaps= new TestsForSolutionMappingsIndex();
        final SolutionMappingsIndexBase solMHashTable = solMaps.createHashTableBasedTwoVars();

        // findSolutionMappings(var3, z1): return one matching solution mapping (hash table is not built based on var3)
        final Iterable<SolutionMapping> solMapVar3 = solMHashTable.findSolutionMappings(solMaps.var3, solMaps.z1);
        final Iterator<SolutionMapping> itVar3 = solMapVar3.iterator();

        assertTrue( itVar3.hasNext() );
        final Binding bItVar3 = itVar3.next().asJenaBinding();
        assertEquals( 3, bItVar3.size() );
        assertEquals( "http://example.org/x1", bItVar3.get(solMaps.var1).getURI() );
        assertEquals( "http://example.org/y1", bItVar3.get(solMaps.var2).getURI() );
        assertEquals( "http://example.org/z1", bItVar3.get(solMaps.var3).getURI() );

        assertFalse( itVar3.hasNext() );
    }

    @Test
    public void hashTableWithTwoInputVariable_findSolutionMappings3() {
        final TestsForSolutionMappingsIndex solMaps= new TestsForSolutionMappingsIndex();
        final SolutionMappingsIndexBase solMHashTable = solMaps.createHashTableBasedTwoVars();

        // findSolutionMappings(var1, x2, var2, y2): return one matching solution mapping (hash table is built based on (var1, var2))
        final Iterable<SolutionMapping> solMapVar12= solMHashTable.findSolutionMappings(solMaps.var1, solMaps.x2, solMaps.var2, solMaps.y2);
        final Iterator<SolutionMapping> itVar12 = solMapVar12.iterator();

        assertTrue( itVar12.hasNext() );
        final Binding bItVar12 = itVar12.next().asJenaBinding();
        assertEquals( 3, bItVar12.size() );
        assertEquals( "http://example.org/x2", bItVar12.get(solMaps.var1).getURI() );
        assertEquals( "http://example.org/y2", bItVar12.get(solMaps.var2).getURI() );
        assertEquals( "http://example.org/z2", bItVar12.get(solMaps.var3).getURI() );

        assertFalse( itVar12.hasNext() );
    }

    @Test
    public void hashTableWithTwoInputVariable_findSolutionMappings4() {
        final TestsForSolutionMappingsIndex solMaps= new TestsForSolutionMappingsIndex();
        final SolutionMappingsIndexBase solMHashTable = solMaps.createHashTableBasedTwoVars();

        // findSolutionMappings(var2, y2, var1, x2), change order of var1 and var2
        final Iterable<SolutionMapping> solMapVar21= solMHashTable.findSolutionMappings(solMaps.var2, solMaps.y2, solMaps.var1, solMaps.x2);
        final Iterator<SolutionMapping> itVar21 = solMapVar21.iterator();

        assertTrue( itVar21.hasNext() );
        final Binding bItVar21 = itVar21.next().asJenaBinding();
        assertEquals( 3, bItVar21.size() );
        assertEquals( "http://example.org/x2", bItVar21.get(solMaps.var1).getURI() );
        assertEquals( "http://example.org/y2", bItVar21.get(solMaps.var2).getURI() );
        assertEquals( "http://example.org/z2", bItVar21.get(solMaps.var3).getURI() );

        assertFalse( itVar21.hasNext() );
    }

    @Test
    public void hashTableWithTwoInputVariable_findSolutionMappings5() {
        final TestsForSolutionMappingsIndex solMaps= new TestsForSolutionMappingsIndex();
        final SolutionMappingsIndexBase solMHashTable = solMaps.createHashTableBasedTwoVars();

        // findSolutionMappings(var1, x1, var3, z1): return one matching solution mapping
        final Iterable<SolutionMapping> solMap13= solMHashTable.findSolutionMappings(solMaps.var1, solMaps.x1, solMaps.var3, solMaps.z1);
        final Iterator<SolutionMapping> itVar13 = solMap13.iterator();

        assertTrue( itVar13.hasNext() );
        final Binding bItVar13 = itVar13.next().asJenaBinding();
        assertEquals( 3, bItVar13.size() );
        assertEquals( "http://example.org/x1", bItVar13.get(solMaps.var1).getURI() );
        assertEquals( "http://example.org/y1", bItVar13.get(solMaps.var2).getURI() );
        assertEquals( "http://example.org/z1", bItVar13.get(solMaps.var3).getURI() );

        assertFalse( itVar13.hasNext() );
    }

    @Test
    public void hashTableWithTwoInputVariable_findSolutionMappings6() {
        final TestsForSolutionMappingsIndex solMaps= new TestsForSolutionMappingsIndex();
        final SolutionMappingsIndexBase solMHashTable = solMaps.createHashTableBasedTwoVars();

        // findSolutionMappings(var1, x1, var2, y1, var3, z1)
        final Iterable<SolutionMapping> solMap123_1= solMHashTable.findSolutionMappings(solMaps.var1, solMaps.x1, solMaps.var2, solMaps.y1, solMaps.var3, solMaps.z1);
        final Iterator<SolutionMapping> itVar123_1 = solMap123_1.iterator();

        assertTrue( itVar123_1.hasNext() );
        final Binding bItVar123_1 = itVar123_1.next().asJenaBinding();
        assertEquals( 3, bItVar123_1.size() );
        assertEquals( "http://example.org/x1", bItVar123_1.get(solMaps.var1).getURI() );
        assertEquals( "http://example.org/y1", bItVar123_1.get(solMaps.var2).getURI() );
        assertEquals( "http://example.org/z1", bItVar123_1.get(solMaps.var3).getURI() );

        assertFalse( itVar123_1.hasNext() );
    }

    @Test
    public void hashTableWithTwoInputVariable_findSolutionMappings7() {
        final TestsForSolutionMappingsIndex solMaps= new TestsForSolutionMappingsIndex();
        final SolutionMappingsIndexBase solMHashTable = solMaps.createHashTableBasedTwoVars();

        // findSolutionMappings(var1, x1, var2, y2, var3, z1): no matching solution mapping
        final Iterable<SolutionMapping> solMap123_2= solMHashTable.findSolutionMappings(solMaps.var1, solMaps.x1, solMaps.var2, solMaps.y2, solMaps.var3, solMaps.z1);
        final Iterator<SolutionMapping> itVar123_2 = solMap123_2.iterator();

        assertFalse( itVar123_2.hasNext() );
    }

    @Test
    public void hashTableWithTwoInputVariable_getJoinPartners1() {
        final TestsForSolutionMappingsIndex solMaps= new TestsForSolutionMappingsIndex();
        final SolutionMappingsIndexBase solMHashTable = solMaps.createHashTableBasedTwoVars();

        // getJoinPartners(): two join variables with two join partners
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
    public void hashTableWithTwoInputVariable_getJoinPartners2() {
        final TestsForSolutionMappingsIndex solMaps= new TestsForSolutionMappingsIndex();
        final SolutionMappingsIndexBase solMHashTable = solMaps.createHashTableBasedTwoVars();

        // getJoinPartners(): two join variables but without join partner (case 1: subset matching)
        final SolutionMapping sm2 = SolutionMappingUtils.createSolutionMapping(solMaps.var1, solMaps.x1, solMaps.var2, solMaps.y2);
        final Iterable<SolutionMapping> matchSolMap2 = solMHashTable.getJoinPartners(sm2);
        final Iterator<SolutionMapping> it2 = matchSolMap2.iterator();
        assertFalse( it2.hasNext() );

    }

    @Test
    public void hashTableWithTwoInputVariable_getJoinPartners3() {
        final TestsForSolutionMappingsIndex solMaps= new TestsForSolutionMappingsIndex();
        final SolutionMappingsIndexBase solMHashTable = solMaps.createHashTableBasedTwoVars();

        // getJoinPartners(): two join variables but without join partner (case 2: no matching)
        final SolutionMapping sm3 = SolutionMappingUtils.createSolutionMapping(solMaps.var1, solMaps.x3, solMaps.var2, solMaps.y3);
        final Iterable<SolutionMapping> matchSolMap3 = solMHashTable.getJoinPartners(sm3);
        final Iterator<SolutionMapping> it3 = matchSolMap3.iterator();
        assertFalse( it3.hasNext() );
    }

    @Test
    public void hashTableWithTwoInputVariable_getJoinPartners4() {
        final TestsForSolutionMappingsIndex solMaps= new TestsForSolutionMappingsIndex();
        final SolutionMappingsIndexBase solMHashTable = solMaps.createHashTableBasedTwoVars();

        // getJoinPartners(): do not contain complete join variables.
        final SolutionMapping sm4 = SolutionMappingUtils.createSolutionMapping(solMaps.var2, solMaps.y2);
        final Iterable<SolutionMapping> matchSolMap4 = solMHashTable.getJoinPartners(sm4);
        final Iterator<SolutionMapping> it4 = matchSolMap4.iterator();
        assertTrue( it4.hasNext() );
        final Binding bIt4 = it4.next().asJenaBinding();
        assertEquals( 3, bIt4.size() );
        assertEquals( "http://example.org/x2", bIt4.get(solMaps.var1).getURI() );
        assertEquals( "http://example.org/y2", bIt4.get(solMaps.var2).getURI() );
        assertEquals( "http://example.org/z2", bIt4.get(solMaps.var3).getURI() );

        assertFalse( it4.hasNext() );
    }

    @Test
    public void hashTableWithTwoInputVariable_getJoinPartners5() {
        final TestsForSolutionMappingsIndex solMaps= new TestsForSolutionMappingsIndex();
        final SolutionMappingsIndexBase solMHashTable = solMaps.createHashTableBasedTwoVars();

        // getJoinPartners(): do not contain complete join variables. Find one join partner if not post matching
        final SolutionMapping sm5 = SolutionMappingUtils.createSolutionMapping(solMaps.var2, solMaps.y2, solMaps.var3, solMaps.z1);
        final Iterable<SolutionMapping> matchSolMap5 = solMHashTable.getJoinPartners(sm5);
        final Iterator<SolutionMapping> it5 = matchSolMap5.iterator();

        // (var1, x2, var2, y2, var3, z2) is not an 'actual' join partner, which will be removed after post-matching
        assertTrue( it5.hasNext() );
        final Binding bIt5 = it5.next().asJenaBinding();
        assertEquals( 3, bIt5.size() );
        assertEquals( "http://example.org/x2", bIt5.get(solMaps.var1).getURI() );
        assertEquals( "http://example.org/y2", bIt5.get(solMaps.var2).getURI() );
        assertEquals( "http://example.org/z2", bIt5.get(solMaps.var3).getURI() );

        assertFalse( it5.hasNext() );
    }

    @Test
    public void hashTableWithTwoInputVariable_getJoinPartners6() {
        final TestsForSolutionMappingsIndex solMaps= new TestsForSolutionMappingsIndex();
        final SolutionMappingsIndexBase solMHashTable = solMaps.createHashTableBasedTwoVars();

        // getJoinPartners(): do not contain any join variable. Return all solution mappings if no post matching
        final SolutionMapping sm6 = SolutionMappingUtils.createSolutionMapping(solMaps.var3, solMaps.z1);
        final Iterable<SolutionMapping> matchSolMap6 = solMHashTable.getJoinPartners(sm6);
        final Iterator<SolutionMapping> it6 = matchSolMap6.iterator();
        assertTrue( it6.hasNext() );
        final Binding bIt61 = it6.next().asJenaBinding();
        assertEquals( 3, bIt61.size() );

        assertTrue( it6.hasNext() );
        final Binding bIt62 = it6.next().asJenaBinding();
        assertEquals( 3, bIt62.size() );

        assertTrue( it6.hasNext() );
        final Binding bIt63 = it6.next().asJenaBinding();
        assertEquals( 3, bIt63.size() );

        assertFalse( it6.hasNext() );
    }
}
