package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.Triple;
import se.liu.ida.hefquin.base.data.impl.TripleImpl;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.base.query.impl.TriplePatternImpl;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.CollectingIntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;
import se.liu.ida.hefquin.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.federation.access.impl.AsyncFederationAccessManagerImpl;
import se.liu.ida.hefquin.federation.access.impl.FederationAccessManagerWithCache;
import se.liu.ida.hefquin.federation.access.impl.req.TriplePatternRequestImpl;
import se.liu.ida.hefquin.federation.catalog.FederationCatalog;

public class ExecOpRequestTPFatTPFServerTest extends ExecOpTestBase
{
	protected static ExecutorService execServiceForFedAccess;

	@BeforeClass
	public static void createExecService() {
		final int numberOfThreads = 10;
		execServiceForFedAccess = Executors.newFixedThreadPool(numberOfThreads);
	}

	@AfterClass
	public static void tearDownExecService() {
		execServiceForFedAccess.shutdownNow();
		try {
			execServiceForFedAccess.awaitTermination(500L, TimeUnit.MILLISECONDS);
		}
		catch ( final InterruptedException ex )  {
			System.err.println("Terminating the thread pool was interrupted." );
			ex.printStackTrace();
		}
	}

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
				getDBpediaTPFServer(),
				false,
				null );

		final CollectingIntermediateResultElementSink sink = new CollectingIntermediateResultElementSink();

		// Create a federation access manager
		final FederationAccessManager internalFedAccMgr = new AsyncFederationAccessManagerImpl(execServiceForFedAccess);
		final FederationAccessManager fedAccessMgr = new FederationAccessManagerWithCache(internalFedAccMgr, 100);
		final ExecutionContext execCxt = new ExecutionContext() {
			@Override public FederationCatalog getFederationCatalog() { return null; }
			@Override public FederationAccessManager getFederationAccessMgr() { return fedAccessMgr; }
			@Override public ExecutorService getExecutorServiceForPlanTasks() { return null; }
			@Override public boolean isExperimentRun() { return false; }
			@Override public boolean skipExecution() { return false; }
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
				new TPFServerForTest(),
				false,
				null );
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
			@Override public ExecutorService getExecutorServiceForPlanTasks() { return null; }
			@Override public boolean isExperimentRun() { return false; }
			@Override public boolean skipExecution() { return false; }
		};
	}

}
