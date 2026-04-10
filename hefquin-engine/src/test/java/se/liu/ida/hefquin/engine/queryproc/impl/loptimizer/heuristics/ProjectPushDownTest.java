package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Set;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.E_IsIRI;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.junit.Test;

import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.base.query.impl.TriplePatternImpl;
import se.liu.ida.hefquin.engine.EngineTestBase;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanUtils;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBind;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpFilter;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPOptAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpProject;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpUnfold;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpUnion;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithNullaryRootImpl;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithUnaryRootImpl;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.federation.access.impl.req.TriplePatternRequestImpl;

public class ProjectPushDownTest extends EngineTestBase
{
	@Test
	public void pushProjectUnderJoinPossible() {
		// A project on top of a join where variables can be
		// partially pushed to each branch.

		// set up
		final Var v1 = Var.alloc("x");
		final Var v2 = Var.alloc("y");
		final Var v3 = Var.alloc("z");

		// Left request produces {x, y}
		final TriplePattern tp1 = new TriplePatternImpl(v1, v2, v2);
		final LogicalOpRequest<?,?> reqOp1 = new LogicalOpRequest<>(
			new SPARQLEndpointForTest("http://exA.org"),
			new SPARQLRequestImpl(tp1) );

		// Right request produces {y, z}
		final TriplePattern tp2 = new TriplePatternImpl(v2, v3, v3);
		final LogicalOpRequest<?,?> reqOp2 = new LogicalOpRequest<>(
			new SPARQLEndpointForTest("http://exB.org"),
			new SPARQLRequestImpl(tp2) );

		final LogicalPlan joinSubPlan = LogicalPlanUtils.createPlanWithBinaryJoin(
			new LogicalPlanWithNullaryRootImpl(reqOp1, null),
			new LogicalPlanWithNullaryRootImpl(reqOp2, null),
			null );

		// Project only keeps {y}
		final LogicalOpProject projectOp = new LogicalOpProject(Set.of(v2));
		final LogicalPlan projectPlan = new LogicalPlanWithUnaryRootImpl(projectOp, null, joinSubPlan);

		// test
		final LogicalPlan result = new ProjectPushDown().apply(projectPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpProject );

		final LogicalPlan joinResult = result.getSubPlan(0);
		assertTrue( joinResult.getRootOperator() instanceof LogicalOpJoin );

		// Left branch
		final LogicalPlan left = joinResult.getSubPlan(0);
		assertTrue( left.getRootOperator() instanceof LogicalOpProject );

		final LogicalOpProject leftProj = (LogicalOpProject) left.getRootOperator();
		assertEquals( Set.of(v2), leftProj.getVariables() );

		// Right branch
		final LogicalPlan right = joinResult.getSubPlan(1);
		assertTrue( right.getRootOperator() instanceof LogicalOpProject );

		final LogicalOpProject rightProj = (LogicalOpProject) right.getRootOperator();
		assertEquals( Set.of(v2), rightProj.getVariables() );
	}

	@Test
	public void pushProjectUnderJoinImpossible() {
		// A project on top of a join where variables cannot
		// be pushed to any of the branches.

		// set up
		final Var v1 = Var.alloc("x");
		final Var v2 = Var.alloc("y");
		final Var v3 = Var.alloc("z");

		// Left request produces {x, z}
		final TriplePattern tp1 = new TriplePatternImpl(v1, v3, v3);
		final LogicalOpRequest<?,?> reqOp1 = new LogicalOpRequest<>(
			new SPARQLEndpointForTest("http://exA.org"),
			new SPARQLRequestImpl(tp1) );

		// Right request produces {z, x}
		final TriplePattern tp2 = new TriplePatternImpl(v3, v1, v1);
		final LogicalOpRequest<?,?> reqOp2 = new LogicalOpRequest<>(
			new SPARQLEndpointForTest("http://exB.org"),
			new SPARQLRequestImpl(tp2) );

		final LogicalPlan joinSubPlan = LogicalPlanUtils.createPlanWithBinaryJoin(
			new LogicalPlanWithNullaryRootImpl(reqOp1, null),
			new LogicalPlanWithNullaryRootImpl(reqOp2, null),
			null );

		// Project only keeps {y}
		final LogicalOpProject projectOp = new LogicalOpProject(Set.of(v2));
		final LogicalPlan projectPlan = new LogicalPlanWithUnaryRootImpl(projectOp, null, joinSubPlan);

		// test
		final LogicalPlan result = new ProjectPushDown().apply(projectPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpProject );

		final LogicalPlan joinResult = result.getSubPlan(0);
		assertTrue( joinResult.getRootOperator() instanceof LogicalOpJoin );

		// Left branch
		final LogicalPlan left = joinResult.getSubPlan(0);
		assertTrue( left.getRootOperator() instanceof LogicalOpRequest );

		// Right branch
		final LogicalPlan right = joinResult.getSubPlan(1);
		assertTrue( right.getRootOperator() instanceof LogicalOpRequest );
	}

