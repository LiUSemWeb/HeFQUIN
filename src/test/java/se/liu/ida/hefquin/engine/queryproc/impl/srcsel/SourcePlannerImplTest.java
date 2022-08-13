package se.liu.ida.hefquin.engine.queryproc.impl.srcsel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.ExecutorService;

import org.apache.jena.graph.Triple;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpLeftJoin;
import org.junit.Test;

import se.liu.ida.hefquin.engine.EngineTestBase;
import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.TPFServer;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.engine.federation.catalog.FederationCatalog;
import se.liu.ida.hefquin.engine.query.Query;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.GenericSPARQLGraphPatternImpl1;
import se.liu.ida.hefquin.engine.query.impl.GenericSPARQLGraphPatternImpl2;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayLeftJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;
import se.liu.ida.hefquin.engine.queryproc.SourcePlanner;
import se.liu.ida.hefquin.engine.queryproc.SourcePlanningException;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.CostModel;

public class SourcePlannerImplTest extends EngineTestBase
{
	@Test
	public void oneTPFoneTriplePattern() throws SourcePlanningException {
		// setup
		final String queryString = "SELECT * WHERE {"
				+ "SERVICE <http://example.org> { ?x <http://example.org/p> ?y }"
				+ "}";
		
		final FederationCatalogForTest fedCat = new FederationCatalogForTest();
		fedCat.addMember( "http://example.org", new TPFServerForTest() );

		final LogicalPlan plan = createLogicalPlan(queryString, fedCat);

		// tests
		assertEquals( 0, plan.numberOfSubPlans() );
		assertTrue( plan.getRootOperator() instanceof LogicalOpRequest<?,?> );

		final LogicalOpRequest<?,?> rootOp = (LogicalOpRequest<?,?>) plan.getRootOperator();
		assertTrue( rootOp.getFederationMember() instanceof TPFServer );
		assertTrue( rootOp.getRequest() instanceof TriplePatternRequest );

		final TriplePatternRequest req = (TriplePatternRequest) rootOp.getRequest();
		assertEqualTriplePatternsVUV( "x", "http://example.org/p", "y", req );
	}

	@Test
	public void oneBRTPFtwoTriplePatterns() throws SourcePlanningException {
		// setup
		final String queryString = "SELECT * WHERE {"
				+ "SERVICE <http://example.org> { ?x <http://example.org/p1> ?y; <http://example.org/p2> ?z }"
				+ "}";
		
		final FederationCatalogForTest fedCat = new FederationCatalogForTest();
		fedCat.addMember( "http://example.org", new BRTPFServerForTest() );

		final LogicalPlan plan = createLogicalPlan(queryString, fedCat);

		// tests
		assertEquals( 2, plan.numberOfSubPlans() );
		assertTrue( plan.getRootOperator() instanceof LogicalOpMultiwayJoin );

		final LogicalPlan subplan1 = plan.getSubPlan(0); 
		assertEquals( 0, subplan1.numberOfSubPlans() );
		assertTrue( subplan1.getRootOperator() instanceof LogicalOpRequest<?,?> );

		final LogicalOpRequest<?,?> subRootOp1 = (LogicalOpRequest<?,?>) subplan1.getRootOperator();
		assertTrue( subRootOp1.getFederationMember() instanceof BRTPFServer );
		assertTrue( subRootOp1.getRequest() instanceof TriplePatternRequest );

		final TriplePatternRequest req1 = (TriplePatternRequest) subRootOp1.getRequest();
		final TriplePattern tp1 = req1.getQueryPattern();
		final boolean firstPredicateWasP1;
		if ( tp1.asJenaTriple().getPredicate().getURI().equals("http://example.org/p1") ) {
			assertEqualTriplePatternsVUV( "x", "http://example.org/p1", "y", tp1 );
			firstPredicateWasP1 = true;
		} else {
			assertEqualTriplePatternsVUV( "x", "http://example.org/p2", "z", tp1 );
			firstPredicateWasP1 = false;
		}

		final LogicalPlan subplan2 = plan.getSubPlan(1); 
		assertEquals( 0, subplan2.numberOfSubPlans() );
		assertTrue( subplan2.getRootOperator() instanceof LogicalOpRequest<?,?> );

		final LogicalOpRequest<?,?> subRootOp2 = (LogicalOpRequest<?,?>) subplan2.getRootOperator();
		assertTrue( subRootOp2.getFederationMember() instanceof BRTPFServer );
		assertTrue( subRootOp2.getRequest() instanceof TriplePatternRequest );

		final TriplePatternRequest req2 = (TriplePatternRequest) subRootOp2.getRequest();
		if ( firstPredicateWasP1 ) {
			assertEqualTriplePatternsVUV( "x", "http://example.org/p2", "z", req2 );
		} else {
			assertEqualTriplePatternsVUV( "x", "http://example.org/p1", "y", req2 );
		}
	}

