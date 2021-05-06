package se.liu.ida.hefquin.engine.queryplan.executable.impl.pullbased;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.junit.Test;
import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.impl.SolutionMappingUtils;
import se.liu.ida.hefquin.engine.datastructures.impl.SolutionMappingsHashTable;
import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.GenericIntermediateResultBlockImpl;

import java.util.*;

import static org.junit.Assert.*;

public class SolutionMappingsHashTableTest {
    @Test
    public void hashTableWithOneJoinVariable_noPossibleVars() {
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

        final GenericIntermediateResultBlockImpl input2 = new GenericIntermediateResultBlockImpl();
        input2.add( SolutionMappingUtils.createSolutionMapping(
                var1, x1,
                var2, y1) );
        input2.add( SolutionMappingUtils.createSolutionMapping(
                var1, x2,
                var2, y3) );

        final Set<Var> varsCertain1 = new HashSet<>();
        final Set<Var> varsPossible1 = new HashSet<>();
        varsCertain1.add(var2);
        varsCertain1.add(var3);

        final Set<Var> varsCertain2 = new HashSet<>();
        final Set<Var> varsPossible2 = new HashSet<>();
        varsCertain2.add(var1);
        varsCertain2.add(var2);

        final ExpectedVariables[] inputVars = getExpectedVariables(varsCertain1, varsPossible1, varsCertain2, varsPossible2);

        // Create SolutionMappingsHashTable for input1
        final SolutionMappingsHashTable solMHashTable1 = new SolutionMappingsHashTable(inputVars);
        for ( final SolutionMapping smL : input1.getSolutionMappings() ) {
            solMHashTable1.add(smL);
        }

        //------------------
        // Checking SolutionMappingsHashTable
        assertEquals( 2, solMHashTable1.size() );

        final Iterator<SolutionMapping> it = solMHashTable1.iterator();
        assertTrue( it.hasNext() );
        final Binding b1 = it.next().asJenaBinding();
        assertEquals( 2, b1.size() );

        assertTrue( it.hasNext() );
        final Binding b2 = it.next().asJenaBinding();
        assertEquals( 2, b2.size() );

        assertTrue( it.hasNext() );
        final Binding b3 = it.next().asJenaBinding();
        assertEquals( 2, b2.size() );

        assertFalse( it.hasNext() );

        // Find solution mappings with (var2, y2)
        final Iterable<SolutionMapping> solMap= solMHashTable1.findSolutionMappings(var2, y2);
        for(final SolutionMapping sm: solMap){
            final Binding bsm = sm.asJenaBinding();
            if ( bsm.get(var3).getURI().equals("http://example.org/z3") ) {
                assertEquals( "http://example.org/y2", bsm.get(var2).getURI() );
            }
            else {
                fail( "Unexpected URI for ?v3: " + bsm.get(var3).getURI() );
            }
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

        // Checking join partner of the second solution mapping (without join partner)
        assertNull(matchSolMapR.get(1));
    }

    @Test
    public void hashTableWithTwoJoinVariable_noPossibleVars() {
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

        final GenericIntermediateResultBlockImpl input2 = new GenericIntermediateResultBlockImpl();
        input2.add( SolutionMappingUtils.createSolutionMapping(
                var1, x1,
                var2, y1) );
        input2.add( SolutionMappingUtils.createSolutionMapping(
                var1, x1,
                var2, y2) );
        input2.add( SolutionMappingUtils.createSolutionMapping(
                var1, x3,
                var2, y3) );

        final Set<Var> varsCertain1 = new HashSet<>();
        final Set<Var> varsPossible1 = new HashSet<>();
        varsCertain1.add(var1);
        varsCertain1.add(var2);

        final Set<Var> varsCertain2 = new HashSet<>();
        final Set<Var> varsPossible2 = new HashSet<>();
        varsCertain2.add(var1);
        varsCertain2.add(var2);
        varsCertain2.add(var3);

        final ExpectedVariables[] inputVars = getExpectedVariables(varsCertain1, varsPossible1, varsCertain2, varsPossible2);

        // Create SolutionMappingsHashTable for input1
        final SolutionMappingsHashTable solMHashTable1 = new SolutionMappingsHashTable(inputVars);
        for ( final SolutionMapping smL : input1.getSolutionMappings() ) {
            solMHashTable1.add(smL);
        }

        //------------------
        // Checking SolutionMappingsHashTable
        assertEquals( 2, solMHashTable1.size() );

        final Iterator<SolutionMapping> it = solMHashTable1.iterator();

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
        final Iterable<SolutionMapping> solMap2= solMHashTable1.findSolutionMappings(var1, x2, var2, y2);
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
        // getJoinPartners of Input2
        final List<Iterable<SolutionMapping>> matchSolMapR = new ArrayList<>();
        for ( final SolutionMapping smR : input2.getSolutionMappings() ) {
            matchSolMapR.add(solMHashTable1.getJoinPartners(smR));
        }

        assertEquals( 3, matchSolMapR.size() );

        // Checking join partner of the first solution mapping (with two join partners)
        final Set<Binding> result = new HashSet<>();
        final Iterator<SolutionMapping> it1 = matchSolMapR.get(0).iterator();
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

        // Checking join partner of the second and third solution mapping (without join partner)
        assertNull(matchSolMapR.get(1));
        assertNull(matchSolMapR.get(2));
    }

    @Test
    public void hashTableWithOneJoinVariable_withPossibleVars_noOverlap() {
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

        final ExpectedVariables[] inputVars = getExpectedVariables(varsCertain1, varsPossible1, varsCertain2, varsPossible2);

        // Create SolutionMappingsHashTable for input1
        final SolutionMappingsHashTable solMHashTable1 = new SolutionMappingsHashTable(inputVars);
        for ( final SolutionMapping smL : input1.getSolutionMappings() ) {
            solMHashTable1.add(smL);
        }

        //------------------
        // Checking SolutionMappingsHashTable
        assertEquals( 2, solMHashTable1.size() );

        final Iterator<SolutionMapping> it = solMHashTable1.iterator();
        assertTrue( it.hasNext() );
        final Binding b1 = it.next().asJenaBinding();
        assertEquals( 3, b1.size() );

        assertTrue( it.hasNext() );
        final Binding b2 = it.next().asJenaBinding();
        assertEquals( 2, b2.size() );

        assertTrue( it.hasNext() );
        final Binding b3 = it.next().asJenaBinding();
        assertEquals( 2, b2.size() );

        assertFalse( it.hasNext() );

        // Checking solution mappings with (var2, y2)
        final Iterable<SolutionMapping> solMapy2= solMHashTable1.findSolutionMappings(var2, y2);
        for(final SolutionMapping sm: solMapy2){
            final Binding bsm = sm.asJenaBinding();
            if ( bsm.get(var3).getURI().equals("http://example.org/z3") ) {
                assertEquals( "http://example.org/y2", bsm.get(var2).getURI() );
            }
            else {
                fail( "Unexpected URI for ?v3: " + bsm.get(var3).getURI() );
            }
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

        // Checking join partner of the second solution mapping (without join partner)
        assertNull(matchSolMapR.get(1));
    }

    @Test
    public void hashTableWithOneJoinVariable_withPossibleVars_overlapped() {
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

        final ExpectedVariables[] inputVars = getExpectedVariables(varsCertain1, varsPossible1, varsCertain2, varsPossible2);

        // Create SolutionMappingsHashTable for input1
        final SolutionMappingsHashTable solMHashTable1 = new SolutionMappingsHashTable(inputVars);
        for ( final SolutionMapping smL : input1.getSolutionMappings() ) {
            solMHashTable1.add(smL);
        }

        //------------------
        // Checking SolutionMappingsHashTable: the same as the first unit test (hashTableWithOneJoinVariable)
        assertEquals( 2, solMHashTable1.size() );

        final Iterator<SolutionMapping> it = solMHashTable1.iterator();
        assertTrue( it.hasNext() );
        final Binding b1 = it.next().asJenaBinding();
        assertEquals( 3, b1.size() );

        assertTrue( it.hasNext() );
        final Binding b2 = it.next().asJenaBinding();
        assertEquals( 2, b2.size() );

        assertTrue( it.hasNext() );
        final Binding b3 = it.next().asJenaBinding();
        assertEquals( 2, b2.size() );

        assertFalse( it.hasNext() );

        // Checking solution mappings with (var2, y2)
        final Iterable<SolutionMapping> solMapy2= solMHashTable1.findSolutionMappings(var2, y2);
        for(final SolutionMapping sm: solMapy2){
            final Binding bsm = sm.asJenaBinding();
            if ( bsm.get(var3).getURI().equals("http://example.org/z3") ) {
                assertEquals( "http://example.org/y2", bsm.get(var2).getURI() );
            }
            else {
                fail( "Unexpected URI for ?v3: " + bsm.get(var3).getURI() );
            }
        }

        //----------------------------
        // getJoinPartners of Input2
        final List<SolutionMapping> matchResult = new ArrayList<>();
        for ( final SolutionMapping smR : input2.getSolutionMappings() ) {
            final Iterable<SolutionMapping> matchSolMapR = solMHashTable1.getJoinPartners(smR);
            if (matchSolMapR != null) {
                for (final SolutionMapping smL : matchSolMapR) {
                    if (SolutionMappingUtils.compatible(smL, smR)) {
                        matchResult.add(smL);
                    }
                }
            }
        }

        assertEquals( 1, matchResult.size() );

        for ( final SolutionMapping sm: matchResult ) {
            Binding b = sm.asJenaBinding();
            assertEquals( 3, b.size() );
            if ( b.get(var2).getURI().equals("http://example.org/y1") ) {
                assertEquals( "http://example.org/z1", b.get(var3).getURI() );
                assertEquals( "http://example.org/p1", b.get(var4).getURI() );
            }
            else {
                fail( "Unexpected URI for ?v2: " + b.get(var2).getURI() );
            }
        }
    }

    @Test
    public void joinWithoutJoinVariable_noPossibleVars() {
        final Var var1 = Var.alloc("v1");
        final Var var2 = Var.alloc("v2");
        final Var var3 = Var.alloc("v3");

        final Node p = NodeFactory.createURI("http://example.org/p");
        final Node x1 = NodeFactory.createURI("http://example.org/x1");
        final Node x2 = NodeFactory.createURI("http://example.org/x2");
        final Node y1 = NodeFactory.createURI("http://example.org/y1");
        final Node y2 = NodeFactory.createURI("http://example.org/y2");
        final Node z1 = NodeFactory.createURI("http://example.org/z1");
        final Node z2 = NodeFactory.createURI("http://example.org/z2");

        final GenericIntermediateResultBlockImpl input1 = new GenericIntermediateResultBlockImpl();
        input1.add( SolutionMappingUtils.createSolutionMapping(var1, x1) );
        input1.add( SolutionMappingUtils.createSolutionMapping(var1, x2) );

        final GenericIntermediateResultBlockImpl input2 = new GenericIntermediateResultBlockImpl();
        input2.add( SolutionMappingUtils.createSolutionMapping(
                var2, y1,
                var3, z1) );
        input2.add( SolutionMappingUtils.createSolutionMapping(
                var2, y2,
                var3, z2) );

        final Set<Var> varsCertain1 = new HashSet<>();
        final Set<Var> varsPossible1 = new HashSet<>();
        varsCertain1.add(var1);

        final Set<Var> varsCertain2 = new HashSet<>();
        final Set<Var> varsPossible2 = new HashSet<>();
        varsCertain2.add(var2);
        varsCertain2.add(var3);

        final ExpectedVariables[] inputVars = getExpectedVariables(varsCertain1, varsPossible1, varsCertain2, varsPossible2);

        // Create SolutionMappingsHashTable for input1
        final SolutionMappingsHashTable solMHashTable1 = new SolutionMappingsHashTable(inputVars);
        for ( final SolutionMapping smL : input1.getSolutionMappings() ) {
            solMHashTable1.add(smL);
        }

        assertEquals( 1, solMHashTable1.size() );
        final Iterator<SolutionMapping> it = solMHashTable1.iterator();

        assertTrue( it.hasNext() );
        final Binding b1 = it.next().asJenaBinding();
        assertEquals( 1, b1.size() );

        assertTrue( it.hasNext() );
        final Binding b2 = it.next().asJenaBinding();
        assertEquals( 1, b2.size() );

        assertFalse( it.hasNext() );

        //----------------------------
        // getJoinPartners of Input2
        final List<SolutionMapping> matchResult = new ArrayList<>();
        for ( final SolutionMapping smR : input2.getSolutionMappings() ) {
            final Iterable<SolutionMapping> matchSolMapR = solMHashTable1.getJoinPartners(smR);
            if (matchSolMapR != null) {
                for (final SolutionMapping smL : matchSolMapR) {
                    if (SolutionMappingUtils.compatible(smL, smR)) {
                        matchResult.add(smL);
                    }
                }
            }
        }

        assertEquals( 4, matchResult.size() );

    }

    @Test
    public void joinWithoutJoinVariable_withPossibleVars_noOverlap() {
        final Var var1 = Var.alloc("v1");
        final Var var2 = Var.alloc("v2");
        final Var var3 = Var.alloc("v3");
        final Var var4 = Var.alloc("v4");

        final Node p1 = NodeFactory.createURI("http://example.org/p1");
        final Node x1 = NodeFactory.createURI("http://example.org/x1");
        final Node x2 = NodeFactory.createURI("http://example.org/x2");
        final Node y1 = NodeFactory.createURI("http://example.org/y1");
        final Node y2 = NodeFactory.createURI("http://example.org/y2");
        final Node z1 = NodeFactory.createURI("http://example.org/z1");
        final Node z2 = NodeFactory.createURI("http://example.org/z2");

        final GenericIntermediateResultBlockImpl input1 = new GenericIntermediateResultBlockImpl();
        input1.add( SolutionMappingUtils.createSolutionMapping(var1, x1, var4, p1) );
        input1.add( SolutionMappingUtils.createSolutionMapping(var1, x2) );

        final GenericIntermediateResultBlockImpl input2 = new GenericIntermediateResultBlockImpl();
        input2.add( SolutionMappingUtils.createSolutionMapping(
                var2, y1,
                var3, z1) );
        input2.add( SolutionMappingUtils.createSolutionMapping(
                var2, y2,
                var3, z2) );

        final Set<Var> varsCertain1 = new HashSet<>();
        varsCertain1.add(var1);
        final Set<Var> varsPossible1 = new HashSet<>();
        varsPossible1.add(var4);

        final Set<Var> varsCertain2 = new HashSet<>();
        varsCertain2.add(var2);
        varsCertain2.add(var3);
        final Set<Var> varsPossible2 = new HashSet<>();

        final ExpectedVariables[] inputVars = getExpectedVariables(varsCertain1, varsPossible1, varsCertain2, varsPossible2);

        // Create SolutionMappingsHashTable for input1
        final SolutionMappingsHashTable solMHashTable1 = new SolutionMappingsHashTable(inputVars);
        for ( final SolutionMapping smL : input1.getSolutionMappings() ) {
            solMHashTable1.add(smL);
        }

        assertEquals( 1, solMHashTable1.size() );
        final Iterator<SolutionMapping> it = solMHashTable1.iterator();

        assertTrue( it.hasNext() );
        final Binding b1 = it.next().asJenaBinding();
        assertEquals( 2, b1.size() );

        assertTrue( it.hasNext() );
        final Binding b2 = it.next().asJenaBinding();
        assertEquals( 1, b2.size() );

        assertFalse( it.hasNext() );

        //----------------------------
        // getJoinPartners of Input2
        final List<SolutionMapping> matchResult = new ArrayList<>();
        for ( final SolutionMapping smR : input2.getSolutionMappings() ) {
            final Iterable<SolutionMapping> matchSolMapR = solMHashTable1.getJoinPartners(smR);
            if (matchSolMapR != null){
                for (final SolutionMapping smL : matchSolMapR){
                    if (SolutionMappingUtils.compatible(smL, smR)){
                        matchResult.add(smL);
                    }
                }
            }
        }

        assertEquals( 4, matchResult.size() );

    }

    @Test
    public void joinWithoutJoinVariable_withPossibleVars_overlapped() {
        final Var var1 = Var.alloc("v1");
        final Var var2 = Var.alloc("v2");
        final Var var3 = Var.alloc("v3");

        final Node p = NodeFactory.createURI("http://example.org/p");
        final Node x1 = NodeFactory.createURI("http://example.org/x1");
        final Node x2 = NodeFactory.createURI("http://example.org/x2");
        final Node y1 = NodeFactory.createURI("http://example.org/y1");
        final Node y2 = NodeFactory.createURI("http://example.org/y2");
        final Node z1 = NodeFactory.createURI("http://example.org/z1");
        final Node z2 = NodeFactory.createURI("http://example.org/z2");

        final GenericIntermediateResultBlockImpl input1 = new GenericIntermediateResultBlockImpl();
        input1.add( SolutionMappingUtils.createSolutionMapping(var1, x1, var2, y1) );
        input1.add( SolutionMappingUtils.createSolutionMapping(var1, x2) );

        final GenericIntermediateResultBlockImpl input2 = new GenericIntermediateResultBlockImpl();
        input2.add( SolutionMappingUtils.createSolutionMapping(
                var2, y1,
                var3, z1) );
        input2.add( SolutionMappingUtils.createSolutionMapping(
                var2, y2,
                var3, z2) );

        final Set<Var> varsCertain1 = new HashSet<>();
        varsCertain1.add(var1);
        final Set<Var> varsPossible1 = new HashSet<>();
        varsPossible1.add(var2);

        final Set<Var> varsCertain2 = new HashSet<>();
        varsCertain2.add(var2);
        varsCertain2.add(var3);
        final Set<Var> varsPossible2 = new HashSet<>();

        final ExpectedVariables[] inputVars = getExpectedVariables(varsCertain1, varsPossible1, varsCertain2, varsPossible2);

        // Create SolutionMappingsHashTable for input1
        final SolutionMappingsHashTable solMHashTable1 = new SolutionMappingsHashTable(inputVars);
        for ( final SolutionMapping smL : input1.getSolutionMappings() ) {
            solMHashTable1.add(smL);
        }

        assertEquals( 1, solMHashTable1.size() );
        final Iterator<SolutionMapping> it = solMHashTable1.iterator();

        assertTrue( it.hasNext() );
        final Binding b1 = it.next().asJenaBinding();
        assertEquals( 2, b1.size() );

        assertTrue( it.hasNext() );
        final Binding b2 = it.next().asJenaBinding();
        assertEquals( 1, b2.size() );

        assertFalse( it.hasNext() );

        //----------------------------
        // getJoinPartners of Input2
        final List<SolutionMapping> matchResult = new ArrayList<>();
        for ( final SolutionMapping smR : input2.getSolutionMappings() ) {
            final Iterable<SolutionMapping> matchSolMapR = solMHashTable1.getJoinPartners(smR);
            if (matchSolMapR != null){
                for (final SolutionMapping smL : matchSolMapR){
                    if (SolutionMappingUtils.compatible(smL, smR)){
                        matchResult.add(smL);
                    }
                }
            }
        }

        assertEquals( 3, matchResult.size() );

    }


    protected ExpectedVariables[] getExpectedVariables(
            final Set<Var> varsCertain1,
            final Set<Var> varsPossible1,
            final Set<Var> varsCertain2,
            final Set<Var> varsPossible2)
    {
        final ExpectedVariables[] inputVars = new ExpectedVariables[2];
        inputVars[0] = new ExpectedVariables() {
            public Set<Var> getCertainVariables() { return varsCertain1;}
            public Set<Var> getPossibleVariables() { return varsPossible1;}
        };
        inputVars[1] = new ExpectedVariables() {
            public Set<Var> getCertainVariables() { return varsCertain2;}
            public Set<Var> getPossibleVariables() { return varsPossible2;}
        };
        return inputVars;
    }
}
