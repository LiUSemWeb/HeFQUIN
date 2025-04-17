package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.E_Bound;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_IsIRI;
import org.apache.jena.sparql.expr.E_LogicalNot;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.junit.Test;

import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.base.query.impl.QueryPatternUtils;
import se.liu.ida.hefquin.base.query.impl.TriplePatternImpl;
import se.liu.ida.hefquin.engine.EngineTestBase;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.engine.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.req.TriplePatternRequestImpl;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanUtils;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBind;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpFilter;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayUnion;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPOptAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithNullaryRootImpl;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithUnaryRootImpl;

public class FilterPushDownTest extends EngineTestBase
{
	@Test
	public void pushFilterUnderJoinPossible() {
		// a filter on top of a join of two triple pattern requests,
		// where the filter can be pushed to both requests and even
		// be merged into the first request, but not into the second

		// set up
		final Var v1 = Var.alloc("x");
		final Var v2 = Var.alloc("y");
		final FederationMember fmA = new SPARQLEndpointForTest("http://exA.org");
		final FederationMember fmB = new TPFServerForTest();

		final TriplePattern tp1 = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp1 = new LogicalOpRequest<>( fmA, new SPARQLRequestImpl(tp1) );

		final TriplePattern tp2 = new TriplePatternImpl(v1 ,v2, v2);
		final LogicalOpRequest<?,?> reqOp2 = new LogicalOpRequest<>( fmB, new TriplePatternRequestImpl(tp2) );

		final LogicalPlan joinSubPlan = LogicalPlanUtils.createPlanWithBinaryJoin(
				new LogicalPlanWithNullaryRootImpl(reqOp1),
				new LogicalPlanWithNullaryRootImpl(reqOp2) );

		final Expr e = new E_IsIRI( new ExprVar(v1) );
		final UnaryLogicalOp rootOp = new LogicalOpFilter(e);
		final LogicalPlan filterPlan = new LogicalPlanWithUnaryRootImpl(rootOp, joinSubPlan);

		// test
		final LogicalPlan result = new FilterPushDown().apply(filterPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpJoin );

		final LogicalPlan subResult1 = result.getSubPlan(0);
		assertTrue( subResult1.getRootOperator() instanceof LogicalOpRequest<?,?> );

		final LogicalOpRequest<?,?> resultReqOp1 = (LogicalOpRequest<?,?>) subResult1.getRootOperator();
		assertTrue( resultReqOp1.getFederationMember() == fmA );

		final SPARQLRequest resultReq1 = (SPARQLRequest) resultReqOp1.getRequest();
		final Element resultElmt1 = QueryPatternUtils.convertToJenaElement( resultReq1.getQueryPattern() );
		assertTrue(                                        resultElmt1 instanceof ElementGroup );
		assertTrue(                        ((ElementGroup) resultElmt1).get(0) instanceof ElementTriplesBlock );
		assertTrue( ((ElementTriplesBlock) ((ElementGroup) resultElmt1).get(0)).getPattern().get(0).equals(tp1.asJenaTriple()) );
		assertTrue(                        ((ElementGroup) resultElmt1).get(1) instanceof ElementFilter );
		assertTrue(       ((ElementFilter) ((ElementGroup) resultElmt1).get(1)).getExpr().equals(e) );

		final LogicalPlan subResult2 = result.getSubPlan(1);
		assertTrue( subResult2.getRootOperator() instanceof LogicalOpFilter );

		final LogicalOpFilter resultFilterOp = (LogicalOpFilter) subResult2.getRootOperator();
		assertEquals( 1, resultFilterOp.getFilterExpressions().size() );
		assertEquals( e, resultFilterOp.getFilterExpressions().get(0) );

		final LogicalPlan subsubResult2 = subResult2.getSubPlan(0);
		assertTrue( subsubResult2.getRootOperator() instanceof LogicalOpRequest<?,?> );

		final LogicalOpRequest<?,?> resultReqOp2 = (LogicalOpRequest<?,?>) subsubResult2.getRootOperator();
		assertTrue( resultReqOp2.getFederationMember() == fmB );
		assertTrue( ((TriplePatternRequest) resultReqOp2.getRequest()).getQueryPattern().equals(tp2) );
	}

