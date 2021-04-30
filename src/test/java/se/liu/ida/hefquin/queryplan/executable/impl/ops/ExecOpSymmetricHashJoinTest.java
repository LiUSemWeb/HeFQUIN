package se.liu.ida.hefquin.queryplan.executable.impl.ops;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.junit.Test;
import se.liu.ida.hefquin.data.SolutionMapping;
import se.liu.ida.hefquin.data.jenaimpl.JenaBasedSolutionMapping;
import se.liu.ida.hefquin.data.jenaimpl.JenaBasedSolutionMappingUtils;
import se.liu.ida.hefquin.queryplan.executable.impl.GenericIntermediateResultBlockImpl;
import se.liu.ida.hefquin.queryplan.executable.impl.MaterializingIntermediateResultElementSink;

import java.util.Iterator;

import static org.junit.Assert.*;

public class ExecOpSymmetricHashJoinTest extends ExecOpTestBase
{
	@Test
	public void JoinWithOneJoinVariable(){
		final Var var1 = Var.alloc("v1");
		final Var var2 = Var.alloc("v2");
		final Var var3 = Var.alloc("v3");

		final GenericIntermediateResultBlockImpl input1 = new GenericIntermediateResultBlockImpl();
		input1.add( JenaBasedSolutionMappingUtils.createJenaBasedSolutionMapping(
				var1, NodeFactory.createURI("http://example.org/x1"),
				var2, NodeFactory.createURI("http://example.org/y1")) );
		input1.add( JenaBasedSolutionMappingUtils.createJenaBasedSolutionMapping(
				var1, NodeFactory.createURI("http://example.org/x2"),
				var2, NodeFactory.createURI("http://example.org/y2")) );

		final GenericIntermediateResultBlockImpl input2 = new GenericIntermediateResultBlockImpl();
		input2.add( JenaBasedSolutionMappingUtils.createJenaBasedSolutionMapping(
				var2, NodeFactory.createURI("http://example.org/y1"),
				var3, NodeFactory.createURI("http://example.org/z1")) );
		input2.add( JenaBasedSolutionMappingUtils.createJenaBasedSolutionMapping(
				var2, NodeFactory.createURI("http://example.org/y1"),
				var3, NodeFactory.createURI("http://example.org/z2")) );
		input2.add( JenaBasedSolutionMappingUtils.createJenaBasedSolutionMapping(
				var2, NodeFactory.createURI("http://example.org/y2"),
				var3, NodeFactory.createURI("http://example.org/z3")) );

		final MaterializingIntermediateResultElementSink sink = new MaterializingIntermediateResultElementSink();

		final ExecOpSymmetricHashJoin op = new ExecOpSymmetricHashJoin();
		op.preprocess(input1, input2, sink);
		op.processBlockFromChild1(input1, sink, null);
		op.processBlockFromChild2(input2, sink, null);

		final Iterator<SolutionMapping> it = sink.getMaterializedIntermediateResult().iterator();

		assertTrue( it.hasNext() );
		final Binding b1 = ( (JenaBasedSolutionMapping) it.next() ).asJenaBinding();
		assertEquals( 3, b1.size() );
		assertEquals( "http://example.org/x1", b1.get(var1).getURI() );
		assertEquals( "http://example.org/y1", b1.get(var2).getURI() );
		assertEquals( "http://example.org/z1", b1.get(var3).getURI() );

		assertTrue( it.hasNext() );
		final Binding b2 = ( (JenaBasedSolutionMapping) it.next() ).asJenaBinding();
		assertEquals( 3, b2.size() );
		assertEquals( "http://example.org/x1", b2.get(var1).getURI() );
		assertEquals( "http://example.org/y1", b2.get(var2).getURI() );
		assertEquals( "http://example.org/z2", b2.get(var3).getURI() );

		assertTrue( it.hasNext() );
		final Binding b3 = ( (JenaBasedSolutionMapping) it.next() ).asJenaBinding();
		assertEquals( 3, b3.size() );
		assertEquals( "http://example.org/x2", b3.get(var1).getURI() );
		assertEquals( "http://example.org/y2", b3.get(var2).getURI() );
		assertEquals( "http://example.org/z3", b3.get(var3).getURI() );

		assertFalse( it.hasNext() );
	}

