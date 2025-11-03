package se.liu.ida.hefquin.engine.queryproc.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.graph.GraphFactory;
import org.junit.Test;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.query.Query;
import se.liu.ida.hefquin.base.query.impl.GenericSPARQLGraphPatternImpl1;
import se.liu.ida.hefquin.engine.EngineTestBase;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalToPhysicalOpConverter;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalToPhysicalPlanConverter;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalToPhysicalPlanConverterImpl;
import se.liu.ida.hefquin.engine.queryproc.ExecutionEngine;
import se.liu.ida.hefquin.engine.queryproc.LogicalOptimizer;
import se.liu.ida.hefquin.engine.queryproc.PhysicalOptimizer;
import se.liu.ida.hefquin.engine.queryproc.QueryPlanCompiler;
import se.liu.ida.hefquin.engine.queryproc.QueryPlanner;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;
import se.liu.ida.hefquin.engine.queryproc.QueryProcException;
import se.liu.ida.hefquin.engine.queryproc.QueryProcessor;
import se.liu.ida.hefquin.engine.queryproc.SourcePlanner;
import se.liu.ida.hefquin.engine.queryproc.impl.compiler.*;
import se.liu.ida.hefquin.engine.queryproc.impl.execution.ExecutionEngineImpl;
import se.liu.ida.hefquin.engine.queryproc.impl.planning.QueryPlannerImpl;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.PhysicalOptimizerWithoutOptimization;
import se.liu.ida.hefquin.engine.queryproc.impl.srcsel.ServiceClauseBasedSourcePlannerImpl;
import se.liu.ida.hefquin.federation.access.BRTPFRequest;
import se.liu.ida.hefquin.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.federation.access.TPFRequest;
import se.liu.ida.hefquin.federation.access.TPFResponse;
import se.liu.ida.hefquin.federation.access.impl.BlockingFederationAccessManagerImpl;
import se.liu.ida.hefquin.federation.access.impl.reqproc.*;
import se.liu.ida.hefquin.federation.catalog.FederationCatalog;
import se.liu.ida.hefquin.federation.catalog.impl.FederationCatalogImpl;
import se.liu.ida.hefquin.federation.members.BRTPFServer;
import se.liu.ida.hefquin.federation.members.TPFServer;

public class QueryProcessorImplTest extends EngineTestBase
{
	@Test
	public void oneTPFoneTriplePattern() throws QueryProcException {
		// setting up
		final String queryString = "SELECT * WHERE {"
				+ "SERVICE <http://example.org> { ?x <http://example.org/p> ?y }"
				+ "}";

		final Graph dataForMember = GraphFactory.createGraphMem();
		dataForMember.add( Triple.create(
				NodeFactory.createURI("http://example.org/s"),
				NodeFactory.createURI("http://example.org/p"),
				NodeFactory.createURI("http://example.org/o")) );
		
		final FederationCatalogImpl fedCat = new FederationCatalogImpl();
		fedCat.addMember( "http://example.org", new TPFServerForTest(dataForMember) );

		final FederationAccessManager fedAccessMgr = new FederationAccessManagerForTest();

		final Iterator<SolutionMapping> it = processQuery(queryString, fedCat, fedAccessMgr);

		// checking
		assertTrue( it.hasNext() );

		final Binding sm1 = it.next().asJenaBinding();
		assertEquals( 2, sm1.size() );
		final Var varX = Var.alloc("x");
		final Var varY = Var.alloc("y");
		assertTrue( sm1.contains(varX) );
		assertTrue( sm1.contains(varY) );
		assertEquals( "http://example.org/s", sm1.get(varX).getURI() );
		assertEquals( "http://example.org/o", sm1.get(varY).getURI() );

		assertFalse( it.hasNext() );
	}

