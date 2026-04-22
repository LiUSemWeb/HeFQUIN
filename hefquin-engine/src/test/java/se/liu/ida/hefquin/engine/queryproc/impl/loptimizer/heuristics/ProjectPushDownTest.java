package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.expr.E_IsIRI;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.junit.Test;

import se.liu.ida.hefquin.base.data.impl.SolutionMappingImpl;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.base.query.impl.TriplePatternImpl;
import se.liu.ida.hefquin.engine.EngineTestBase;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanUtils;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBind;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpFilter;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpFixedSolMap;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPOptAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpLocalToGlobal;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpProject;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpUnfold;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpUnion;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithNullaryRootImpl;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithUnaryRootImpl;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.federation.access.impl.req.TriplePatternRequestImpl;
import se.liu.ida.hefquin.federation.members.RESTEndpoint;
import se.liu.ida.hefquin.federation.members.WrappedRESTEndpoint;

public class ProjectPushDownTest extends EngineTestBase
{
	@Test
	public void pushProjectIntoFixedSolMapEqual() {
		// A project on top of a fixed solution mapping operator;
		// set of project variables equal to the set of variables
		// in the fixed solution mapping, so project is removed.

		// set up
		final Var v1 = Var.alloc("x");

		// Fixed solution mapping with x
		final Binding jenaBinding = Binding.builder().add(v1, NodeValue.makeString("a").asNode()).build();
		final LogicalOpFixedSolMap fixedSolMapOp = new LogicalOpFixedSolMap( new SolutionMappingImpl(jenaBinding) );
		final LogicalPlan fixedSolMapPlan = new LogicalPlanWithNullaryRootImpl(fixedSolMapOp, null);

		// Project keeps x
		final LogicalOpProject projectOp = new LogicalOpProject(Set.of(v1), false);
		final LogicalPlan projectPlan = new LogicalPlanWithUnaryRootImpl(projectOp, null, fixedSolMapPlan);

		// test
		final LogicalPlan result = new ProjectPushDown().apply(projectPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpFixedSolMap );
		final LogicalOpFixedSolMap resultFixedSolMap = (LogicalOpFixedSolMap) result.getRootOperator();
		final Binding resultBinding = resultFixedSolMap.getSolutionMapping().asJenaBinding();
		assertEquals( Set.of(v1), resultBinding.varsMentioned() );
		assertEquals( "a", resultBinding.get(v1).getLiteralLexicalForm() );
	}

	@Test
	public void pushProjectIntoFixedSolMapNotEqual() {
		// A project on top of a fixed solution mapping operator;
		// project variables are a subset of the variables in the
		// fixed solution mapping, so fixed solution mapping is
		// projected.

		// set up
		final Var v1 = Var.alloc("x");
		final Var v2 = Var.alloc("y");

		// Fixed solution mapping with x, y
		final BindingBuilder bb = Binding.builder();
		bb.add( v1, NodeValue.makeString("a").asNode() );
		bb.add( v2, NodeValue.makeString("b").asNode() );
		final Binding jenaBinding = bb.build();

		final LogicalOpFixedSolMap fixedSolMapOp = new LogicalOpFixedSolMap( new SolutionMappingImpl(jenaBinding) );
		final LogicalPlan fixedSolMapPlan = new LogicalPlanWithNullaryRootImpl(fixedSolMapOp, null);

		// Project keeps y
		final LogicalOpProject projectOp = new LogicalOpProject(Set.of(v2), false);
		final LogicalPlan projectPlan = new LogicalPlanWithUnaryRootImpl(projectOp, null, fixedSolMapPlan);

		// test
		final LogicalPlan result = new ProjectPushDown().apply(projectPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpFixedSolMap );
		final LogicalOpFixedSolMap resultFixedSolMap = (LogicalOpFixedSolMap) result.getRootOperator();
		final Binding resultBinding = resultFixedSolMap.getSolutionMapping().asJenaBinding();
		assertEquals( Set.of(v2), resultBinding.varsMentioned() );
		assertEquals( "b", resultBinding.get(v2).getLiteralLexicalForm() );
	}