	@Test
	public void pushProjectOverProjectUnderGPOptAdd() {
		// A project on top of another project on top of a gpOptAdd operator.
		// The two project operators have disjoint variable sets and are expected
		// to be merged into a single project operator with an empty set of variables.
		// This resulting project operator is then pushed under the gpOptAdd operator.

		// set up
		final Var v1 = Var.alloc("x");
		final Var v2 = Var.alloc("y");
		final FederationMember fm = new TPFServerForTest();

		final TriplePattern tp1 = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>( fm, new TriplePatternRequestImpl(tp1) );
		final LogicalPlan reqSubPlan = new LogicalPlanWithNullaryRootImpl(reqOp, null);

		final TriplePattern tp2 = new TriplePatternImpl(v1 ,v2, v2);
		final LogicalOpGPOptAdd gpOptAdd = new LogicalOpGPOptAdd(fm, tp2);
		final LogicalPlan gpOptAddSubPlan = new LogicalPlanWithUnaryRootImpl(gpOptAdd, null, reqSubPlan);

		final LogicalOpProject projectOp1 = new LogicalOpProject(Set.of(v1));
		final LogicalPlan projectSubPlan = new LogicalPlanWithUnaryRootImpl(projectOp1, null, gpOptAddSubPlan);

		final LogicalOpProject rootOp = new LogicalOpProject(Set.of(v2));
		final LogicalPlan projectPlan = new LogicalPlanWithUnaryRootImpl(rootOp, null, projectSubPlan);

		// test
		final LogicalPlan result = new ProjectPushDown().apply(projectPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpGPOptAdd );
		assertTrue( result.getRootOperator().equals(gpOptAdd) );

		final LogicalPlan subResult = result.getSubPlan(0);
		assertTrue( subResult.getRootOperator() instanceof LogicalOpProject );

		final LogicalOpProject resultProjectOp = (LogicalOpProject) subResult.getRootOperator();
		assertEquals( 0, resultProjectOp.getVariables().size() );
		assertTrue( resultProjectOp.getVariables().isEmpty() );

		assertTrue( subResult.getSubPlan(0).equals(reqSubPlan) );
	}

	@Test
	public void pushProjectOverProjectUnderUnion() {
		// A project on top of another project on top of a union
		// over two requests; the two projects can be merged and pushed
		// under the union over both requests.

		// set up
		final Var v1 = Var.alloc("x");
		final Var v2 = Var.alloc("y");
		final Var v3 = Var.alloc("z");

		// Left request produces {x, y}
		final TriplePattern tp1 = new TriplePatternImpl(v1, v2, v2);
		final LogicalOpRequest<?,?> reqOp1 = new LogicalOpRequest<>(
			new SPARQLEndpointForTest("http://exA.org"),
			new SPARQLRequestImpl(tp1) );

		// Right request produces {y, z}
		final TriplePattern tp2 = new TriplePatternImpl(v2, v3, v3);
		final LogicalOpRequest<?,?> reqOp2 = new LogicalOpRequest<>(
			new SPARQLEndpointForTest("http://exB.org"),
			new SPARQLRequestImpl(tp2) );

		final LogicalPlan unionSubPlan = LogicalPlanUtils.createPlanWithBinaryUnion(
			new LogicalPlanWithNullaryRootImpl(reqOp1, null),
			new LogicalPlanWithNullaryRootImpl(reqOp2, null),
			null );

		// Project1 keeps {x, y}
		final LogicalOpProject projectOp1 = new LogicalOpProject(Set.of(v1, v2));
		final LogicalPlan projectSubPlan = new LogicalPlanWithUnaryRootImpl(projectOp1, null, unionSubPlan);

		// Project2 only keeps {y}
		final LogicalOpProject projectOp2 = new LogicalOpProject(Set.of(v2));
		final LogicalPlan projectPlan = new LogicalPlanWithUnaryRootImpl(projectOp2, null, projectSubPlan);

		// test
		final LogicalPlan result = new ProjectPushDown().apply(projectPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpUnion );

		// Left branch
		final LogicalPlan left = result.getSubPlan(0);
		assertTrue( left.getRootOperator() instanceof LogicalOpProject );

		final LogicalOpProject leftProj = (LogicalOpProject) left.getRootOperator();
		assertEquals( Set.of(v2), leftProj.getVariables() );

		// Right branch
		final LogicalPlan right = result.getSubPlan(1);
		assertTrue( right.getRootOperator() instanceof LogicalOpProject );

		final LogicalOpProject rightProj = (LogicalOpProject) right.getRootOperator();
		assertEquals( Set.of(v2), rightProj.getVariables() );
	}

