package se.liu.ida.hefquin.engine.queryplan.executable.impl.pullbased;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.junit.Test;
import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.impl.SolutionMappingUtils;
import se.liu.ida.hefquin.engine.datastructures.impl.SolutionMappingsHashTable;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.GenericIntermediateResultBlockImpl;

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

        final GenericIntermediateResultBlockImpl input1 = new GenericIntermediateResultBlockImpl();
        input1.add( SolutionMappingUtils.createSolutionMapping(
                var2, y1,
                var3, z1) );
        input1.add( SolutionMappingUtils.createSolutionMapping(
                var2, y1,
                var3, z2) );
        input1.add( SolutionMappingUtils.createSolutionMapping(
                var2, y2,
                var3, z3) );

        final Set<Var> inputVars = new HashSet<>();
        inputVars.add(var2);

        // Build SolutionMappingsHashTable for input1
        final SolutionMappingsHashTable solMHashTable = new SolutionMappingsHashTable(inputVars);
        for ( final SolutionMapping sm : input1.getSolutionMappings() ) {
            solMHashTable.add(sm);
        }

        //------------------
        // Checking SolutionMappingsHashTable
        assertEquals( 2, solMHashTable.size() );
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
            if ( bsm.get(var3).getURI().equals("http://example.org/z3") ) {
                assertEquals( "http://example.org/y2", bsm.get(var2).getURI() );
            }
            else {
                fail( "Unexpected URI for ?v3: " + bsm.get(var3).getURI() );
            }
        }

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

        boolean b1Found = false;
        boolean b2Found = false;
        for ( final Binding b: result ) {
            assertEquals( 2, b.size() );
            if ( b.get(var3).getURI().equals("http://example.org/z1") ) {
                assertEquals( "http://example.org/y1", b.get(var2).getURI() );
                b1Found = true;
            }
            else if ( b.get(var3).getURI().equals("http://example.org/z2") ) {
                assertEquals( "http://example.org/y1", b.get(var2).getURI() );
                b2Found = true;
            }
            else {
                fail( "Unexpected URI for ?v3: " + b.get(var3).getURI() );
            }
        }
        assertTrue(b1Found);
        assertTrue(b2Found);

        // getJoinPartners of sm2: one join variable but without join partner
        final SolutionMapping sm2 = SolutionMappingUtils.createSolutionMapping(var1, x2, var2, y3);
        Iterable<SolutionMapping> matchSolMap2 = solMHashTable.getJoinPartners(sm2);
        final Iterator<SolutionMapping> it2 = matchSolMap2.iterator();
        assertFalse( it2.hasNext() );

        // getJoinPartners of sm3: do not contain the join variable, should throw IllegalArgumentException
        final SolutionMapping sm3 = SolutionMappingUtils.createSolutionMapping(var1, x2, var3, z3);
        assertThrows(IllegalArgumentException.class,
                ()->{ solMHashTable.getJoinPartners(sm3); });

    }

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

        final GenericIntermediateResultBlockImpl input1 = new GenericIntermediateResultBlockImpl();
        input1.add( SolutionMappingUtils.createSolutionMapping(
                var1, x1,
                var2, y1,
                var3, z1) );
        input1.add( SolutionMappingUtils.createSolutionMapping(
                var1, x1,
                var2, y1,
                var3, z2) );
        input1.add( SolutionMappingUtils.createSolutionMapping(
                var1, x2,
                var2, y2,
                var3, z2) );

        final Set<Var> inputVars = new HashSet<>();
        inputVars.add(var1);
        inputVars.add(var2);

        // Create SolutionMappingsHashTable for input1
        final SolutionMappingsHashTable solMHashTable = new SolutionMappingsHashTable(inputVars);
        for ( final SolutionMapping smL : input1.getSolutionMappings() ) {
            solMHashTable.add(smL);
        }

        //------------------
        // Checking SolutionMappingsHashTable
        assertEquals( 2, solMHashTable.size() );

        final Iterator<SolutionMapping> it = solMHashTable.iterator();
        assertTrue( it.hasNext() );
        final Binding b1 = it.next().asJenaBinding();
        assertEquals( 3, b1.size() );

        assertTrue( it.hasNext() );
        final Binding b2 = it.next().asJenaBinding();
        assertEquals( 3, b2.size() );

        assertTrue( it.hasNext() );
        final Binding b3 = it.next().asJenaBinding();
        assertEquals( 3, b2.size() );

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

        // getJoinPartners of sm4: do not contain any join variable, should throw IllegalArgumentException
        final SolutionMapping sm4 = SolutionMappingUtils.createSolutionMapping(var3, z2);
        assertThrows(IllegalArgumentException.class,
                ()->{ solMHashTable.getJoinPartners(sm4); });

        // getJoinPartners of sm5: do not contain complete join variables, should throw IllegalArgumentException
        final SolutionMapping sm5 = SolutionMappingUtils.createSolutionMapping(var1, x2);
        assertThrows(IllegalArgumentException.class,
                ()->{ solMHashTable.getJoinPartners(sm5); });
    }

    @Test
    public void hashTableWithEmptyInputVariable() {
        final Var var1 = Var.alloc("v1");
        final Var var2 = Var.alloc("v2");
        final Var var3 = Var.alloc("v3");

        final Node x1 = NodeFactory.createURI("http://example.org/x1");
        final Node x2 = NodeFactory.createURI("http://example.org/x2");
        final Node y1 = NodeFactory.createURI("http://example.org/y1");
        final Node z1 = NodeFactory.createURI("http://example.org/z1");

        final GenericIntermediateResultBlockImpl input1 = new GenericIntermediateResultBlockImpl();
        input1.add( SolutionMappingUtils.createSolutionMapping(var1, x1) );
        input1.add( SolutionMappingUtils.createSolutionMapping(var1, x2) );

        final Set<Var> inputVars = new HashSet<>();

        // Create SolutionMappingsHashTable for input1
        final SolutionMappingsHashTable solMHashTable = new SolutionMappingsHashTable(inputVars);
        for ( final SolutionMapping smL : input1.getSolutionMappings() ) {
            solMHashTable.add(smL);
        }

        // Key: NULL
        assertEquals( 1, solMHashTable.size() );
        final Iterator<SolutionMapping> it = solMHashTable.iterator();

        assertTrue( it.hasNext() );
        final Binding b1 = it.next().asJenaBinding();
        assertEquals( 1, b1.size() );

        assertTrue( it.hasNext() );
        final Binding b2 = it.next().asJenaBinding();
        assertEquals( 1, b2.size() );

        assertFalse( it.hasNext() );

        // getJoinPartners of sm1: empty join variable, return all solMaps as join partners
        final SolutionMapping sm1 = SolutionMappingUtils.createSolutionMapping(var2, y1, var3, z1);
        Iterable<SolutionMapping> matchSolMap1 = solMHashTable.getJoinPartners(sm1);

        final Iterator<SolutionMapping> it1 = matchSolMap1.iterator();
        assertTrue( it1.hasNext() );
        final Binding it1b1 = it1.next().asJenaBinding();
        assertEquals( 1, it1b1.size() );

        assertTrue( it1.hasNext() );
        final Binding it1b2 = it1.next().asJenaBinding();
        assertEquals( 1, it1b2.size() );

        assertFalse( it1.hasNext() );

        // getJoinPartners of sm2: empty join variable but with possible matching variable (return compatible join partner)
        final SolutionMapping sm2 = SolutionMappingUtils.createSolutionMapping(var1, x1, var2, y1, var3, z1);
        Iterable<SolutionMapping> matchSolMap2 = solMHashTable.getJoinPartners(sm2);

        final Iterator<SolutionMapping> it2 = matchSolMap2.iterator();
        assertTrue( it2.hasNext() );
        final Binding it2b1 = it2.next().asJenaBinding();
        assertEquals( 1, it2b1.size() );

        assertFalse( it2.hasNext() );
    }

    @Test
    public void joinWithOneJoinVariable_withPossibleVars_noOverlap() {
        final Var var1 = Var.alloc("v1");
        final Var var2 = Var.alloc("v2");
        final Var var3 = Var.alloc("v3");
        final Var var4 = Var.alloc("v4");
        final Var var5 = Var.alloc("v5");

        final Node x1 = NodeFactory.createURI("http://example.org/x1");
        final Node x2 = NodeFactory.createURI("http://example.org/x2");
        final Node y1 = NodeFactory.createURI("http://example.org/y1");
        final Node y2 = NodeFactory.createURI("http://example.org/y2");
        final Node y3 = NodeFactory.createURI("http://example.org/y3");
        final Node z1 = NodeFactory.createURI("http://example.org/z1");
        final Node z2 = NodeFactory.createURI("http://example.org/z2");
        final Node z3 = NodeFactory.createURI("http://example.org/z3");
        final Node p1 = NodeFactory.createURI("http://example.org/p1");
        final Node p2 = NodeFactory.createURI("http://example.org/p2");

        final GenericIntermediateResultBlockImpl input1 = new GenericIntermediateResultBlockImpl();
        input1.add( SolutionMappingUtils.createSolutionMapping(
                var2, y1,
                var3, z1,
                var5, p2) );
        input1.add( SolutionMappingUtils.createSolutionMapping(
                var2, y1,
                var3, z2) );
        input1.add( SolutionMappingUtils.createSolutionMapping(
                var2, y2,
                var3, z3) );

        final GenericIntermediateResultBlockImpl input2 = new GenericIntermediateResultBlockImpl();
        input2.add( SolutionMappingUtils.createSolutionMapping(
                var1, x1,
                var2, y1,
                var4, p1) );
        input2.add( SolutionMappingUtils.createSolutionMapping(
                var1, x2,
                var2, y3) );

        final Set<Var> varsCertain1 = new HashSet<>();
        varsCertain1.add(var2);
        varsCertain1.add(var3);
        final Set<Var> varsPossible1 = new HashSet<>();
        varsPossible1.add(var5);

        final Set<Var> varsCertain2 = new HashSet<>();
        varsCertain2.add(var1);
        varsCertain2.add(var2);
        final Set<Var> varsPossible2 = new HashSet<>();
        varsPossible2.add(var4);

        varsCertain1.retainAll( varsCertain2 );

        // Create SolutionMappingsHashTable for input1
        final SolutionMappingsHashTable solMHashTable1 = new SolutionMappingsHashTable(varsCertain1);
        for ( final SolutionMapping smL : input1.getSolutionMappings() ) {
            solMHashTable1.add(smL);
        }

        //----------------------------
        // getJoinPartners of Input2
        final List<Iterable<SolutionMapping>> matchSolMapR = new ArrayList<>();
        for ( final SolutionMapping smR : input2.getSolutionMappings() ) {
            matchSolMapR.add(solMHashTable1.getJoinPartners(smR));
        }

        assertEquals( 2, matchSolMapR.size() );

        // Checking join partner of the first solution mapping (with two join partners)
        final Set<Binding> result = new HashSet<>();
        final Iterator<SolutionMapping> it1 = matchSolMapR.get(0).iterator();
        assertTrue( it1.hasNext() );
        result.add( it1.next().asJenaBinding() );

        assertTrue( it1.hasNext() );
        result.add( it1.next().asJenaBinding() );

        assertFalse( it1.hasNext() );

        boolean b1Found = false;
        boolean b2Found = false;
        for ( final Binding b: result ) {
            if ( b.get(var3).getURI().equals("http://example.org/z1") ) {
                assertEquals( "http://example.org/y1", b.get(var2).getURI() );
                assertEquals( "http://example.org/p2", b.get(var5).getURI() );
                b1Found = true;
            }
            else if ( b.get(var3).getURI().equals("http://example.org/z2") ) {
                assertEquals( "http://example.org/y1", b.get(var2).getURI() );
                b2Found = true;
            }
            else {
                fail( "Unexpected URI for ?v3: " + b.get(var3).getURI() );
            }
        }
        assertTrue(b1Found);
        assertTrue(b2Found);
    }

    @Test
    public void joinWithOneJoinVariable_withPossibleVars_overlapped() {
        final Var var1 = Var.alloc("v1");
        final Var var2 = Var.alloc("v2");
        final Var var3 = Var.alloc("v3");
        final Var var4 = Var.alloc("v4");

        final Node x1 = NodeFactory.createURI("http://example.org/x1");
        final Node x2 = NodeFactory.createURI("http://example.org/x2");
        final Node y1 = NodeFactory.createURI("http://example.org/y1");
        final Node y2 = NodeFactory.createURI("http://example.org/y2");
        final Node y3 = NodeFactory.createURI("http://example.org/y3");
        final Node z1 = NodeFactory.createURI("http://example.org/z1");
        final Node z2 = NodeFactory.createURI("http://example.org/z2");
        final Node z3 = NodeFactory.createURI("http://example.org/z3");
        final Node p1 = NodeFactory.createURI("http://example.org/p1");

        final GenericIntermediateResultBlockImpl input1 = new GenericIntermediateResultBlockImpl();
        input1.add( SolutionMappingUtils.createSolutionMapping(
                var4, p1,
                var2, y1,
                var3, z1) );
        input1.add( SolutionMappingUtils.createSolutionMapping(
                var2, y1,
                var3, z2) );
        input1.add( SolutionMappingUtils.createSolutionMapping(
                var2, y2,
                var3, z3) );

        final GenericIntermediateResultBlockImpl input2 = new GenericIntermediateResultBlockImpl();
        input2.add( SolutionMappingUtils.createSolutionMapping(
                var1, x1,
                var2, y1,
                var3, z1) );
        input2.add( SolutionMappingUtils.createSolutionMapping(
                var1, x2,
                var2, y3) );

        final Set<Var> varsCertain1 = new HashSet<>();
        varsCertain1.add(var2);
        varsCertain1.add(var3);
        final Set<Var> varsPossible1 = new HashSet<>();
        varsPossible1.add(var1);

        final Set<Var> varsCertain2 = new HashSet<>();
        varsCertain2.add(var1);
        varsCertain2.add(var2);
        final Set<Var> varsPossible2 = new HashSet<>();
        varsPossible2.add(var3);

        varsCertain1.retainAll( varsCertain2 );

        // Create SolutionMappingsHashTable for input1
        final SolutionMappingsHashTable solMHashTable1 = new SolutionMappingsHashTable(varsCertain1);
        for ( final SolutionMapping smL : input1.getSolutionMappings() ) {
            solMHashTable1.add(smL);
        }

        //----------------------------
        // getJoinPartners of Input2: Only one join partner
        final List<Iterable<SolutionMapping>> matchSolMapR = new ArrayList<>();
        for ( final SolutionMapping smR : input2.getSolutionMappings() ) {
            matchSolMapR.add(solMHashTable1.getJoinPartners(smR));
        }

        assertEquals( 2, matchSolMapR.size() );

        final Iterator<SolutionMapping> it1 = matchSolMapR.get(0).iterator();
        assertTrue( it1.hasNext() );
        final Binding b = it1.next().asJenaBinding();
        assertEquals( 3, b.size() );

        assertFalse( it1.hasNext() );
    }
}