	@Test
	public void oneBRTPFtwoTriplePatterns() throws QueryProcException {
		// setting up
		final String queryString = "SELECT * WHERE {"
				+ "SERVICE <http://example.org> { ?x <http://example.org/p1> ?y; <http://example.org/p2> ?z }"
				+ "}";

		final Graph dataForMember = GraphFactory.createGraphMem();
		dataForMember.add( Triple.create(
				NodeFactory.createURI("http://example.org/s"),
				NodeFactory.createURI("http://example.org/p1"),
				NodeFactory.createURI("http://example.org/o1")) );
		dataForMember.add( Triple.create(
				NodeFactory.createURI("http://example.org/s"),
				NodeFactory.createURI("http://example.org/p2"),
				NodeFactory.createURI("http://example.org/o2")) );

		final FederationCatalogImpl fedCat = new FederationCatalogImpl();
		fedCat.addMember( "http://example.org", new BRTPFServerForTest(dataForMember) );

		final FederationAccessManager fedAccessMgr = new FederationAccessManagerForTest();

		final Iterator<SolutionMapping> it = processQuery(queryString, fedCat, fedAccessMgr);

		// checking
		assertTrue( it.hasNext() );

		final Binding sm1 = it.next().asJenaBinding();
		assertEquals( 3, sm1.size() );
		final Var varX = Var.alloc("x");
		final Var varY = Var.alloc("y");
		final Var varZ = Var.alloc("z");
		assertTrue( sm1.contains(varX) );
		assertTrue( sm1.contains(varY) );
		assertTrue( sm1.contains(varZ) );
		assertEquals( "http://example.org/s", sm1.get(varX).getURI() );
		assertEquals( "http://example.org/o1", sm1.get(varY).getURI() );
		assertEquals( "http://example.org/o2", sm1.get(varZ).getURI() );

		assertFalse( it.hasNext() );
	}

	@Test
	public void twoTPFtwoTriplePatterns() throws QueryProcException {
		// setting up
		final String queryString = "SELECT * WHERE {"
				+ "SERVICE <http://example.org/tpf1> { ?x <http://example.org/p1> ?y }"
				+ "SERVICE <http://example.org/tpf2> { ?x <http://example.org/p2> ?z }"
				+ "}";

		final Graph dataForMember1 = GraphFactory.createGraphMem();
		dataForMember1.add( Triple.create(
				NodeFactory.createURI("http://example.org/s"),
				NodeFactory.createURI("http://example.org/p1"),
				NodeFactory.createURI("http://example.org/o1")) );

		final Graph dataForMember2 = GraphFactory.createGraphMem();
		dataForMember2.add( Triple.create(
				NodeFactory.createURI("http://example.org/s"),
				NodeFactory.createURI("http://example.org/p2"),
				NodeFactory.createURI("http://example.org/o2")) );
		
		final FederationCatalogImpl fedCat = new FederationCatalogImpl();
		fedCat.addMember( "http://example.org/tpf1", new TPFServerForTest(dataForMember1) );
		fedCat.addMember( "http://example.org/tpf2", new TPFServerForTest(dataForMember2) );

		final FederationAccessManager fedAccessMgr = new FederationAccessManagerForTest();

		final Iterator<SolutionMapping> it = processQuery(queryString, fedCat, fedAccessMgr);

		// checking
		assertTrue( it.hasNext() );

		final Binding sm1 = it.next().asJenaBinding();
		assertEquals( 3, sm1.size() );
		final Var varX = Var.alloc("x");
		final Var varY = Var.alloc("y");
		final Var varZ = Var.alloc("z");
		assertTrue( sm1.contains(varX) );
		assertTrue( sm1.contains(varY) );
		assertTrue( sm1.contains(varZ) );
		assertEquals( "http://example.org/s", sm1.get(varX).getURI() );
		assertEquals( "http://example.org/o1", sm1.get(varY).getURI() );
		assertEquals( "http://example.org/o2", sm1.get(varZ).getURI() );

		assertFalse( it.hasNext() );
	}

	@Test
	public void unionOverTwoTriplePatterns() throws QueryProcException {
		// setting up
		final String queryString = "SELECT * WHERE {"
				+ "{ SERVICE <http://example.org/tpf1> { ?x <http://example.org/p1> ?y } }"
				+ "UNION"
				+ "{ SERVICE <http://example.org/tpf2> { ?x <http://example.org/p1> ?y } }"
				+ "}";

		final Graph dataForMember1 = GraphFactory.createGraphMem();
		dataForMember1.add( Triple.create(
				NodeFactory.createURI("http://example.org/s"),
				NodeFactory.createURI("http://example.org/p1"),
				NodeFactory.createURI("http://example.org/o1")) );

		final Graph dataForMember2 = GraphFactory.createGraphMem();
		dataForMember2.add( Triple.create(
				NodeFactory.createURI("http://example.org/s"),
				NodeFactory.createURI("http://example.org/p1"),
				NodeFactory.createURI("http://example.org/o2")) );
		
		final FederationCatalogImpl fedCat = new FederationCatalogImpl();
		fedCat.addMember( "http://example.org/tpf1", new TPFServerForTest(dataForMember1) );
		fedCat.addMember( "http://example.org/tpf2", new TPFServerForTest(dataForMember2) );

		final FederationAccessManager fedAccessMgr = new FederationAccessManagerForTest();

		final Iterator<SolutionMapping> it = processQuery(queryString, fedCat, fedAccessMgr);

		// checking
		assertTrue( it.hasNext() );

		final Binding sm1 = it.next().asJenaBinding();
		assertEquals( 2, sm1.size() );
		final Var varX = Var.alloc("x");
		final Var varY = Var.alloc("y");
		assertTrue( sm1.contains(varX) );
		assertTrue( sm1.contains(varY) );
		assertEquals( "http://example.org/s", sm1.get(varX).getURI() );
		final String uriY1 = sm1.get(varY).getURI();
		assertTrue( uriY1.equals("http://example.org/o1") || uriY1.equals("http://example.org/o2") );

		assertTrue( it.hasNext() );

		final Binding sm2 = it.next().asJenaBinding();
		assertEquals( 2, sm2.size() );
		assertTrue( sm2.contains(varX) );
		assertTrue( sm2.contains(varY) );
		assertEquals( "http://example.org/s", sm2.get(varX).getURI() );
		final String uriY2 = sm2.get(varY).getURI();
		assertTrue( uriY2.equals("http://example.org/o1") || uriY2.equals("http://example.org/o2") );

		assertFalse( it.hasNext() );
	}