	@Test
	public void pushFilterUnderJoinImpossible() {
		// a filter on top of a join of two triple pattern requests,
		// where the filter cannot be pushed to any of the two requests

		// set up
		final Var v1 = Var.alloc("x");
		final Var v2 = Var.alloc("y");
		final Var v3 = Var.alloc("z");
		final FederationMember fm = new SPARQLEndpointForTest("http://exA.org");

		final TriplePattern tp1 = new TriplePatternImpl(v3, v1, v1);
		final LogicalOpRequest<?,?> reqOp1 = new LogicalOpRequest<>( fm, new SPARQLRequestImpl(tp1) );

		final TriplePattern tp2 = new TriplePatternImpl(v3 ,v2, v2);
		final LogicalOpRequest<?,?> reqOp2 = new LogicalOpRequest<>( fm, new SPARQLRequestImpl(tp2) );

		final LogicalPlan joinSubPlan = LogicalPlanUtils.createPlanWithBinaryJoin(
				new LogicalPlanWithNullaryRootImpl(reqOp1),
				new LogicalPlanWithNullaryRootImpl(reqOp2) );

		final Expr e = new E_Equals( new ExprVar(v1), new ExprVar(v2) );
		final UnaryLogicalOp rootOp = new LogicalOpFilter(e);
		final LogicalPlan filterPlan = new LogicalPlanWithUnaryRootImpl(rootOp, joinSubPlan);

		// test
		final LogicalPlan result = new FilterPushDown().apply(filterPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpFilter );

		final LogicalOpFilter resultFilterOp = (LogicalOpFilter) result.getRootOperator();
		assertEquals( 1, resultFilterOp.getFilterExpressions().size() );
		assertEquals( e, resultFilterOp.getFilterExpressions().get(0) );

		assertTrue( result.getSubPlan(0).equals(joinSubPlan) );
	}

	@Test
	public void pushFilterOverFilterUnderTPOptAdd() {
		// a filter on top of another filter on top of a tpOptAdd operator,
		// where (only) the top filter cannot be pushed under the tpOptAdd;
		// hence, the second filter becomes the root

		// set up
		final Var v1 = Var.alloc("x");
		final Var v2 = Var.alloc("y");
		final FederationMember fm = new TPFServerForTest();

		final TriplePattern tp1 = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>( fm, new TriplePatternRequestImpl(tp1) );
		final LogicalPlan reqSubPlan = new LogicalPlanWithNullaryRootImpl(reqOp);

		final TriplePattern tp2 = new TriplePatternImpl(v1 ,v2, v2);
		final LogicalOpTPOptAdd tpOptAdd = new LogicalOpTPOptAdd(fm, tp2);
		final LogicalPlan tpOptAddSubPlan = new LogicalPlanWithUnaryRootImpl(tpOptAdd, reqSubPlan );

		final Expr e1 = new E_LogicalNot( new E_Bound(new ExprVar(v2)) );
		final LogicalOpFilter filterOp1 = new LogicalOpFilter(e1);
		final LogicalPlan filterSubPlan = new LogicalPlanWithUnaryRootImpl(filterOp1, tpOptAddSubPlan);

		final Expr e2 = new E_IsIRI( new ExprVar(v1) );
		final LogicalOpFilter rootOp = new LogicalOpFilter(e2);
		final LogicalPlan filterPlan = new LogicalPlanWithUnaryRootImpl(rootOp, filterSubPlan);

		// test
		final LogicalPlan result = new FilterPushDown().apply(filterPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpFilter );

		final LogicalOpFilter resultFilterOp = (LogicalOpFilter) result.getRootOperator();
		assertEquals( 1, resultFilterOp.getFilterExpressions().size() );
		assertEquals( e1, resultFilterOp.getFilterExpressions().get(0) );

		final LogicalPlan subResult = result.getSubPlan(0);
		assertTrue( subResult.getRootOperator() instanceof LogicalOpTPOptAdd );
		assertTrue( subResult.getRootOperator().equals(tpOptAdd) );

		final LogicalPlan subsubResult = subResult.getSubPlan(0);
		assertTrue( subsubResult.getRootOperator() instanceof LogicalOpFilter );

		final LogicalOpFilter resultFilterOp2 = (LogicalOpFilter) subsubResult.getRootOperator();
		assertEquals( 1, resultFilterOp2.getFilterExpressions().size() );
		assertEquals( e2, resultFilterOp2.getFilterExpressions().get(0) );

		assertTrue( subsubResult.getSubPlan(0).equals(reqSubPlan) );
	}