	@Test
	public void twoBRTPFtwoTriplePatterns() throws SourcePlanningException {
		// setup
		final String queryString = "SELECT * WHERE {"
				+ "SERVICE <http://example.org/tpf1> { ?x <http://example.org/p1> ?y }"
				+ "SERVICE <http://example.org/tpf2> { ?x <http://example.org/p2> ?z }"
				+ "}";
		
		final FederationCatalogForTest fedCat = new FederationCatalogForTest();
		fedCat.addMember( "http://example.org/tpf1", new TPFServerForTest() );
		fedCat.addMember( "http://example.org/tpf2", new TPFServerForTest() );

		final LogicalPlan plan = createLogicalPlan(queryString, fedCat);

		// tests
		assertEquals( 2, plan.numberOfSubPlans() );
		assertTrue( plan.getRootOperator() instanceof LogicalOpMultiwayJoin );

		final LogicalPlan subplan1 = plan.getSubPlan(0); 
		assertEquals( 0, subplan1.numberOfSubPlans() );
		assertTrue( subplan1.getRootOperator() instanceof LogicalOpRequest<?,?> );

		final LogicalOpRequest<?,?> subRootOp1 = (LogicalOpRequest<?,?>) subplan1.getRootOperator();
		assertTrue( subRootOp1.getFederationMember() instanceof TPFServer );
		assertTrue( subRootOp1.getRequest() instanceof TriplePatternRequest );

		final TriplePatternRequest req1 = (TriplePatternRequest) subRootOp1.getRequest();
		assertEqualTriplePatternsVUV( "x", "http://example.org/p1", "y", req1 );

		final LogicalPlan subplan2 = plan.getSubPlan(1); 
		assertEquals( 0, subplan2.numberOfSubPlans() );
		assertTrue( subplan2.getRootOperator() instanceof LogicalOpRequest<?,?> );

		final LogicalOpRequest<?,?> subRootOp2 = (LogicalOpRequest<?,?>) subplan2.getRootOperator();
		assertTrue( subRootOp2.getFederationMember() instanceof TPFServer );
		assertTrue( subRootOp2.getRequest() instanceof TriplePatternRequest );

		final TriplePatternRequest req2 = (TriplePatternRequest) subRootOp2.getRequest();
		assertEqualTriplePatternsVUV( "x", "http://example.org/p2", "z", req2 );
	}

	@Test
	public void optionalOutsideService() throws SourcePlanningException {
		// setup
		final String queryString = "SELECT * WHERE {"
				+ "  SERVICE <http://example.org> { ?x <http://example.org/p> ?y }"
				+ "  OPTIONAL"
				+ "  { SERVICE <http://example.org> { ?z <http://example.org/q> ?y } }"
				+ "}";
		
		final FederationCatalogForTest fedCat = new FederationCatalogForTest();
		fedCat.addMember( "http://example.org", new TPFServerForTest() );

		final LogicalPlan plan = createLogicalPlan(queryString, fedCat);

		// tests
		assertEquals( 2, plan.numberOfSubPlans() );
		assertTrue( plan.getRootOperator() instanceof LogicalOpMultiwayLeftJoin );

		final LogicalPlan subplan1 = plan.getSubPlan(0); 
		assertEquals( 0, subplan1.numberOfSubPlans() );
		assertTrue( subplan1.getRootOperator() instanceof LogicalOpRequest<?,?> );

		final LogicalOpRequest<?,?> rootOp1 = (LogicalOpRequest<?,?>) subplan1.getRootOperator();
		assertTrue( rootOp1.getRequest() instanceof TriplePatternRequest );

		final TriplePatternRequest req1 = (TriplePatternRequest) rootOp1.getRequest();
		assertEqualTriplePatternsVUV( "x", "http://example.org/p", "y", req1 );

		final LogicalPlan subplan2 = plan.getSubPlan(1); 
		assertEquals( 0, subplan2.numberOfSubPlans() );
		assertTrue( subplan2.getRootOperator() instanceof LogicalOpRequest<?,?> );

		final LogicalOpRequest<?,?> rootOp2 = (LogicalOpRequest<?,?>) subplan2.getRootOperator();
		assertTrue( rootOp2.getRequest() instanceof TriplePatternRequest );

		final TriplePatternRequest req2 = (TriplePatternRequest) rootOp2.getRequest();
		assertEqualTriplePatternsVUV( "z", "http://example.org/q", "y", req2 );
	}

