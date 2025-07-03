package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_IsIRI;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.junit.Test;

import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.base.query.impl.TriplePatternImpl;
import se.liu.ida.hefquin.engine.EngineTestBase;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanUtils;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpFilter;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayUnion;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithNullaryRootImpl;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithUnaryRootImpl;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.federation.access.impl.req.TriplePatternRequestImpl;

public class PushJoinUnderUnionWithRequestsTest extends EngineTestBase
{
	@Test
	public void pushJoinUnderUnionPossible1() {
		// a join of a SPARQL request and a union where the union contains two
		// triple pattern requests; the two triple pattern requests should be
		// turned into gpAdd operators with the SPARQL request as their input
		// and the union on top

		// set up
		final Var v1 = Var.alloc("x");
		final Var v2 = Var.alloc("y");
		final FederationMember fmA = new SPARQLEndpointForTest("http://exA.org");
		final FederationMember fmB = new TPFServerForTest();
		final FederationMember fmC = new TPFServerForTest();

		final TriplePattern tp1 = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp1 = new LogicalOpRequest<>( fmA, new SPARQLRequestImpl(tp1) );

		final TriplePattern tp2 = new TriplePatternImpl(v2 ,v2, v2);
		final LogicalOpRequest<?,?> reqOp2 = new LogicalOpRequest<>( fmB, new TriplePatternRequestImpl(tp2) );
		final LogicalOpRequest<?,?> reqOp3 = new LogicalOpRequest<>( fmC, new TriplePatternRequestImpl(tp2) );

		final LogicalPlan unionSubPlan = LogicalPlanUtils.createPlanWithBinaryUnion(
				new LogicalPlanWithNullaryRootImpl(reqOp2),
				new LogicalPlanWithNullaryRootImpl(reqOp3) );

		final LogicalPlan joinPlan = LogicalPlanUtils.createPlanWithBinaryJoin(
				new LogicalPlanWithNullaryRootImpl(reqOp1),
				unionSubPlan );

		// test
		final LogicalPlan result = new PushJoinUnderUnionWithRequests().apply(joinPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpMultiwayUnion );
		assertEquals( 2, result.numberOfSubPlans() );

		final LogicalPlan subResult1 = result.getSubPlan(0);
		final LogicalPlan subResult2 = result.getSubPlan(1);
		assertTrue( subResult1.getRootOperator() instanceof LogicalOpGPAdd );
		assertTrue( subResult2.getRootOperator() instanceof LogicalOpGPAdd );

		final LogicalOpGPAdd gpAddOp1 = (LogicalOpGPAdd) subResult1.getRootOperator();
		final LogicalOpGPAdd gpAddOp2 = (LogicalOpGPAdd) subResult2.getRootOperator();

		assertTrue( gpAddOp1.getTP().equals(tp2) );
		assertTrue( gpAddOp2.getTP().equals(tp2) );
		assertTrue( gpAddOp1.getFederationMember().equals(fmB) || gpAddOp1.getFederationMember().equals(fmC) );
		assertTrue( gpAddOp2.getFederationMember().equals(fmB) || gpAddOp2.getFederationMember().equals(fmC) );
		assertTrue( gpAddOp1.getFederationMember() != gpAddOp2.getFederationMember() );

		final LogicalPlan subsubResult1 = subResult1.getSubPlan(0);
		final LogicalPlan subsubResult2 = subResult2.getSubPlan(0);

		assertTrue( subsubResult1.getRootOperator() instanceof LogicalOpRequest );
		assertTrue( subsubResult2.getRootOperator() instanceof LogicalOpRequest );
		assertTrue( subsubResult1 == subsubResult2 );
	}

