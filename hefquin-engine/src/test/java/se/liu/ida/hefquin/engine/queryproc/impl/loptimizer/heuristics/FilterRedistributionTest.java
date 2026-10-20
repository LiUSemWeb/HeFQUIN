package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics;

import static org.junit.Assert.assertTrue;

import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_IsIRI;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.junit.Test;

import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.base.query.impl.GenericSPARQLGraphPatternImpl2;
import se.liu.ida.hefquin.base.query.impl.TriplePatternImpl;
import se.liu.ida.hefquin.base.query.utils.QueryPatternUtils;
import se.liu.ida.hefquin.engine.EngineTestBase;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpFilter;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithNullaryRootImpl;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithUnaryRootImpl;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalPlanUtils;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.federation.access.impl.req.SPARQLRequestImpl;

public class FilterRedistributionTest extends EngineTestBase
{
	@Test
	public void redistributeFilterWhenTargetCanEvaluateIt() {
		// a filter in one branch of a join, where the filter can
		// be evaluated in the other branch and redistributed to it

		// set up
		final Var v1 = Var.alloc("x");
		final Var v2 = Var.alloc("y");
		final FederationMember fmA = new SPARQLEndpointForTest("http://exA.org");

		final TriplePattern tp1 = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp1 = new LogicalOpRequest<>( fmA, false, new SPARQLRequestImpl(tp1) );

		final TriplePattern tp2 = new TriplePatternImpl(v1 ,v2, v2);
		final LogicalOpRequest<?,?> reqOp2 = new LogicalOpRequest<>( fmA, false, new SPARQLRequestImpl(tp2) );

		final Expr e = new E_IsIRI( new ExprVar(v1) );
		final UnaryLogicalOp filterOp = new LogicalOpFilter(e, false);
		final LogicalPlan filterPlan = new LogicalPlanWithUnaryRootImpl(filterOp, null, new LogicalPlanWithNullaryRootImpl(reqOp2, null));

		final LogicalPlan joinSubPlan = LogicalPlanUtils.createPlanWithBinaryJoin(
				false,
				new LogicalPlanWithNullaryRootImpl(reqOp1, null),
				filterPlan,
				null );

		// test
		final LogicalPlan result = new FilterRedistribution().apply(joinSubPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpJoin );

		final LogicalPlan leftBranch = result.getSubPlan(0);
		assertTrue( leftBranch.getRootOperator() instanceof LogicalOpFilter );

		final LogicalPlan rightBranch = result.getSubPlan(1);
		assertTrue( rightBranch.getRootOperator() instanceof LogicalOpFilter );
	}

	@Test
	public void doesNotRedistributeFilterWhenVariableIsUnavailable() {
		// a filter in one branch of a join, where the filter cannot
		// be evaluated in the other branch because the required variable
		// is unavailable

		// set up
		final Var v1 = Var.alloc("x");
		final Var v2 = Var.alloc("y");
		final FederationMember fmA = new SPARQLEndpointForTest("http://exA.org");

		final TriplePattern tp1 = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp1 = new LogicalOpRequest<>( fmA, false, new SPARQLRequestImpl(tp1) );

		final TriplePattern tp2 = new TriplePatternImpl(v2 ,v2, v2);
		final LogicalOpRequest<?,?> reqOp2 = new LogicalOpRequest<>( fmA, false, new SPARQLRequestImpl(tp2) );

		final Expr e = new E_IsIRI( new ExprVar(v2) );
		final UnaryLogicalOp filterOp = new LogicalOpFilter(e, false);
		final LogicalPlan filterPlan = new LogicalPlanWithUnaryRootImpl(filterOp, null, new LogicalPlanWithNullaryRootImpl(reqOp2, null));

		final LogicalPlan joinSubPlan = LogicalPlanUtils.createPlanWithBinaryJoin(
				false,
				new LogicalPlanWithNullaryRootImpl(reqOp1, null),
				filterPlan,
				null );

		// test
		final LogicalPlan result = new FilterRedistribution().apply(joinSubPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpJoin );

		final LogicalPlan leftBranch = result.getSubPlan(0);
		assertTrue( leftBranch.getRootOperator() instanceof LogicalOpRequest );

		final LogicalPlan rightBranch = result.getSubPlan(1);
		assertTrue( rightBranch.getRootOperator() instanceof LogicalOpFilter );
	}

