package se.liu.ida.hefquin.engine.queryproc.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.graph.GraphFactory;
import org.junit.Test;

import se.liu.ida.hefquin.engine.EngineTestBase;
import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.TPFServer;
import se.liu.ida.hefquin.engine.federation.access.BRTPFRequest;
import se.liu.ida.hefquin.engine.federation.access.TPFRequest;
import se.liu.ida.hefquin.engine.federation.access.TPFResponse;
import se.liu.ida.hefquin.engine.federation.access.impl.BlockingFederationAccessManagerImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.*;
import se.liu.ida.hefquin.engine.federation.catalog.FederationCatalog;
import se.liu.ida.hefquin.engine.query.Query;
import se.liu.ida.hefquin.engine.query.impl.SPARQLGraphPatternImpl;
import se.liu.ida.hefquin.engine.queryproc.ExecutionEngine;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizer;
import se.liu.ida.hefquin.engine.queryproc.QueryPlanCompiler;
import se.liu.ida.hefquin.engine.queryproc.QueryPlanner;
import se.liu.ida.hefquin.engine.queryproc.QueryProcessor;
import se.liu.ida.hefquin.engine.queryproc.SourcePlanner;
import se.liu.ida.hefquin.engine.queryproc.impl.compiler.QueryPlanCompilerImpl;
import se.liu.ida.hefquin.engine.queryproc.impl.execution.ExecutionEngineImpl;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.LogicalToPhysicalPlanConverter;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.LogicalToPhysicalPlanConverterImpl;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.QueryOptimizerImpl;
import se.liu.ida.hefquin.engine.queryproc.impl.planning.QueryPlannerImpl;
import se.liu.ida.hefquin.engine.queryproc.impl.srcsel.SourcePlannerImpl;

public class QueryProcessorImplTest extends EngineTestBase
{
	@Test
	public void oneTPFoneTriplePattern() {
		// setting up
		final String queryString = "SELECT * WHERE {"
				+ "SERVICE <http://example.org> { ?x <http://example.org/p> ?y }"
				+ "}";

		final Graph dataForMember = GraphFactory.createGraphMem();
		dataForMember.add( Triple.create(
				NodeFactory.createURI("http://example.org/s"),
				NodeFactory.createURI("http://example.org/p"),
				NodeFactory.createURI("http://example.org/o")) );
		
		final FederationCatalogForTest fedCat = new FederationCatalogForTest();
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
	public void oneBRTPFtwoTriplePatterns() {
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

		final FederationCatalogForTest fedCat = new FederationCatalogForTest();
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
	public void twoTPFtwoTriplePatterns() {
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
		
		final FederationCatalogForTest fedCat = new FederationCatalogForTest();
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
	public void liveTestWithDBpedia() {
		if ( ! skipLiveWebTests ) {
			// setting up
			final String dbpediaURL = "http://dbpedia.org/sparql";
			final String queryString = "SELECT * WHERE {"
					+ "SERVICE <" + dbpediaURL + "> { <http://dbpedia.org/resource/Berlin> <http://xmlns.com/foaf/0.1/name> ?o }"
					+ "}";

			final FederationCatalogForTest fedCat = new FederationCatalogForTest();
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
	                                                  final FederationAccessManager fedAccessMgr ) {
		final LogicalToPhysicalPlanConverter l2pConverter = new LogicalToPhysicalPlanConverterImpl();
		final SourcePlanner sourcePlanner = new SourcePlannerImpl(fedCat);
		final QueryOptimizer optimizer = new QueryOptimizerImpl(l2pConverter);
		final QueryPlanner planner = new QueryPlannerImpl(sourcePlanner, optimizer);
		final QueryPlanCompiler planCompiler = new QueryPlanCompilerImpl(fedAccessMgr);
		final ExecutionEngine execEngine = new ExecutionEngineImpl();
		final QueryProcessor qProc = new QueryProcessorImpl(planner, planCompiler, execEngine);
		final MaterializingQueryResultSinkImpl resultSink = new MaterializingQueryResultSinkImpl();
		final Query query = new SPARQLGraphPatternImpl( QueryFactory.create(queryString).getQueryPattern() );

		qProc.processQuery(query, resultSink);

		return resultSink.getSolMapsIter();
	}

}
