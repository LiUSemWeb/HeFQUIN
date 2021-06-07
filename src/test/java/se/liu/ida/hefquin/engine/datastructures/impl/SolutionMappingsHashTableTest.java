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

public class SolutionMappingsHashTableTest {
    @Test
    public void hashTableWithOneInputVariable() {
        final Var var1 = Var.alloc("v1");
        final Var var2 = Var.alloc("v2");
        final Var var3 = Var.alloc("v3");

        final Node x1 = NodeFactory.createURI("http://example.org/x1");
        final Node x2 = NodeFactory.createURI("http://example.org/x2");
        final Node y1 = NodeFactory.createURI("http://example.org/y1");
        final Node y2 = NodeFactory.createURI("http://example.org/y2");
        final Node y3 = NodeFactory.createURI("http://example.org/y3");
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

        //------------------
        // Checking SolutionMappingsHashTable
        assertEquals( 3, solMHashTable.size() );
        final Iterator<SolutionMapping> it = solMHashTable.iterator();
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

        // Find solution mappings with (var2, y2)
        final Iterable<SolutionMapping> solMap= solMHashTable.findSolutionMappings(var2, y2);
        for ( final SolutionMapping sm: solMap ){
            final Binding bsm = sm.asJenaBinding();
            if ( bsm.get(var2).getURI().equals("http://example.org/y2") ) {
                assertEquals( "http://example.org/z3", bsm.get(var3).getURI() );
            }
            else {
                fail( "Unexpected URI for ?v2: " + bsm.get(var2).getURI() );
            }
        }

        // Find solution mappings with (var3, z3), getAllSolutionMappings()
        Iterable<SolutionMapping> solMapVar3 = solMHashTable.findSolutionMappings(var3, z3);
        final Iterator<SolutionMapping> itVar3 = solMapVar3.iterator();

        assertTrue( itVar3.hasNext() );
        final Binding bItVar31 = itVar3.next().asJenaBinding();
        assertEquals( 2, bItVar31.size() );

        assertTrue( itVar3.hasNext() );
        final Binding bItVar32 = itVar3.next().asJenaBinding();
        assertEquals( 2, bItVar32.size() );

        assertTrue( itVar3.hasNext() );
        final Binding bItVar33 = itVar3.next().asJenaBinding();
        assertEquals( 2, bItVar33.size() );

        assertFalse( itVar3.hasNext() );

        //----------------------------
        // Probe
        // getJoinPartners of sm1: one join variable with two join partners
        final SolutionMapping sm1 = SolutionMappingUtils.createSolutionMapping(var1, x1, var2, y1);
        Iterable<SolutionMapping> matchSolMap1 = solMHashTable.getJoinPartners(sm1);

        final Set<Binding> result = new HashSet<>();
        final Iterator<SolutionMapping> it1 = matchSolMap1.iterator();
        assertTrue( it1.hasNext() );
        result.add( it1.next().asJenaBinding() );

        assertTrue( it1.hasNext() );
        result.add( it1.next().asJenaBinding() );

        assertFalse( it1.hasNext() );

        for ( final Binding b: result ) {
            assertEquals( 2, b.size() );
            if ( b.get(var3).getURI().equals("http://example.org/z1") ) {
                assertEquals( "http://example.org/y1", b.get(var2).getURI() );
            }
            else if ( b.get(var3).getURI().equals("http://example.org/z2") ) {
                assertEquals( "http://example.org/y1", b.get(var2).getURI() );
            }
            else {
                fail( "Unexpected URI for ?v3: " + b.get(var3).getURI() );
            }
        }

        // getJoinPartners of sm2: one join variable but without join partner
        final SolutionMapping sm2 = SolutionMappingUtils.createSolutionMapping(var1, x2, var2, y3);
        Iterable<SolutionMapping> matchSolMap2 = solMHashTable.getJoinPartners(sm2);
        final Iterator<SolutionMapping> it2 = matchSolMap2.iterator();
        assertFalse( it2.hasNext() );

        // getJoinPartners of sm3: do not contain the join variable, should return all sm in hash table as join partners
        final SolutionMapping sm3 = SolutionMappingUtils.createSolutionMapping(var1, x2);
        Iterable<SolutionMapping> matchSolMap3 = solMHashTable.getJoinPartners(sm3);
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

        // Clear SolutionMappingsHashTable
        final SolutionMapping sm0 = SolutionMappingUtils.createSolutionMapping(var2, y1, var3, z1);

        assertTrue(solMHashTable.contains(sm0));
        solMHashTable.clear();
        assertFalse(solMHashTable.contains(sm0));

        assertTrue(solMHashTable.isEmpty());
    }

