package se.liu.ida.hefquin.engine.queryproc.impl.srcsel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayUnion;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;
import se.liu.ida.hefquin.engine.queryproc.SourcePlanner;
import se.liu.ida.hefquin.engine.queryproc.SourcePlanningException;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.federation.catalog.impl.FederationCatalogImpl;

public class ExhaustiveSourcePlannerImplTest extends SourcePlannerImplTestBase
{
	@Test
	public void oneFedMemberOneTriplePattern() throws SourcePlanningException {
		// setup
		final String queryString = "SELECT * WHERE {"
				+ "  ?x <http://example.org/p> ?y"
				+ "}";

		final FederationCatalogImpl fedCat = new FederationCatalogImpl();

		final FederationMember fm = new TPFServerForTest();
		fedCat.addMember("http://example.org", fm);

		final LogicalPlan plan = createLogicalPlan(queryString, fedCat);

		// tests
		assertEquals( 0, plan.numberOfSubPlans() );
		assertTrue( plan.getRootOperator() instanceof LogicalOpRequest<?,?> );

		final LogicalOpRequest<?,?> rootOp = (LogicalOpRequest<?,?>) plan.getRootOperator();
		assertTrue( rootOp.getFederationMember() == fm );
		assertTrue( rootOp.getRequest() instanceof TriplePatternRequest );

		final TriplePatternRequest req = (TriplePatternRequest) rootOp.getRequest();
		assertEqualTriplePatternsVUV( "x", "http://example.org/p", "y", req );
	}

	@Test
	public void twoFedMembersOneTriplePattern() throws SourcePlanningException {
		// setup
		final String queryString = "SELECT * WHERE {"
				+ "  ?x <http://example.org/p> ?y"
				+ "}";

		final FederationCatalogImpl fedCat = new FederationCatalogImpl();

		final FederationMember fm1 = new TPFServerForTest();
		fedCat.addMember("http://example1.org", fm1);

		final FederationMember fm2 = new TPFServerForTest();
		fedCat.addMember("http://example2.org", fm2);

		final LogicalPlan plan = createLogicalPlan(queryString, fedCat);

		// tests
		assertTrue( plan.getRootOperator() instanceof LogicalOpMultiwayUnion );
		assertEquals( 2, plan.numberOfSubPlans() );

		final LogicalPlan subPlan1 = plan.getSubPlan(0);
		final LogicalPlan subPlan2 = plan.getSubPlan(1);

		assertTrue( subPlan1.getRootOperator() instanceof LogicalOpRequest<?,?> );
		assertTrue( subPlan2.getRootOperator() instanceof LogicalOpRequest<?,?> );

		final LogicalOpRequest<?,?> subPlan1Root = (LogicalOpRequest<?,?>) subPlan1.getRootOperator();
		final LogicalOpRequest<?,?> subPlan2Root = (LogicalOpRequest<?,?>) subPlan2.getRootOperator();

		assertTrue( subPlan1Root.getFederationMember() != subPlan2Root.getFederationMember() );
		assertTrue( subPlan1Root.getFederationMember() == fm1 || subPlan1Root.getFederationMember() == fm2 );
		assertTrue( subPlan2Root.getFederationMember() == fm1 || subPlan2Root.getFederationMember() == fm2 );

		final TriplePatternRequest req1 = (TriplePatternRequest) subPlan1Root.getRequest();
		final TriplePatternRequest req2 = (TriplePatternRequest) subPlan1Root.getRequest();
		assertEqualTriplePatternsVUV( "x", "http://example.org/p", "y", req1 );
		assertEqualTriplePatternsVUV( "x", "http://example.org/p", "y", req2 );
	}

	@Test
	public void oneFedMemberTwoTriplePattern() throws SourcePlanningException {
		// setup
		final String queryString = "SELECT * WHERE {"
				+ "  ?x <http://example.org/p1> ?y ."
				+ "  ?x <http://example.org/p2> ?y ."
				+ "}";

		final FederationCatalogImpl fedCat = new FederationCatalogImpl();

		final FederationMember fm = new TPFServerForTest();
		fedCat.addMember("http://example.org", fm);

		final LogicalPlan plan = createLogicalPlan(queryString, fedCat);

		// tests
		assertTrue( plan.getRootOperator() instanceof LogicalOpMultiwayJoin );
		assertEquals( 2, plan.numberOfSubPlans() );

		final LogicalPlan subPlan1 = plan.getSubPlan(0);
		final LogicalPlan subPlan2 = plan.getSubPlan(1);

		assertTrue( subPlan1.getRootOperator() instanceof LogicalOpRequest<?,?> );
		assertTrue( subPlan2.getRootOperator() instanceof LogicalOpRequest<?,?> );

		final LogicalOpRequest<?,?> subPlan1Root = (LogicalOpRequest<?,?>) subPlan1.getRootOperator();
		final LogicalOpRequest<?,?> subPlan2Root = (LogicalOpRequest<?,?>) subPlan2.getRootOperator();

		assertTrue( subPlan1Root.getFederationMember() == fm );
		assertTrue( subPlan2Root.getFederationMember() == fm );

		final TriplePatternRequest req1 = (TriplePatternRequest) subPlan1Root.getRequest();
		final TriplePatternRequest req2 = (TriplePatternRequest) subPlan2Root.getRequest();
		assertTrue( ! req1.getQueryPattern().equals(req2.getQueryPattern()) );

		final String p1 = req1.getQueryPattern().asJenaTriple().getPredicate().getURI();
		final String p2 = req2.getQueryPattern().asJenaTriple().getPredicate().getURI();

		assertTrue( ! p1.equals(p2) );
		assertTrue( p1.equals("http://example.org/p1") || p1.equals("http://example.org/p2") );
		assertTrue( p2.equals("http://example.org/p1") || p2.equals("http://example.org/p2") );
	}

