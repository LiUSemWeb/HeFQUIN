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

import java.util.Iterator;

import static org.junit.Assert.*;

public class SolutionMappingsIterableWithThreeVarsFilterTest {
    @Test
    public void hashTableWithOneInputVariable() {
        final Var var1 = Var.alloc("v1");
        final Var var2 = Var.alloc("v2");
        final Var var3 = Var.alloc("v3");
        final Var var4 = Var.alloc("v4");
        final Var var5 = Var.alloc("v4");
        final Var var6 = Var.alloc("v4");

        final Node x1 = NodeFactory.createURI("http://example.org/x1");
        final Node x2 = NodeFactory.createURI("http://example.org/x2");
        final Node y1 = NodeFactory.createURI("http://example.org/y1");
        final Node y2 = NodeFactory.createURI("http://example.org/y2");
        final Node z1 = NodeFactory.createURI("http://example.org/z1");
        final Node z2 = NodeFactory.createURI("http://example.org/z2");
        final Node p = NodeFactory.createURI("http://example.org/p");

        // create SolutionMappingsIndexBase
        final SolutionMappingsIndexBase solMHashTable = new SolutionMappingsHashTableBasedOnOneVar(var2);

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
        final Iterable<SolutionMapping> allSolMap = solMHashTable.getAllSolutionMappings();

        // iterate over the subset of solution mappings that have a given value for var2 and vae3
        final Iterable<SolutionMapping> solMapAfterFilter1 = new SolutionMappingsIterableWithThreeVarsFilter( allSolMap, var3, z1, var1, x1, var2, y1 );
        final Iterator<SolutionMapping> it1 = solMapAfterFilter1.iterator();
        assertTrue( it1.hasNext() );
        final Binding bIt1 = it1.next().asJenaBinding();
        assertEquals( 3, bIt1.size() );

        assertFalse( it1.hasNext() );

        // filter based on subset of variables
        final Iterable<SolutionMapping> solMapAfterFilter2 = new SolutionMappingsIterableWithThreeVarsFilter( allSolMap, var2, y1, var1, x1, var4, p );
        final Iterator<SolutionMapping> it2 = solMapAfterFilter2.iterator();
        assertTrue( it2.hasNext() );
        final Binding bIt21 = it2.next().asJenaBinding();
        assertEquals( 3, bIt21.size() );

        assertTrue( it2.hasNext() );
        final Binding bIt22 = it2.next().asJenaBinding();
        assertEquals( 3, bIt22.size() );

        assertFalse( it2.hasNext() );

        // no solution mappings that having (var1, x2, var2, y1, var3, z2)
        final Iterable<SolutionMapping> solMapAfterFilter3 = new SolutionMappingsIterableWithThreeVarsFilter( allSolMap, var1, x2, var2, y1, var3, z2 );
        final Iterator<SolutionMapping> it3 = solMapAfterFilter3.iterator();

        assertFalse( it3.hasNext() );

        // no solution mappings that having (var2, y1, var1, x2)
        final Iterable<SolutionMapping> solMapAfterFilter4 = new SolutionMappingsIterableWithThreeVarsFilter( allSolMap, var2, y1, var1, x2, var4, p );
        final Iterator<SolutionMapping> it4 = solMapAfterFilter4.iterator();

        assertFalse( it4.hasNext() );

        // return all solution mappings: no solution mappings that have value for var4 or var5 or var6
        final Iterable<SolutionMapping> solMapAfterFilter5 = new SolutionMappingsIterableWithThreeVarsFilter( allSolMap, var5, y1, var6, x2, var4, p );
        final Iterator<SolutionMapping> it5 = solMapAfterFilter5.iterator();
        assertTrue( it5.hasNext() );
        final Binding bIt51 = it5.next().asJenaBinding();
        assertEquals( 3, bIt51.size() );

        assertTrue( it5.hasNext() );
        final Binding bIt52 = it5.next().asJenaBinding();
        assertEquals( 3, bIt52.size() );

        assertTrue( it5.hasNext() );
        final Binding bIt53 = it5.next().asJenaBinding();
        assertEquals( 3, bIt53.size() );

        assertFalse( it5.hasNext() );
    }
}
