package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.junit.Test;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.Triple;
import se.liu.ida.hefquin.engine.data.impl.TripleImpl;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.access.impl.req.TriplePatternRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.utils.FederationAccessUtils;
import se.liu.ida.hefquin.engine.federation.catalog.FederationCatalog;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.TriplePatternImpl;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.CollectingIntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.CostModel;

public class ExecOpRequestTPFatTPFServerTest extends ExecOpTestBase
{
	@Test
	public void testOnline() throws ExecOpExecutionException {
		if ( skipLiveWebTests ) { return; }

		// setting up
		final Node s = NodeFactory.createURI("http://dbpedia.org/resource/Berlin");
		final Node p = NodeFactory.createVariable("p");
		final Node o = NodeFactory.createVariable("o");
		final TriplePattern tp = new TriplePatternImpl(s,p,o);

		final ExecOpRequestTPFatTPFServer op = new ExecOpRequestTPFatTPFServer(
				new TriplePatternRequestImpl(tp),
				getDBpediaTPFServer() );

		final CollectingIntermediateResultElementSink sink = new CollectingIntermediateResultElementSink();

		final FederationAccessManager fedAccessMgr = FederationAccessUtils.getDefaultFederationAccessManager();
		final ExecutionContext execCxt = new ExecutionContext() {
			@Override public FederationCatalog getFederationCatalog() { return null; }
			@Override public FederationAccessManager getFederationAccessMgr() { return fedAccessMgr; }
			@Override public CostModel getCostModel() { return null; }
			@Override public boolean isExperimentRun() { return false; }
		};

		op.execute(sink, execCxt);

		final Collection<SolutionMapping> res = (Collection<SolutionMapping>) sink.getCollectedSolutionMappings();
		assertTrue( res.size() > 100 );
	}

	@Test
	public void testOffline() throws ExecOpExecutionException {
		final Node s = NodeFactory.createURI("http://example.org/s");
		final Node p = NodeFactory.createURI("http://example.org/p");
		final Var v = Var.alloc("v");
		final TriplePattern tp = new TriplePatternImpl(s,p,v);
		final ExecOpRequestTPFatTPFServer op = new ExecOpRequestTPFatTPFServer(
				new TriplePatternRequestImpl(tp),
				new TPFServerForTest() );
		final CollectingIntermediateResultElementSink sink = new CollectingIntermediateResultElementSink();

		op.execute( sink, createExecContextForTests() );

		final Iterator<SolutionMapping> it = sink.getCollectedSolutionMappings().iterator();

		assertTrue( it.hasNext() );
		final Binding b1 = it.next().asJenaBinding();
		assertEquals( 1, b1.size() );
		assertTrue( b1.contains(v) );
		assertEquals( "http://example.org/o1", b1.get(v).getURI() );

		assertTrue( it.hasNext() );
		final Binding b2 = it.next().asJenaBinding();
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
		l.add( new TripleImpl(s,p,o1) );

		final Node o2 = NodeFactory.createURI("http://example.org/o2");
		l.add( new TripleImpl(s,p,o2) );

		final FederationAccessManager fedAccessMgr = new FederationAccessManagerForTest(null, l);
		return new ExecutionContext() {
			@Override public FederationCatalog getFederationCatalog() { return null; }
			@Override public FederationAccessManager getFederationAccessMgr() { return fedAccessMgr; }
			@Override public CostModel getCostModel() { return null; }
			@Override public boolean isExperimentRun() { return false; }
		};
	}

}