	@Test
	public void pushFilterOverFilterUnderUnion() {
		// a filter on top of another filter on top of a union over two
		// requests; the two filters can be merged and pushed under the
		// union over both requests

		// set up
		final Var v1 = Var.alloc("x");
		final Var v2 = Var.alloc("y");
		final FederationMember fm = new TPFServerForTest();

		final TriplePattern tp1 = new TriplePatternImpl(v2, v2, v1);
		final LogicalOpRequest<?,?> reqOp1 = new LogicalOpRequest<>( fm, new TriplePatternRequestImpl(tp1) );

		final TriplePattern tp2 = new TriplePatternImpl(v1 ,v2, v2);
		final LogicalOpRequest<?,?> reqOp2 = new LogicalOpRequest<>( fm, new TriplePatternRequestImpl(tp2) );

		final LogicalPlan unionSubPlan = LogicalPlanUtils.createPlanWithMultiwayUnion(
				new LogicalPlanWithNullaryRootImpl(reqOp1),
				new LogicalPlanWithNullaryRootImpl(reqOp2) );

		final Expr e1 = new E_IsIRI( new ExprVar(v1) );
		final LogicalOpFilter filterOp1 = new LogicalOpFilter(e1);
		final LogicalPlan filterSubPlan = new LogicalPlanWithUnaryRootImpl(filterOp1, unionSubPlan);

		final Expr e2 = new E_IsIRI( new ExprVar(v2) );
		final LogicalOpFilter rootOp = new LogicalOpFilter(e2);
		final LogicalPlan filterPlan = new LogicalPlanWithUnaryRootImpl(rootOp, filterSubPlan);

		// test
		final LogicalPlan result = new FilterPushDown().apply(filterPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpMultiwayUnion );
		assertEquals( 2, result.numberOfSubPlans() );

		final LogicalPlan subResult1 = result.getSubPlan(0);
		final LogicalPlan subResult2 = result.getSubPlan(1);
		assertTrue( subResult1.getRootOperator() instanceof LogicalOpFilter );
		assertTrue( subResult2.getRootOperator() instanceof LogicalOpFilter );

		final ExprList resultExprList1 = ((LogicalOpFilter) subResult1.getRootOperator()).getFilterExpressions();
		final ExprList resultExprList2 = ((LogicalOpFilter) subResult2.getRootOperator()).getFilterExpressions();
		assertEquals( 2, resultExprList1.size() );
		assertEquals( 2, resultExprList2.size() );

		final ExprList expectedExprListA = new ExprList( Arrays.asList(e1,e2) );
		final ExprList expectedExprListB = new ExprList( Arrays.asList(e2,e1) );
		assertTrue( resultExprList1.equals(expectedExprListA) || resultExprList1.equals(expectedExprListB) );
		assertTrue( resultExprList2.equals(expectedExprListA) || resultExprList2.equals(expectedExprListB) );

		assertTrue( subResult1.getSubPlan(0).getRootOperator().equals(reqOp1) );
		assertTrue( subResult2.getSubPlan(0).getRootOperator().equals(reqOp2) );
	}

	@Test
	public void pushFilterUnderBindImpossible() {
		// a filter on top of a bind with a TPF request underneath, where the
		// filter refers to the variable assigned by the bind;
		// hence, the filter can *not* be pushed under the bind

		// set up
		// - request operator
		final FederationMember fm = new TPFServerForTest();
		final Var v1 = Var.alloc("x");
		final TriplePattern tp = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>( fm, new TriplePatternRequestImpl(tp) );

		// - bind operator
		final Var v2 = Var.alloc("y");
		final Expr bindExpr = NodeValue.makeInteger(42);
		final VarExprList bindExpressions = new VarExprList(v2, bindExpr);
		final LogicalOpBind bindOp = new LogicalOpBind(bindExpressions);

		// - filter operator
		final Expr filterExpr = new E_IsIRI( new ExprVar(v2) ); // v2 !!!
		final LogicalOpFilter filterOp = new LogicalOpFilter(filterExpr);

		// - plan
		final LogicalPlan reqPlan = new LogicalPlanWithNullaryRootImpl(reqOp);
		final LogicalPlan bindPlan = new LogicalPlanWithUnaryRootImpl(bindOp, reqPlan);
		final LogicalPlan filterPlan = new LogicalPlanWithUnaryRootImpl(filterOp, bindPlan);

		// test
		final LogicalPlan result = new FilterPushDown().apply(filterPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpFilter );

		final LogicalPlan subResult1 = result.getSubPlan(0);
		assertTrue( subResult1.getRootOperator() instanceof LogicalOpBind );
	}

