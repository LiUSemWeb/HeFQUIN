package se.liu.ida.hefquin.queryplan.executable.impl.ops;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.junit.Test;

import se.liu.ida.hefquin.data.SolutionMapping;
import se.liu.ida.hefquin.data.Triple;
import se.liu.ida.hefquin.data.jenaimpl.JenaBasedSolutionMapping;
import se.liu.ida.hefquin.data.jenaimpl.JenaBasedTripleUtils;
import se.liu.ida.hefquin.federation.access.impl.req.TriplePatternRequestImpl;
import se.liu.ida.hefquin.query.TriplePattern;
import se.liu.ida.hefquin.query.jenaimpl.JenaBasedQueryPatternUtils;
import se.liu.ida.hefquin.queryplan.executable.impl.MaterializingIntermediateResultElementSink;
import se.liu.ida.hefquin.queryproc.ExecutionContext;

public class ExecOpRequestTPFatTPFServerTest extends ExecOpTestBase
{
	@Test
	public void test() {
		final Node s = NodeFactory.createURI("http://example.org/s");
		final Node p = NodeFactory.createURI("http://example.org/p");
		final Var v = Var.alloc("v");
		final TriplePattern tp = JenaBasedQueryPatternUtils.createJenaBasedTriplePattern(s,p,v);
		final ExecOpRequestTPFatTPFServer op = new ExecOpRequestTPFatTPFServer(
				new TriplePatternRequestImpl(tp),
				new TPFServerForTest() );
		final MaterializingIntermediateResultElementSink sink = new MaterializingIntermediateResultElementSink();

		op.execute( sink, createExecContextForTests() );

		final Iterator<SolutionMapping> it = sink.getMaterializedIntermediateResult().iterator();

		assertTrue( it.hasNext() );
		final SolutionMapping sm1 = it.next();
		assertTrue( sm1 instanceof JenaBasedSolutionMapping );
		final Binding b1 = ( (JenaBasedSolutionMapping) sm1 ).asJenaBinding();
		assertEquals( 1, b1.size() );
		assertTrue( b1.contains(v) );
		assertEquals( "http://example.org/o1", b1.get(v).getURI() );

		assertTrue( it.hasNext() );
		final SolutionMapping sm2 = it.next();
		assertTrue( sm2 instanceof JenaBasedSolutionMapping );
		final Binding b2 = ( (JenaBasedSolutionMapping) sm2 ).asJenaBinding();
		assertEquals( 1, b2.size() );
		assertTrue( b2.contains(v) );
		assertEquals( "http://example.org/o2", b2.get(v).getURI() );

		assertFalse( it.hasNext() );
	}


	public static ExecutionContext createExecContextForTests() {
		final List<Triple> l = new ArrayList<Triple>();

		final Node s = NodeFactory.createURI("http://example.org/s");
		final Node p = NodeFactory.createURI("http://example.org/p");
		final Node o1 = NodeFactory.createURI("http://example.org/o1");
		l.add( JenaBasedTripleUtils.createJenaBasedTriple(s,p,o1) );

		final Node o2 = NodeFactory.createURI("http://example.org/o2");
		l.add( JenaBasedTripleUtils.createJenaBasedTriple(s,p,o2) );

		return new ExecutionContext( new FederationAccessManagerForTest(null,l) );
	}

}