	@Test
	public void pushProjectUnderJoinImpossible() {
		// A project on top of a join where variables cannot
		// be pushed to any of the branches.

		// set up
		final Var v1 = Var.alloc("x");
		final Var v2 = Var.alloc("y");
		final Var v3 = Var.alloc("z");

		// Left request produces x
		final TriplePattern tp1 = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp1 = new LogicalOpRequest<>(
			new SPARQLEndpointForTest("http://exA.org"),
			false,
			new SPARQLRequestImpl(tp1) );

		// Right request produces y
		final TriplePattern tp2 = new TriplePatternImpl(v2, v2, v2);
		final LogicalOpRequest<?,?> reqOp2 = new LogicalOpRequest<>(
			new SPARQLEndpointForTest("http://exB.org"),
			false,
			new SPARQLRequestImpl(tp2) );

		final LogicalPlan joinSubPlan = LogicalPlanUtils.createPlanWithBinaryJoin(
			false,
			new LogicalPlanWithNullaryRootImpl(reqOp1, null),
			new LogicalPlanWithNullaryRootImpl(reqOp2, null),
			null );

		// Project keeps z
		final LogicalOpProject projectOp = new LogicalOpProject(Set.of(v3), false);
		final LogicalPlan projectPlan = new LogicalPlanWithUnaryRootImpl(projectOp, null, joinSubPlan);

		// test
		final LogicalPlan result = new ProjectPushDown().apply(projectPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpProject );

		final LogicalOpProject rootProj = (LogicalOpProject) result.getRootOperator();
		assertEquals( Set.of(v3), rootProj.getVariables() );

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
	public void pushProjectUnderJoinSplit() {
		// A project on top of a join where variables can be
		// partially pushed to each branch.

		// set up
		final Var v1 = Var.alloc("x");
		final Var v2 = Var.alloc("y");
		final Var v3 = Var.alloc("z");

		// Left request produces x, y
		final TriplePattern tp1 = new TriplePatternImpl(v1, v2, v2);
		final LogicalOpRequest<?,?> reqOp1 = new LogicalOpRequest<>(
			new SPARQLEndpointForTest("http://exA.org"),
			false,
			new SPARQLRequestImpl(tp1) );

		// Right request produces x, z
		final TriplePattern tp2 = new TriplePatternImpl(v1, v3, v3);
		final LogicalOpRequest<?,?> reqOp2 = new LogicalOpRequest<>(
			new SPARQLEndpointForTest("http://exB.org"),
			false,
			new SPARQLRequestImpl(tp2) );

		final LogicalPlan joinSubPlan = LogicalPlanUtils.createPlanWithBinaryJoin(
			false,
			new LogicalPlanWithNullaryRootImpl(reqOp1, null),
			new LogicalPlanWithNullaryRootImpl(reqOp2, null),
			null );

		// Project only keeps {y}
		final LogicalOpProject projectOp = new LogicalOpProject(Set.of(v2), false);
		final LogicalPlan projectPlan = new LogicalPlanWithUnaryRootImpl(projectOp, null, joinSubPlan);

		// test
		final LogicalPlan result = new ProjectPushDown().apply(projectPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpProject );

		final LogicalOpProject rootProj = (LogicalOpProject) result.getRootOperator();
		assertEquals( Set.of(v2), rootProj.getVariables() );

		final LogicalPlan joinResult = result.getSubPlan(0);
		assertTrue( joinResult.getRootOperator() instanceof LogicalOpJoin );

		// Left branch
		final LogicalPlan left = joinResult.getSubPlan(0);
		assertTrue( left.getRootOperator() instanceof LogicalOpProject );

		final LogicalOpProject leftProj = (LogicalOpProject) left.getRootOperator();
		assertEquals( Set.of(v1,v2), leftProj.getVariables() );

		// Right branch
		final LogicalPlan right = joinResult.getSubPlan(1);
		assertTrue( right.getRootOperator() instanceof LogicalOpProject );

		final LogicalOpProject rightProj = (LogicalOpProject) right.getRootOperator();
		assertEquals( Set.of(v1), rightProj.getVariables() );
	}

	@Test
	public void pushProjectUnderJoinWhole() {
		// A project on top of a join where variables can be
		// fully pushed to each branch.

		// set up
		final Var v1 = Var.alloc("x");
		final Var v2 = Var.alloc("y");
		final Var v3 = Var.alloc("z");

		// Left request produces x, y
		final TriplePattern tp1 = new TriplePatternImpl(v1, v2, v2);
		final LogicalOpRequest<?,?> reqOp1 = new LogicalOpRequest<>(
			new SPARQLEndpointForTest("http://exA.org"),
			false,
			new SPARQLRequestImpl(tp1) );

		// Right request produces y, z
		final TriplePattern tp2 = new TriplePatternImpl(v2, v3, v3);
		final LogicalOpRequest<?,?> reqOp2 = new LogicalOpRequest<>(
			new SPARQLEndpointForTest("http://exB.org"),
			false,
			new SPARQLRequestImpl(tp2) );

		final LogicalPlan joinSubPlan = LogicalPlanUtils.createPlanWithBinaryJoin(
			false,
			new LogicalPlanWithNullaryRootImpl(reqOp1, null),
			new LogicalPlanWithNullaryRootImpl(reqOp2, null),
			null );

		// Project only keeps y
		final LogicalOpProject projectOp = new LogicalOpProject(Set.of(v2), false);
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
		final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>( fm, false, new TriplePatternRequestImpl(tp1) );
		final LogicalPlan reqSubPlan = new LogicalPlanWithNullaryRootImpl(reqOp, null);

		final TriplePattern tp2 = new TriplePatternImpl(v1 ,v2, v2);
		final LogicalOpGPOptAdd gpOptAdd = new LogicalOpGPOptAdd(fm, tp2, false);
		final LogicalPlan gpOptAddSubPlan = new LogicalPlanWithUnaryRootImpl(gpOptAdd, null, reqSubPlan);

		final LogicalOpProject projectOp1 = new LogicalOpProject(Set.of(v1), false);
		final LogicalPlan projectSubPlan = new LogicalPlanWithUnaryRootImpl(projectOp1, null, gpOptAddSubPlan);

		final LogicalOpProject rootOp = new LogicalOpProject(Set.of(v2), false);
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
			false,
			new SPARQLRequestImpl(tp1) );

		// Right request produces {y, z}
		final TriplePattern tp2 = new TriplePatternImpl(v2, v3, v3);
		final LogicalOpRequest<?,?> reqOp2 = new LogicalOpRequest<>(
			new SPARQLEndpointForTest("http://exB.org"),
			false,
			new SPARQLRequestImpl(tp2) );

		final LogicalPlan unionSubPlan = LogicalPlanUtils.createPlanWithBinaryUnion(
			false,
			new LogicalPlanWithNullaryRootImpl(reqOp1, null),
			new LogicalPlanWithNullaryRootImpl(reqOp2, null),
			null );

		// Project1 keeps {x, y}
		final LogicalOpProject projectOp1 = new LogicalOpProject(Set.of(v1, v2), false);
		final LogicalPlan projectSubPlan = new LogicalPlanWithUnaryRootImpl(projectOp1, null, unionSubPlan);

		// Project2 only keeps {y}
		final LogicalOpProject projectOp2 = new LogicalOpProject(Set.of(v2), false);
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
			false,
			new SPARQLRequestImpl(tp1) );
		final LogicalPlan reqSubPlan = new LogicalPlanWithNullaryRootImpl(reqOp, null);


