package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryproc.ExecutionException;

public abstract class TestsForRightJoinAlgorithms extends TestsForJoinAlgorithms
{
	protected void _joinWithEmptyInput1( final boolean sendAllSolMapsSeparately )
			throws ExecutionException
	{
		final Var var2 = Var.alloc("v2");
		final Var var3 = Var.alloc("v3");

		final Node y1 = NodeFactory.createURI("http://example.org/y1");
		final Node z1 = NodeFactory.createURI("http://example.org/z1");

		final List<SolutionMapping> input1 = new ArrayList<>();

		final List<SolutionMapping> input2 = new ArrayList<>();
		input2.add( SolutionMappingUtils.createSolutionMapping(
				var2, y1,
				var3, z1) );

		Set<Var> varsCertain1 = new HashSet<>();
		varsCertain1.add(var2);
		Set<Var> varsPossible1 = new HashSet<>();

		Set<Var> varsCertain2 = new HashSet<>();
		varsCertain2.add(var2);
		varsCertain2.add(var3);
		Set<Var> varsPossible2 = new HashSet<>();

		ExpectedVariables[] inputVars = getExpectedVariables(varsCertain1, varsPossible1, varsCertain2, varsPossible2);

		final Iterator<SolutionMapping> it = runTest(input1, input2, sendAllSolMapsSeparately, inputVars);

		assertTrue( it.hasNext() );
		final Binding b = it.next().asJenaBinding();
		assertEquals( 2, b.size() );
		assertTrue( b.contains(var2) );
		assertTrue( b.contains(var3) );
		assertEquals( "http://example.org/y1", b.get(var2).getURI() );
		assertEquals( "http://example.org/z1", b.get(var3).getURI() );

		assertFalse( it.hasNext() );
	}

	protected void _joinWithEmptyInput2( final boolean sendAllSolMapsSeparately )
			throws ExecutionException
	{
		final Var var1 = Var.alloc("v1");

		final List<SolutionMapping> input1 = new ArrayList<>();
		input1.add( SolutionMappingUtils.createSolutionMapping(
				var1, NodeFactory.createURI("http://example.org/x1")) );
		input1.add( SolutionMappingUtils.createSolutionMapping(
				var1, NodeFactory.createURI("http://example.org/x2")) );

		final List<SolutionMapping> input2 = new ArrayList<>();

		Set<Var> varsCertain1 = new HashSet<>();
		varsCertain1.add(var1);
		Set<Var> varsPossible1 = new HashSet<>();

		Set<Var> varsCertain2 = new HashSet<>();
		varsCertain2.add(var1);
		Set<Var> varsPossible2 = new HashSet<>();

		ExpectedVariables[] inputVars = getExpectedVariables(varsCertain1, varsPossible1, varsCertain2, varsPossible2);

		final Iterator<SolutionMapping> it = runTest(input1, input2, sendAllSolMapsSeparately, inputVars);

		assertFalse( it.hasNext() );
	}

	protected void _joinWithEmptySolutionMapping1( final boolean sendAllSolMapsSeparately )
			throws ExecutionException
	{
		final Var var1 = Var.alloc("v1");
		final Var var2 = Var.alloc("v2");

		final Node x1 = NodeFactory.createURI("http://example.org/x1");
		final Node x2 = NodeFactory.createURI("http://example.org/x2");
		final Node y1 = NodeFactory.createURI("http://example.org/y1");
		final Node y2 = NodeFactory.createURI("http://example.org/y2");

		final List<SolutionMapping> input1 = new ArrayList<>();
		input1.add( SolutionMappingUtils.createSolutionMapping() );

		final List<SolutionMapping> input2 = new ArrayList<>();
		input2.add( SolutionMappingUtils.createSolutionMapping(
				var1, x1,
				var2, y1) );
		input2.add( SolutionMappingUtils.createSolutionMapping(
				var1, x2,
				var2, y2) );

		Set<Var> varsCertain1 = new HashSet<>();
		Set<Var> varsPossible1 = new HashSet<>();

		Set<Var> varsCertain2 = new HashSet<>();
		varsCertain2.add(var1);
		varsCertain2.add(var2);
		Set<Var> varsPossible2 = new HashSet<>();

		ExpectedVariables[] inputVars = getExpectedVariables(varsCertain1, varsPossible1, varsCertain2, varsPossible2);

		final Iterator<SolutionMapping> it = runTest(input1, input2, sendAllSolMapsSeparately, inputVars);

		assertTrue( it.hasNext() );
		final Binding b1 = it.next().asJenaBinding();
		assertEquals( 2, b1.size() );

		assertTrue( it.hasNext() );
		final Binding b2 = it.next().asJenaBinding();
		assertEquals( 2, b2.size() );

		assertFalse( it.hasNext() );
	}