	@Test
	public void optionalInsideServiceTPF() throws SourcePlanningException {
		// setup
		final String queryString = "SELECT * WHERE {"
				+ "  SERVICE <http://example.org> {"
				+ "    { ?x <http://example.org/p> ?y } OPTIONAL { ?z <http://example.org/q> ?y }"
				+ "  }"
				+ "}";
		
		final FederationCatalogForTest fedCat = new FederationCatalogForTest();
		fedCat.addMember( "http://example.org", new TPFServerForTest() );

		final LogicalPlan plan = createLogicalPlan(queryString, fedCat);

		// tests
		assertEquals( 2, plan.numberOfSubPlans() );
		assertTrue( plan.getRootOperator() instanceof LogicalOpMultiwayLeftJoin );

		final LogicalPlan subplan1 = plan.getSubPlan(0); 
		assertEquals( 0, subplan1.numberOfSubPlans() );
		assertTrue( subplan1.getRootOperator() instanceof LogicalOpRequest<?,?> );

		final LogicalOpRequest<?,?> rootOp1 = (LogicalOpRequest<?,?>) subplan1.getRootOperator();
		assertTrue( rootOp1.getRequest() instanceof TriplePatternRequest );

		final TriplePatternRequest req1 = (TriplePatternRequest) rootOp1.getRequest();
		assertEqualTriplePatternsVUV( "x", "http://example.org/p", "y", req1 );

		final LogicalPlan subplan2 = plan.getSubPlan(1); 
		assertEquals( 0, subplan2.numberOfSubPlans() );
		assertTrue( subplan2.getRootOperator() instanceof LogicalOpRequest<?,?> );

		final LogicalOpRequest<?,?> rootOp2 = (LogicalOpRequest<?,?>) subplan2.getRootOperator();
		assertTrue( rootOp2.getRequest() instanceof TriplePatternRequest );

		final TriplePatternRequest req2 = (TriplePatternRequest) rootOp2.getRequest();
		assertEqualTriplePatternsVUV( "z", "http://example.org/q", "y", req2 );
	}

	@Test
	public void optionalInsideServiceSPARQL() throws SourcePlanningException {
		// setup
		final String queryString = "SELECT * WHERE {"
				+ "  SERVICE <http://example.org> {"
				+ "    { ?x <http://example.org/p> ?y } OPTIONAL { ?z <http://example.org/q> ?y }"
				+ "  }"
				+ "}";
		
		final FederationCatalogForTest fedCat = new FederationCatalogForTest();
		fedCat.addMember( "http://example.org", new SPARQLEndpointForTest() );

		final LogicalPlan plan = createLogicalPlan(queryString, fedCat);

		// tests
		assertEquals( 0, plan.numberOfSubPlans() );
		assertTrue( plan.getRootOperator() instanceof LogicalOpRequest<?,?> );

		final LogicalOpRequest<?,?> rootOp = (LogicalOpRequest<?,?>) plan.getRootOperator();
		assertTrue( rootOp.getRequest() instanceof SPARQLRequest );

		final SPARQLRequest req = (SPARQLRequest) rootOp.getRequest();
		assertTrue( req.getQueryPattern() instanceof GenericSPARQLGraphPatternImpl2 );

		final Op jenaOp = ( (GenericSPARQLGraphPatternImpl2) req.getQueryPattern() ).asJenaOp();
		assertTrue( jenaOp instanceof OpLeftJoin );
	}