	@Test
	public void doNotDuplicateExistingFilter() {
		// a filter in both branches of a join,
		// where the filter should not be duplicated
		// because both branches already contain it

		// set up
		final Var v1 = Var.alloc("x");
		final Var v2 = Var.alloc("y");
		final FederationMember fmA = new SPARQLEndpointForTest("http://exA.org");

		final Expr e = new E_IsIRI( new ExprVar(v1) );

		final UnaryLogicalOp filterOp1 = new LogicalOpFilter(e, false);
		final TriplePattern tp1 = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp1 = new LogicalOpRequest<>( fmA, false, new SPARQLRequestImpl(tp1) );
		final LogicalPlan filterPlan1 = new LogicalPlanWithUnaryRootImpl(filterOp1, null, new LogicalPlanWithNullaryRootImpl(reqOp1, null));

		final UnaryLogicalOp filterOp2 = new LogicalOpFilter(e, false);
		final TriplePattern tp2 = new TriplePatternImpl(v1 ,v2, v2);
		final LogicalOpRequest<?,?> reqOp2 = new LogicalOpRequest<>( fmA, false, new SPARQLRequestImpl(tp2) );
		final LogicalPlan filterPlan2 = new LogicalPlanWithUnaryRootImpl(filterOp2, null, new LogicalPlanWithNullaryRootImpl(reqOp2, null));

		final LogicalPlan joinSubPlan = LogicalPlanUtils.createPlanWithBinaryJoin(
				false,
				filterPlan1,
				filterPlan2,
				null );

		// test
		final LogicalPlan result = new FilterRedistribution().apply(joinSubPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpJoin );

		final LogicalPlan leftBranch = result.getSubPlan(0);
		assertTrue( leftBranch.getRootOperator() instanceof LogicalOpFilter );

		final LogicalPlan leftBranchChild = leftBranch.getSubPlan(0);
		assertTrue( leftBranchChild.getRootOperator() instanceof LogicalOpRequest );

		final LogicalPlan rightBranch = result.getSubPlan(1);
		assertTrue( rightBranch.getRootOperator() instanceof LogicalOpFilter );

		final LogicalPlan rightBranchChild = rightBranch.getSubPlan(0);
		assertTrue( rightBranchChild.getRootOperator() instanceof LogicalOpRequest );
	}