	@Test
	public void pushProjectUnderFilterImpossible() {
		// The project removes variables required by the filter expression;
		// hence, it cannot be pushed under the filter.

		// set up
		final Var v1 = Var.alloc("x");
		final Var v2 = Var.alloc("y");

		// Request produces {x, y}
		final TriplePattern tp1 = new TriplePatternImpl(v1, v2, v2);
		final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>(
			new SPARQLEndpointForTest("http://exA.org"),
			new SPARQLRequestImpl(tp1) );
		final LogicalPlan reqSubPlan = new LogicalPlanWithNullaryRootImpl(reqOp, null);


		// Filter operator with y
		final Expr filterExpr = new E_IsIRI( new ExprVar(v2) );
		final LogicalOpFilter filterOp = new LogicalOpFilter(filterExpr);
		final LogicalPlan filterSubPlan = new LogicalPlanWithUnaryRootImpl(filterOp, null, reqSubPlan);

		// Project operator with y
		final LogicalOpProject projectOp = new LogicalOpProject(Set.of(v2));
		final LogicalPlan projectPlan = new LogicalPlanWithUnaryRootImpl(projectOp, null, filterSubPlan);

		// test
		final LogicalPlan result = new ProjectPushDown().apply(projectPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpProject );

		final LogicalPlan subResult = result.getSubPlan(0);
		assertTrue( subResult.getRootOperator() instanceof LogicalOpFilter );

		final ExprList resultExprList = ((LogicalOpFilter) subResult.getRootOperator()).getFilterExpressions();
		assertEquals( 1, resultExprList.size() );

		final ExprList expectedExprList = new ExprList( Arrays.asList(filterExpr) );
		assertTrue( resultExprList.equals(expectedExprList) );

		assertTrue( subResult.getSubPlan(0).getRootOperator().equals(reqOp) );
	}

	@Test
	public void pushProjectUnderBindImpossible() {
		// A project on top of a bind with a TPF request underneath, where the
		// project does not preserve all variables required to evaluate the bind
		// expressions; hence, the project cannot be pushed under the bind.

		// set up
		final Var v1 = Var.alloc("x");
		final Var v2 = Var.alloc("y");
		final FederationMember fm = new TPFServerForTest();

		// Request produces {x, y}
		final TriplePattern tp1 = new TriplePatternImpl(v1, v2, v2);
		final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>( fm, new TriplePatternRequestImpl(tp1) );
		final LogicalPlan reqSubPlan = new LogicalPlanWithNullaryRootImpl(reqOp, null);

		// Bind with y
		final Expr bindExpr = new ExprVar(v2);
		final VarExprList bindExpressions = new VarExprList(v2, bindExpr);
		final LogicalOpBind bindOp = new LogicalOpBind(bindExpressions);
		final LogicalPlan bindSubPlan = new LogicalPlanWithUnaryRootImpl(bindOp, null, reqSubPlan);

		// Project operator with x
		final LogicalOpProject projectOp = new LogicalOpProject(Set.of(v1));
		final LogicalPlan projectPlan = new LogicalPlanWithUnaryRootImpl(projectOp, null, bindSubPlan);

		// test
		final LogicalPlan result = new ProjectPushDown().apply(projectPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpProject );

		final LogicalOpProject resultProj = (LogicalOpProject) result.getRootOperator();
		assertEquals( Set.of(v1), resultProj.getVariables() );

		final LogicalPlan subResult = result.getSubPlan(0);
		assertTrue( subResult.getRootOperator() instanceof LogicalOpBind );
	}

	@Test
	public void pushProjectUnderBindPossible() {
		// A project on top of a bind with a TPF request underneath, where the
		// project preserves all variables required to evaluate the bind
		// expressions; hence, the project can be pushed under the bind.

		// set up
		final Var v1 = Var.alloc("x");
		final Var v2 = Var.alloc("y");
		final FederationMember fm = new TPFServerForTest();

		// Request produces {x, y}
		final TriplePattern tp1 = new TriplePatternImpl(v1, v2, v2);
		final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>( fm, new TriplePatternRequestImpl(tp1) );
		final LogicalPlan reqSubPlan = new LogicalPlanWithNullaryRootImpl(reqOp, null);

		// Bind with y
		final Expr bindExpr = new ExprVar(v2);
		final VarExprList bindExpressions = new VarExprList(v2, bindExpr);
		final LogicalOpBind bindOp = new LogicalOpBind(bindExpressions);
		final LogicalPlan bindSubPlan = new LogicalPlanWithUnaryRootImpl(bindOp, null, reqSubPlan);

		// Project operator with y
		final LogicalOpProject projectOp = new LogicalOpProject(Set.of(v2));
		final LogicalPlan projectPlan = new LogicalPlanWithUnaryRootImpl(projectOp, null, bindSubPlan);

		// test
		final LogicalPlan result = new ProjectPushDown().apply(projectPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpBind );

		final LogicalPlan subResult = result.getSubPlan(0);
		assertTrue( subResult.getRootOperator() instanceof LogicalOpProject );

		final LogicalOpProject resultProj = (LogicalOpProject) subResult.getRootOperator();
		assertEquals( Set.of(v2), resultProj.getVariables() );
	}