    @Test
    public void hashTableWithTwoInputVariable() {
        final Var var1 = Var.alloc("v1");
        final Var var2 = Var.alloc("v2");
        final Var var3 = Var.alloc("v3");
        final Var var4 = Var.alloc("v4");

        final Node x1 = NodeFactory.createURI("http://example.org/x1");
        final Node x2 = NodeFactory.createURI("http://example.org/x2");
        final Node x3 = NodeFactory.createURI("http://example.org/x3");
        final Node y1 = NodeFactory.createURI("http://example.org/y1");
        final Node y2 = NodeFactory.createURI("http://example.org/y2");
        final Node y3 = NodeFactory.createURI("http://example.org/y3");
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

        //------------------
        // Checking SolutionMappingsHashTable
        assertEquals( 3, solMHashTable.size() );

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

        // Checking solution mappings with (var1, x2, var2, y2)
        final Iterable<SolutionMapping> solMap2= solMHashTable.findSolutionMappings(var1, x2, var2, y2);
        for(final SolutionMapping sm: solMap2){
            final Binding bsm = sm.asJenaBinding();
            if ( bsm.get(var3).getURI().equals("http://example.org/z2") ) {
                assertEquals( "http://example.org/x2", bsm.get(var1).getURI() );
                assertEquals( "http://example.org/y2", bsm.get(var2).getURI() );
            }
            else {
                fail( "Unexpected URI for ?v3: " + bsm.get(var3).getURI() );
            }
        }

        // Checking solution mappings with (var2, y2, var1, x2), change order of var1 and var2
        final Iterable<SolutionMapping> solMap3= solMHashTable.findSolutionMappings(var2, y2, var1, x2);
        for(final SolutionMapping sm: solMap3){
            final Binding bsm = sm.asJenaBinding();
            if ( bsm.get(var3).getURI().equals("http://example.org/z2") ) {
                assertEquals( "http://example.org/x2", bsm.get(var1).getURI() );
                assertEquals( "http://example.org/y2", bsm.get(var2).getURI() );
            }
            else {
                fail( "Unexpected URI for ?v3: " + bsm.get(var3).getURI() );
            }
        }

        //----------------------------
        // Probe
        // getJoinPartners of sm1: two join variables with two join partners
        final SolutionMapping sm1 = SolutionMappingUtils.createSolutionMapping(var1, x1, var2, y1);
        Iterable<SolutionMapping> matchSolMap1 = solMHashTable.getJoinPartners(sm1);

        final Set<Binding> result = new HashSet<>();
        final Iterator<SolutionMapping> it1 = matchSolMap1.iterator();
        assertTrue( it1.hasNext() );
        result.add( it1.next().asJenaBinding() );

        assertTrue( it1.hasNext() );
        result.add( it1.next().asJenaBinding() );

        assertFalse( it1.hasNext() );

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

        // getJoinPartners of sm2: two join variables but without join partner (case 1: subset matching)
        final SolutionMapping sm2 = SolutionMappingUtils.createSolutionMapping(var1, x1, var2, y2);
        Iterable<SolutionMapping> matchSolMap2 = solMHashTable.getJoinPartners(sm2);
        final Iterator<SolutionMapping> it2 = matchSolMap2.iterator();
        assertFalse( it2.hasNext() );

        // getJoinPartners of sm3: two join variables but without join partner (case 2: no matching)
        final SolutionMapping sm3 = SolutionMappingUtils.createSolutionMapping(var1, x3, var2, y3);
        Iterable<SolutionMapping> matchSolMap3 = solMHashTable.getJoinPartners(sm3);
        final Iterator<SolutionMapping> it3 = matchSolMap3.iterator();
        assertFalse( it3.hasNext() );

        // getJoinPartners of sm4: do not contain any join variable
        final SolutionMapping sm4 = SolutionMappingUtils.createSolutionMapping(var4, z2);
        Iterable<SolutionMapping> matchSolMap4 = solMHashTable.getJoinPartners(sm4);
        final Iterator<SolutionMapping> it4 = matchSolMap4.iterator();
        assertTrue( it4.hasNext() );
        final Binding bIt41 = it4.next().asJenaBinding();
        assertEquals( 3, bIt41.size() );

        assertTrue( it4.hasNext() );
        final Binding bIt42 = it4.next().asJenaBinding();
        assertEquals( 3, bIt42.size() );

        assertTrue( it4.hasNext() );
        final Binding bIt43 = it4.next().asJenaBinding();
        assertEquals( 3, bIt43.size() );

        assertFalse( it4.hasNext() );

        // getJoinPartners of sm5: do not contain complete join variables (compare with the sm4, this one needs double-check of compatible)
        final SolutionMapping sm5 = SolutionMappingUtils.createSolutionMapping(var1, x2);
        Iterable<SolutionMapping> matchSolMap5 = new SolutionMappingsIndexWithPostMatching(solMHashTable).getJoinPartners(sm5);
        final Iterator<SolutionMapping> it5 = matchSolMap5.iterator();
        assertTrue( it5.hasNext() );
        final Binding bIt51 = it5.next().asJenaBinding();
        assertEquals( 3, bIt51.size() );

        assertFalse( it5.hasNext() );

    }

    @Test
    public void hashTableWithEmptyInputVariable() {

        final List<Var> inputVars = Arrays.asList(); // create an empty list
        assertThrows(AssertionError.class,
                ()->{ new SolutionMappingsHashTable(inputVars); });
    }
}
