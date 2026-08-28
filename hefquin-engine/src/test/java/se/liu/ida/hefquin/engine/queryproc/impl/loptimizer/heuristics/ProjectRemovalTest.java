package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics;

import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.junit.Test;

import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.base.query.impl.TriplePatternImpl;
import se.liu.ida.hefquin.engine.EngineTestBase;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanUtils;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBind;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpProject;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithNullaryRootImpl;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithUnaryRootImpl;
import se.liu.ida.hefquin.federation.access.impl.req.SPARQLRequestImpl;

public class ProjectRemovalTest extends EngineTestBase
{
	@Test
	public void removeUnnecessaryProject() {
		// A project with mayReduce = false is removed.

		// set up
		final Var v1 = Var.alloc("x");
		final Var v2 = Var.alloc("y");

		// Left request produces x
		final TriplePattern tp1 = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp1 = new LogicalOpRequest<>(
			new SPARQLEndpointForTest("http://exA.org"),
			false,
			new SPARQLRequestImpl(tp1) );
		final LogicalPlan reqPlan = new LogicalPlanWithNullaryRootImpl(reqOp1, null);

		// Project keeps y
		final LogicalOpProject projectOp = new LogicalOpProject(Set.of(v2), false);
		final LogicalPlan projectPlan = new LogicalPlanWithUnaryRootImpl(projectOp, null, reqPlan);

		// test
		final LogicalPlan result = new ProjectRemoval().apply(projectPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpRequest );
	}

	@Test
	public void keepNecessaryProject() {
		// A project with mayReduce = true is retained.

		// set up
		final Var v1 = Var.alloc("x");
		final Var v2 = Var.alloc("y");

		// Left request produces x
		final TriplePattern tp1 = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp1 = new LogicalOpRequest<>(
			new SPARQLEndpointForTest("http://exA.org"),
			false,
			new SPARQLRequestImpl(tp1) );
		final LogicalPlan reqPlan = new LogicalPlanWithNullaryRootImpl(reqOp1, null);

		// Project keeps y
		final LogicalOpProject projectOp = new LogicalOpProject(Set.of(v2), true);
		final LogicalPlan projectPlan = new LogicalPlanWithUnaryRootImpl(projectOp, null, reqPlan);

		// test
		final LogicalPlan result = new ProjectRemoval().apply(projectPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpProject );
	}

	@Test
	public void removeProjectInSubPlan() {
		// A project with mayReduce = false in a subplan is removed.

		// set up
		final Var v1 = Var.alloc("x");
		final Var v2 = Var.alloc("y");

		// Left request produces x, y
		final TriplePattern tp1 = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp1 = new LogicalOpRequest<>(
			new SPARQLEndpointForTest("http://exA.org"),
			false,
			new SPARQLRequestImpl(tp1) );

		// Right request produces y, z
		final TriplePattern tp2 = new TriplePatternImpl(v2, v2, v2);
		final LogicalOpRequest<?,?> reqOp2 = new LogicalOpRequest<>(
			new SPARQLEndpointForTest("http://exB.org"),
			false,
			new SPARQLRequestImpl(tp2) );

		// Project only keeps y
		final LogicalOpProject projectOp = new LogicalOpProject(Set.of(v2), false);
		final LogicalPlan projectSubPlan = new LogicalPlanWithUnaryRootImpl(projectOp, null, new LogicalPlanWithNullaryRootImpl(reqOp1, null));

		final LogicalPlan joinPlan = LogicalPlanUtils.createPlanWithBinaryJoin(
			false,
			projectSubPlan,
			new LogicalPlanWithNullaryRootImpl(reqOp2, null),
			null );

		// test
		final LogicalPlan result = new ProjectRemoval().apply(joinPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpJoin );

		final LogicalPlan leftChild = result.getSubPlan(0);
		assertTrue( leftChild.getRootOperator() instanceof LogicalOpRequest );

		final LogicalPlan rightChild = result.getSubPlan(1);
		assertTrue( rightChild.getRootOperator() instanceof LogicalOpRequest );
	}

	@Test
	public void keepProjectInSubPlan() {
		// A project with mayReduce = true in a subplan is retained.

		// set up
		final Var v1 = Var.alloc("x");
		final Var v2 = Var.alloc("y");

		// Left request produces x, y
		final TriplePattern tp1 = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp1 = new LogicalOpRequest<>(
			new SPARQLEndpointForTest("http://exA.org"),
			false,
			new SPARQLRequestImpl(tp1) );

		// Right request produces y, z
		final TriplePattern tp2 = new TriplePatternImpl(v2, v2, v2);
		final LogicalOpRequest<?,?> reqOp2 = new LogicalOpRequest<>(
			new SPARQLEndpointForTest("http://exB.org"),
			false,
			new SPARQLRequestImpl(tp2) );

		// Project only keeps y
		final LogicalOpProject projectOp = new LogicalOpProject(Set.of(v2), true);
		final LogicalPlan projectSubPlan = new LogicalPlanWithUnaryRootImpl(projectOp, null, new LogicalPlanWithNullaryRootImpl(reqOp1, null));

		final LogicalPlan joinPlan = LogicalPlanUtils.createPlanWithBinaryJoin(
			false,
			projectSubPlan,
			new LogicalPlanWithNullaryRootImpl(reqOp2, null),
			null );

		// test
		final LogicalPlan result = new ProjectRemoval().apply(joinPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpJoin );

		final LogicalPlan leftChild = result.getSubPlan(0);
		assertTrue( leftChild.getRootOperator() instanceof LogicalOpProject );

		final LogicalPlan rightChild = result.getSubPlan(1);
		assertTrue( rightChild.getRootOperator() instanceof LogicalOpRequest );
	}

	@Test
	public void noProjectPlanUnchanged() {
		// No project operator present and the
		// resulting plan after the heuristic is applied
		// is expected to be unchanged.

		// set up
		final Var v1 = Var.alloc("x");
		final Var v2 = Var.alloc("y");

		// Left request produces x
		final TriplePattern tp1 = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp1 = new LogicalOpRequest<>(
			new SPARQLEndpointForTest("http://exA.org"),
			false,
			new SPARQLRequestImpl(tp1) );
		final LogicalPlan reqPlan = new LogicalPlanWithNullaryRootImpl(reqOp1, null);

		// Project keeps y
		final Expr bindExpr = new ExprVar(v1);
		final VarExprList bindExpressions = new VarExprList(v2, bindExpr);
		final LogicalOpBind bindOp = new LogicalOpBind(bindExpressions, false);
		final LogicalPlan bindPlan = new LogicalPlanWithUnaryRootImpl(bindOp, null, reqPlan);

		// test
		final LogicalPlan result = new ProjectRemoval().apply(bindPlan);

		// check
		assertTrue( bindPlan.equals(result) );
	}
}