	@Test
	public void pushFilterUnderOneBind() {
		// a filter on top of a bind with a TPF request underneath, where the
		// filter refers to the variable assigned by the request;
		// hence, the filter can be pushed under the bind but not into the request

		// set up
		// - request operator
		final FederationMember fm = new TPFServerForTest();
		final Var v1 = Var.alloc("x");
		final TriplePattern tp = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>( fm, new TriplePatternRequestImpl(tp) );

		// - bind operator
		final Var v2 = Var.alloc("y");
		final Expr bindExpr = NodeValue.makeInteger(42);
		final VarExprList bindExpressions = new VarExprList(v2, bindExpr);
		final LogicalOpBind bindOp = new LogicalOpBind(bindExpressions);

		// - filter operator
		final Expr filterExpr = new E_IsIRI( new ExprVar(v1) );
		final LogicalOpFilter filterOp = new LogicalOpFilter(filterExpr);

		// - plan
		final LogicalPlan reqPlan = new LogicalPlanWithNullaryRootImpl(reqOp);
		final LogicalPlan bindPlan = new LogicalPlanWithUnaryRootImpl(bindOp, reqPlan);
		final LogicalPlan filterPlan = new LogicalPlanWithUnaryRootImpl(filterOp, bindPlan);

		// test
		final LogicalPlan result = new FilterPushDown().apply(filterPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpBind );

		final LogicalPlan subResult1 = result.getSubPlan(0);
		assertTrue( subResult1.getRootOperator() instanceof LogicalOpFilter );
	}

	@Test
	public void pushFilterUnderTwoBinds() {
		// a filter on top of two bind with a TPF request underneath, where the
		// filter refers to the variable assigned by the request; hence, the
		// filter can be pushed under both binds but not into the request

		// set up
		// - request operator
		final FederationMember fm = new TPFServerForTest();
		final Var v1 = Var.alloc("x");
		final TriplePattern tp = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>( fm, new TriplePatternRequestImpl(tp) );

		// - 1st bind operator
		final Var v2 = Var.alloc("y");
		final Expr bind1Expr = NodeValue.makeInteger(42);
		final VarExprList bind1Expressions = new VarExprList(v2, bind1Expr);
		final LogicalOpBind bind1Op = new LogicalOpBind(bind1Expressions);

		// - 2nd bind operator
		final Var v3 = Var.alloc("z");
		final Expr bind2Expr = NodeValue.makeInteger(42);
		final VarExprList bind2Expressions = new VarExprList(v3, bind2Expr);
		final LogicalOpBind bind2Op = new LogicalOpBind(bind2Expressions);

		// - filter operator
		final Expr filterExpr = new E_IsIRI( new ExprVar(v1) );
		final LogicalOpFilter filterOp = new LogicalOpFilter(filterExpr);

		// - plan
		final LogicalPlan reqPlan = new LogicalPlanWithNullaryRootImpl(reqOp);
		final LogicalPlan bind1Plan = new LogicalPlanWithUnaryRootImpl(bind1Op, reqPlan);
		final LogicalPlan bind2Plan = new LogicalPlanWithUnaryRootImpl(bind2Op, bind1Plan);
		final LogicalPlan filterPlan = new LogicalPlanWithUnaryRootImpl(filterOp, bind2Plan);

		// test
		final LogicalPlan result = new FilterPushDown().apply(filterPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpBind );

		final LogicalPlan subResult1 = result.getSubPlan(0);
		assertTrue( subResult1.getRootOperator() instanceof LogicalOpBind );

		final LogicalPlan subResult2 = subResult1.getSubPlan(0);
		assertTrue( subResult2.getRootOperator() instanceof LogicalOpFilter );
	}

}