	@Test
	public void redistributeFilterFromRequest() {
		// a filter inside a SPARQL request in one branch of a join,
		// where the filter can be redistributed to the other branch
		// as a logical filter operator

		// set up
		final Var v1 = Var.alloc("x");
		final Var v2 = Var.alloc("y");
		final FederationMember fmA = new SPARQLEndpointForTest("http://exA.org");

		final TriplePattern tp1 = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp1 = new LogicalOpRequest<>( fmA, false, new SPARQLRequestImpl(tp1) );

		final TriplePattern tp2 = new TriplePatternImpl(v1 ,v2, v2);
		final LogicalOpRequest<?,?> reqOp2 = new LogicalOpRequest<>( fmA, false, new SPARQLRequestImpl(tp2) );

		final Expr e = new E_IsIRI( new ExprVar(v1) );
		final SPARQLRequest req = (SPARQLRequest) reqOp2.getRequest();

		final SPARQLGraphPattern mergedPattern = req.getQueryPattern().mergeWith(new ExprList(e));
		final SPARQLRequest mergedReq = new SPARQLRequestImpl( mergedPattern, req.getProjectionVars(), req.getDistinctRequired() );
		final LogicalOpRequest<?,?> mergedReqOp = new LogicalOpRequest<>( fmA, false, mergedReq );

		final LogicalPlan joinSubPlan = LogicalPlanUtils.createPlanWithBinaryJoin(
				false,
				new LogicalPlanWithNullaryRootImpl(reqOp1, null),
				new LogicalPlanWithNullaryRootImpl(mergedReqOp, null),
				null );

		// test
		final LogicalPlan result = new FilterRedistribution().apply(joinSubPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpJoin );

		final LogicalPlan leftBranch = result.getSubPlan(0);
		assertTrue( leftBranch.getRootOperator() instanceof LogicalOpFilter );

		final LogicalPlan leftBranchChild = leftBranch.getSubPlan(0);
		assertTrue( leftBranchChild.getRootOperator() instanceof LogicalOpRequest );

		final LogicalPlan rightBranch = result.getSubPlan(1);
		assertTrue( rightBranch.getRootOperator() instanceof LogicalOpRequest<?,?> );

		final LogicalOpRequest<?,?> resultReqOp1 = (LogicalOpRequest<?,?>) rightBranch.getRootOperator();

		final SPARQLRequest resultReq1 = (SPARQLRequest) resultReqOp1.getRequest();
		final Element resultElmt1 = QueryPatternUtils.convertToJenaElement( resultReq1.getQueryPattern() );
		assertTrue(                                        resultElmt1 instanceof ElementGroup );
		assertTrue(                        ((ElementGroup) resultElmt1).get(1) instanceof ElementFilter );
		assertTrue(       ((ElementFilter) ((ElementGroup) resultElmt1).get(1)).getExpr().equals(e) );
	}

	@Test
	public void doNotDuplicateExistingFilterInRequests() {
		// a filter inside SPARQL requests in both branches of a join,
		// where the filter should not be duplicated
		// because it already exists in both requests

		// set up
		final Var v1 = Var.alloc("x");
		final Var v2 = Var.alloc("y");
		final FederationMember fmA = new SPARQLEndpointForTest("http://exA.org");

		final TriplePattern tp2 = new TriplePatternImpl(v1 ,v2, v2);
		final LogicalOpRequest<?,?> reqOp2 = new LogicalOpRequest<>( fmA, false, new SPARQLRequestImpl(tp2) );

		final Expr e = new E_IsIRI( new ExprVar(v1) );
		final SPARQLRequest req = (SPARQLRequest) reqOp2.getRequest();

		final SPARQLGraphPattern mergedPattern = req.getQueryPattern().mergeWith(new ExprList(e));
		final SPARQLRequest mergedReq = new SPARQLRequestImpl( mergedPattern, req.getProjectionVars(), req.getDistinctRequired() );
		final LogicalOpRequest<?,?> mergedReqOp = new LogicalOpRequest<>( fmA, false, mergedReq );

		final LogicalPlan joinSubPlan = LogicalPlanUtils.createPlanWithBinaryJoin(
				false,
				new LogicalPlanWithNullaryRootImpl(mergedReqOp, null),
				new LogicalPlanWithNullaryRootImpl(mergedReqOp, null),
				null );

		// test
		final LogicalPlan result = new FilterRedistribution().apply(joinSubPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpJoin );

		// check left branch
		final LogicalPlan leftBranch = result.getSubPlan(0);
		assertTrue( leftBranch.getRootOperator() instanceof LogicalOpRequest<?,?> );
		final LogicalOpRequest<?,?> resultReqOp1 = (LogicalOpRequest<?,?>) leftBranch.getRootOperator();

		final SPARQLRequest resultReq1 = (SPARQLRequest) resultReqOp1.getRequest();
		final Element resultElmt1 = QueryPatternUtils.convertToJenaElement( resultReq1.getQueryPattern() );
		assertTrue(                                        resultElmt1 instanceof ElementGroup );
		assertTrue(                        ((ElementGroup) resultElmt1).get(1) instanceof ElementFilter );
		assertTrue(       ((ElementFilter) ((ElementGroup) resultElmt1).get(1)).getExpr().equals(e) );

		// check right branch
		final LogicalPlan rightBranch = result.getSubPlan(1);
		assertTrue( rightBranch.getRootOperator() instanceof LogicalOpRequest<?,?> );
		final LogicalOpRequest<?,?> resultReqOp2 = (LogicalOpRequest<?,?>) rightBranch.getRootOperator();

		final SPARQLRequest resultReq2 = (SPARQLRequest) resultReqOp2.getRequest();
		final Element resultElmt2 = QueryPatternUtils.convertToJenaElement( resultReq1.getQueryPattern() );
		assertTrue(                                        resultElmt1 instanceof ElementGroup );
		assertTrue(                        ((ElementGroup) resultElmt1).get(1) instanceof ElementFilter );
		assertTrue(       ((ElementFilter) ((ElementGroup) resultElmt1).get(1)).getExpr().equals(e) );
	}