		// Filter operator with y
		final Expr filterExpr = new E_IsIRI( new ExprVar(v2) );
		final LogicalOpFilter filterOp = new LogicalOpFilter(filterExpr, false);
		final LogicalPlan filterSubPlan = new LogicalPlanWithUnaryRootImpl(filterOp, null, reqSubPlan);

		// Project operator with y
		final LogicalOpProject projectOp = new LogicalOpProject(Set.of(v2), false);
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
		final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>( fm, false, new TriplePatternRequestImpl(tp1) );
		final LogicalPlan reqSubPlan = new LogicalPlanWithNullaryRootImpl(reqOp, null);

		// Bind y with the value of x
		final Expr bindExpr = new ExprVar(v1);
		final VarExprList bindExpressions = new VarExprList(v2, bindExpr);
		final LogicalOpBind bindOp = new LogicalOpBind(bindExpressions, false);
		final LogicalPlan bindSubPlan = new LogicalPlanWithUnaryRootImpl(bindOp, null, reqSubPlan);

		// Project operator with x
		final LogicalOpProject projectOp = new LogicalOpProject(Set.of(v2), false);
		final LogicalPlan projectPlan = new LogicalPlanWithUnaryRootImpl(projectOp, null, bindSubPlan);

		// test
		final LogicalPlan result = new ProjectPushDown().apply(projectPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpProject );

		final LogicalOpProject resultProj = (LogicalOpProject) result.getRootOperator();
		assertEquals( Set.of(v2), resultProj.getVariables() );

		final LogicalPlan subResult = result.getSubPlan(0);
		assertTrue( subResult.getRootOperator() instanceof LogicalOpBind );
	}

	@Test
	public void pushProjectUnderBindSplit() {
		// A project on top of a bind with a TPF request underneath, where the
		// bind expression depends on a variable that is not projected.
		// The rewrite extends the pushed projection to include variables
		// required by the bind expression, while removing variables assigned
		// by the bind. As a result, the project is split. A reduced (but
		// extended) project is pushed below the bind, and the original
		// project remains on top.

		// set up
		final Var v1 = Var.alloc("x");
		final Var v2 = Var.alloc("y");
		final Var v3 = Var.alloc("z");
		final FederationMember fm = new TPFServerForTest();

		// Request produces {x, y}
		final TriplePattern tp1 = new TriplePatternImpl(v1, v2, v2);
		final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>( fm, false, new TriplePatternRequestImpl(tp1) );
		final LogicalPlan reqSubPlan = new LogicalPlanWithNullaryRootImpl(reqOp, null);

		// Bind y with the value of z
		final Expr bindExpr = new ExprVar(v3);
		final VarExprList bindExpressions = new VarExprList(v2, bindExpr);
		final LogicalOpBind bindOp = new LogicalOpBind(bindExpressions, false);
		final LogicalPlan bindSubPlan = new LogicalPlanWithUnaryRootImpl(bindOp, null, reqSubPlan);

		// Project operator with x, y
		final LogicalOpProject projectOp = new LogicalOpProject(Set.of(v1,v2), false);
		final LogicalPlan projectPlan = new LogicalPlanWithUnaryRootImpl(projectOp, null, bindSubPlan);

		// test
		final LogicalPlan result = new ProjectPushDown().apply(projectPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpProject );

		final LogicalOpProject resultProj1 = (LogicalOpProject) result.getRootOperator();
		assertEquals( Set.of(v1,v2), resultProj1.getVariables() );

		final LogicalPlan subResult = result.getSubPlan(0);
		assertTrue( subResult.getRootOperator() instanceof LogicalOpBind );

		final LogicalPlan subsubResult = subResult.getSubPlan(0);
		assertTrue( subsubResult.getRootOperator() instanceof LogicalOpProject );

		final LogicalOpProject resultProj2 = (LogicalOpProject) subsubResult.getRootOperator();
		assertEquals( Set.of(v1,v3), resultProj2.getVariables() );

		// pushed project must not contain bind target vars
		final Set<Var> bindVars = new HashSet<>(bindOp.getBindExpressions().getVars());
		assertFalse(resultProj2.getVariables().containsAll(bindVars));
	}

	@Test
	public void pushProjectUnderBindWhole() {
		// Project removes variables required by the bind expression (x),
		// therefore pushdown is blocked.

		// set up
		final Var v1 = Var.alloc("x");
		final Var v2 = Var.alloc("y");
		final FederationMember fm = new TPFServerForTest();

		// Request produces {x, y}
		final TriplePattern tp1 = new TriplePatternImpl(v1, v2, v2);
		final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>( fm, false, new TriplePatternRequestImpl(tp1) );
		final LogicalPlan reqSubPlan = new LogicalPlanWithNullaryRootImpl(reqOp, null);

		// Bind y = constant
		final Expr bindExpr = NodeValue.makeInteger(42);
		final VarExprList bindExpressions = new VarExprList(v2, bindExpr);
		final LogicalOpBind bindOp = new LogicalOpBind(bindExpressions, false);
		final LogicalPlan bindSubPlan = new LogicalPlanWithUnaryRootImpl(bindOp, null, reqSubPlan);

		// Project operator with x
		final LogicalOpProject projectOp = new LogicalOpProject(Set.of(v1), false);
		final LogicalPlan projectPlan = new LogicalPlanWithUnaryRootImpl(projectOp, null, bindSubPlan);

		// test
		final LogicalPlan result = new ProjectPushDown().apply(projectPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpBind );

		final LogicalPlan subResult = result.getSubPlan(0);
		assertTrue( subResult.getRootOperator() instanceof LogicalOpProject );

		final LogicalOpProject resultProj1 = (LogicalOpProject) subResult.getRootOperator();
		assertEquals( Set.of(v1), resultProj1.getVariables() );
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
		final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>( fm, false, new TriplePatternRequestImpl(tp) );
		final LogicalPlan reqSubPlan = new LogicalPlanWithNullaryRootImpl(reqOp, null);

		// - unfold operator
		final Var v2 = Var.alloc("y");
		final Expr unfoldExpr = NodeValue.makeInteger(42);
		final LogicalOpUnfold unfoldOp = new LogicalOpUnfold(unfoldExpr, v2, null, false);
		final LogicalPlan unfoldPlan = new LogicalPlanWithUnaryRootImpl(unfoldOp, null, reqSubPlan);

		// - project operator
		final LogicalOpProject projectOp = new LogicalOpProject(Set.of(v2), false);
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
		final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>( fm, false, new TriplePatternRequestImpl(tp) );
		final LogicalPlan reqSubPlan = new LogicalPlanWithNullaryRootImpl(reqOp, null);

		// - unfold operator
		final Var v2 = Var.alloc("y");
		final Expr unfoldExpr = NodeValue.makeInteger(42);
		final LogicalOpUnfold unfoldOp = new LogicalOpUnfold(unfoldExpr, v1, v2, false);
		final LogicalPlan unfoldPlan = new LogicalPlanWithUnaryRootImpl(unfoldOp, null, reqSubPlan);

		// - project operator
		final LogicalOpProject projectOp = new LogicalOpProject(Set.of(v2), false);
		final LogicalPlan projectPlan = new LogicalPlanWithUnaryRootImpl(projectOp, null, unfoldPlan);

		// test
		final LogicalPlan result = new ProjectPushDown().apply(projectPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpProject );

		final LogicalPlan subResult = result.getSubPlan(0);
		assertTrue( subResult.getRootOperator() instanceof LogicalOpUnfold );
	}

	@Test
	public void pushProjectUnderUnfoldSplit() {
		// A project on top of an unfold over a request where the project
		// contains variables both needed by the unfold expression and
		// produced by the request. The project is therefore split:
		// part of it remains above the unfold, while the remainder is
		// pushed below the unfold.

		// set up
		// - request operator
		final Var v1 = Var.alloc("x");
		final FederationMember fm = new TPFServerForTest();
		final TriplePattern tp = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>( fm, false, new TriplePatternRequestImpl(tp) );
		final LogicalPlan reqSubPlan = new LogicalPlanWithNullaryRootImpl(reqOp, null);

		// Unfold operator with x as var1
		final Var v2 = Var.alloc("y");
		final Expr unfoldExpr = new ExprVar(v2);
		final LogicalOpUnfold unfoldOp = new LogicalOpUnfold(unfoldExpr, v1, null, false);
		final LogicalPlan unfoldPlan = new LogicalPlanWithUnaryRootImpl(unfoldOp, null, reqSubPlan);

		// Project operator with x, y
		final Var v3 = Var.alloc("z");
		final LogicalOpProject projectOp = new LogicalOpProject(Set.of(v1,v2,v3), false);
		final LogicalPlan projectPlan = new LogicalPlanWithUnaryRootImpl(projectOp, null, unfoldPlan);

		// test
		final LogicalPlan result = new ProjectPushDown().apply(projectPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpProject );
		final LogicalOpProject resultProj1 = (LogicalOpProject) result.getRootOperator();
		assertEquals( Set.of(v1,v2,v3), resultProj1.getVariables() );

		final LogicalPlan subResult = result.getSubPlan(0);
		assertTrue( subResult.getRootOperator() instanceof LogicalOpUnfold );

		final LogicalPlan subsubResult = subResult.getSubPlan(0);
		assertTrue( subsubResult.getRootOperator() instanceof LogicalOpProject );

		final LogicalOpProject resultProj2 = (LogicalOpProject) subsubResult.getRootOperator();
		assertEquals( Set.of(v2,v3), resultProj2.getVariables() );
	}

	@Test
	public void pushProjectUnderUnfoldWhole() {
		// A project on top of an unfold with a TPF request underneath, where
		// the project refers to the variable assigned by the request; hence,
		// the whole project can be pushed under the unfold but not into the request

		// set up
		// - request operator
		final FederationMember fm = new TPFServerForTest();
		final Var v1 = Var.alloc("x");
		final TriplePattern tp = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>( fm, false, new TriplePatternRequestImpl(tp) );
		final LogicalPlan reqSubPlan = new LogicalPlanWithNullaryRootImpl(reqOp, null);

		// - unfold operator
		final Var v2 = Var.alloc("y");
		final Expr unfoldExpr = NodeValue.makeInteger(42);
		final LogicalOpUnfold unfoldOp = new LogicalOpUnfold(unfoldExpr, v2, null, false);
		final LogicalPlan unfoldPlan = new LogicalPlanWithUnaryRootImpl(unfoldOp, null, reqSubPlan);

		// - project operator
		final LogicalOpProject projectOp = new LogicalOpProject(Set.of(v1), false);
		final LogicalPlan projectPlan = new LogicalPlanWithUnaryRootImpl(projectOp, null, unfoldPlan);

		// test
		final LogicalPlan result = new ProjectPushDown().apply(projectPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpUnfold );

		final LogicalPlan subResult = result.getSubPlan(0);
		assertTrue( subResult.getRootOperator() instanceof LogicalOpProject );

		final LogicalOpProject resultProj1 = (LogicalOpProject) subResult.getRootOperator();
		assertEquals( Set.of(v1), resultProj1.getVariables() );
	}

	@Test
	public void pushProjectUnderL2g() {
		// A project on top of an l2g operator with a TPF request underneath
		// The project and l2g operator are expected to swap places.

		// set up
		// - request operator
		final FederationMember fm = new TPFServerForTest();
		final Var v1 = Var.alloc("x");
		final TriplePattern tp = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>( fm, false, new TriplePatternRequestImpl(tp) );
		final LogicalPlan reqSubPlan = new LogicalPlanWithNullaryRootImpl(reqOp, null);

		// - l2g operator
		final LogicalOpLocalToGlobal l2gOp = new LogicalOpLocalToGlobal( null, false);
		final LogicalPlan l2gPlan = new LogicalPlanWithUnaryRootImpl(l2gOp, null, reqSubPlan);

		// - project operator
		final LogicalOpProject projectOp = new LogicalOpProject(Set.of(v1), false);
		final LogicalPlan projectPlan = new LogicalPlanWithUnaryRootImpl(projectOp, null, l2gPlan);

		// test
		final LogicalPlan result = new ProjectPushDown().apply(projectPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpLocalToGlobal );

		final LogicalPlan subResult = result.getSubPlan(0);
		assertTrue( subResult.getRootOperator() instanceof LogicalOpProject );

		final LogicalPlan subsubResult = subResult.getSubPlan(0);
		assertTrue( subsubResult.getRootOperator() instanceof LogicalOpRequest );
	}

	@Test
	public void pushProjectUnderAddCaseImpossible() {
		// Verifies that a projection cannot be pushed below GPAdd when it
		// would lose required variables, so the project remains on top.

		// set up
		// - request operator
		final FederationMember fm = new TPFServerForTest();
		final Var v1 = Var.alloc("x");
		final TriplePattern tp = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>( fm, false, new TriplePatternRequestImpl(tp) );
		final LogicalPlan reqSubPlan = new LogicalPlanWithNullaryRootImpl(reqOp, null);

		// - gpAdd operator
		final LogicalOpGPAdd gpAddOp = new LogicalOpGPAdd(fm, tp, null, false);
		final LogicalPlan gpAddPlan = new LogicalPlanWithUnaryRootImpl(gpAddOp, null, reqSubPlan);

		// - project operator
		final Var v2 = Var.alloc("y");
		final LogicalOpProject projectOp = new LogicalOpProject(Set.of(v2), false);
		final LogicalPlan projectPlan = new LogicalPlanWithUnaryRootImpl(projectOp, null, gpAddPlan);

		// test
		final LogicalPlan result = new ProjectPushDown().apply(projectPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpProject );

		final LogicalOpProject resultProj1 = (LogicalOpProject) result.getRootOperator();
		assertEquals( Set.of(v2), resultProj1.getVariables() );

		final LogicalPlan subResult = result.getSubPlan(0);
		assertTrue( subResult.getRootOperator() instanceof LogicalOpGPAdd );

		final LogicalPlan subsubResult = subResult.getSubPlan(0);
		assertTrue( subsubResult.getRootOperator() instanceof LogicalOpRequest );
	}

	@Test
	public void pushProjectUnderAddSplit() {
		// Verifies partial pushdown of a projection into GPAdd:
		// - top projection is preserved
		// - a reduced projection is pushed below GPAdd where safe

		// set up
		// - request operator
		final FederationMember fm = new TPFServerForTest();
		final Var v1 = Var.alloc("x");
		final Var v2 = Var.alloc("y");
		final TriplePattern tp = new TriplePatternImpl(v1, v2, v2);
		final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>( fm, false, new TriplePatternRequestImpl(tp) );
		final LogicalPlan reqSubPlan = new LogicalPlanWithNullaryRootImpl(reqOp, null);

		// gpAdd operator with x, y
		final LogicalOpGPAdd gpAddOp = new LogicalOpGPAdd(fm, tp, null, false);
		final LogicalPlan gpAddPlan = new LogicalPlanWithUnaryRootImpl(gpAddOp, null, reqSubPlan);

		// Project operator with y, z
		final Var v3 = Var.alloc("z");
		final LogicalOpProject projectOp = new LogicalOpProject(Set.of(v2,v3), false);
		final LogicalPlan projectPlan = new LogicalPlanWithUnaryRootImpl(projectOp, null, gpAddPlan);

		// test
		final LogicalPlan result = new ProjectPushDown().apply(projectPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpProject );

		final LogicalOpProject resultProj1 = (LogicalOpProject) result.getRootOperator();
		assertEquals( Set.of(v2,v3), resultProj1.getVariables() );

		final LogicalPlan subResult = result.getSubPlan(0);
		assertTrue( subResult.getRootOperator() instanceof LogicalOpGPAdd );

		final LogicalPlan subsubResult = subResult.getSubPlan(0);
		assertTrue( subsubResult.getRootOperator() instanceof LogicalOpProject );

		final LogicalOpProject resultProj2 = (LogicalOpProject) subsubResult.getRootOperator();
		assertEquals( Set.of(v2), resultProj2.getVariables() );

		final LogicalPlan subsubsubResult = subsubResult.getSubPlan(0);
		assertTrue( subsubsubResult.getRootOperator() instanceof LogicalOpRequest );
	}

	@Test
	public void pushProjectUnderAddWhole() {
		// Verifies full pushdown of a projection into GPAdd when all
		// required variables are available in the subplan.

		// set up
		// - request operator
		final FederationMember fm = new TPFServerForTest();
		final Var v1 = Var.alloc("x");
		final TriplePattern tp = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>( fm, false, new TriplePatternRequestImpl(tp) );
		final LogicalPlan reqSubPlan = new LogicalPlanWithNullaryRootImpl(reqOp, null);

		// gpAdd operator with x
		final LogicalOpGPAdd gpAddOp = new LogicalOpGPAdd(fm, tp, null, false);
		final LogicalPlan gpAddPlan = new LogicalPlanWithUnaryRootImpl(gpAddOp, null, reqSubPlan);

		// Project operator with x
		final LogicalOpProject projectOp = new LogicalOpProject(Set.of(v1), false);
		final LogicalPlan projectPlan = new LogicalPlanWithUnaryRootImpl(projectOp, null, gpAddPlan);

		// test
		final LogicalPlan result = new ProjectPushDown().apply(projectPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpGPAdd );

		final LogicalPlan subResult = result.getSubPlan(0);
		assertTrue( subResult.getRootOperator() instanceof LogicalOpProject );

		final LogicalOpProject resultProj1 = (LogicalOpProject) subResult.getRootOperator();
		assertEquals( Set.of(v1), resultProj1.getVariables() );

		final LogicalPlan subsubResult = subResult.getSubPlan(0);
		assertTrue( subsubResult.getRootOperator() instanceof LogicalOpRequest );
	}

	@Test
	public void pushProjectUnderAddWithParamVar() {
		// Verifies that projection is not pushed below GPAdd when it would
		// remove required parameter variables for the endpoint.

		// set up
		// endpoint with required param "lat"
		final RESTEndpoint.Parameter param = new RESTEndpoint.Parameter() {
			@Override public String getName() { return "lat"; }
			@Override public RDFDatatype getType() { return XSDDatatype.XSDdouble; }
			@Override public boolean isRequired() { return true; }
		};

		final WrappedRESTEndpoint fm = new WrappedRESTEndpointForTest(
						null,
						null,
						List.of(param) );

		final Var v1 = Var.alloc("x");
		final Var v2 = Var.alloc("y");

		// Request operator with x
		final TriplePattern tp = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>( fm, false, new TriplePatternRequestImpl(tp) );
		final LogicalPlan reqSubPlan = new LogicalPlanWithNullaryRootImpl(reqOp, null);

		// gpAdd operator with x
		final Map<String,Var> paramVar = new HashMap<>();
		paramVar.put("lat", v2);
		final LogicalOpGPAdd gpAddOp = new LogicalOpGPAdd(fm, tp, paramVar, false);
		final LogicalPlan gpAddPlan = new LogicalPlanWithUnaryRootImpl(gpAddOp, null, reqSubPlan);

		// Project operator with x
		final LogicalOpProject projectOp = new LogicalOpProject(Set.of(v1), false);
		final LogicalPlan projectPlan = new LogicalPlanWithUnaryRootImpl(projectOp, null, gpAddPlan);

		// test
		final LogicalPlan result = new ProjectPushDown().apply(projectPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpProject );

		final LogicalOpProject resultProj1 = (LogicalOpProject) result.getRootOperator();
		assertEquals( Set.of(v1), resultProj1.getVariables() );

		final LogicalPlan subResult = result.getSubPlan(0);
		assertTrue( subResult.getRootOperator() instanceof LogicalOpGPAdd );

		final LogicalPlan subsubResult = subResult.getSubPlan(0);
		assertTrue( subsubResult.getRootOperator() instanceof LogicalOpRequest );
	}

}
