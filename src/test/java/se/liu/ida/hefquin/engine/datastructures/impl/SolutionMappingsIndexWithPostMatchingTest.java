package se.liu.ida.hefquin.engine.datastructures.impl;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.junit.Test;
import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.impl.SolutionMappingUtils;

import java.util.*;

import static org.junit.Assert.*;

public class SolutionMappingsIndexWithPostMatchingTest {
    @Test
    public void hashTableWithOneInputVariable() {
        final Var var2 = Var.alloc("v2");
        final Var var3 = Var.alloc("v3");

        final Node y1 = NodeFactory.createURI("http://example.org/y1");
        final Node y2 = NodeFactory.createURI("http://example.org/y2");
        final Node z1 = NodeFactory.createURI("http://example.org/z1");
        final Node z2 = NodeFactory.createURI("http://example.org/z2");
        final Node z3 = NodeFactory.createURI("http://example.org/z3");

        final SolutionMappingsHashTable solMHashTable = new SolutionMappingsHashTable(var2);
        solMHashTable.add( SolutionMappingUtils.createSolutionMapping(
                var2, y1,
                var3, z1) );
        solMHashTable.add( SolutionMappingUtils.createSolutionMapping(
                var2, y1,
                var3, z2) );
        solMHashTable.add( SolutionMappingUtils.createSolutionMapping(
                var2, y2,
                var3, z3) );

        // getJoinPartners with post matching
        final SolutionMapping sm1 = SolutionMappingUtils.createSolutionMapping(var3, z3);
        final Iterable<SolutionMapping> matchSolMap1 = new SolutionMappingsIndexWithPostMatching(solMHashTable).getJoinPartners(sm1);
        final Iterator<SolutionMapping> itVar1 = matchSolMap1.iterator();

        assertTrue( itVar1.hasNext() );
        final Binding bItVar1 = itVar1.next().asJenaBinding();
        assertEquals( 2, bItVar1.size() );

        assertFalse( itVar1.hasNext() );

        // getJoinPartners with post matching
        final SolutionMapping sm2 = SolutionMappingUtils.createSolutionMapping(var2, y1, var3, z2);
        final Iterable<SolutionMapping> matchSolMap2 = new SolutionMappingsIndexWithPostMatching(solMHashTable).getJoinPartners(sm2);
        final Iterator<SolutionMapping> itVar2 = matchSolMap2.iterator();

        assertTrue( itVar2.hasNext() );
        final Binding bItVar2 = itVar2.next().asJenaBinding();
        assertEquals( 2, bItVar2.size() );

        assertFalse( itVar2.hasNext() );
    }

    @Test
    public void hashTableWithTwoInputVariable() {
        final Var var1 = Var.alloc("v1");
        final Var var2 = Var.alloc("v2");
        final Var var3 = Var.alloc("v3");

        final Node x1 = NodeFactory.createURI("http://example.org/x1");
        final Node x2 = NodeFactory.createURI("http://example.org/x2");
        final Node y1 = NodeFactory.createURI("http://example.org/y1");
        final Node y2 = NodeFactory.createURI("http://example.org/y2");
        final Node z1 = NodeFactory.createURI("http://example.org/z1");
        final Node z2 = NodeFactory.createURI("http://example.org/z2");

        final SolutionMappingsHashTable solMHashTable = new SolutionMappingsHashTable(var1, var2);
        solMHashTable.add( SolutionMappingUtils.createSolutionMapping(
                var1, x1,
                var2, y1,
                var3, z1) );
        solMHashTable.add( SolutionMappingUtils.createSolutionMapping(
                var1, x1,
                var2, y1,
                var3, z2) );
        solMHashTable.add( SolutionMappingUtils.createSolutionMapping(
                var1, x2,
                var2, y2,
                var3, z2) );

        // getJoinPartners with post matching
        final SolutionMapping sm1 = SolutionMappingUtils.createSolutionMapping(var1, x2);
        final Iterable<SolutionMapping> matchSolMap1 = new SolutionMappingsIndexWithPostMatching(solMHashTable).getJoinPartners(sm1);
        final Iterator<SolutionMapping> it1 = matchSolMap1.iterator();
        assertTrue( it1.hasNext() );
        final Binding bIt1 = it1.next().asJenaBinding();
        assertEquals( 3, bIt1.size() );

        assertFalse( it1.hasNext() );

        // getJoinPartners()
        final SolutionMapping sm2 = SolutionMappingUtils.createSolutionMapping(var2, y2, var3, z1);
        final Iterable<SolutionMapping> matchSolMap2 =  new SolutionMappingsIndexWithPostMatching(solMHashTable).getJoinPartners(sm2);
        final Iterator<SolutionMapping> it2 = matchSolMap2.iterator();

        assertFalse( it2.hasNext() );

        // getJoinPartners()
        final SolutionMapping sm3 = SolutionMappingUtils.createSolutionMapping(var3, z1);
        final Iterable<SolutionMapping> matchSolMap3 =  new SolutionMappingsIndexWithPostMatching(solMHashTable).getJoinPartners(sm3);
        final Iterator<SolutionMapping> it3 = matchSolMap3.iterator();
        assertTrue( it3.hasNext() );
        final Binding bIt3 = it3.next().asJenaBinding();
        assertEquals( 3, bIt3.size() );

        assertFalse( it3.hasNext() );
    }

}