	@Test
	public void redistributeDifferentFiltersBetweenBranches() {
		// different filters in each branch of a join,
		// where each filter can be redistributed to the other branch

		// set up
		final Var v1 = Var.alloc("x");
		final Var v2 = Var.alloc("y");
		final FederationMember fmA = new SPARQLEndpointForTest("http://exA.org");

		final TriplePattern tp1 = new TriplePatternImpl(v1, v2, v2);
		final LogicalOpRequest<?,?> reqOp1 = new LogicalOpRequest<>( fmA, false, new SPARQLRequestImpl(tp1) );

		final Expr e1 = new E_IsIRI( new ExprVar(v2) );
		final UnaryLogicalOp filterOp = new LogicalOpFilter(e1, false);
		final LogicalPlan filterPlan = new LogicalPlanWithUnaryRootImpl(filterOp, null, new LogicalPlanWithNullaryRootImpl(reqOp1, null));

		final TriplePattern tp2 = new TriplePatternImpl(v1 ,v2, v2);
		final LogicalOpRequest<?,?> reqOp2 = new LogicalOpRequest<>( fmA, false, new SPARQLRequestImpl(tp2) );

		final Expr e2 = new E_IsIRI( new ExprVar(v1) );
		final SPARQLRequest req = (SPARQLRequest) reqOp2.getRequest();

		final SPARQLGraphPattern mergedPattern = req.getQueryPattern().mergeWith(new ExprList(e2));
		final SPARQLRequest mergedReq = new SPARQLRequestImpl( mergedPattern, req.getProjectionVars(), req.getDistinctRequired() );
		final LogicalOpRequest<?,?> mergedReqOp = new LogicalOpRequest<>( fmA, false, mergedReq );

		final LogicalPlan joinSubPlan = LogicalPlanUtils.createPlanWithBinaryJoin(
				false,
				filterPlan,
				new LogicalPlanWithNullaryRootImpl(mergedReqOp, null),
				null );

		// test
		final LogicalPlan result = new FilterRedistribution().apply(joinSubPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpJoin );

		final LogicalPlan leftBranch = result.getSubPlan(0);
		// check filter expressions matching the copied over one
		assertTrue( leftBranch.getRootOperator() instanceof LogicalOpFilter );
		final LogicalOpFilter leftBranchOp = (LogicalOpFilter) leftBranch.getRootOperator();
		assertTrue( leftBranchOp.getFilterExpressions().equals( new ExprList(e2)) );

		// check child and that the filter expression matches the original one
		final LogicalPlan leftBranchChild = leftBranch.getSubPlan(0);
		assertTrue( leftBranchChild.getRootOperator() instanceof LogicalOpFilter );
		final LogicalOpFilter leftBranchChildOp = (LogicalOpFilter) leftBranchChild.getRootOperator();
		assertTrue( leftBranchChildOp.getFilterExpressions().equals( new ExprList(e1)) );

		// check filter expressions matching the copied over one
		final LogicalPlan rightBranch = result.getSubPlan(1);
		assertTrue( rightBranch.getRootOperator() instanceof LogicalOpFilter );
		final LogicalOpFilter rightBranchOp = (LogicalOpFilter) rightBranch.getRootOperator();
		assertTrue( rightBranchOp.getFilterExpressions().equals( new ExprList(e1)) );

		// check child and that the filter expression matches the original one
		final LogicalPlan rightBranchChild = rightBranch.getSubPlan(0);
		assertTrue( rightBranchChild.getRootOperator() instanceof LogicalOpRequest<?,?> );

		final LogicalOpRequest<?,?> resultReqOp1 = (LogicalOpRequest<?,?>) rightBranchChild.getRootOperator();

		final SPARQLRequest resultReq1 = (SPARQLRequest) resultReqOp1.getRequest();
		final Element resultElmt1 = QueryPatternUtils.convertToJenaElement( resultReq1.getQueryPattern() );
		assertTrue(                                        resultElmt1 instanceof ElementGroup );
		assertTrue(                        ((ElementGroup) resultElmt1).get(1) instanceof ElementFilter );
		assertTrue(       ((ElementFilter) ((ElementGroup) resultElmt1).get(1)).getExpr().equals(e2) );
	}

