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

public class SolutionMappingsHashTableBasedOnTwoVarsTest {
    @Test
    public void hashTableWithTwoInputVariable() {
        final Var var1 = Var.alloc("v1");
        final Var var2 = Var.alloc("v2");
        final Var var3 = Var.alloc("v3");

        final Node x1 = NodeFactory.createURI("http://example.org/x1");
        final Node x2 = NodeFactory.createURI("http://example.org/x2");
        final Node x3 = NodeFactory.createURI("http://example.org/x3");
        final Node y1 = NodeFactory.createURI("http://example.org/y1");
        final Node y2 = NodeFactory.createURI("http://example.org/y2");
        final Node y3 = NodeFactory.createURI("http://example.org/y3");
        final Node z1 = NodeFactory.createURI("http://example.org/z1");
        final Node z2 = NodeFactory.createURI("http://example.org/z2");

        // create SolutionMappingsIndexBase, test method: isEmpty(), contains(), add(), size()
        final SolutionMappingsIndexBase solMHashTable = new SolutionMappingsHashTableBasedOnTwoVars(var1, var2);
        assertTrue(solMHashTable.isEmpty());

        final SolutionMapping sm0 = SolutionMappingUtils.createSolutionMapping(var1, x1, var2, y1, var3, z1);
        assertFalse(solMHashTable.contains(sm0));
        solMHashTable.add(sm0);
        assertTrue(solMHashTable.contains(sm0));

        solMHashTable.add( SolutionMappingUtils.createSolutionMapping(
                var1, x1,
                var2, y1,
                var3, z2) );
        solMHashTable.add( SolutionMappingUtils.createSolutionMapping(
                var1, x2,
                var2, y2,
                var3, z2) );
        assertFalse(solMHashTable.isEmpty());
        assertEquals( 3, solMHashTable.size() );

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

        //---------------------------------------
        // findSolutionMappings(var2, y2): one matching solution mapping
        final Iterable<SolutionMapping> solMapVar2= solMHashTable.findSolutionMappings(var2, y2);
        final Iterator<SolutionMapping> itVar2 = solMapVar2.iterator();

        assertTrue( itVar2.hasNext() );
        final Binding bItVar2 = itVar2.next().asJenaBinding();
        assertEquals( 3, bItVar2.size() );

        assertFalse( itVar2.hasNext() );

        // findSolutionMappings(var3, z1): return one matching solution mapping (hash table is not built based on var3)
        Iterable<SolutionMapping> solMapVar3 = solMHashTable.findSolutionMappings(var3, z1);
        final Iterator<SolutionMapping> itVar3 = solMapVar3.iterator();

        assertTrue( itVar3.hasNext() );
        final Binding bItVar3 = itVar3.next().asJenaBinding();
        assertEquals( 3, bItVar3.size() );

        assertFalse( itVar3.hasNext() );

        // findSolutionMappings(var1, x2, var2, y2): return one matching solution mapping (hash table is built based on (var1, var2))
        final Iterable<SolutionMapping> solMapVar13= solMHashTable.findSolutionMappings(var1, x2, var2, y2);
        for(final SolutionMapping sm: solMapVar13){
            final Binding bsm = sm.asJenaBinding();
            if ( bsm.get(var3).getURI().equals("http://example.org/z2") ) {
                assertEquals( "http://example.org/x2", bsm.get(var1).getURI() );
                assertEquals( "http://example.org/y2", bsm.get(var2).getURI() );
            }
            else {
                fail( "Unexpected URI for ?v3: " + bsm.get(var3).getURI() );
            }
        }

        // findSolutionMappings(var2, y2, var1, x2), change order of var1 and var2
        final Iterable<SolutionMapping> solMapVar21= solMHashTable.findSolutionMappings(var2, y2, var1, x2);
        for(final SolutionMapping sm: solMapVar21){
            final Binding bsm = sm.asJenaBinding();
            if ( bsm.get(var3).getURI().equals("http://example.org/z2") ) {
                assertEquals( "http://example.org/x2", bsm.get(var1).getURI() );
                assertEquals( "http://example.org/y2", bsm.get(var2).getURI() );
            }
            else {
                fail( "Unexpected URI for ?v3: " + bsm.get(var3).getURI() );
            }
        }

        // findSolutionMappings(var1, x1, var3, z1): return one matching solution mapping
        final Iterable<SolutionMapping> solMap13= solMHashTable.findSolutionMappings(var1, x1, var3, z1);
        final Iterator<SolutionMapping> itVar13 = solMap13.iterator();

        assertTrue( itVar13.hasNext() );
        final Binding bItVar13 = itVar13.next().asJenaBinding();
        assertEquals( 3, bItVar13.size() );

        assertFalse( itVar13.hasNext() );

        // findSolutionMappings(var1, x1, var2, y1, var3, z1)
        final Iterable<SolutionMapping> solMap123_1= solMHashTable.findSolutionMappings(var1, x1, var2, y1, var3, z1);
        final Iterator<SolutionMapping> itVar123_1 = solMap123_1.iterator();

        assertTrue( itVar123_1.hasNext() );
        final Binding bItVar123_1 = itVar123_1.next().asJenaBinding();
        assertEquals( 3, bItVar123_1.size() );

        assertFalse( itVar123_1.hasNext() );

        // findSolutionMappings(var1, x1, var2, y2, var3, z1): no matching solution mapping
        final Iterable<SolutionMapping> solMap123_2= solMHashTable.findSolutionMappings(var1, x1, var2, y2, var3, z1);
        final Iterator<SolutionMapping> itVar123_2 = solMap123_2.iterator();

        assertFalse( itVar123_2.hasNext() );

        //----------------------------
        // getJoinPartners(): two join variables with two join partners
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

        // getJoinPartners(): two join variables but without join partner (case 1: subset matching)
        final SolutionMapping sm2 = SolutionMappingUtils.createSolutionMapping(var1, x1, var2, y2);
        Iterable<SolutionMapping> matchSolMap2 = solMHashTable.getJoinPartners(sm2);
        final Iterator<SolutionMapping> it2 = matchSolMap2.iterator();
        assertFalse( it2.hasNext() );

        // getJoinPartners(): two join variables but without join partner (case 2: no matching)
        final SolutionMapping sm3 = SolutionMappingUtils.createSolutionMapping(var1, x3, var2, y3);
        Iterable<SolutionMapping> matchSolMap3 = solMHashTable.getJoinPartners(sm3);
        final Iterator<SolutionMapping> it3 = matchSolMap3.iterator();
        assertFalse( it3.hasNext() );

        // getJoinPartners(): do not contain complete join variables. Return one solution mappings after filtering
        final SolutionMapping sm4 = SolutionMappingUtils.createSolutionMapping(var2, y2);
        Iterable<SolutionMapping> matchSolMap4 = solMHashTable.getJoinPartners(sm4);
        final Iterator<SolutionMapping> it4 = matchSolMap4.iterator();
        assertTrue( it4.hasNext() );
        final Binding bIt4 = it4.next().asJenaBinding();
        assertEquals( 3, bIt4.size() );

        assertFalse( it4.hasNext() );

        // getJoinPartners(): do not contain complete join variables. Return (var1, x2, var2, y2, var3, z2) after filtering
        final SolutionMapping sm5 = SolutionMappingUtils.createSolutionMapping(var2, y2, var3, z1);
        Iterable<SolutionMapping> matchSolMap5 = solMHashTable.getJoinPartners(sm5);
        final Iterator<SolutionMapping> it5 = matchSolMap5.iterator();

        assertTrue( it5.hasNext() );
        final Binding bIt5 = it5.next().asJenaBinding();
        assertEquals( 3, bIt5.size() );

        assertFalse( it5.hasNext() );

        // getJoinPartners(): do not contain any join variable. Return all solution mappings if no postingMatching is applied.
        final SolutionMapping sm6 = SolutionMappingUtils.createSolutionMapping(var3, z1);
        Iterable<SolutionMapping> matchSolMap6 = solMHashTable.getJoinPartners(sm6);
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

        // clear()
        solMHashTable.clear();
        assertTrue(solMHashTable.isEmpty());
    }
}