	protected void _joinWithEmptySolutionMapping2( final boolean sendAllSolMapsSeparately )
			throws ExecutionException
	{
		final Var var1 = Var.alloc("v1");

		final List<SolutionMapping> input1 = new ArrayList<>();
		input1.add( SolutionMappingUtils.createSolutionMapping(
				var1, NodeFactory.createURI("http://example.org/x1")) );
		input1.add( SolutionMappingUtils.createSolutionMapping(
				var1, NodeFactory.createURI("http://example.org/x2")) );

		final List<SolutionMapping> input2 = new ArrayList<>();
		input2.add( SolutionMappingUtils.createSolutionMapping() );

		Set<Var> varsCertain1 = new HashSet<>();
		Set<Var> varsPossible1 = new HashSet<>();

		Set<Var> varsCertain2 = new HashSet<>();
		varsCertain2.add(var1);
		Set<Var> varsPossible2 = new HashSet<>();

		ExpectedVariables[] inputVars = getExpectedVariables(varsCertain1, varsPossible1, varsCertain2, varsPossible2);

		final Iterator<SolutionMapping> it = runTest(input1, input2, sendAllSolMapsSeparately, inputVars);

		assertTrue( it.hasNext() );
		final Binding b1 = it.next().asJenaBinding();
		assertEquals( 1, b1.size() );

		assertTrue( it.hasNext() );
		final Binding b2 = it.next().asJenaBinding();
		assertEquals( 1, b2.size() );

		assertFalse( it.hasNext() );
	}

