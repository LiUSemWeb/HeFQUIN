package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.graph.GraphFactory;
import org.junit.Test;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.impl.SolutionMappingUtils;
import se.liu.ida.hefquin.engine.federation.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.TPFServer;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.jenaimpl.JenaBasedQueryPatternUtils;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.GenericIntermediateResultBlockImpl;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.MaterializingIntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

public class ExecOpIndexNestedLoopsJoinTPFTest extends ExecOpTestBase
{
	@Test
	public void tpWithJoinOnObject() {
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
		final Node z3 = NodeFactory.createURI("http://example.org/z3");

		final GenericIntermediateResultBlockImpl input = new GenericIntermediateResultBlockImpl();
		input.add( SolutionMappingUtils.createSolutionMapping(
				var1, x1,
				var2, y1) );
		input.add( SolutionMappingUtils.createSolutionMapping(
				var1, x2,
				var2, y2) );

		final TriplePattern tp = JenaBasedQueryPatternUtils.createJenaBasedTriplePattern(var2,p,var3);

		final Graph dataForMember = GraphFactory.createGraphMem();
		dataForMember.add( Triple.create(y1,p,z1) );
		dataForMember.add( Triple.create(y1,p,z2) );
		dataForMember.add( Triple.create(y2,p,z3) );

		final Iterator<SolutionMapping> it = runTest(input, dataForMember, tp);

		assertTrue( it.hasNext() );
		final Binding b1 = it.next().asJenaBinding();
		assertEquals( 3, b1.size() );
		assertEquals( "http://example.org/x1", b1.get(var1).getURI() );
		assertEquals( "http://example.org/y1", b1.get(var2).getURI() );

		final boolean z1CameOutFirst;
		if ( b1.get(var3).getURI().equals("http://example.org/z1") ) {
			z1CameOutFirst = true;
		} else {
			z1CameOutFirst = false;
			assertEquals( "http://example.org/z2", b1.get(var3).getURI() );
			
		}

		assertTrue( it.hasNext() );
		final Binding b2 = it.next().asJenaBinding();
		assertEquals( 3, b2.size() );
		assertEquals( "http://example.org/x1", b2.get(var1).getURI() );
		assertEquals( "http://example.org/y1", b2.get(var2).getURI() );

		if ( z1CameOutFirst ) {
			assertEquals( "http://example.org/z2", b2.get(var3).getURI() );
		} else {
			assertEquals( "http://example.org/z1", b2.get(var3).getURI() );
		}

		assertTrue( it.hasNext() );
		final Binding b3 = it.next().asJenaBinding();
		assertEquals( 3, b3.size() );
		assertEquals( "http://example.org/x2", b3.get(var1).getURI() );
		assertEquals( "http://example.org/y2", b3.get(var2).getURI() );
		assertEquals( "http://example.org/z3", b3.get(var3).getURI() );

		assertFalse( it.hasNext() );
	}

	@Test
	public void tpWithJoinOnSubjectAndObject() {
		final Var var1 = Var.alloc("v1");
		final Var var2 = Var.alloc("v2");
		final Var var3 = Var.alloc("v3");

		final Node x1 = NodeFactory.createURI("http://example.org/x1");
		final Node x2 = NodeFactory.createURI("http://example.org/x2");
		final Node y1 = NodeFactory.createURI("http://example.org/y1");
		final Node y2 = NodeFactory.createURI("http://example.org/y2");
		final Node z1 = NodeFactory.createURI("http://example.org/z1");
		final Node z2 = NodeFactory.createURI("http://example.org/z2");

		final GenericIntermediateResultBlockImpl input = new GenericIntermediateResultBlockImpl();
		input.add( SolutionMappingUtils.createSolutionMapping(
				var1, x1,
				var2, y1) );
		input.add( SolutionMappingUtils.createSolutionMapping(
				var1, x2,
				var2, y2) );

		final TriplePattern tp = JenaBasedQueryPatternUtils.createJenaBasedTriplePattern(var1,var2,var3);

		final Graph dataForMember = GraphFactory.createGraphMem();
		dataForMember.add( Triple.create(x1,y1,z1) );
		dataForMember.add( Triple.create(x2,y2,z2) );

		final Iterator<SolutionMapping> it = runTest(input, dataForMember, tp);

		assertTrue( it.hasNext() );
		final Binding b1 = it.next().asJenaBinding();
		assertEquals( 3, b1.size() );
		assertEquals( "http://example.org/x1", b1.get(var1).getURI() );
		assertEquals( "http://example.org/y1", b1.get(var2).getURI() );
		assertEquals( "http://example.org/z1", b1.get(var3).getURI() );

		assertTrue( it.hasNext() );
		final Binding b2 = it.next().asJenaBinding();
		assertEquals( 3, b2.size() );
		assertEquals( "http://example.org/x2", b2.get(var1).getURI() );
		assertEquals( "http://example.org/y2", b2.get(var2).getURI() );
		assertEquals( "http://example.org/z2", b2.get(var3).getURI() );

		assertFalse( it.hasNext() );
	}