	@Test
	public void twoFedMembersTwoTriplePattern() throws SourcePlanningException {
		// setup
		final String queryString = "SELECT * WHERE {"
				+ "  ?x <http://example.org/p1> ?y ."
				+ "  ?x <http://example.org/p2> ?y ."
				+ "}";

		final FederationCatalogImpl fedCat = new FederationCatalogImpl();

		final FederationMember fm1 = new TPFServerForTest();
		fedCat.addMember("http://example1.org", fm1);

		final FederationMember fm2 = new TPFServerForTest();
		fedCat.addMember("http://example2.org", fm2);

		final LogicalPlan plan = createLogicalPlan(queryString, fedCat);

		// tests
		assertTrue( plan.getRootOperator() instanceof LogicalOpMultiwayJoin );
		assertEquals( 2, plan.numberOfSubPlans() );

		final LogicalPlan subPlan1 = plan.getSubPlan(0);
		final LogicalPlan subPlan2 = plan.getSubPlan(1);

		assertTrue( subPlan1.getRootOperator() instanceof LogicalOpMultiwayUnion );
		assertTrue( subPlan2.getRootOperator() instanceof LogicalOpMultiwayUnion );
		assertEquals( 2, subPlan1.numberOfSubPlans() );
		assertEquals( 2, subPlan2.numberOfSubPlans() );

		final LogicalPlan subsubPlan1_1 = subPlan1.getSubPlan(0);
		final LogicalPlan subsubPlan1_2 = subPlan1.getSubPlan(1);
		final LogicalPlan subsubPlan2_1 = subPlan2.getSubPlan(0);
		final LogicalPlan subsubPlan2_2 = subPlan2.getSubPlan(1);

		assertTrue( subsubPlan1_1.getRootOperator() instanceof LogicalOpRequest<?,?> );
		assertTrue( subsubPlan1_2.getRootOperator() instanceof LogicalOpRequest<?,?> );
		assertTrue( subsubPlan2_1.getRootOperator() instanceof LogicalOpRequest<?,?> );
		assertTrue( subsubPlan2_2.getRootOperator() instanceof LogicalOpRequest<?,?> );

		final LogicalOpRequest<?,?> subsubPlan1_1Root = (LogicalOpRequest<?,?>) subsubPlan1_1.getRootOperator();
		final LogicalOpRequest<?,?> subsubPlan1_2Root = (LogicalOpRequest<?,?>) subsubPlan1_2.getRootOperator();
		final LogicalOpRequest<?,?> subsubPlan2_1Root = (LogicalOpRequest<?,?>) subsubPlan2_1.getRootOperator();
		final LogicalOpRequest<?,?> subsubPlan2_2Root = (LogicalOpRequest<?,?>) subsubPlan2_2.getRootOperator();

		assertTrue( subsubPlan1_1Root.getFederationMember() != subsubPlan1_2Root.getFederationMember() );
		assertTrue( subsubPlan1_1Root.getFederationMember() == fm1 || subsubPlan1_1Root.getFederationMember() == fm2 );
		assertTrue( subsubPlan1_2Root.getFederationMember() == fm1 || subsubPlan1_2Root.getFederationMember() == fm2 );

		assertTrue( subsubPlan2_1Root.getFederationMember() != subsubPlan2_2Root.getFederationMember() );
		assertTrue( subsubPlan2_1Root.getFederationMember() == fm1 || subsubPlan2_1Root.getFederationMember() == fm2 );
		assertTrue( subsubPlan2_2Root.getFederationMember() == fm1 || subsubPlan2_2Root.getFederationMember() == fm2 );

		final TriplePatternRequest req1_1 = (TriplePatternRequest) subsubPlan1_1Root.getRequest();
		final TriplePatternRequest req1_2 = (TriplePatternRequest) subsubPlan1_2Root.getRequest();
		assertTrue( req1_1.getQueryPattern().equals(req1_2.getQueryPattern()) );

		final TriplePatternRequest req2_1 = (TriplePatternRequest) subsubPlan2_1Root.getRequest();
		final TriplePatternRequest req2_2 = (TriplePatternRequest) subsubPlan2_2Root.getRequest();
		assertTrue( req2_1.getQueryPattern().equals(req2_2.getQueryPattern()) );

		final String p1 = req1_1.getQueryPattern().asJenaTriple().getPredicate().getURI();
		final String p2 = req2_1.getQueryPattern().asJenaTriple().getPredicate().getURI();

		assertTrue( ! p1.equals(p2) );
		assertTrue( p1.equals("http://example.org/p1") || p1.equals("http://example.org/p2") );
		assertTrue( p2.equals("http://example.org/p1") || p2.equals("http://example.org/p2") );
	}


	// --------- helper functions ---------

	@Override
	protected SourcePlanner createSourcePlanner( final QueryProcContext ctxt )
	{
		return new ExhaustiveSourcePlannerImpl(ctxt);
	}

}