	@Test
	public void pushJoinUnderUnionImpossible() {
		// a join of a union and a SPARQL request where the union
		// contains two triple pattern requests (i.e., almost the
		// same as in pushJoinUnderUnionPossible1, but the two
		// subplans under the join have the reverse order);
		// since the union is the first input to the join, the
		// join cannot be pushed

		// set up
		final Var v1 = Var.alloc("x");
		final Var v2 = Var.alloc("y");
		final FederationMember fmA = new SPARQLEndpointForTest("http://exA.org");
		final FederationMember fmB = new TPFServerForTest();
		final FederationMember fmC = new TPFServerForTest();

		final TriplePattern tp1 = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp1 = new LogicalOpRequest<>( fmA, new SPARQLRequestImpl(tp1) );

		final TriplePattern tp2 = new TriplePatternImpl(v2 ,v2, v2);
		final LogicalOpRequest<?,?> reqOp2 = new LogicalOpRequest<>( fmB, new TriplePatternRequestImpl(tp2) );
		final LogicalOpRequest<?,?> reqOp3 = new LogicalOpRequest<>( fmC, new TriplePatternRequestImpl(tp2) );

		final LogicalPlan unionSubPlan = LogicalPlanUtils.createPlanWithBinaryUnion(
				new LogicalPlanWithNullaryRootImpl(reqOp2),
				new LogicalPlanWithNullaryRootImpl(reqOp3) );

		final LogicalPlan joinPlan = LogicalPlanUtils.createPlanWithBinaryJoin(
				unionSubPlan,
				new LogicalPlanWithNullaryRootImpl(reqOp1) );

		// test
		final LogicalPlan result = new PushJoinUnderUnionWithRequests().apply(joinPlan);

		// check
		assertTrue( result.equals(joinPlan) );
	}

	@Test
	public void pushJoinUnderUnionWithFilter() {
		// like pushJoinUnderUnionPossible1 but with filters under the union

		// set up
		final Var v1 = Var.alloc("x");
		final Var v2 = Var.alloc("y");
		final FederationMember fmA = new SPARQLEndpointForTest("http://exA.org");
		final FederationMember fmB = new TPFServerForTest();
		final FederationMember fmC = new TPFServerForTest();

		final TriplePattern tp1 = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp1 = new LogicalOpRequest<>( fmA, new SPARQLRequestImpl(tp1) );

		final TriplePattern tp2 = new TriplePatternImpl(v2 ,v2, v2);
		final LogicalOpRequest<?,?> reqOp2 = new LogicalOpRequest<>( fmB, new TriplePatternRequestImpl(tp2) );
		final LogicalOpRequest<?,?> reqOp3 = new LogicalOpRequest<>( fmC, new TriplePatternRequestImpl(tp2) );

		final Expr e = new E_IsIRI( new ExprVar(v2) );
		final LogicalOpFilter filterOp = new LogicalOpFilter(e);
		
		final LogicalPlan unionSubPlan = LogicalPlanUtils.createPlanWithBinaryUnion(
				new LogicalPlanWithUnaryRootImpl(filterOp, new LogicalPlanWithNullaryRootImpl(reqOp2)),
				new LogicalPlanWithUnaryRootImpl(filterOp, new LogicalPlanWithNullaryRootImpl(reqOp3)) );

		final LogicalPlan joinPlan = LogicalPlanUtils.createPlanWithBinaryJoin(
				new LogicalPlanWithNullaryRootImpl(reqOp1),
				unionSubPlan );

		// test
		final LogicalPlan result = new PushJoinUnderUnionWithRequests().apply(joinPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpMultiwayUnion );
		assertEquals( 2, result.numberOfSubPlans() );

		final LogicalPlan subResult1 = result.getSubPlan(0);
		final LogicalPlan subResult2 = result.getSubPlan(1);
		assertTrue( subResult1.getRootOperator().equals(filterOp) );
		assertTrue( subResult2.getRootOperator().equals(filterOp) );

		final LogicalPlan subsubResult1 = subResult1.getSubPlan(0);
		final LogicalPlan subsubResult2 = subResult2.getSubPlan(0);
		assertTrue( subsubResult1.getRootOperator() instanceof LogicalOpGPAdd );
		assertTrue( subsubResult2.getRootOperator() instanceof LogicalOpGPAdd );

		final LogicalOpGPAdd gpAddOp1 = (LogicalOpGPAdd) subsubResult1.getRootOperator();
		final LogicalOpGPAdd gpAddOp2 = (LogicalOpGPAdd) subsubResult2.getRootOperator();

		assertTrue( gpAddOp1.getTP().equals(tp2) );
		assertTrue( gpAddOp2.getTP().equals(tp2) );
		assertTrue( gpAddOp1.getFederationMember().equals(fmB) || gpAddOp1.getFederationMember().equals(fmC) );
		assertTrue( gpAddOp2.getFederationMember().equals(fmB) || gpAddOp2.getFederationMember().equals(fmC) );
		assertTrue( gpAddOp1.getFederationMember() != gpAddOp2.getFederationMember() );

		final LogicalPlan sssResult1 = subsubResult1.getSubPlan(0);
		final LogicalPlan sssResult2 = subsubResult2.getSubPlan(0);

		assertTrue( sssResult1.getRootOperator() instanceof LogicalOpRequest );
		assertTrue( sssResult2.getRootOperator() instanceof LogicalOpRequest );
		assertTrue( sssResult1 == sssResult2 );
	}