	@Test
	public void pushProjectUnderUnfoldImpossible1() {
		// A project on top of an unfold with a TPF request underneath,
		// where the project refers to the first variable assigned by the
		// unfold; hence, the project can *not* be pushed under the unfold

		// set up
		// - request operator
		final FederationMember fm = new TPFServerForTest();
		final Var v1 = Var.alloc("x");
		final TriplePattern tp = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>( fm, new TriplePatternRequestImpl(tp) );
		final LogicalPlan reqSubPlan = new LogicalPlanWithNullaryRootImpl(reqOp, null);

		// - unfold operator
		final Var v2 = Var.alloc("y");
		final Expr unfoldExpr = NodeValue.makeInteger(42);
		final LogicalOpUnfold unfoldOp = new LogicalOpUnfold(unfoldExpr, v2, null);
		final LogicalPlan unfoldPlan = new LogicalPlanWithUnaryRootImpl(unfoldOp, null, reqSubPlan);

		// - project operator
		final LogicalOpProject projectOp = new LogicalOpProject(Set.of(v2));
		final LogicalPlan projectPlan = new LogicalPlanWithUnaryRootImpl(projectOp, null, unfoldPlan);

		// test
		final LogicalPlan result = new ProjectPushDown().apply(projectPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpProject );

		final LogicalPlan subResult = result.getSubPlan(0);
		assertTrue( subResult.getRootOperator() instanceof LogicalOpUnfold );
	}

	@Test
	public void pushProjectUnderUnfoldImpossible2() {
		// A project on top of an unfold with a TPF request underneath,
		// where the project refers to the second variable assigned by the
		// unfold; hence, the project can *not* be pushed under the unfold

		// set up
		// - request operator
		final FederationMember fm = new TPFServerForTest();
		final Var v1 = Var.alloc("x");
		final TriplePattern tp = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>( fm, new TriplePatternRequestImpl(tp) );
		final LogicalPlan reqSubPlan = new LogicalPlanWithNullaryRootImpl(reqOp, null);

		// - unfold operator
		final Var v2 = Var.alloc("y");
		final Expr unfoldExpr = NodeValue.makeInteger(42);
		final LogicalOpUnfold unfoldOp = new LogicalOpUnfold(unfoldExpr, v1, v2);
		final LogicalPlan unfoldPlan = new LogicalPlanWithUnaryRootImpl(unfoldOp, null, reqSubPlan);

		// - project operator
		final LogicalOpProject projectOp = new LogicalOpProject(Set.of(v2));
		final LogicalPlan projectPlan = new LogicalPlanWithUnaryRootImpl(projectOp, null, unfoldPlan);

		// test
		final LogicalPlan result = new ProjectPushDown().apply(projectPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpProject );

		final LogicalPlan subResult = result.getSubPlan(0);
		assertTrue( subResult.getRootOperator() instanceof LogicalOpUnfold );
	}

	@Test
	public void pushProjectUnderUnfoldPossible() {
		// a project on top of an unfold with a TPF request underneath, where
		// the project refers to the variable assigned by the request; hence,
		// the project can be pushed under the unfold but not into the request

		// set up
		// - request operator
		final FederationMember fm = new TPFServerForTest();
		final Var v1 = Var.alloc("x");
		final TriplePattern tp = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>( fm, new TriplePatternRequestImpl(tp) );
		final LogicalPlan reqSubPlan = new LogicalPlanWithNullaryRootImpl(reqOp, null);

		// - unfold operator
		final Var v2 = Var.alloc("y");
		final Expr unfoldExpr = NodeValue.makeInteger(42);
		final LogicalOpUnfold unfoldOp = new LogicalOpUnfold(unfoldExpr, v2, null);
		final LogicalPlan unfoldPlan = new LogicalPlanWithUnaryRootImpl(unfoldOp, null, reqSubPlan);

		// - project operator
		final LogicalOpProject projectOp = new LogicalOpProject(Set.of(v1));
		final LogicalPlan projectPlan = new LogicalPlanWithUnaryRootImpl(projectOp, null, unfoldPlan);

		// test
		final LogicalPlan result = new ProjectPushDown().apply(projectPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpUnfold );

		final LogicalPlan subResult = result.getSubPlan(0);
		assertTrue( subResult.getRootOperator() instanceof LogicalOpProject );
	}

}
