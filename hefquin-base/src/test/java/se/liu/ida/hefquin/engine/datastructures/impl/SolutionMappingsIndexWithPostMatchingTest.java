package se.liu.ida.hefquin.engine.datastructures.impl;

import org.junit.Test;
import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.utils.SolutionMappingUtils;

import java.util.*;

import static org.junit.Assert.*;

public class SolutionMappingsIndexWithPostMatchingTest extends TestsForSolutionMappingsIndex {
    @Test
    public void hashTableWithOneInputVariable_getJoinPartnersWithPostMatching1() {
        // getJoinPartners of (var3, z3): would find three solution mappings if without post matching
        final SolutionMapping sm = SolutionMappingUtils.createSolutionMapping(var3, z3);
        final Iterator<SolutionMapping> it = new SolutionMappingsIndexWithPostMatching(createHashTableBasedOneVar()).getJoinPartners(sm).iterator();

        assertHasNext( it, "http://example.org/y2", var2, "http://example.org/z3", var3 );

        assertFalse( it.hasNext() );
    }

    @Test
    public void hashTableWithOneInputVariable_getJoinPartnersWithPostMatching2() {
        // getJoinPartners of (var2, y1, var3, z2): would find two solution mappings if without post matching
        final SolutionMapping sm = SolutionMappingUtils.createSolutionMapping(var2, y1, var3, z2);
        final Iterator<SolutionMapping> it = new SolutionMappingsIndexWithPostMatching(createHashTableBasedOneVar()).getJoinPartners(sm).iterator();

        assertHasNext( it, "http://example.org/y1", var2, "http://example.org/z2", var3 );

        assertFalse( it.hasNext() );
    }

    @Test
    public void hashTableWithTwoInputVariable_getJoinPartnersWithPostMatching1() {
        // getJoinPartners of (var3, z1): would find three solution mappings if without post matching
        final SolutionMapping sm = SolutionMappingUtils.createSolutionMapping(var3, z1);
        final Iterator<SolutionMapping> it = new SolutionMappingsIndexWithPostMatching(createHashTableBasedTwoVars()).getJoinPartners(sm).iterator();

        assertHasNext( it, "http://example.org/x1", var1, "http://example.org/y1", var2, "http://example.org/z1", var3 );

        assertFalse( it.hasNext() );
    }

    @Test
    public void hashTableWithTwoInputVariable_getJoinPartnersWithPostMatching2() {
        // getJoinPartners of (var2, y2, var3, z1): would find one solution mappings if without post matching
        final SolutionMapping sm = SolutionMappingUtils.createSolutionMapping(var2, y2, var3, z1);
        final Iterator<SolutionMapping> it = new SolutionMappingsIndexWithPostMatching(createHashTableBasedTwoVars()).getJoinPartners(sm).iterator();

        assertFalse( it.hasNext() );
    }

    @Test
    public void hashTableWithThreeInputVariable_getJoinPartnersWithPostMatching1() {
        // getJoinPartners(): would find three solution mappings if no post-matching
        final SolutionMapping sm = SolutionMappingUtils.createSolutionMapping(var1, x2);
        final Iterator<SolutionMapping> it = new SolutionMappingsIndexWithPostMatching(createHashTableBasedThreeVars()).getJoinPartners(sm).iterator();

        assertHasNext( it, "http://example.org/x2", var1, "http://example.org/y2", var2, "http://example.org/z2", var3 );

        assertFalse( it.hasNext() );
    }
}