	@Test
	public void JoinWithTwoJoinVariables() {
		final Var var1 = Var.alloc("v1");
		final Var var2 = Var.alloc("v2");
		final Var var3 = Var.alloc("v3");

		final GenericIntermediateResultBlockImpl input1 = new GenericIntermediateResultBlockImpl();
		input1.add( JenaBasedSolutionMappingUtils.createJenaBasedSolutionMapping(
				var1, NodeFactory.createURI("http://example.org/x1"),
				var2, NodeFactory.createURI("http://example.org/y1")) );
		input1.add( JenaBasedSolutionMappingUtils.createJenaBasedSolutionMapping(
				var1, NodeFactory.createURI("http://example.org/x2"),
				var2, NodeFactory.createURI("http://example.org/y2")) );

		final GenericIntermediateResultBlockImpl input2 = new GenericIntermediateResultBlockImpl();
		input2.add( JenaBasedSolutionMappingUtils.createJenaBasedSolutionMapping(
				var1, NodeFactory.createURI("http://example.org/x1"),
				var2, NodeFactory.createURI("http://example.org/y1"),
				var3, NodeFactory.createURI("http://example.org/z1")) );
		input2.add( JenaBasedSolutionMappingUtils.createJenaBasedSolutionMapping(
				var1, NodeFactory.createURI("http://example.org/x2"),
				var2, NodeFactory.createURI("http://example.org/y2"),
				var3, NodeFactory.createURI("http://example.org/z2")) );

		final MaterializingIntermediateResultElementSink sink = new MaterializingIntermediateResultElementSink();

		final ExecOpSymmetricHashJoin op = new ExecOpSymmetricHashJoin();
		op.preprocess(input1, input2, sink);
		op.processBlockFromChild1(input1, sink, null);
		op.processBlockFromChild2(input2, sink, null);

		final Iterator<SolutionMapping> it = sink.getMaterializedIntermediateResult().iterator();

		assertTrue( it.hasNext() );
		final Binding b1 = ( (JenaBasedSolutionMapping) it.next() ).asJenaBinding();
		assertEquals( 3, b1.size() );
		assertEquals( "http://example.org/x1", b1.get(var1).getURI() );
		assertEquals( "http://example.org/y1", b1.get(var2).getURI() );
		assertEquals( "http://example.org/z1", b1.get(var3).getURI() );

		assertTrue( it.hasNext() );
		final Binding b2 = ( (JenaBasedSolutionMapping) it.next() ).asJenaBinding();
		assertEquals( 3, b2.size() );
		assertEquals( "http://example.org/x2", b2.get(var1).getURI() );
		assertEquals( "http://example.org/y2", b2.get(var2).getURI() );
		assertEquals( "http://example.org/z2", b2.get(var3).getURI() );

		assertFalse( it.hasNext() );
	}

	@Test
	public void JoinWithoutJoinVariable() {
		final Var var1 = Var.alloc("v1");
		final Var var2 = Var.alloc("v2");
		final Var var3 = Var.alloc("v3");

		final GenericIntermediateResultBlockImpl input1 = new GenericIntermediateResultBlockImpl();
		input1.add( JenaBasedSolutionMappingUtils.createJenaBasedSolutionMapping(
				var1, NodeFactory.createURI("http://example.org/x1")) );
		input1.add( JenaBasedSolutionMappingUtils.createJenaBasedSolutionMapping(
				var1, NodeFactory.createURI("http://example.org/x2")) );


		final GenericIntermediateResultBlockImpl input2 = new GenericIntermediateResultBlockImpl();
		input2.add( JenaBasedSolutionMappingUtils.createJenaBasedSolutionMapping(
				var2, NodeFactory.createURI("http://example.org/y1"),
				var3, NodeFactory.createURI("http://example.org/z1")) );

		final MaterializingIntermediateResultElementSink sink = new MaterializingIntermediateResultElementSink();

		final ExecOpSymmetricHashJoin op = new ExecOpSymmetricHashJoin();
		op.preprocess(input1, input2, sink);
		op.processBlockFromChild1(input1, sink, null);
		op.processBlockFromChild2(input2, sink, null);

		final Iterator<SolutionMapping> it = sink.getMaterializedIntermediateResult().iterator();

		assertFalse( it.hasNext() );
	}

	@Test
	public void JoinWithEmptyInput1() {
		final Var var1 = Var.alloc("v1");
		final Var var2 = Var.alloc("v2");
		final Var var3 = Var.alloc("v3");

		final GenericIntermediateResultBlockImpl input1 = new GenericIntermediateResultBlockImpl();

		final GenericIntermediateResultBlockImpl input2 = new GenericIntermediateResultBlockImpl();
		input2.add( JenaBasedSolutionMappingUtils.createJenaBasedSolutionMapping(
				var2, NodeFactory.createURI("http://example.org/y1"),
				var3, NodeFactory.createURI("http://example.org/z1")) );

		final MaterializingIntermediateResultElementSink sink = new MaterializingIntermediateResultElementSink();

		final ExecOpSymmetricHashJoin op = new ExecOpSymmetricHashJoin();
		op.preprocess(input1, input2, sink);
		op.processBlockFromChild1(input1, sink, null);
		op.processBlockFromChild2(input2, sink, null);

		final Iterator<SolutionMapping> it = sink.getMaterializedIntermediateResult().iterator();

		assertFalse( it.hasNext() );
	}

	@Test
	public void JoinWithEmptyInput2() {
		final Var var1 = Var.alloc("v1");
		final Var var2 = Var.alloc("v2");

		final GenericIntermediateResultBlockImpl input1 = new GenericIntermediateResultBlockImpl();
		input1.add( JenaBasedSolutionMappingUtils.createJenaBasedSolutionMapping(
				var1, NodeFactory.createURI("http://example.org/x1")) );
		input1.add( JenaBasedSolutionMappingUtils.createJenaBasedSolutionMapping(
				var1, NodeFactory.createURI("http://example.org/x2")) );

		final GenericIntermediateResultBlockImpl input2 = new GenericIntermediateResultBlockImpl();

		final MaterializingIntermediateResultElementSink sink = new MaterializingIntermediateResultElementSink();

		final ExecOpSymmetricHashJoin op = new ExecOpSymmetricHashJoin();
		op.preprocess(input1, input2, sink);
		op.processBlockFromChild1(input1, sink, null);
		op.processBlockFromChild2(input2, sink, null);

		final Iterator<SolutionMapping> it = sink.getMaterializedIntermediateResult().iterator();

		assertFalse( it.hasNext() );
	}

}
