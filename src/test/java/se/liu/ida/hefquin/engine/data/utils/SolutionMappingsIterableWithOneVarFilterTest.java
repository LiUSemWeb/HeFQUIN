package se.liu.ida.hefquin.engine.data.utils;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.junit.Test;
import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.impl.SolutionMappingUtils;
import se.liu.ida.hefquin.engine.datastructures.impl.SolutionMappingsHashTableBasedOnOneVar;
import se.liu.ida.hefquin.engine.datastructures.impl.SolutionMappingsIndexBase;

import java.util.*;

import static org.junit.Assert.*;

public class SolutionMappingsIterableWithOneVarFilterTest {
    @Test
    public void hashTableWithOneInputVariable() {
        final Var var1 = Var.alloc("v1");
        final Var var2 = Var.alloc("v2");
        final Var var3 = Var.alloc("v3");

        final Node y1 = NodeFactory.createURI("http://example.org/y1");
        final Node y2 = NodeFactory.createURI("http://example.org/y2");
        final Node z1 = NodeFactory.createURI("http://example.org/z1");
        final Node z2 = NodeFactory.createURI("http://example.org/z2");
        final Node z3 = NodeFactory.createURI("http://example.org/z3");
        final Node p = NodeFactory.createURI("http://example.org/p");

        // create SolutionMappingsIndexBase
        final SolutionMappingsIndexBase solMHashTable = new SolutionMappingsHashTableBasedOnOneVar(var2);
        solMHashTable.add( SolutionMappingUtils.createSolutionMapping(
                var2, y1,
                var3, z1) );
        solMHashTable.add( SolutionMappingUtils.createSolutionMapping(
                var2, y1,
                var3, z2) );
        solMHashTable.add( SolutionMappingUtils.createSolutionMapping(
                var2, y2,
                var3, z3) );
        final Iterable<SolutionMapping> allSolMap = solMHashTable.getAllSolutionMappings();

        // iterate over the subset of solution mappings that have a given value for var2
        final Iterable<SolutionMapping> solMapAfterFilter1 = new SolutionMappingsIterableWithOneVarFilter( allSolMap, var2, y1 );
        final Iterator<SolutionMapping> it1 = solMapAfterFilter1.iterator();
        assertTrue( it1.hasNext() );
        final Binding bIt1 = it1.next().asJenaBinding();
        assertEquals( 2, bIt1.size() );

        assertTrue( it1.hasNext() );
        final Binding bIt2 = it1.next().asJenaBinding();
        assertEquals( 2, bIt2.size() );

        assertFalse( it1.hasNext() );

        // no solution mappings that have p for var3
        final Iterable<SolutionMapping> solMapAfterFilter2 = new SolutionMappingsIterableWithOneVarFilter( allSolMap, var3, p );
        final Iterator<SolutionMapping> it2 = solMapAfterFilter2.iterator();

        assertFalse( it2.hasNext() );

        // return all solution mappings: no solution mappings that have value for var1
        final Iterable<SolutionMapping> solMapAfterFilter3 = new SolutionMappingsIterableWithOneVarFilter( allSolMap, var1, y1 );
        final Iterator<SolutionMapping> it3 = solMapAfterFilter3.iterator();
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