	protected void _joinWithOneJoinVariable( final boolean sendAllSolMapsSeparately )
			throws ExecutionException
	{
		final Var var1 = Var.alloc("v1");
		final Var var2 = Var.alloc("v2");
		final Var var3 = Var.alloc("v3");

		final Node x1 = NodeFactory.createURI("http://example.org/x1");
		final Node x2 = NodeFactory.createURI("http://example.org/x2");
		final Node y1 = NodeFactory.createURI("http://example.org/y1");
		final Node y2 = NodeFactory.createURI("http://example.org/y2");
		final Node z1 = NodeFactory.createURI("http://example.org/z1");
		final Node z2 = NodeFactory.createURI("http://example.org/z2");
		final Node z3 = NodeFactory.createURI("http://example.org/z3");

		final List<SolutionMapping> input1 = new ArrayList<>();
		input1.add( SolutionMappingUtils.createSolutionMapping(
				var1, x1,
				var2, y1) );
		input1.add( SolutionMappingUtils.createSolutionMapping(
				var1, x2,
				var2, y2) );

		final List<SolutionMapping> input2 = new ArrayList<>();
		input2.add( SolutionMappingUtils.createSolutionMapping(
				var2, y1,
				var3, z1) );
		input2.add( SolutionMappingUtils.createSolutionMapping(
				var2, y1,
				var3, z2) );
		input2.add( SolutionMappingUtils.createSolutionMapping(
				var2, y2,
				var3, z3) );

		Set<Var> varsCertain1 = new HashSet<>();
		varsCertain1.add(var1);
		varsCertain1.add(var2);
		Set<Var> varsPossible1 = new HashSet<>();

		Set<Var> varsCertain2 = new HashSet<>();
		varsCertain2.add(var2);
		varsCertain2.add(var3);
		Set<Var> varsPossible2 = new HashSet<>();

		ExpectedVariables[] inputVars = getExpectedVariables(varsCertain1, varsPossible1, varsCertain2, varsPossible2);

		final Iterator<SolutionMapping> it = runTest(input1, input2, sendAllSolMapsSeparately, inputVars);

		// checking
		final Set<Binding> result = new HashSet<>();

		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );

		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );

		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );

		assertFalse( it.hasNext() );

		boolean b1Found = false;
		boolean b2Found = false;
		boolean b3Found = false;
		for ( final Binding b : result ) {
			assertEquals( 3, b.size() );

			if ( b.get(var1).getURI().equals("http://example.org/x1") ) {
				assertEquals( "http://example.org/y1", b.get(var2).getURI() );
				if ( b.get(var3).getURI().equals("http://example.org/z1") ) {
					b1Found = true;
				}
				else if ( b.get(var3).getURI().equals("http://example.org/z2") ) {
					b2Found = true;
				}
				else {
					fail( "Unexpected URI for ?v3: " + b.get(var3).getURI() );
				}
			}
			else if ( b.get(var1).getURI().equals("http://example.org/x2") ) {
				assertEquals( "http://example.org/y2", b.get(var2).getURI() );
				assertEquals( "http://example.org/z3", b.get(var3).getURI() );
				b3Found = true;
			}
			else {
				fail( "Unexpected URI for ?v1: " + b.get(var1).getURI() );
			}
		}

		assertTrue(b1Found);
		assertTrue(b2Found);
		assertTrue(b3Found);
	}

	protected void _joinWithOneJoinVariable_withPossibleVars_noOverlap( final boolean sendAllSolMapsSeparately )
			throws ExecutionException
	{
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

		final List<SolutionMapping> input1 = new ArrayList<>();
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

		final List<SolutionMapping> input2 = new ArrayList<>();
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

		ExpectedVariables[] inputVars = getExpectedVariables(varsCertain1, varsPossible1, varsCertain2, varsPossible2);

		final Iterator<SolutionMapping> it = runTest(input1, input2, sendAllSolMapsSeparately, inputVars);

		final Set<Binding> result = new HashSet<>();
		assertTrue( it.hasNext() );
		final Binding b1 = it.next().asJenaBinding();
		assertEquals( 5, b1.size() );
		result.add( b1 );

		assertTrue( it.hasNext() );
		final Binding b2 = it.next().asJenaBinding();
		assertEquals( 4, b2.size() );
		result.add( b2 );

		assertTrue( it.hasNext() );
		final Binding b3 = it.next().asJenaBinding();
		assertEquals( 2, b3.size() );
		result.add( b3 );

		assertFalse( it.hasNext() );

		boolean b1Found = false;
		boolean b2Found = false;
		boolean b3Found = false;
		for ( final Binding b: result ) {
			if ( ! b.contains(var3) ) {
				assertEquals( "http://example.org/x2", b.get(var1).getURI() );
				assertEquals( "http://example.org/y3", b.get(var2).getURI() );
				b3Found = true;
			}
			else if ( b.get(var3).getURI().equals("http://example.org/z1") ) {
				assertEquals( "http://example.org/x1", b.get(var1).getURI() );
				assertEquals( "http://example.org/y1", b.get(var2).getURI() );
				assertEquals( "http://example.org/p1", b.get(var4).getURI() );
				assertEquals( "http://example.org/p2", b.get(var5).getURI() );
				b1Found = true;
			}
			else if ( b.get(var3).getURI().equals("http://example.org/z2") ) {
				assertEquals( "http://example.org/x1", b.get(var1).getURI() );
				assertEquals( "http://example.org/y1", b.get(var2).getURI() );
				assertEquals( "http://example.org/p1", b.get(var4).getURI() );
				b2Found = true;
			}
			else {
				fail( "Unexpected URI for ?v3: " + b.get(var3).getURI() );
			}
		}
		assertTrue(b1Found);
		assertTrue(b2Found);
		assertTrue(b3Found);
	}

	protected void _joinWithOneJoinVariable_withPossibleVars_overlapped( final boolean sendAllSolMapsSeparately )
			throws ExecutionException
	{
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

		final List<SolutionMapping> input1 = new ArrayList<>();
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

		final List<SolutionMapping> input2 = new ArrayList<>();
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

		ExpectedVariables[] inputVars = getExpectedVariables(varsCertain1, varsPossible1, varsCertain2, varsPossible2);

		final Iterator<SolutionMapping> it = runTest(input1, input2, sendAllSolMapsSeparately, inputVars);

		assertTrue( it.hasNext() );
		final Binding b1 = it.next().asJenaBinding();
		assertEquals( 4, b1.size() );

		assertTrue( it.hasNext() );
		final Binding b2 = it.next().asJenaBinding();
		assertEquals( 2, b2.size() );

		assertFalse( it.hasNext() );
	}

	protected void _joinWithTwoJoinVariables( final boolean sendAllSolMapsSeparately )
			throws ExecutionException
	{
		final Var var1 = Var.alloc("v1");
		final Var var2 = Var.alloc("v2");
		final Var var3 = Var.alloc("v3");

		final Node x1 = NodeFactory.createURI("http://example.org/x1");
		final Node x2 = NodeFactory.createURI("http://example.org/x2");
		final Node y1 = NodeFactory.createURI("http://example.org/y1");
		final Node y2 = NodeFactory.createURI("http://example.org/y2");
		final Node z1 = NodeFactory.createURI("http://example.org/z1");
		final Node z2 = NodeFactory.createURI("http://example.org/z2");

		final List<SolutionMapping> input1 = new ArrayList<>();
		input1.add( SolutionMappingUtils.createSolutionMapping(
				var1, x1,
				var2, y1) );
		input1.add( SolutionMappingUtils.createSolutionMapping(
				var1, x2,
				var2, y2) );

		final List<SolutionMapping> input2 = new ArrayList<>();
		input2.add( SolutionMappingUtils.createSolutionMapping(
				var1, x1,
				var2, y1,
				var3, z1) );
		input2.add( SolutionMappingUtils.createSolutionMapping(
				var1, x2,
				var2, y2,
				var3, z2) );

		Set<Var> varsCertain1 = new HashSet<>();
		varsCertain1.add(var1);
		varsCertain1.add(var2);
		Set<Var> varsPossible1 = new HashSet<>();

		Set<Var> varsCertain2 = new HashSet<>();
		varsCertain2.add(var1);
		varsCertain2.add(var2);
		varsCertain2.add(var3);
		Set<Var> varsPossible2 = new HashSet<>();

		ExpectedVariables[] inputVars = getExpectedVariables(varsCertain1, varsPossible1, varsCertain2, varsPossible2);

		final Iterator<SolutionMapping> it = runTest(input1, input2, sendAllSolMapsSeparately, inputVars);

		// checking
		final Set<Binding> result = new HashSet<>();

		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );

		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );

		assertFalse( it.hasNext() );

		boolean b1Found = false;
		boolean b2Found = false;
		for ( final Binding b : result ) {
			assertEquals( 3, b.size() );

			if ( b.get(var1).getURI().equals("http://example.org/x1") ) {
				assertEquals( "http://example.org/y1", b.get(var2).getURI() );
				assertEquals( "http://example.org/z1", b.get(var3).getURI() );
				b1Found = true;
			}
			else if ( b.get(var1).getURI().equals("http://example.org/x2") ) {
				assertEquals( "http://example.org/y2", b.get(var2).getURI() );
				assertEquals( "http://example.org/z2", b.get(var3).getURI() );
				b2Found = true;
			}
			else {
				fail( "Unexpected URI for ?v1: " + b.get(var1).getURI() );
			}
		}

		assertTrue(b1Found);
		assertTrue(b2Found);
	}

	protected void _joinWithTwoJoinVariables_noJoinPartner( final boolean sendAllSolMapsSeparately )
			throws ExecutionException
	{
		final Var var1 = Var.alloc("v1");
		final Var var2 = Var.alloc("v2");
		final Var var3 = Var.alloc("v3");

		final Node x1 = NodeFactory.createURI("http://example.org/x1");
		final Node x2 = NodeFactory.createURI("http://example.org/x2");
		final Node y1 = NodeFactory.createURI("http://example.org/y1");
		final Node y2 = NodeFactory.createURI("http://example.org/y2");
		final Node z1 = NodeFactory.createURI("http://example.org/z1");
		final Node z2 = NodeFactory.createURI("http://example.org/z2");

		final List<SolutionMapping> input1 = new ArrayList<>();
		input1.add( SolutionMappingUtils.createSolutionMapping(
				var1, x1,
				var2, y1) );
		input1.add( SolutionMappingUtils.createSolutionMapping(
				var1, x2,
				var2, y2) );

		final List<SolutionMapping> input2 = new ArrayList<>();
		input2.add( SolutionMappingUtils.createSolutionMapping(
				var1, x1,
				var2, y2,
				var3, z1) );
		input2.add( SolutionMappingUtils.createSolutionMapping(
				var1, x2,
				var2, y1,
				var3, z2) );

		Set<Var> varsCertain1 = new HashSet<>();
		varsCertain1.add(var1);
		varsCertain1.add(var2);
		Set<Var> varsPossible1 = new HashSet<>();

		Set<Var> varsCertain2 = new HashSet<>();
		varsCertain2.add(var1);
		varsCertain2.add(var2);
		varsCertain2.add(var3);
		Set<Var> varsPossible2 = new HashSet<>();

		ExpectedVariables[] inputVars = getExpectedVariables(varsCertain1, varsPossible1, varsCertain2, varsPossible2);

		final Iterator<SolutionMapping> it = runTest(input1, input2, sendAllSolMapsSeparately, inputVars);

		// checking
		final Set<Binding> result = new HashSet<>();

		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );

		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );

		assertFalse( it.hasNext() );

		boolean b1Found = false;
		boolean b2Found = false;
		for ( final Binding b : result ) {
			assertEquals( 3, b.size() );

			if ( b.get(var1).getURI().equals("http://example.org/x1") ) {
				assertEquals( "http://example.org/y2", b.get(var2).getURI() );
				assertEquals( "http://example.org/z1", b.get(var3).getURI() );
				b1Found = true;
			}
			else if ( b.get(var1).getURI().equals("http://example.org/x2") ) {
				assertEquals( "http://example.org/y1", b.get(var2).getURI() );
				assertEquals( "http://example.org/z2", b.get(var3).getURI() );
				b2Found = true;
			}
			else {
				fail( "Unexpected URI for ?v1: " + b.get(var1).getURI() );
			}
		}

		assertTrue(b1Found);
		assertTrue(b2Found);
	}

	protected void _joinWithoutJoinVariable( final boolean sendAllSolMapsSeparately )
			throws ExecutionException
	{
		final Var var1 = Var.alloc("v1");
		final Var var2 = Var.alloc("v2");
		final Var var3 = Var.alloc("v3");

		final Node x1 = NodeFactory.createURI("http://example.org/x1");
		final Node x2 = NodeFactory.createURI("http://example.org/x2");
		final Node y1 = NodeFactory.createURI("http://example.org/y1");
		final Node y2 = NodeFactory.createURI("http://example.org/y2");
		final Node z1 = NodeFactory.createURI("http://example.org/z1");
		final Node z2 = NodeFactory.createURI("http://example.org/z2");

		final List<SolutionMapping> input1 = new ArrayList<>();
		input1.add( SolutionMappingUtils.createSolutionMapping(var1, x1) );
		input1.add( SolutionMappingUtils.createSolutionMapping(var1, x2) );

		final List<SolutionMapping> input2 = new ArrayList<>();
		input2.add( SolutionMappingUtils.createSolutionMapping(
				var2, y1,
				var3, z1) );
		input2.add( SolutionMappingUtils.createSolutionMapping(
				var2, y2,
				var3, z2) );

		Set<Var> varsCertain1 = new HashSet<>();
		varsCertain1.add(var1);
		Set<Var> varsPossible1 = new HashSet<>();

		Set<Var> varsCertain2 = new HashSet<>();
		varsCertain2.add(var2);
		varsCertain2.add(var3);
		Set<Var> varsPossible2 = new HashSet<>();

		ExpectedVariables[] inputVars = getExpectedVariables(varsCertain1, varsPossible1, varsCertain2, varsPossible2);

		final Iterator<SolutionMapping> it = runTest(input1, input2, sendAllSolMapsSeparately, inputVars);

		assertTrue( it.hasNext() );
		final Binding b1 = it.next().asJenaBinding();
		assertEquals( 3, b1.size() );

		assertTrue( it.hasNext() );
		final Binding b2 = it.next().asJenaBinding();
		assertEquals( 3, b2.size() );

		assertTrue( it.hasNext() );
		final Binding b3 = it.next().asJenaBinding();
		assertEquals( 3, b3.size() );

		assertTrue( it.hasNext() );
		final Binding b4 = it.next().asJenaBinding();
		assertEquals( 3, b4.size() );

		assertFalse( it.hasNext() );
	}

}