	@Test
	public void redistributeTwoFiltersInOneBranch() {
		// two filters in one branch of a join,
		// where both filters can be redistributed to the other branch

		// set up
		final Var v1 = Var.alloc("x");
		final Var v2 = Var.alloc("y");
		final FederationMember fmA = new SPARQLEndpointForTest("http://exA.org");

		final TriplePattern tp1 = new TriplePatternImpl(v1, v2, v2);
		final LogicalOpRequest<?,?> reqOp1 = new LogicalOpRequest<>( fmA, false, new SPARQLRequestImpl(tp1) );

		final TriplePattern tp2 = new TriplePatternImpl(v1 ,v2, v2);
		final LogicalOpRequest<?,?> reqOp2 = new LogicalOpRequest<>( fmA, false, new SPARQLRequestImpl(tp2) );

		final Expr e1 = new E_IsIRI( new ExprVar(v1) );
		final SPARQLRequest req = (SPARQLRequest) reqOp2.getRequest();
		final SPARQLGraphPattern mergedPattern = req.getQueryPattern().mergeWith(new ExprList(e1));
		final SPARQLRequest mergedReq = new SPARQLRequestImpl( mergedPattern, req.getProjectionVars(), req.getDistinctRequired() );
		final LogicalOpRequest<?,?> mergedReqOp = new LogicalOpRequest<>( fmA, false, mergedReq );

		final Expr e2 = new E_IsIRI( new ExprVar(v2) );
		final UnaryLogicalOp filterOp = new LogicalOpFilter(e2, false);
		final LogicalPlan filterPlan = new LogicalPlanWithUnaryRootImpl(filterOp, null, new LogicalPlanWithNullaryRootImpl(mergedReqOp, null));

		final LogicalPlan joinSubPlan = LogicalPlanUtils.createPlanWithBinaryJoin(
				false,
				new LogicalPlanWithNullaryRootImpl(reqOp1, null),
				filterPlan,
				null );

		// test
		final LogicalPlan result = new FilterRedistribution().apply(joinSubPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpJoin );

		final LogicalPlan leftBranch = result.getSubPlan(0);
		assertTrue( leftBranch.getRootOperator() instanceof LogicalOpFilter );
		final LogicalOpFilter leftBranchOp = (LogicalOpFilter) leftBranch.getRootOperator();
		assertTrue( leftBranchOp.getFilterExpressions().equals( new ExprList(e2) ) );

		final LogicalPlan leftBranchChild = leftBranch.getSubPlan(0);
		assertTrue( leftBranchChild.getRootOperator() instanceof LogicalOpFilter );
		final LogicalOpFilter leftBranchChildOp = (LogicalOpFilter) leftBranchChild.getRootOperator();
		assertTrue( leftBranchChildOp.getFilterExpressions().equals( new ExprList(e1) ) );

		final LogicalPlan rightBranch = result.getSubPlan(1);
		assertTrue( rightBranch.getRootOperator() instanceof LogicalOpFilter );
	}