	@Test
	public void optionalSequence() throws SourcePlanningException {
		// setup
		final String queryString = "SELECT * WHERE {"
				+ "  SERVICE <http://example.org> { ?x <http://example.org/p> ?y }"
				+ "  OPTIONAL"
				+ "  { SERVICE <http://example.org> { ?z <http://example.org/q> ?y } }"
				+ "  OPTIONAL"
				+ "  { SERVICE <http://example.org> { ?v <http://example.org/r> ?y } }"
				+ "}";
		
		final FederationCatalogForTest fedCat = new FederationCatalogForTest();
		fedCat.addMember( "http://example.org", new TPFServerForTest() );

		final LogicalPlan plan = createLogicalPlan(queryString, fedCat);

		// tests
		assertEquals( 3, plan.numberOfSubPlans() );
		assertTrue( plan.getRootOperator() instanceof LogicalOpMultiwayLeftJoin );

		final LogicalPlan subplan1 = plan.getSubPlan(0); 
		assertEquals( 0, subplan1.numberOfSubPlans() );
		assertTrue( subplan1.getRootOperator() instanceof LogicalOpRequest<?,?> );

		final LogicalOpRequest<?,?> rootOp1 = (LogicalOpRequest<?,?>) subplan1.getRootOperator();
		assertTrue( rootOp1.getRequest() instanceof TriplePatternRequest );

		final TriplePatternRequest req1 = (TriplePatternRequest) rootOp1.getRequest();
		assertEqualTriplePatternsVUV( "x", "http://example.org/p", "y", req1 );

		final LogicalPlan subplan2 = plan.getSubPlan(1); 
		assertEquals( 0, subplan2.numberOfSubPlans() );
		assertTrue( subplan2.getRootOperator() instanceof LogicalOpRequest<?,?> );

		final LogicalOpRequest<?,?> rootOp2 = (LogicalOpRequest<?,?>) subplan2.getRootOperator();
		assertTrue( rootOp2.getRequest() instanceof TriplePatternRequest );

		final TriplePatternRequest req2 = (TriplePatternRequest) rootOp2.getRequest();
		assertEqualTriplePatternsVUV( "z", "http://example.org/q", "y", req2 );

		final LogicalPlan subplan3 = plan.getSubPlan(2); 
		assertEquals( 0, subplan3.numberOfSubPlans() );
		assertTrue( subplan3.getRootOperator() instanceof LogicalOpRequest<?,?> );

		final LogicalOpRequest<?,?> rootOp3 = (LogicalOpRequest<?,?>) subplan3.getRootOperator();
		assertTrue( rootOp3.getRequest() instanceof TriplePatternRequest );

		final TriplePatternRequest req3 = (TriplePatternRequest) rootOp3.getRequest();
		assertEqualTriplePatternsVUV( "v", "http://example.org/r", "y", req3 );
	}


