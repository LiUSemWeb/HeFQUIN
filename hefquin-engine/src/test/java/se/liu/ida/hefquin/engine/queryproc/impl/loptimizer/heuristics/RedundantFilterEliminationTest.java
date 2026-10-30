package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Set;

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
import se.liu.ida.hefquin.base.query.impl.TriplePatternImpl;
import se.liu.ida.hefquin.base.query.utils.QueryPatternUtils;
import se.liu.ida.hefquin.engine.EngineTestBase;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPOptAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpLeftJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayUnion;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpProject;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpUnion;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithBinaryRootImpl;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithNaryRootImpl;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithNullaryRootImpl;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithUnaryRootImpl;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.federation.access.impl.req.SPARQLRequestImpl;

public class RedundantFilterEliminationTest extends EngineTestBase
{
	@Test
	public void removesRedundantFilter() {
		// a filter in a GPAdd operator is redundant if the input plan guarantees it

		// set up
		final Var v1 = Var.alloc("v1");
		final FederationMember fm = new SPARQLEndpointForTest("http://exA.org");

		final TriplePattern tp = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>( fm, false, new SPARQLRequestImpl(tp) );

		final Expr e = new E_IsIRI( new ExprVar(v1) );
		final SPARQLRequest req = (SPARQLRequest) reqOp.getRequest();

		final SPARQLGraphPattern mergedPattern = req.getQueryPattern().mergeWith(new ExprList(e));
		final SPARQLRequest mergedReq = new SPARQLRequestImpl( mergedPattern, req.getProjectionVars(), req.getDistinctRequired() );
		final LogicalOpRequest<?,?> mergedReqOp = new LogicalOpRequest<>( fm, false, mergedReq );
		final LogicalPlan reqWithFilterPlan = new LogicalPlanWithNullaryRootImpl(mergedReqOp, null);

		final LogicalOpGPAdd gpAddOp = new LogicalOpGPAdd(fm, mergedPattern, null, false);
		final LogicalPlan gpAddPlan = new LogicalPlanWithUnaryRootImpl(gpAddOp, null, reqWithFilterPlan);

		// test
		final LogicalPlan result = new RedundantFilterElimination().apply(gpAddPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpGPAdd );
		final LogicalOpGPAdd resultGpAddOp = (LogicalOpGPAdd) result.getRootOperator();

		final Element resultElmt1 = QueryPatternUtils.convertToJenaElement( resultGpAddOp.getPattern() );
		assertTrue(                                        resultElmt1 instanceof ElementGroup );
		assertTrue(                        ((ElementGroup) resultElmt1).size() == 1 );
		assertFalse(                       ((ElementGroup) resultElmt1).get(0) instanceof ElementFilter );
	}

	@Test
	public void retainsNonRedundantFilter() {
		// a filter in a GPAdd operator must be retained if the input plan does not guarantee it

		// set up
		final Var v1 = Var.alloc("v1");
		final FederationMember fm = new SPARQLEndpointForTest("http://exA.org");

		final TriplePattern tp = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>( fm, false, new SPARQLRequestImpl(tp) );

		final Expr e1 = new E_IsIRI( new ExprVar(v1) );
		final SPARQLRequest req = (SPARQLRequest) reqOp.getRequest();

		final SPARQLGraphPattern mergedPattern1 = req.getQueryPattern().mergeWith(new ExprList(e1));
		final SPARQLRequest mergedReq = new SPARQLRequestImpl( mergedPattern1, req.getProjectionVars(), req.getDistinctRequired() );
		final LogicalOpRequest<?,?> mergedReqOp = new LogicalOpRequest<>( fm, false, mergedReq );
		final LogicalPlan reqWithFilterPlan = new LogicalPlanWithNullaryRootImpl(mergedReqOp, null);

		final Var v2 = Var.alloc("v2");
		final Expr e2 = new E_IsIRI( new ExprVar(v2) );
		final SPARQLGraphPattern mergedPattern2 = req.getQueryPattern().mergeWith(new ExprList(e2));
		final LogicalOpGPAdd gpAddOp = new LogicalOpGPAdd(fm, mergedPattern2, null, false);
		final LogicalPlan gpAddPlan = new LogicalPlanWithUnaryRootImpl(gpAddOp, null, reqWithFilterPlan);

		// test
		final LogicalPlan result = new RedundantFilterElimination().apply(gpAddPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpGPAdd );
		final LogicalOpGPAdd resultGpAddOp = (LogicalOpGPAdd) result.getRootOperator();

		final Element resultElmt1 = QueryPatternUtils.convertToJenaElement( resultGpAddOp.getPattern() );
		assertTrue(                                        resultElmt1 instanceof ElementGroup );
		assertTrue(                        ((ElementGroup) resultElmt1).size() == 2 );
		assertTrue(                        ((ElementGroup) resultElmt1).get(1) instanceof ElementFilter );
		assertTrue(       ((ElementFilter) ((ElementGroup) resultElmt1).get(1)).getExpr().equals(e2) );
	}

