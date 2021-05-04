package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.impl.SolutionMappingUtils;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.GenericIntermediateResultBlockImpl;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.MaterializingIntermediateResultElementSink;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * This is an abstract class with tests for any algorithm that is
 * meant to be used as an implementation for the join operator.
 */
public abstract class TestsForJoinAlgorithms extends ExecOpTestBase
{
	protected void _joinWithOneJoinVariable() {
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

		final GenericIntermediateResultBlockImpl input1 = new GenericIntermediateResultBlockImpl();
		input1.add( SolutionMappingUtils.createSolutionMapping(
				var1, x1,
				var2, y1) );
		input1.add( SolutionMappingUtils.createSolutionMapping(
				var1, x2,
				var2, y2) );

		final GenericIntermediateResultBlockImpl input2 = new GenericIntermediateResultBlockImpl();
		input2.add( SolutionMappingUtils.createSolutionMapping(
				var2, y1,
				var3, z1) );
		input2.add( SolutionMappingUtils.createSolutionMapping(
				var2, y1,
				var3, z2) );
		input2.add( SolutionMappingUtils.createSolutionMapping(
				var2, y2,
				var3, z3) );

		final Iterator<SolutionMapping> it = runTest(input1, input2);

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

	protected void _joinWithTwoJoinVariables() {
		final Var var1 = Var.alloc("v1");
		final Var var2 = Var.alloc("v2");
		final Var var3 = Var.alloc("v3");

		final Node x1 = NodeFactory.createURI("http://example.org/x1");
		final Node x2 = NodeFactory.createURI("http://example.org/x2");
		final Node y1 = NodeFactory.createURI("http://example.org/y1");
		final Node y2 = NodeFactory.createURI("http://example.org/y2");
		final Node z1 = NodeFactory.createURI("http://example.org/z1");
		final Node z2 = NodeFactory.createURI("http://example.org/z2");

		final GenericIntermediateResultBlockImpl input1 = new GenericIntermediateResultBlockImpl();
		input1.add( SolutionMappingUtils.createSolutionMapping(
				var1, x1,
				var2, y1) );
		input1.add( SolutionMappingUtils.createSolutionMapping(
				var1, x2,
				var2, y2) );

		final GenericIntermediateResultBlockImpl input2 = new GenericIntermediateResultBlockImpl();
		input2.add( SolutionMappingUtils.createSolutionMapping(
				var1, x1,
				var2, y1,
				var3, z1) );
		input2.add( SolutionMappingUtils.createSolutionMapping(
				var1, x2,
				var2, y2,
				var3, z2) );

		final Iterator<SolutionMapping> it = runTest(input1, input2);

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

	protected void _joinWithoutJoinVariable() {
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

		final Iterator<SolutionMapping> it = runTest(input1, input2);

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

	protected void _joinWithEmptyInput1() {
		final Var var2 = Var.alloc("v2");
		final Var var3 = Var.alloc("v3");

		final Node p = NodeFactory.createURI("http://example.org/p");
		final Node y1 = NodeFactory.createURI("http://example.org/y1");
		final Node z1 = NodeFactory.createURI("http://example.org/z1");

		final GenericIntermediateResultBlockImpl input1 = new GenericIntermediateResultBlockImpl();

		final GenericIntermediateResultBlockImpl input2 = new GenericIntermediateResultBlockImpl();
		input2.add( SolutionMappingUtils.createSolutionMapping(
				var2, y1,
				var3, z1) );

		final Iterator<SolutionMapping> it = runTest(input1, input2);

		assertFalse( it.hasNext() );
	}

	protected void _joinWithEmptyInput2() {
		final Var var1 = Var.alloc("v1");
		final Var var2 = Var.alloc("v2");

		final GenericIntermediateResultBlockImpl input1 = new GenericIntermediateResultBlockImpl();
		input1.add( SolutionMappingUtils.createSolutionMapping(
				var1, NodeFactory.createURI("http://example.org/x1")) );
		input1.add( SolutionMappingUtils.createSolutionMapping(
				var1, NodeFactory.createURI("http://example.org/x2")) );

		final GenericIntermediateResultBlockImpl input2 = new GenericIntermediateResultBlockImpl();

		final Iterator<SolutionMapping> it = runTest(input1, input2);

		assertFalse( it.hasNext() );
	}

	protected void _joinWithEmptySolutionMapping1() {
		final Var var1 = Var.alloc("v1");
		final Var var2 = Var.alloc("v2");

		final Node x1 = NodeFactory.createURI("http://example.org/x1");
		final Node x2 = NodeFactory.createURI("http://example.org/x2");
		final Node y1 = NodeFactory.createURI("http://example.org/y1");
		final Node y2 = NodeFactory.createURI("http://example.org/y2");

		final GenericIntermediateResultBlockImpl input1 = new GenericIntermediateResultBlockImpl();
		input1.add(SolutionMappingUtils.createSolutionMapping());

		final GenericIntermediateResultBlockImpl input2 = new GenericIntermediateResultBlockImpl();
		input2.add( SolutionMappingUtils.createSolutionMapping(
				var1, x1,
				var2, y1) );
		input2.add( SolutionMappingUtils.createSolutionMapping(
				var1, x2,
				var2, y2) );

		final Iterator<SolutionMapping> it = runTest(input1, input2);

		assertTrue( it.hasNext() );
		final Binding b1 = it.next().asJenaBinding();
		assertEquals( 2, b1.size() );

		assertTrue( it.hasNext() );
		final Binding b2 = it.next().asJenaBinding();
		assertEquals( 2, b2.size() );

		assertFalse( it.hasNext() );
	}

	protected void _joinWithEmptySolutionMapping2() {
		final Var var1 = Var.alloc("v1");

		final GenericIntermediateResultBlockImpl input1 = new GenericIntermediateResultBlockImpl();
		input1.add( SolutionMappingUtils.createSolutionMapping(
				var1, NodeFactory.createURI("http://example.org/x1")) );
		input1.add( SolutionMappingUtils.createSolutionMapping(
				var1, NodeFactory.createURI("http://example.org/x2")) );

		final GenericIntermediateResultBlockImpl input2 = new GenericIntermediateResultBlockImpl();
		input2.add(SolutionMappingUtils.createSolutionMapping());

		final Iterator<SolutionMapping> it = runTest(input1, input2);

		//assertFalse( it.hasNext() );
		assertTrue( it.hasNext() );
		final Binding b1 = it.next().asJenaBinding();
		assertEquals( 1, b1.size() );

		assertTrue( it.hasNext() );
		final Binding b2 = it.next().asJenaBinding();
		assertEquals( 1, b2.size() );

		assertFalse( it.hasNext() );
	}

	protected Iterator<SolutionMapping> runTest(
			final IntermediateResultBlock input1,
			final IntermediateResultBlock input2)
	{
		final MaterializingIntermediateResultElementSink sink = new MaterializingIntermediateResultElementSink();

		final BinaryExecutableOp op = createExecOpForTest();

		op.processBlockFromChild1(input1, sink, null);
		op.processBlockFromChild2(input2, sink, null);

		return sink.getMaterializedIntermediateResult().iterator();
	}

	protected abstract BinaryExecutableOp createExecOpForTest();
}