	@Test
	public void oneTPFoneTriplePatternWithFilterInside() throws QueryProcException {
		// setting up
		final String queryString = "SELECT * WHERE { "
				+ "SERVICE <http://example.org> { ?x <http://example.org/p> ?y FILTER (?y = <http://example.org/o1>) } "
				+ "}";

		final Graph dataForMember = GraphFactory.createGraphMem();
		dataForMember.add( Triple.create(
				NodeFactory.createURI("http://example.org/s1"),
				NodeFactory.createURI("http://example.org/p"),
				NodeFactory.createURI("http://example.org/o1")) );
		dataForMember.add( Triple.create(
				NodeFactory.createURI("http://example.org/s2"),
				NodeFactory.createURI("http://example.org/p"),
				NodeFactory.createURI("http://example.org/o2")) );
		
		final FederationCatalogImpl fedCat = new FederationCatalogImpl();
		fedCat.addMember( "http://example.org", new TPFServerForTest(dataForMember) );

		final FederationAccessManager fedAccessMgr = new FederationAccessManagerForTest();

		final Iterator<SolutionMapping> it = processQuery(queryString, fedCat, fedAccessMgr);

		// checking
		assertTrue( it.hasNext() );

		final Binding sm1 = it.next().asJenaBinding();
		assertEquals( 2, sm1.size() );
		final Var varX = Var.alloc("x");
		final Var varY = Var.alloc("y");
		assertTrue( sm1.contains(varX) );
		assertTrue( sm1.contains(varY) );
		assertEquals( "http://example.org/s1", sm1.get(varX).getURI() );
		assertEquals( "http://example.org/o1", sm1.get(varY).getURI() );

		assertFalse( it.hasNext() );
	}

	@Test
	public void oneTPFoneTriplePatternWithFilterOutside() throws QueryProcException {
		// setting up
		final String queryString = "SELECT * WHERE { "
				+ "SERVICE <http://example.org> { ?x <http://example.org/p> ?y } "
				+ "FILTER (?y = <http://example.org/o1>) "
				+ "}";

		final Graph dataForMember = GraphFactory.createGraphMem();
		dataForMember.add( Triple.create(
				NodeFactory.createURI("http://example.org/s1"),
				NodeFactory.createURI("http://example.org/p"),
				NodeFactory.createURI("http://example.org/o1")) );
		dataForMember.add( Triple.create(
				NodeFactory.createURI("http://example.org/s2"),
				NodeFactory.createURI("http://example.org/p"),
				NodeFactory.createURI("http://example.org/o2")) );
		
		final FederationCatalogImpl fedCat = new FederationCatalogImpl();
		fedCat.addMember( "http://example.org", new TPFServerForTest(dataForMember) );

		final FederationAccessManager fedAccessMgr = new FederationAccessManagerForTest();

		final Iterator<SolutionMapping> it = processQuery(queryString, fedCat, fedAccessMgr);

		// checking
		assertTrue( it.hasNext() );

		final Binding sm1 = it.next().asJenaBinding();
		assertEquals( 2, sm1.size() );
		final Var varX = Var.alloc("x");
		final Var varY = Var.alloc("y");
		assertTrue( sm1.contains(varX) );
		assertTrue( sm1.contains(varY) );
		assertEquals( "http://example.org/s1", sm1.get(varX).getURI() );
		assertEquals( "http://example.org/o1", sm1.get(varY).getURI() );

		assertFalse( it.hasNext() );
	}