	@Test
	public void removesOnlyRedundantFilters() {
		// only filters guaranteed by the input plan should be removed from a GPAdd operator

		// set up
		final Var v1 = Var.alloc("v1");
		final FederationMember fm = new SPARQLEndpointForTest("http://exA.org");

		final TriplePattern tp = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>( fm, false, new SPARQLRequestImpl(tp) );

		final Expr e1 = new E_IsIRI( new ExprVar(v1) );
		final SPARQLRequest req = (SPARQLRequest) reqOp.getRequest();

		final SPARQLGraphPattern mergedPattern1 = req.getQueryPattern().mergeWith(new ExprList(e1));
		final SPARQLRequest mergedReq = new SPARQLRequestImpl( mergedPattern1, req.getProjectionVars(), req.getDistinctRequired() );
		final LogicalOpRequest<?,?> mergedReqOp = new LogicalOpRequest<>( fm, false, mergedReq );
		final LogicalPlan reqWithFilterPlan = new LogicalPlanWithNullaryRootImpl(mergedReqOp, null);

		final Var v2 = Var.alloc("v2");
		final Expr e2 = new E_IsIRI( new ExprVar(v2) );
		final SPARQLGraphPattern mergedPattern2 = req.getQueryPattern().mergeWith(new ExprList( Arrays.asList(e1,e2) ));
		final LogicalOpGPAdd gpAddOp = new LogicalOpGPAdd(fm, mergedPattern2, null, false);
		final LogicalPlan gpAddPlan = new LogicalPlanWithUnaryRootImpl(gpAddOp, null, reqWithFilterPlan);

		// test
		final LogicalPlan result = new RedundantFilterElimination().apply(gpAddPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpGPAdd );
		final LogicalOpGPAdd resultGpAddOp = (LogicalOpGPAdd) result.getRootOperator();

		final Element resultElmt1 = QueryPatternUtils.convertToJenaElement( resultGpAddOp.getPattern() );
		assertTrue(                                        resultElmt1 instanceof ElementGroup );
		assertTrue(                        ((ElementGroup) resultElmt1).size() == 2 );
		assertTrue(                        ((ElementGroup) resultElmt1).get(1) instanceof ElementFilter );
		assertTrue(       ((ElementFilter) ((ElementGroup) resultElmt1).get(1)).getExpr().equals(e2) );
	}