	@Test
	public void pushJoinUnderUnionPossible2() {
		// a join of a SPARQL request and a union where the union contains two
		// gpAdd operators; the two gpAdd operators should be joined with the
		// with the SPARQL request, respectively, with the union on top

		// set up
		final Var v1 = Var.alloc("x");
		final Var v2 = Var.alloc("y");
		final Var v3 = Var.alloc("z");
		final FederationMember fmA = new TPFServerForTest();
		final FederationMember fmB = new TPFServerForTest();
		final FederationMember fmC = new TPFServerForTest();
		final FederationMember fmD = new SPARQLEndpointForTest("http://exD.org");

		final TriplePattern tp1 = new TriplePatternImpl(v1, v1, v1);
		final TriplePattern tp2 = new TriplePatternImpl(v2 ,v2, v2);
		final TriplePattern tp3 = new TriplePatternImpl(v3 ,v3, v3);
		final TriplePattern tp4 = new TriplePatternImpl(v1 ,v2, v3);

		final LogicalOpRequest<?,?> reqOp1 = new LogicalOpRequest<>( fmA, new TriplePatternRequestImpl(tp1) );
		final LogicalOpRequest<?,?> reqOp2 = new LogicalOpRequest<>( fmB, new TriplePatternRequestImpl(tp2) );
		final LogicalOpRequest<?,?> reqOp3 = new LogicalOpRequest<>( fmC, new TriplePatternRequestImpl(tp3) );

		final LogicalPlan gpAddPlan1 = new LogicalPlanWithUnaryRootImpl( new LogicalOpGPAdd(fmD, tp4),
		                                                                 new LogicalPlanWithNullaryRootImpl(reqOp1) );

		final LogicalPlan gpAddPlan2 = new LogicalPlanWithUnaryRootImpl( new LogicalOpGPAdd(fmD, tp4),
		                                                                 new LogicalPlanWithNullaryRootImpl(reqOp2) );

		final LogicalPlan unionSubPlan = LogicalPlanUtils.createPlanWithBinaryUnion(gpAddPlan1, gpAddPlan2);

		final LogicalPlan reqPlan = new LogicalPlanWithNullaryRootImpl(reqOp3);

		final LogicalPlan joinPlan = LogicalPlanUtils.createPlanWithBinaryJoin(reqPlan, unionSubPlan);

		// test
		final LogicalPlan result = new PushJoinUnderUnionWithRequests().apply(joinPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpMultiwayUnion );
		assertEquals( 2, result.numberOfSubPlans() );

		final LogicalPlan subResult1 = result.getSubPlan(0);
		final LogicalPlan subResult2 = result.getSubPlan(1);
		assertTrue( subResult1.getRootOperator() instanceof LogicalOpMultiwayJoin );
		assertTrue( subResult2.getRootOperator() instanceof LogicalOpMultiwayJoin );

		assertEquals( 2, subResult1.numberOfSubPlans() );
		assertEquals( 2, subResult2.numberOfSubPlans() );

		assertTrue( subResult1.getSubPlan(0).equals(reqPlan) );
		assertTrue( subResult2.getSubPlan(0).equals(reqPlan) );

		assertTrue( subResult1.getSubPlan(1).equals(gpAddPlan1) || subResult1.getSubPlan(1).equals(gpAddPlan2) );
		if ( subResult1.getSubPlan(1).equals(gpAddPlan1) ) {
			assertTrue( subResult2.getSubPlan(1).equals(gpAddPlan2) );
		}
		else {
			assertTrue( subResult2.getSubPlan(1).equals(gpAddPlan1) );
		}
	}

