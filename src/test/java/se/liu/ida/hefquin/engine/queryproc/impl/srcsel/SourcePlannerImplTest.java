package se.liu.ida.hefquin.engine.queryproc.impl.srcsel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.jena.graph.Triple;
import org.apache.jena.query.QueryFactory;
import org.junit.Test;

import se.liu.ida.hefquin.engine.EngineTestBase;
import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.TPFServer;
import se.liu.ida.hefquin.engine.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.engine.federation.catalog.FederationCatalog;
import se.liu.ida.hefquin.engine.query.Query;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.SPARQLGraphPatternImpl;
import se.liu.ida.hefquin.engine.queryplan.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;
import se.liu.ida.hefquin.engine.queryproc.SourcePlanner;

public class SourcePlannerImplTest extends EngineTestBase
{
	@Test
	public void oneTPFoneTriplePattern() {
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
	public void oneBRTPFtwoTriplePatterns() {
		// setup
		final String queryString = "SELECT * WHERE {"
				+ "SERVICE <http://example.org> { ?x <http://example.org/p1> ?y; <http://example.org/p2> ?z }"
				+ "}";
		
		final FederationCatalogForTest fedCat = new FederationCatalogForTest();
		fedCat.addMember( "http://example.org", new BRTPFServerForTest() );

		final LogicalPlan plan = createLogicalPlan(queryString, fedCat);

		// tests
		assertEquals( 1, plan.numberOfSubPlans() );
		assertTrue( plan.getRootOperator() instanceof LogicalOpTPAdd );

		final LogicalOpTPAdd rootOp = (LogicalOpTPAdd) plan.getRootOperator();
		assertTrue( rootOp.getFederationMember() instanceof BRTPFServer );

		final Triple firstTP = rootOp.getTP().asJenaTriple();
		final boolean firstPredicateWasP1;
		if ( firstTP.getPredicate().getURI().equals("http://example.org/p1") ) {
			assertEqualTriplePatternsVUV( "x", "http://example.org/p1", "y", rootOp.getTP() );
			firstPredicateWasP1 = true;
		} else {
			assertEqualTriplePatternsVUV( "x", "http://example.org/p2", "z", rootOp.getTP() );
			firstPredicateWasP1 = false;
		}

		final LogicalPlan subplan = plan.getSubPlan(0); 
		assertEquals( 0, subplan.numberOfSubPlans() );
		assertTrue( subplan.getRootOperator() instanceof LogicalOpRequest<?,?> );

		final LogicalOpRequest<?,?> subRootOp = (LogicalOpRequest<?,?>) subplan.getRootOperator();
		assertTrue( subRootOp.getFederationMember() instanceof BRTPFServer );
		assertTrue( subRootOp.getRequest() instanceof TriplePatternRequest );

		final TriplePatternRequest req = (TriplePatternRequest) subRootOp.getRequest();
		if ( firstPredicateWasP1 ) {
			assertEqualTriplePatternsVUV( "x", "http://example.org/p2", "z", req );
		} else {
			assertEqualTriplePatternsVUV( "x", "http://example.org/p1", "y", req );
		}
	}

	@Test
	public void twoBRTPFtwoTriplePatterns() {
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
		assertTrue( plan.getRootOperator() instanceof LogicalOpJoin );

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


	// --------- helper functions ---------

	protected LogicalPlan createLogicalPlan( final String queryString,
	                                         final FederationCatalog fedCat ) {
		final QueryProcContext ctxt = new QueryProcContext() {
			@Override public FederationCatalog getFederationCatalog() { return fedCat; }
			@Override public FederationAccessManager getFederationAccessMgr() { return null; }
		};

		final SourcePlanner sourcePlanner = new SourcePlannerImpl(ctxt);
		final Query query = new SPARQLGraphPatternImpl( QueryFactory.create(queryString).getQueryPattern() );
		return sourcePlanner.createSourceAssignment(query);
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