	@Test
	public void leavesPatternUnchangedWhenNoFiltersArePresent() {
		// a graph pattern without filters leads to no filter elimination

		// set up
		final Var v1 = Var.alloc("v1");
		final FederationMember fm = new SPARQLEndpointForTest("http://exA.org");

		final TriplePattern tp = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>( fm, false, new SPARQLRequestImpl(tp) );

		final Expr e1 = new E_IsIRI( new ExprVar(v1) );
		final SPARQLRequest req = (SPARQLRequest) reqOp.getRequest();

		final SPARQLGraphPattern mergedPattern1 = req.getQueryPattern().mergeWith(new ExprList(e1));
		final SPARQLRequest mergedReq = new SPARQLRequestImpl( mergedPattern1, req.getProjectionVars(), req.getDistinctRequired() );
		final LogicalOpRequest<?,?> mergedReqOp = new LogicalOpRequest<>( fm, false, mergedReq );
		final LogicalPlan reqWithFilterPlan = new LogicalPlanWithNullaryRootImpl(mergedReqOp, null);

		final LogicalOpGPAdd gpAddOp = new LogicalOpGPAdd(fm, tp, null, false);
		final LogicalPlan gpAddPlan = new LogicalPlanWithUnaryRootImpl(gpAddOp, null, reqWithFilterPlan);

		// test
		final LogicalPlan result = new RedundantFilterElimination().apply(gpAddPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpGPAdd );
		final LogicalOpGPAdd resultGpAddOp = (LogicalOpGPAdd) result.getRootOperator();

		final Element resultElmt1 = QueryPatternUtils.convertToJenaElement( resultGpAddOp.getPattern() );
		assertTrue( resultGpAddOp.getPattern().equals(tp) );
	}

	@Test
	public void removesFilterGuaranteedByLowerGPAdd() {
		// a filter guaranteed by a lower GPAdd is redundant in the upper GPAdd

		// set up
		final Var v1 = Var.alloc("v1");
		final FederationMember fm = new SPARQLEndpointForTest("http://exA.org");

		final TriplePattern tp = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>( fm, false, new SPARQLRequestImpl(tp) );
		final LogicalPlan reqPlan = new LogicalPlanWithNullaryRootImpl(reqOp, null);

		final Expr e = new E_IsIRI( new ExprVar(v1) );
		final SPARQLRequest req = (SPARQLRequest) reqOp.getRequest();

		final SPARQLGraphPattern mergedPattern = req.getQueryPattern().mergeWith(new ExprList(e));

		final LogicalOpGPAdd gpAddOp1 = new LogicalOpGPAdd(fm, mergedPattern, null, false);
		final LogicalPlan gpAddPlan1 = new LogicalPlanWithUnaryRootImpl(gpAddOp1, null, reqPlan);

		final LogicalOpGPAdd gpAddOp2 = new LogicalOpGPAdd(fm, mergedPattern, null, false);
		final LogicalPlan gpAddPlan2 = new LogicalPlanWithUnaryRootImpl(gpAddOp2, null, gpAddPlan1);

		// test
		final LogicalPlan result = new RedundantFilterElimination().apply(gpAddPlan2);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpGPAdd );
		final LogicalOpGPAdd resultGpAddOp = (LogicalOpGPAdd) result.getRootOperator();

		final Element resultElmt1 = QueryPatternUtils.convertToJenaElement( resultGpAddOp.getPattern() );
		assertTrue(                                        resultElmt1 instanceof ElementGroup );
		assertTrue(                        ((ElementGroup) resultElmt1).size() == 1 );
		assertFalse(                       ((ElementGroup) resultElmt1).get(0) instanceof ElementFilter );

		final LogicalPlan resultSubPlan = result.getSubPlan(0);
		assertTrue( resultSubPlan.getRootOperator() instanceof LogicalOpGPAdd );
		final LogicalOpGPAdd subPlanGpAddOp = (LogicalOpGPAdd) resultSubPlan.getRootOperator();