	@Test
	public void optionalNesting() throws SourcePlanningException {
		// setup
		final String queryString = "SELECT * WHERE {"
				+ "  SERVICE <http://example.org> { ?x <http://example.org/p> ?y }"
				+ "  OPTIONAL {"
				+ "    SERVICE <http://example.org> { ?z <http://example.org/q> ?y }"
				+ "    OPTIONAL"
				+ "    { SERVICE <http://example.org> { ?v <http://example.org/r> ?y } }"
				+ "  }"
				+ "}";
		
		final FederationCatalogForTest fedCat = new FederationCatalogForTest();
		fedCat.addMember( "http://example.org", new TPFServerForTest() );

		final LogicalPlan plan = createLogicalPlan(queryString, fedCat);

		// tests
		assertEquals( 2, plan.numberOfSubPlans() );
		assertTrue( plan.getRootOperator() instanceof LogicalOpMultiwayLeftJoin );

		final LogicalPlan subplan1 = plan.getSubPlan(0); 
		assertEquals( 0, subplan1.numberOfSubPlans() );
		assertTrue( subplan1.getRootOperator() instanceof LogicalOpRequest<?,?> );

		final LogicalOpRequest<?,?> rootOp1 = (LogicalOpRequest<?,?>) subplan1.getRootOperator();
		assertTrue( rootOp1.getRequest() instanceof TriplePatternRequest );

		final TriplePatternRequest req1 = (TriplePatternRequest) rootOp1.getRequest();
		assertEqualTriplePatternsVUV( "x", "http://example.org/p", "y", req1 );

		final LogicalPlan subplan2 = plan.getSubPlan(1); 
		assertEquals( 2, subplan2.numberOfSubPlans() );
		assertTrue( subplan2.getRootOperator() instanceof LogicalOpMultiwayLeftJoin );

		final LogicalOpRequest<?,?> rootOp2 = (LogicalOpRequest<?,?>) subplan2.getSubPlan(0).getRootOperator();
		assertTrue( rootOp2.getRequest() instanceof TriplePatternRequest );

		final TriplePatternRequest req2 = (TriplePatternRequest) rootOp2.getRequest();
		assertEqualTriplePatternsVUV( "z", "http://example.org/q", "y", req2 );

		final LogicalOpRequest<?,?> rootOp3 = (LogicalOpRequest<?,?>) subplan2.getSubPlan(1).getRootOperator();
		assertTrue( rootOp3.getRequest() instanceof TriplePatternRequest );

		final TriplePatternRequest req3 = (TriplePatternRequest) rootOp3.getRequest();
		assertEqualTriplePatternsVUV( "v", "http://example.org/r", "y", req3 );
	}


	// --------- helper functions ---------

	protected LogicalPlan createLogicalPlan( final String queryString,
	                                         final FederationCatalog fedCat )
				throws SourcePlanningException
	{
		final QueryProcContext ctxt = new QueryProcContext() {
			@Override public FederationCatalog getFederationCatalog() { return fedCat; }
			@Override public FederationAccessManager getFederationAccessMgr() { return null; }
			@Override public ExecutorService getExecutorServiceForPlanTasks() { return null; }
			@Override public CostModel getCostModel() { return null; }
			@Override public boolean isExperimentRun() { return false; }
		};

		final SourcePlanner sourcePlanner = new SourcePlannerImpl(ctxt);
		final Query query = new GenericSPARQLGraphPatternImpl1( QueryFactory.create(queryString).getQueryPattern() );
		return sourcePlanner.createSourceAssignment(query).object1;
	}

	public static void assertEqualTriplePatternsVUV( final String expectedSubjectVarName,
	                                                 final String expectedPredicateURI,
	                                                 final String expectedObjectVarName,
	                                                 final TriplePatternRequest actualTriplePatternRequest ) {
		assertEquals( 2, actualTriplePatternRequest.getQueryPattern().numberOfVars() );
		assertEqualTriplePatternsVUV( expectedSubjectVarName,
		                              expectedPredicateURI,
		                              expectedObjectVarName,
		                              actualTriplePatternRequest.getQueryPattern() );
	}

	public static void assertEqualTriplePatternsVUV( final String expectedSubjectVarName,
	                                                 final String expectedPredicateURI,
	                                                 final String expectedObjectVarName,
	                                                 final TriplePattern actualTriplePattern ) {
		assertEqualTriplePatternsVUV( expectedSubjectVarName,
		                              expectedPredicateURI,
		                              expectedObjectVarName,
		                              actualTriplePattern.asJenaTriple() );
	}

	public static void assertEqualTriplePatternsVUV( final String expectedSubjectVarName,
	                                                 final String expectedPredicateURI,
	                                                 final String expectedObjectVarName,
	                                                 final Triple actualTriplePattern ) {
		assertTrue( actualTriplePattern.getSubject().isVariable() );
		assertTrue( actualTriplePattern.getPredicate().isURI() );
		assertTrue( actualTriplePattern.getObject().isVariable() );
		assertEquals( expectedSubjectVarName, actualTriplePattern.getSubject().getName() );
		assertEquals( expectedPredicateURI, actualTriplePattern.getPredicate().getURI() );
		assertEquals( expectedObjectVarName, actualTriplePattern.getObject().getName() );
	}

}