	@Test
	public void redistributeFiltersInMultiwayJoin() {
		// a filter in one branch of a multiway join,
		// where the filter can be copied to all other branches

		// set up
		final Var v1 = Var.alloc("x");
		final Var v2 = Var.alloc("y");
		final FederationMember fmA = new SPARQLEndpointForTest("http://exA.org");

		final TriplePattern tp1 = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp1 = new LogicalOpRequest<>( fmA, false, new SPARQLRequestImpl(tp1) );

		final TriplePattern tp2 = new TriplePatternImpl(v1 ,v2, v2);
		final LogicalOpRequest<?,?> reqOp2 = new LogicalOpRequest<>( fmA, false, new SPARQLRequestImpl(tp2) );

		final Expr e = new E_IsIRI( new ExprVar(v1) );
		final UnaryLogicalOp filterOp = new LogicalOpFilter(e, false);
		final LogicalPlan filterPlan = new LogicalPlanWithUnaryRootImpl(filterOp, null, new LogicalPlanWithNullaryRootImpl(reqOp2, null));

		final LogicalPlan joinSubPlan = LogicalPlanUtils.createPlanWithMultiwayJoin(
				false,
				null,
				new LogicalPlanWithNullaryRootImpl(reqOp1, null),
				new LogicalPlanWithNullaryRootImpl(reqOp1, null),
				filterPlan );

		// test
		final LogicalPlan result = new FilterRedistribution().apply(joinSubPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpMultiwayJoin );

		// check first subplan
		final LogicalPlan firstSubPlan = result.getSubPlan(0);
		assertTrue( firstSubPlan.getRootOperator() instanceof LogicalOpFilter );
		final LogicalOpFilter filterOp1 = (LogicalOpFilter) firstSubPlan.getRootOperator();
		assertTrue( filterOp1.getFilterExpressions().equals( new ExprList(e)) );

		// check second subplan
		final LogicalPlan secondSubPlan = result.getSubPlan(1);
		assertTrue( secondSubPlan.getRootOperator() instanceof LogicalOpFilter );
		final LogicalOpFilter filterOp2 = (LogicalOpFilter) secondSubPlan.getRootOperator();
		assertTrue( filterOp2.getFilterExpressions().equals( new ExprList(e)) );

		// check third subplan
		final LogicalPlan thirdSubPlan = result.getSubPlan(2);
		assertTrue( thirdSubPlan.getRootOperator() instanceof LogicalOpFilter );
		final LogicalOpFilter filterOp3 = (LogicalOpFilter) thirdSubPlan.getRootOperator();
		assertTrue( filterOp3.getFilterExpressions().equals( new ExprList(e)) );

	}