		final Element resultElmt2 = QueryPatternUtils.convertToJenaElement( subPlanGpAddOp.getPattern() );
		assertTrue(                                        resultElmt2 instanceof ElementGroup );
		assertTrue(                        ((ElementGroup) resultElmt2).get(1) instanceof ElementFilter );
		assertTrue(       ((ElementFilter) ((ElementGroup) resultElmt2).get(1)).getExpr().equals(e) );
	}

	@Test
	public void retainsFilterWhenProjectionRemovesReferencedVariable() {
		// a filter cannot be considered guaranteed above a projection that
		// removes one of its variables

		// set up
		final Var v1 = Var.alloc("v1");
		final Var v2 = Var.alloc("v2");
		final FederationMember fm = new SPARQLEndpointForTest("http://exA.org");

		final TriplePattern tp = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>( fm, false, new SPARQLRequestImpl(tp) );

		final Expr e = new E_IsIRI( new ExprVar(v1) );
		final SPARQLRequest req = (SPARQLRequest) reqOp.getRequest();

		final SPARQLGraphPattern mergedPattern = req.getQueryPattern().mergeWith(new ExprList(e));
		final SPARQLRequest mergedReq = new SPARQLRequestImpl( mergedPattern, req.getProjectionVars(), req.getDistinctRequired() );
		final LogicalOpRequest<?,?> mergedReqOp = new LogicalOpRequest<>( fm, false, mergedReq );
		final LogicalPlan reqWithFilterPlan = new LogicalPlanWithNullaryRootImpl(mergedReqOp, null);

		final LogicalOpProject projectOp = new LogicalOpProject(Set.of(v2), false);
		final LogicalPlan projectPlan = new LogicalPlanWithUnaryRootImpl(projectOp, null, reqWithFilterPlan);

		final LogicalOpGPAdd gpAddOp = new LogicalOpGPAdd(fm, mergedPattern, null, false);
		final LogicalPlan gpAddPlan = new LogicalPlanWithUnaryRootImpl(gpAddOp, null, projectPlan);

		// test
		final LogicalPlan result = new RedundantFilterElimination().apply(gpAddPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpGPAdd );
		final LogicalOpGPAdd resultGpAddOp = (LogicalOpGPAdd) result.getRootOperator();

		final Element resultElmt1 = QueryPatternUtils.convertToJenaElement( resultGpAddOp.getPattern() );
		assertTrue(                                        resultElmt1 instanceof ElementGroup );
		assertTrue(                        ((ElementGroup) resultElmt1).size() == 2 );
		assertTrue(                        ((ElementGroup) resultElmt1).get(1) instanceof ElementFilter );
		assertTrue(       ((ElementFilter) ((ElementGroup) resultElmt1).get(1)).getExpr().equals(e) );
	}

	@Test
	public void removesFilterWhenProjectionRetainsReferencedVariables() {
		// a filter remains guaranteed when the projection retains all variables it references

		// set up
		final Var v1 = Var.alloc("v1");
		final Var v2 = Var.alloc("v2");
		final FederationMember fm = new SPARQLEndpointForTest("http://exA.org");

		final TriplePattern tp = new TriplePatternImpl(v1, v1, v2);
		final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>( fm, false, new SPARQLRequestImpl(tp) );

		final Expr e = new E_IsIRI( new ExprVar(v1) );
		final SPARQLRequest req = (SPARQLRequest) reqOp.getRequest();

		final SPARQLGraphPattern mergedPattern = req.getQueryPattern().mergeWith(new ExprList(e));
		final SPARQLRequest mergedReq = new SPARQLRequestImpl( mergedPattern, req.getProjectionVars(), req.getDistinctRequired() );
		final LogicalOpRequest<?,?> mergedReqOp = new LogicalOpRequest<>( fm, false, mergedReq );
		final LogicalPlan reqWithFilterPlan = new LogicalPlanWithNullaryRootImpl(mergedReqOp, null);

		final LogicalOpProject projectOp = new LogicalOpProject(Set.of(v1,v2), false);
		final LogicalPlan projectPlan = new LogicalPlanWithUnaryRootImpl(projectOp, null, reqWithFilterPlan);

		// gpAddOp with same filter
		final LogicalOpGPAdd gpAddOp = new LogicalOpGPAdd(fm, mergedPattern, null, false);
		final LogicalPlan gpAddPlan = new LogicalPlanWithUnaryRootImpl(gpAddOp, null, projectPlan);

		// test
		final LogicalPlan result = new RedundantFilterElimination().apply(gpAddPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpGPAdd );
		final LogicalOpGPAdd resultGpAddOp = (LogicalOpGPAdd) result.getRootOperator();

		final Element resultElmt1 = QueryPatternUtils.convertToJenaElement( resultGpAddOp.getPattern() );
		assertTrue(                                        resultElmt1 instanceof ElementGroup );
		assertTrue(                        ((ElementGroup) resultElmt1).size() == 1 );
		assertFalse(                       ((ElementGroup) resultElmt1).get(0) instanceof ElementFilter );
	}

	@Test
	public void retainsFilterWhenUnionBranchDoesNotGuaranteeIt() {
		// a filter is retained when it is not guaranteed in every union branch of
		// a binary union plan

		// set up
		final Var v1 = Var.alloc("v1");
		final FederationMember fm = new SPARQLEndpointForTest("http://exA.org");

		final TriplePattern tp = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>( fm, false, new SPARQLRequestImpl(tp) );

		final Expr e = new E_IsIRI( new ExprVar(v1) );
		final SPARQLRequest req = (SPARQLRequest) reqOp.getRequest();

		final SPARQLGraphPattern mergedPattern = req.getQueryPattern().mergeWith(new ExprList(e));
		final SPARQLRequest mergedReq = new SPARQLRequestImpl( mergedPattern, req.getProjectionVars(), req.getDistinctRequired() );
		final LogicalOpRequest<?,?> mergedReqOp = new LogicalOpRequest<>( fm, false, mergedReq );
		final LogicalPlan reqWithFilterPlan = new LogicalPlanWithNullaryRootImpl(mergedReqOp, null);

		final LogicalPlan unionPlan = new LogicalPlanWithBinaryRootImpl(LogicalOpUnion.getInstance(), null, reqWithFilterPlan, new LogicalPlanWithNullaryRootImpl(reqOp, null));

		final LogicalOpGPAdd gpAddOp = new LogicalOpGPAdd(fm, mergedPattern, null, false);
		final LogicalPlan gpAddPlan = new LogicalPlanWithUnaryRootImpl(gpAddOp, null, unionPlan);

		// test
		final LogicalPlan result = new RedundantFilterElimination().apply(gpAddPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpGPAdd );
		final LogicalOpGPAdd resultGpAddOp = (LogicalOpGPAdd) result.getRootOperator();

		final Element resultElmt1 = QueryPatternUtils.convertToJenaElement( resultGpAddOp.getPattern() );
		assertTrue(                                        resultElmt1 instanceof ElementGroup );
		assertTrue(                        ((ElementGroup) resultElmt1).size() == 2 );
		assertTrue(                        ((ElementGroup) resultElmt1).get(1) instanceof ElementFilter );
		assertTrue(       ((ElementFilter) ((ElementGroup) resultElmt1).get(1)).getExpr().equals(e) );
	}

	@Test
	public void removesFilterWhenEveryUnionBranchGuaranteesIt() {
		// a filter is removed when it is guaranteed in every union branch

		// set up
		final Var v1 = Var.alloc("v1");
		final FederationMember fm = new SPARQLEndpointForTest("http://exA.org");

		final TriplePattern tp = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>( fm, false, new SPARQLRequestImpl(tp) );

		final Expr e = new E_IsIRI( new ExprVar(v1) );
		final SPARQLRequest req = (SPARQLRequest) reqOp.getRequest();

		final SPARQLGraphPattern mergedPattern = req.getQueryPattern().mergeWith(new ExprList(e));
		final SPARQLRequest mergedReq = new SPARQLRequestImpl( mergedPattern, req.getProjectionVars(), req.getDistinctRequired() );
		final LogicalOpRequest<?,?> mergedReqOp = new LogicalOpRequest<>( fm, false, mergedReq );
		final LogicalPlan reqWithFilterPlan1 = new LogicalPlanWithNullaryRootImpl(mergedReqOp, null);
		final LogicalPlan reqWithFilterPlan2 = new LogicalPlanWithNullaryRootImpl(mergedReqOp, null);
		final LogicalPlan reqWithFilterPlan3 = new LogicalPlanWithNullaryRootImpl(mergedReqOp, null);

		final LogicalPlan unionPlan = new LogicalPlanWithNaryRootImpl(LogicalOpMultiwayUnion.getInstance(), null, reqWithFilterPlan1, reqWithFilterPlan2, reqWithFilterPlan3);

		final LogicalOpGPAdd gpAddOp = new LogicalOpGPAdd(fm, mergedPattern, null, false);
		final LogicalPlan gpAddPlan = new LogicalPlanWithUnaryRootImpl(gpAddOp, null, unionPlan);

		// test
		final LogicalPlan result = new RedundantFilterElimination().apply(gpAddPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpGPAdd );
		final LogicalOpGPAdd resultGpAddOp = (LogicalOpGPAdd) result.getRootOperator();

		final Element resultElmt1 = QueryPatternUtils.convertToJenaElement( resultGpAddOp.getPattern() );
		assertTrue(                                        resultElmt1 instanceof ElementGroup );
		assertTrue(                        ((ElementGroup) resultElmt1).size() == 1 );
		assertFalse(                       ((ElementGroup) resultElmt1).get(0) instanceof ElementFilter );
	}

	@Test
	public void doesNotPropagateOptionalSideFilterGuarantees() {
		// a filter guaranteed only by the optional side of a left join is not
		// guaranteed by the left join's output, so the filter is retained

		// set up
		final Var v1 = Var.alloc("v1");
		final FederationMember fm = new SPARQLEndpointForTest("http://exA.org");

		final TriplePattern tp = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>( fm, false, new SPARQLRequestImpl(tp) );

		final Expr e = new E_IsIRI( new ExprVar(v1) );
		final SPARQLRequest req = (SPARQLRequest) reqOp.getRequest();

		final SPARQLGraphPattern mergedPattern = req.getQueryPattern().mergeWith(new ExprList(e));
		final SPARQLRequest mergedReq = new SPARQLRequestImpl( mergedPattern, req.getProjectionVars(), req.getDistinctRequired() );
		final LogicalOpRequest<?,?> mergedReqOp = new LogicalOpRequest<>( fm, false, mergedReq );
		final LogicalPlan reqWithFilterPlan = new LogicalPlanWithNullaryRootImpl(mergedReqOp, null);

		final LogicalPlan leftJoinPlan = new LogicalPlanWithBinaryRootImpl(LogicalOpLeftJoin.getInstance(), null, new LogicalPlanWithNullaryRootImpl(reqOp, null), reqWithFilterPlan);

		final LogicalOpGPAdd gpAddOp = new LogicalOpGPAdd(fm, mergedPattern, null, false);
		final LogicalPlan gpAddPlan = new LogicalPlanWithUnaryRootImpl(gpAddOp, null, leftJoinPlan);

		// test
		final LogicalPlan result = new RedundantFilterElimination().apply(gpAddPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpGPAdd );
		final LogicalOpGPAdd resultGpAddOp = (LogicalOpGPAdd) result.getRootOperator();

		final Element resultElmt1 = QueryPatternUtils.convertToJenaElement( resultGpAddOp.getPattern() );
		assertTrue(                                        resultElmt1 instanceof ElementGroup );
		assertTrue(                        ((ElementGroup) resultElmt1).size() == 2 );
		assertTrue(                        ((ElementGroup) resultElmt1).get(1) instanceof ElementFilter );
		assertTrue(       ((ElementFilter) ((ElementGroup) resultElmt1).get(1)).getExpr().equals(e) );
	}

	@Test
	public void removesRedundantFilterInGPOpt() {
		// a filter in a GPOptAdd operator is redundant if the input plan guarantees it

		// set up
		final Var v1 = Var.alloc("v1");
		final FederationMember fm = new SPARQLEndpointForTest("http://exA.org");

		final TriplePattern tp = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>( fm, false, new SPARQLRequestImpl(tp) );

		final Expr e = new E_IsIRI( new ExprVar(v1) );
		final SPARQLRequest req = (SPARQLRequest) reqOp.getRequest();

		final SPARQLGraphPattern mergedPattern = req.getQueryPattern().mergeWith(new ExprList(e));
		final SPARQLRequest mergedReq = new SPARQLRequestImpl( mergedPattern, req.getProjectionVars(), req.getDistinctRequired() );
		final LogicalOpRequest<?,?> mergedReqOp = new LogicalOpRequest<>( fm, false, mergedReq );
		final LogicalPlan reqWithFilterPlan = new LogicalPlanWithNullaryRootImpl(mergedReqOp, null);

		final LogicalOpGPOptAdd gpOptAddOp = new LogicalOpGPOptAdd(fm, mergedPattern, false);
		final LogicalPlan gpOptAddPlan = new LogicalPlanWithUnaryRootImpl(gpOptAddOp, null, reqWithFilterPlan);

		// test
		final LogicalPlan result = new RedundantFilterElimination().apply(gpOptAddPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpGPOptAdd );
		final LogicalOpGPOptAdd resultGpOptAddOp = (LogicalOpGPOptAdd) result.getRootOperator();

		final Element resultElmt1 = QueryPatternUtils.convertToJenaElement( resultGpOptAddOp.getPattern() );
		assertTrue(                                        resultElmt1 instanceof ElementGroup );
		assertTrue(                        ((ElementGroup) resultElmt1).size() == 1 );
		assertFalse(                       ((ElementGroup) resultElmt1).get(0) instanceof ElementFilter );
	}

	@Test
	public void retainsNonRedundantFilterInGPOpt() {
		// a filter in a GPOptAdd operator must be retained if the input plan does not guarantee it

		// set up
		final Var v1 = Var.alloc("v1");
		final FederationMember fm = new SPARQLEndpointForTest("http://exA.org");

		final TriplePattern tp = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>( fm, false, new SPARQLRequestImpl(tp) );

		final Expr e1 = new E_IsIRI( new ExprVar(v1) );
		final SPARQLRequest req = (SPARQLRequest) reqOp.getRequest();

		final SPARQLGraphPattern mergedPattern1 = req.getQueryPattern().mergeWith(new ExprList(e1));
		final SPARQLRequest mergedReq = new SPARQLRequestImpl( mergedPattern1, req.getProjectionVars(), req.getDistinctRequired() );
		final LogicalOpRequest<?,?> mergedReqOp = new LogicalOpRequest<>( fm, false, mergedReq );
		final LogicalPlan reqWithFilterPlan = new LogicalPlanWithNullaryRootImpl(mergedReqOp, null);

		final Var v2 = Var.alloc("v2");
		final Expr e2 = new E_IsIRI( new ExprVar(v2) );
		final SPARQLGraphPattern mergedPattern2 = req.getQueryPattern().mergeWith(new ExprList(e2));
		final LogicalOpGPOptAdd gpOptAddOp = new LogicalOpGPOptAdd(fm, mergedPattern2, false);
		final LogicalPlan gpOptAddPlan = new LogicalPlanWithUnaryRootImpl(gpOptAddOp, null, reqWithFilterPlan);

		// test
		final LogicalPlan result = new RedundantFilterElimination().apply(gpOptAddPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpGPOptAdd );
		final LogicalOpGPOptAdd resultGpOptAddOp = (LogicalOpGPOptAdd) result.getRootOperator();

		final Element resultElmt1 = QueryPatternUtils.convertToJenaElement( resultGpOptAddOp.getPattern() );
		assertTrue(                                        resultElmt1 instanceof ElementGroup );
		assertTrue(                        ((ElementGroup) resultElmt1).size() == 2 );
		assertTrue(                        ((ElementGroup) resultElmt1).get(1) instanceof ElementFilter );
		assertTrue(       ((ElementFilter) ((ElementGroup) resultElmt1).get(1)).getExpr().equals(e2) );
	}
}
