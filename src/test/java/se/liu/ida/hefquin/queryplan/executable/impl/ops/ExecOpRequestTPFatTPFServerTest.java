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
import se.liu.ida.hefquin.federation.BRTPFServer;
import se.liu.ida.hefquin.federation.FederationAccessManager;
import se.liu.ida.hefquin.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.federation.TPFServer;
import se.liu.ida.hefquin.federation.access.BindingsRestrictedTriplePatternRequest;
import se.liu.ida.hefquin.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.federation.access.TPFInterface;
import se.liu.ida.hefquin.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.federation.access.TriplesResponse;
import se.liu.ida.hefquin.federation.access.impl.iface.TPFInterfaceImpl;
import se.liu.ida.hefquin.federation.access.impl.req.TriplePatternRequestImpl;
import se.liu.ida.hefquin.federation.access.impl.response.TriplesResponseImpl;
import se.liu.ida.hefquin.query.TriplePattern;
import se.liu.ida.hefquin.query.jenaimpl.JenaBasedQueryPatternUtils;
import se.liu.ida.hefquin.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.queryproc.ExecutionContext;

public class ExecOpRequestTPFatTPFServerTest
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
		final IntermediateResultElementSinkForTest sink = new IntermediateResultElementSinkForTest();

		op.execute( sink, createExecContextForTests() );

		final Iterator<SolutionMapping> it = sink.getSolMapsIter();

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
		return new ExecutionContext( new FederationAccessManagerForTest() );
	}

	protected static class FederationAccessManagerForTest implements FederationAccessManager
	{
		@Override
		public SolMapsResponse performRequest( final SPARQLRequest req, final SPARQLEndpoint fm ) {
			return null;
		}

		@Override
		public TriplesResponse performRequest( final TriplePatternRequest req, final TPFServer fm ) {
			final List<Triple> l = new ArrayList<Triple>();

			final Node s = NodeFactory.createURI("http://example.org/s");
			final Node p = NodeFactory.createURI("http://example.org/p");
			final Node o1 = NodeFactory.createURI("http://example.org/o1");
			l.add( JenaBasedTripleUtils.createJenaBasedTriple(s,p,o1) );

			final Node o2 = NodeFactory.createURI("http://example.org/o2");
			l.add( JenaBasedTripleUtils.createJenaBasedTriple(s,p,o2) );
			
			return new TriplesResponseImpl(l, fm);
		}

		@Override
		public TriplesResponse performRequest( final TriplePatternRequest req, final BRTPFServer fm ) {
			return null;
		}

		@Override
		public TriplesResponse performRequest( final BindingsRestrictedTriplePatternRequest req, final BRTPFServer fm ) {
			return null;
		}
	}

	protected static class TPFServerForTest implements TPFServer
	{
		final TPFInterface iface = new TPFInterfaceImpl();

		@Override
		public TPFInterface getInterface() {
			return iface;
		}
	}

	protected static class IntermediateResultElementSinkForTest implements IntermediateResultElementSink
	{
		protected final List<SolutionMapping> l = new ArrayList<>();

		@Override
		public void send(SolutionMapping element) {
			l.add(element);
		}

		public Iterator<SolutionMapping> getSolMapsIter() {
			return l.iterator();
		}
	}

}