	@Test
	public void tpWithoutJoinVariable() {
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

		final GenericIntermediateResultBlockImpl input = new GenericIntermediateResultBlockImpl();
		input.add( SolutionMappingUtils.createSolutionMapping(var1, x1) );
		input.add( SolutionMappingUtils.createSolutionMapping(var1, x2) );

		final TriplePattern tp = JenaBasedQueryPatternUtils.createJenaBasedTriplePattern(var2,p,var3);

		final Graph dataForMember = GraphFactory.createGraphMem();
		dataForMember.add( Triple.create(y1,p,z1) );
		dataForMember.add( Triple.create(y2,p,z2) );

		final Iterator<SolutionMapping> it = runTest(input, dataForMember, tp);

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

	@Test
	public void tpWithEmptyInput() {
		final Var var2 = Var.alloc("v2");
		final Var var3 = Var.alloc("v3");

		final Node p = NodeFactory.createURI("http://example.org/p");
		final Node y1 = NodeFactory.createURI("http://example.org/y1");
		final Node z1 = NodeFactory.createURI("http://example.org/z1");

		final GenericIntermediateResultBlockImpl input = new GenericIntermediateResultBlockImpl();

		final TriplePattern tp = JenaBasedQueryPatternUtils.createJenaBasedTriplePattern(var2,p,var3);

		final Graph dataForMember = GraphFactory.createGraphMem();
		dataForMember.add( Triple.create(y1,p,z1) );

		final Iterator<SolutionMapping> it = runTest(input, dataForMember, tp);

		assertFalse( it.hasNext() );
	}

	@Test
	public void tpWithEmptyResponses() {
		final Var var1 = Var.alloc("v1");
		final Var var2 = Var.alloc("v2");

		final GenericIntermediateResultBlockImpl input = new GenericIntermediateResultBlockImpl();
		input.add( SolutionMappingUtils.createSolutionMapping(
				var1, NodeFactory.createURI("http://example.org/x1")) );
		input.add( SolutionMappingUtils.createSolutionMapping(
				var1, NodeFactory.createURI("http://example.org/x2")) );

		final Node p = NodeFactory.createURI("http://example.org/p");
		final TriplePattern tp = JenaBasedQueryPatternUtils.createJenaBasedTriplePattern(var1,p,var2);

		final Graph dataForMember = GraphFactory.createGraphMem();

		final Iterator<SolutionMapping> it = runTest(input, dataForMember, tp);

		assertFalse( it.hasNext() );
	}


	protected Iterator<SolutionMapping> runTest(
			final IntermediateResultBlock input,
			final Graph dataForMember,
			final TriplePattern tp )
	{
		final FederationAccessManager fedAccessMgr = new FederationAccessManagerForTest();
		final ExecutionContext execCxt = new ExecutionContext(fedAccessMgr);
		final MaterializingIntermediateResultElementSink sink = new MaterializingIntermediateResultElementSink();
		final TPFServer fm = new TPFServerForTest(dataForMember);

		final ExecOpIndexNestedLoopsJoinTPF op = new ExecOpIndexNestedLoopsJoinTPF(tp, fm);
		op.process(input, sink, execCxt);
		op.concludeExecution(sink, execCxt);

		return sink.getMaterializedIntermediateResult().iterator();
	}

}