	@Test
	public void liveTestWithDBpedia() throws QueryProcException {
		if ( ! skipLiveWebTests ) {
			// setting up
			final String dbpediaURL = "http://dbpedia.org/sparql";
			final String queryString = "SELECT * WHERE {"
					+ "SERVICE <" + dbpediaURL + "> { <http://dbpedia.org/resource/Berlin> <http://xmlns.com/foaf/0.1/name> ?o }"
					+ "}";

			final FederationCatalogImpl fedCat = new FederationCatalogImpl();
			fedCat.addMember( dbpediaURL, new SPARQLEndpointForTest(dbpediaURL) );

			final SPARQLRequestProcessor reqProcSPARQL = new SPARQLRequestProcessorImpl();
			final TPFRequestProcessor reqProcTPF = new TPFRequestProcessor() {
				@Override public TPFResponse performRequest(TPFRequest req, TPFServer fm) { return null; }
				@Override public TPFResponse performRequest(TPFRequest req, BRTPFServer fm) { return null; }
			};
			final BRTPFRequestProcessor reqProcBRTPF = new BRTPFRequestProcessor() {
				@Override public TPFResponse performRequest(BRTPFRequest req, BRTPFServer fm) { return null; }
			};
			final Neo4jRequestProcessor reqProcNeo4j = new Neo4jRequestProcessorImpl();
			final FederationAccessManager fedAccessMgr = new BlockingFederationAccessManagerImpl(reqProcSPARQL, reqProcTPF, reqProcBRTPF, reqProcNeo4j);

			// executing the tested method
			final Iterator<SolutionMapping> it = processQuery(queryString, fedCat, fedAccessMgr);

			// checking
			assertTrue( it.hasNext() );

			final Binding b = it.next().asJenaBinding();
			final Var var = Var.alloc("o");
			assertEquals( 1, b.size() );
			assertTrue( b.contains(var) );

			final Node n = b.get(var);
			assertTrue( n.isLiteral() );
		}
	}


	protected Iterator<SolutionMapping> processQuery( final String queryString,
	                                                  final FederationCatalog fedCat,
	                                                  final FederationAccessManager fedAccessMgr ) throws QueryProcException {
		final ExecutorService execServiceForPlanTasks = Executors.newFixedThreadPool(10);
		final LogicalToPhysicalPlanConverter lp2pp = new LogicalToPhysicalPlanConverterImpl(false, false);
		final LogicalToPhysicalOpConverter lop2pop = getLOP2POPForTests();

		final QueryProcContext ctxt = new QueryProcContext() {
			@Override public FederationCatalog getFederationCatalog() { return fedCat; }
			@Override public FederationAccessManager getFederationAccessMgr() { return fedAccessMgr; }
			@Override public ExecutorService getExecutorServiceForPlanTasks() { return execServiceForPlanTasks; }
			@Override public LogicalToPhysicalPlanConverter getLogicalToPhysicalPlanConverter() { return lp2pp; }
			@Override public LogicalToPhysicalOpConverter getLogicalToPhysicalOpConverter() { return lop2pop; }
			@Override public boolean isExperimentRun() { return false; }
			@Override public boolean skipExecution() { return false; }
		};

		final SourcePlanner sourcePlanner = new ServiceClauseBasedSourcePlannerImpl();

		final LogicalOptimizer loptimizer = new LogicalOptimizer() {
			@Override
			public LogicalPlan optimize( final LogicalPlan p, final boolean keepNaryOperators, final QueryProcContext ctxt ) {
				return p;
			}
		};

		final PhysicalOptimizer poptimizer = new PhysicalOptimizerWithoutOptimization();
		final QueryPlanner planner = new QueryPlannerImpl(sourcePlanner, loptimizer, poptimizer, null, null, null,  null);
		final QueryPlanCompiler planCompiler = new
				//IteratorBasedQueryPlanCompilerImpl(ctxt);
				//PullBasedQueryPlanCompilerImpl(ctxt);
				QueryPlanCompilerForPushBasedExecution(ctxt);
		final ExecutionEngine execEngine = new ExecutionEngineImpl();
		final QueryProcessor qProc = new QueryProcessorImpl(planner, planCompiler, execEngine, ctxt);
		final MaterializingQueryResultSinkImpl resultSink = new MaterializingQueryResultSinkImpl();
		final Query query = new GenericSPARQLGraphPatternImpl1( QueryFactory.create(queryString).getQueryPattern() );

		qProc.processQuery(query, resultSink);

		execServiceForPlanTasks.shutdownNow();
		try {
			execServiceForPlanTasks.awaitTermination(500L, TimeUnit.MILLISECONDS);
		}
		catch ( final InterruptedException ex )  {
System.err.println("Terminating the thread pool was interrupted." );
			ex.printStackTrace();
		}

		return resultSink.getSolMapsIter();
	}

}
