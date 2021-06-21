package se.liu.ida.hefquin.engine.datastructures.impl;

import org.apache.jena.sparql.engine.binding.Binding;
import org.junit.Test;
import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.impl.SolutionMappingUtils;

import java.util.*;

import static org.junit.Assert.*;

public class SolutionMappingsIndexWithPostMatchingTest {
    @Test
    public void hashTableWithOneInputVariable_getJoinPartnersWithPostMatching1() {
        final TestsForSolutionMappingsIndex solMaps= new TestsForSolutionMappingsIndex();
        final SolutionMappingsIndexBase solMHashTable = solMaps.createHashTableBasedOneVar();

        // getJoinPartners of (var3, z3): find three solution mappings if without post matching
        final SolutionMapping sm1 = SolutionMappingUtils.createSolutionMapping(solMaps.var3, solMaps.z3);
        final Iterable<SolutionMapping> matchSolMap1 = new SolutionMappingsIndexWithPostMatching(solMHashTable).getJoinPartners(sm1);
        final Iterator<SolutionMapping> itVar1 = matchSolMap1.iterator();

        assertTrue( itVar1.hasNext() );
        final Binding bItVar1 = itVar1.next().asJenaBinding();
        assertEquals( 2, bItVar1.size() );
        assertEquals("http://example.org/y2", bItVar1.get(solMaps.var2).getURI());
        assertEquals("http://example.org/z3", bItVar1.get(solMaps.var3).getURI());

        assertFalse( itVar1.hasNext() );
    }

    @Test
    public void hashTableWithOneInputVariable_getJoinPartnersWithPostMatching2() {
        final TestsForSolutionMappingsIndex solMaps= new TestsForSolutionMappingsIndex();
        final SolutionMappingsIndexBase solMHashTable = solMaps.createHashTableBasedOneVar();

        // getJoinPartners of (var2, y1, var3, z2): find two solution mappings if without post matching
        final SolutionMapping sm2 = SolutionMappingUtils.createSolutionMapping(solMaps.var2, solMaps.y1, solMaps.var3, solMaps.z2);
        final Iterable<SolutionMapping> matchSolMap2 = new SolutionMappingsIndexWithPostMatching(solMHashTable).getJoinPartners(sm2);
        final Iterator<SolutionMapping> itVar2 = matchSolMap2.iterator();

        assertTrue( itVar2.hasNext() );
        final Binding bItVar2 = itVar2.next().asJenaBinding();
        assertEquals( 2, bItVar2.size() );
        assertEquals("http://example.org/y1", bItVar2.get(solMaps.var2).getURI());
        assertEquals("http://example.org/z2", bItVar2.get(solMaps.var3).getURI());

        assertFalse( itVar2.hasNext() );
    }

    @Test
    public void hashTableWithTwoInputVariable_getJoinPartnersWithPostMatching1() {
        final TestsForSolutionMappingsIndex solMaps= new TestsForSolutionMappingsIndex();
        final SolutionMappingsIndexBase solMHashTable = solMaps.createHashTableBasedTwoVars();

        // getJoinPartners of (var2, y2, var3, z1): find one solution mappings if without post matching
        final SolutionMapping sm2 = SolutionMappingUtils.createSolutionMapping(solMaps.var2, solMaps.y2, solMaps.var3, solMaps.z1);
        final Iterable<SolutionMapping> matchSolMap2 =  new SolutionMappingsIndexWithPostMatching(solMHashTable).getJoinPartners(sm2);
        final Iterator<SolutionMapping> it2 = matchSolMap2.iterator();

        assertFalse( it2.hasNext() );
    }

    @Test
    public void hashTableWithTwoInputVariable_getJoinPartnersWithPostMatching2() {
        final TestsForSolutionMappingsIndex solMaps= new TestsForSolutionMappingsIndex();
        final SolutionMappingsIndexBase solMHashTable = solMaps.createHashTableBasedTwoVars();

        // getJoinPartners of (var3, z1): find three solution mappings if without post matching
        final SolutionMapping sm3 = SolutionMappingUtils.createSolutionMapping(solMaps.var3, solMaps.z1);
        final Iterable<SolutionMapping> matchSolMap3 =  new SolutionMappingsIndexWithPostMatching(solMHashTable).getJoinPartners(sm3);
        final Iterator<SolutionMapping> it3 = matchSolMap3.iterator();
        assertTrue( it3.hasNext() );
        final Binding bIt3 = it3.next().asJenaBinding();
        assertEquals( 3, bIt3.size() );
        assertEquals( "http://example.org/x1", bIt3.get(solMaps.var1).getURI() );
        assertEquals( "http://example.org/y1", bIt3.get(solMaps.var2).getURI() );
        assertEquals( "http://example.org/z1", bIt3.get(solMaps.var3).getURI() );

        assertFalse( it3.hasNext() );
    }

    @Test
    public void hashTableWithThreeInputVariable_getJoinPartnersWithPostMatching1() {
        final TestsForSolutionMappingsIndex solMaps= new TestsForSolutionMappingsIndex();
        final SolutionMappingsIndexBase solMHashTable = solMaps.createHashTableBasedThreeVars();

        // getJoinPartners(): do not contain complete join variables; Find three solution mappings if no post-matching
        final SolutionMapping sm1 = SolutionMappingUtils.createSolutionMapping(solMaps.var1, solMaps.x2);
        final Iterable<SolutionMapping> matchSolMap1 =  new SolutionMappingsIndexWithPostMatching(solMHashTable).getJoinPartners(sm1);
        final Iterator<SolutionMapping> it1 = matchSolMap1.iterator();

        assertTrue( it1.hasNext() );
        final Binding bIt1 = it1.next().asJenaBinding();
        assertEquals( 3, bIt1.size() );
        assertEquals( "http://example.org/x2", bIt1.get(solMaps.var1).getURI() );
        assertEquals( "http://example.org/y2", bIt1.get(solMaps.var2).getURI() );
        assertEquals( "http://example.org/z2", bIt1.get(solMaps.var3).getURI() );

        assertFalse( it1.hasNext() );
    }
}