	@Test
	public void pushJoinUnderUnionTwice() {
		// a join of a SPARQL request and two unions where both unions contain
		// two triple pattern requests each; the two pairs of triple pattern
		// requests should be turned into pairs gpAdd operators, with the
		// SPARQL request as the input to the first pair, a union on top,
		// and that union input to the next pair, plus a final union on top

		// set up
		final Var v1 = Var.alloc("x");
		final Var v2 = Var.alloc("y");
		final Var v3 = Var.alloc("z");
		final FederationMember fmA = new SPARQLEndpointForTest("http://exA.org");
		final FederationMember fmB = new TPFServerForTest();
		final FederationMember fmC = new TPFServerForTest();

		final TriplePattern tp1 = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp1 = new LogicalOpRequest<>( fmA, new SPARQLRequestImpl(tp1) );

		final TriplePattern tp2 = new TriplePatternImpl(v2 ,v2, v2);
		final LogicalOpRequest<?,?> reqOp2B = new LogicalOpRequest<>( fmB, new TriplePatternRequestImpl(tp2) );
		final LogicalOpRequest<?,?> reqOp2C = new LogicalOpRequest<>( fmC, new TriplePatternRequestImpl(tp2) );

		final TriplePattern tp3 = new TriplePatternImpl(v3 ,v3, v3);
		final LogicalOpRequest<?,?> reqOp3B = new LogicalOpRequest<>( fmB, new TriplePatternRequestImpl(tp3) );
		final LogicalOpRequest<?,?> reqOp3C = new LogicalOpRequest<>( fmC, new TriplePatternRequestImpl(tp3) );

		final LogicalPlan unionSubPlan1 = LogicalPlanUtils.createPlanWithBinaryUnion(
				new LogicalPlanWithNullaryRootImpl(reqOp2B),
				new LogicalPlanWithNullaryRootImpl(reqOp2C) );

		final LogicalPlan unionSubPlan2 = LogicalPlanUtils.createPlanWithBinaryUnion(
				new LogicalPlanWithNullaryRootImpl(reqOp3B),
				new LogicalPlanWithNullaryRootImpl(reqOp3C) );

		final LogicalPlan reqPlan = new LogicalPlanWithNullaryRootImpl(reqOp1);

		final LogicalPlan joinPlan = LogicalPlanUtils.createPlanWithMultiwayJoin(
				reqPlan,
				unionSubPlan1,
				unionSubPlan2 );

		// test
		final LogicalPlan result = new PushJoinUnderUnionWithRequests().apply(joinPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpMultiwayUnion );
		assertEquals( 2, result.numberOfSubPlans() );

		final LogicalPlan subResult1 = result.getSubPlan(0);
		final LogicalPlan subResult2 = result.getSubPlan(1);
		assertTrue( subResult1.getRootOperator() instanceof LogicalOpGPAdd );
		assertTrue( subResult2.getRootOperator() instanceof LogicalOpGPAdd );

		final LogicalOpGPAdd gpAddOp1 = (LogicalOpGPAdd) subResult1.getRootOperator();
		final LogicalOpGPAdd gpAddOp2 = (LogicalOpGPAdd) subResult2.getRootOperator();

		assertTrue( gpAddOp1.getTP().equals(tp3) );
		assertTrue( gpAddOp2.getTP().equals(tp3) );

		final LogicalPlan subsubResult = subResult1.getSubPlan(0);
		assertTrue( subsubResult.equals( subResult2.getSubPlan(0) ) );
		assertTrue( subsubResult.getRootOperator() instanceof LogicalOpMultiwayUnion );
		assertEquals( 2, subsubResult.numberOfSubPlans() );

		final LogicalPlan sssResult1 = subsubResult.getSubPlan(0);
		final LogicalPlan sssResult2 = subsubResult.getSubPlan(1);
		assertTrue( sssResult1.getRootOperator() instanceof LogicalOpGPAdd );
		assertTrue( sssResult2.getRootOperator() instanceof LogicalOpGPAdd );

		final LogicalOpGPAdd gpAddOp1_1 = (LogicalOpGPAdd) sssResult1.getRootOperator();
		final LogicalOpGPAdd gpAddOp2_2 = (LogicalOpGPAdd) sssResult2.getRootOperator();

		assertTrue( gpAddOp1_1.getTP().equals(tp2) );
		assertTrue( gpAddOp2_2.getTP().equals(tp2) );

		assertTrue( sssResult1.getSubPlan(0).equals(reqPlan) );
		assertTrue( sssResult2.getSubPlan(0).equals(reqPlan) );
	}

}