	@Test
	public void multiJoinDifferentCases() {
		// a filter in one branch of a multiway join,
		// where one branch can evaluate the filter and receives a copy,
		// one branch cannot evaluate it and one branch already contains it

		// set up
		final Var v1 = Var.alloc("x");
		final Var v2 = Var.alloc("y");
		final FederationMember fmA = new SPARQLEndpointForTest("http://exA.org");

		final TriplePattern tp1 = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp1 = new LogicalOpRequest<>( fmA, false, new SPARQLRequestImpl(tp1) );

		final Expr e = new E_IsIRI( new ExprVar(v1) );
		final UnaryLogicalOp filterOp = new LogicalOpFilter(e, false);
		final LogicalPlan filterPlan1 = new LogicalPlanWithUnaryRootImpl(filterOp, null, new LogicalPlanWithNullaryRootImpl(reqOp1, null));
		final LogicalPlan filterPlan2 = new LogicalPlanWithUnaryRootImpl(filterOp, null, new LogicalPlanWithNullaryRootImpl(reqOp1, null));

		final TriplePattern tp2 = new TriplePatternImpl(v1 ,v2, v2);
		final LogicalOpRequest<?,?> reqOp2 = new LogicalOpRequest<>( fmA, false, new SPARQLRequestImpl(tp2) );

		final Var v3 = Var.alloc("z");
		final TriplePattern tp3 = new TriplePatternImpl(v3 ,v3, v3);
		final LogicalOpRequest<?,?> reqOp3 = new LogicalOpRequest<>( fmA, false, new SPARQLRequestImpl(tp3) );

		final LogicalPlan joinSubPlan = LogicalPlanUtils.createPlanWithMultiwayJoin(
				false,
				null,
				filterPlan1,
				new LogicalPlanWithNullaryRootImpl(reqOp2, null),
				new LogicalPlanWithNullaryRootImpl(reqOp3, null),
				filterPlan2 );

		// test
		final LogicalPlan result = new FilterRedistribution().apply(joinSubPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpMultiwayJoin );

		// check first subplan
		final LogicalPlan firstSubPlan = result.getSubPlan(0);
		assertTrue( firstSubPlan.getRootOperator() instanceof LogicalOpFilter );
		final LogicalOpFilter filterOp1 = (LogicalOpFilter) firstSubPlan.getRootOperator();
		assertTrue( filterOp1.getFilterExpressions().equals( new ExprList(e)) );

		// check second subplan
		final LogicalPlan secondSubPlan = result.getSubPlan(1);
		assertTrue( secondSubPlan.getRootOperator() instanceof LogicalOpFilter );
		final LogicalOpFilter filterOp2 = (LogicalOpFilter) secondSubPlan.getRootOperator();
		assertTrue( filterOp2.getFilterExpressions().equals( new ExprList(e)) );

		// check third subplan
		final LogicalPlan thirdSubPlan = result.getSubPlan(2);
		assertTrue( thirdSubPlan.getRootOperator() instanceof LogicalOpRequest<?,?> );

		// check third subplan
		final LogicalPlan fourthSubPlan = result.getSubPlan(3);
		final LogicalOpFilter filterOp3 = (LogicalOpFilter) fourthSubPlan.getRootOperator();
		assertTrue( filterOp3.getFilterExpressions().equals( new ExprList(e)) );

		assertTrue( fourthSubPlan.getSubPlan(0).getRootOperator() instanceof LogicalOpRequest<?,?> );
	}

	@Test
	public void redistributeFiltersInGPAdd() {
		// a filter in one the pattern of a gpAdd operator in one
		// branch of a join, where the filter can be evaluated in
		// the other branch and redistributed to it, also tests
		// filter collection from GenericSPARQLGraphPatternImpl2

		// set up
		final Var v1 = Var.alloc("x");
		final Var v2 = Var.alloc("y");
		final FederationMember fmA = new SPARQLEndpointForTest("http://exA.org");

		final TriplePattern tp1 = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp1 = new LogicalOpRequest<>( fmA, false, new SPARQLRequestImpl(tp1) );

		final TriplePattern tp2 = new TriplePatternImpl(v1 ,v2, v2);
		final LogicalOpRequest<?,?> reqOp2 = new LogicalOpRequest<>( fmA, false, new SPARQLRequestImpl(tp2) );

		final Expr e = new E_IsIRI( new ExprVar(v1) );

		final BasicPattern basicPattern = new BasicPattern();
		basicPattern.add(Triple.create(v1, v1, v1));

		final Op op = OpFilter.filter( e, new OpBGP(basicPattern) );

		final SPARQLGraphPattern pattern = new GenericSPARQLGraphPatternImpl2(op);
		final LogicalOpGPAdd gpAddOp = new LogicalOpGPAdd( fmA, pattern, null, false );

		final LogicalPlan gpAddPlan = new LogicalPlanWithUnaryRootImpl(gpAddOp, null, new LogicalPlanWithNullaryRootImpl(reqOp2, null));

		final LogicalPlan joinSubPlan = LogicalPlanUtils.createPlanWithBinaryJoin(
			false,
			new LogicalPlanWithNullaryRootImpl(reqOp1, null),
			gpAddPlan,
			null );

		// test
		final LogicalPlan result = new FilterRedistribution().apply(joinSubPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpJoin );

		final LogicalPlan leftBranch = result.getSubPlan(0);
		assertTrue( leftBranch.getRootOperator() instanceof LogicalOpFilter );

		final LogicalPlan rightBranch = result.getSubPlan(1);
		assertTrue( rightBranch.getRootOperator() instanceof LogicalOpGPAdd );
	}
}
