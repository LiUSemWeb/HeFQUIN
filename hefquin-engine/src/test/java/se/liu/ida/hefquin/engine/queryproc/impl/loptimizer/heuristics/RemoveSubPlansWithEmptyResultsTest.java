package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static se.liu.ida.hefquin.engine.queryplan.info.QueryPlanProperty.CARDINALITY;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;
import org.junit.Test;

import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.base.query.impl.TriplePatternImpl;
import se.liu.ida.hefquin.engine.EngineTestBase;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanProperty;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanProperty.Quality;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanUtils;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBind;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpLeftJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayUnion;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpUnion;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithBinaryRootImpl;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithNaryRootImpl;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithNullaryRootImpl;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithUnaryRootImpl;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithoutResult;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.impl.req.SPARQLRequestImpl;

public class RemoveSubPlansWithEmptyResultsTest extends EngineTestBase
{
	@Test
	public void removeEmptySubPlansRemoveUnionKeepSubPlan() {
		// Union operator with one empty and one non-empty branch.
		// Empty branch is removed and union collapses to the remaining subplan.

		// Empty request
		final Var v1 = Var.alloc("x");
		final FederationMember fm = new TPFServerForTest();
		final TriplePattern tp = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>( fm, false, new SPARQLRequestImpl(tp) );

		final LogicalPlan leftReqPlan = new LogicalPlanWithNullaryRootImpl(reqOp, null);
		final QueryPlanningInfo leftQPInfo = leftReqPlan.getQueryPlanningInfo();
		leftQPInfo.addProperty( QueryPlanProperty.cardinality(0, Quality.ACCURATE) );
		leftQPInfo.addProperty( QueryPlanProperty.maxCardinality(0, Quality.ACCURATE) );
		leftQPInfo.addProperty( QueryPlanProperty.minCardinality(0, Quality.ACCURATE) );

		// Bind operator above request
		final Expr bindExpr = NodeValue.makeInteger(42);
		final VarExprList bindExpressions = new VarExprList(v1, bindExpr);
		final LogicalOpBind bindOp = new LogicalOpBind(bindExpressions, false);
		final LogicalPlan bindPlan = new LogicalPlanWithUnaryRootImpl(bindOp, null, leftReqPlan);

		// Request with non-zero cardinality
		final LogicalPlan rightReqPlan = new LogicalPlanWithNullaryRootImpl(reqOp, null);
		final QueryPlanningInfo rightQPInfo = rightReqPlan.getQueryPlanningInfo();
		rightQPInfo.addProperty( QueryPlanProperty.cardinality(42, Quality.ACCURATE) );
		rightQPInfo.addProperty( QueryPlanProperty.maxCardinality(42, Quality.ACCURATE) );
		rightQPInfo.addProperty( QueryPlanProperty.minCardinality(42, Quality.ACCURATE) );

		// Union at the top
		final LogicalPlan unionPlan = new LogicalPlanWithBinaryRootImpl( LogicalOpUnion.getInstance(), null, bindPlan, rightReqPlan);

		// Test
		final LogicalPlan result = new RemoveSubPlansWithEmptyResults().apply(unionPlan);

		// Check
		assertTrue( result.getRootOperator() instanceof LogicalOpRequest );
		assertEquals( 0, result.numberOfSubPlans() );
		assertEquals( 42, result.getQueryPlanningInfo().getProperty(CARDINALITY).getValue() );
	}

	@Test
	public void removeEmptySubPlansKeepUnionKeepTwoSubPlans() {
		// Multiway union operator with one empty and two non-empty branches.
		// Empty branch is removed but union remains with the two non-empty subplans.

		// Request that has cardinality 0 and quality accurate
		final Var v1 = Var.alloc("x");
		final FederationMember fm = new TPFServerForTest();
		final TriplePattern tp = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp1 = new LogicalOpRequest<>( fm, false, new SPARQLRequestImpl(tp) );

		// Empty request
		final LogicalPlan reqPlan1 = new LogicalPlanWithNullaryRootImpl(reqOp1, null);
		final QueryPlanningInfo qpInfo1 = reqPlan1.getQueryPlanningInfo();
		qpInfo1.addProperty( QueryPlanProperty.cardinality(0, Quality.ACCURATE) );
		qpInfo1.addProperty( QueryPlanProperty.maxCardinality(0, Quality.ACCURATE) );
		qpInfo1.addProperty( QueryPlanProperty.minCardinality(0, Quality.ACCURATE) );

		// Request with non-zero cardinality
		final LogicalOpRequest<?,?> reqOp2 = new LogicalOpRequest<>( fm, false, new SPARQLRequestImpl(tp) );
		List<QueryPlanProperty> qpInfo2 = new ArrayList<>();
		qpInfo2.add(QueryPlanProperty.cardinality(42, Quality.ACCURATE));
		qpInfo2.add(QueryPlanProperty.maxCardinality(42, Quality.ACCURATE));
		qpInfo2.add(QueryPlanProperty.minCardinality(42, Quality.ACCURATE));
		final LogicalPlan reqPlan2 = new LogicalPlanWithNullaryRootImpl(reqOp2, qpInfo2);

		// Bind above request
		final Expr bindExpr = NodeValue.makeInteger(42);
		final VarExprList bindExpressions = new VarExprList(v1, bindExpr);
		final LogicalOpBind bindOp = new LogicalOpBind(bindExpressions, false);
		final LogicalPlan bindPlan = new LogicalPlanWithUnaryRootImpl(bindOp, null, reqPlan2);

		// Union at the top
		final LogicalPlan multiwayUnionPlan = new LogicalPlanWithNaryRootImpl( LogicalOpMultiwayUnion.getInstance(), null, reqPlan1, reqPlan2, bindPlan);

		// Test
		final LogicalPlan result = new RemoveSubPlansWithEmptyResults().apply(multiwayUnionPlan);

		// Check
		assertTrue( result.getRootOperator() instanceof LogicalOpMultiwayUnion );
		assertEquals( 2, result.numberOfSubPlans() );

		final LogicalPlan left = result.getSubPlan(0);
		assertTrue( left.getRootOperator() instanceof LogicalOpRequest );
		assertEquals( 42, left.getQueryPlanningInfo().getProperty(CARDINALITY).getValue() );

		final LogicalPlan right = result.getSubPlan(1);
		assertTrue( right.getRootOperator() instanceof LogicalOpBind );
		assertEquals( 42, right.getQueryPlanningInfo().getProperty(CARDINALITY).getValue() );
		assertTrue( right.getSubPlan(0).getRootOperator() instanceof LogicalOpRequest );
	}

	@Test
	public void removeEmptySubPlansRemoveWholeUnion() {
		// Union where all branches are empty.
		// Entire plan is replaced with an empty plan (LogicalPlanWithoutResult).

		// Empty requests
		final Var v1 = Var.alloc("x");
		final FederationMember fm = new TPFServerForTest();
		final TriplePattern tp = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp1 = new LogicalOpRequest<>( fm, false, new SPARQLRequestImpl(tp) );

		List<QueryPlanProperty> qpInfo = new ArrayList<>();
		qpInfo.add(QueryPlanProperty.cardinality(0, Quality.ACCURATE));
		qpInfo.add(QueryPlanProperty.maxCardinality(0, Quality.ACCURATE));
		qpInfo.add(QueryPlanProperty.minCardinality(0, Quality.ACCURATE));
		final LogicalPlan reqPlan1 = new LogicalPlanWithNullaryRootImpl(reqOp1, qpInfo);
		final LogicalPlan reqPlan2 = new LogicalPlanWithNullaryRootImpl(reqOp1, qpInfo);

		// Union at the top
		final LogicalPlan unionPlan = new LogicalPlanWithBinaryRootImpl( LogicalOpUnion.getInstance(), null, reqPlan1, reqPlan2);

		// Test
		final LogicalPlan result = new RemoveSubPlansWithEmptyResults().apply(unionPlan);

		// Check
		assertTrue( result instanceof LogicalPlanWithoutResult );
	}

	@Test
	public void removeEmptySubPlansNestedUnion() {
		// Nested union operator where an inner union contains an empty branch.
		// Inner union collapses and outer union keeps the remaining non-empty branches.

		List<QueryPlanProperty> qpInfoNonZero = new ArrayList<>();
		qpInfoNonZero.add(QueryPlanProperty.cardinality(42, Quality.ACCURATE));
		qpInfoNonZero.add(QueryPlanProperty.maxCardinality(42, Quality.ACCURATE));
		qpInfoNonZero.add(QueryPlanProperty.minCardinality(42, Quality.ACCURATE));

		// Request with non-zero cardinality
		final Var v1 = Var.alloc("x");
		final FederationMember fm = new TPFServerForTest();
		final TriplePattern tp = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>( fm, false, new SPARQLRequestImpl(tp) );

		final LogicalPlan nonZeroReqPlan = new LogicalPlanWithNullaryRootImpl(reqOp, qpInfoNonZero);

		// Empty request
		final LogicalPlan emptyPlan = new LogicalPlanWithNullaryRootImpl(reqOp, null);
		final QueryPlanningInfo qpInfoZero = emptyPlan.getQueryPlanningInfo();
		qpInfoZero.addProperty(QueryPlanProperty.cardinality(0, Quality.ACCURATE));
		qpInfoZero.addProperty(QueryPlanProperty.maxCardinality(0, Quality.ACCURATE));
		qpInfoZero.addProperty(QueryPlanProperty.minCardinality(0, Quality.ACCURATE));

		final LogicalPlan childUnionPlan = new LogicalPlanWithBinaryRootImpl( LogicalOpUnion.getInstance(), null, emptyPlan, nonZeroReqPlan);

		// Union at the top
		final LogicalPlan parentUnionPlan = new LogicalPlanWithBinaryRootImpl( LogicalOpUnion.getInstance(), null, childUnionPlan, nonZeroReqPlan);

		// Test
		final LogicalPlan result = new RemoveSubPlansWithEmptyResults().apply(parentUnionPlan);

		// Check
		assertTrue( result.getRootOperator() instanceof LogicalOpUnion );
		assertEquals( 2, result.numberOfSubPlans() );

		final LogicalPlan left = result.getSubPlan(0);
		assertTrue( left.getRootOperator() instanceof LogicalOpRequest );

		final LogicalPlan right = result.getSubPlan(1);
		assertTrue( right.getRootOperator() instanceof LogicalOpRequest );
	}

	@Test
	public void removeEmptySubPlansRemoveJoin() {
		// Join operator with one empty and one non-empty subplan.
		// Result is empty and entire plan is removed.

		// Empty request
		final Var v1 = Var.alloc("x");
		final FederationMember fm = new TPFServerForTest();
		final TriplePattern tp = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>( fm, false, new SPARQLRequestImpl(tp) );

		final LogicalPlan leftReqPlan = new LogicalPlanWithNullaryRootImpl(reqOp, null);
		final QueryPlanningInfo leftQPInfo = leftReqPlan.getQueryPlanningInfo();
		leftQPInfo.addProperty( QueryPlanProperty.cardinality(0, Quality.ACCURATE) );
		leftQPInfo.addProperty( QueryPlanProperty.maxCardinality(0, Quality.ACCURATE) );
		leftQPInfo.addProperty( QueryPlanProperty.minCardinality(0, Quality.ACCURATE) );

		// Request with non-zero cardinality
		final LogicalPlan rightReqPlan = new LogicalPlanWithNullaryRootImpl(reqOp, null);
		final QueryPlanningInfo rightQPInfo = rightReqPlan.getQueryPlanningInfo();
		rightQPInfo.addProperty( QueryPlanProperty.cardinality(42, Quality.ACCURATE) );
		rightQPInfo.addProperty( QueryPlanProperty.maxCardinality(42, Quality.ACCURATE) );
		rightQPInfo.addProperty( QueryPlanProperty.minCardinality(42, Quality.ACCURATE) );

		// Join at the top
		final LogicalPlan joinPlan = LogicalPlanUtils.createPlanWithBinaryJoin(
				false,
				leftReqPlan,
				rightReqPlan,
				null );

		// Test
		final LogicalPlan result = new RemoveSubPlansWithEmptyResults().apply(joinPlan);

		// Check
		assertTrue( result instanceof LogicalPlanWithoutResult );
	}

	@Test
	public void removeEmptySubPlansRemoveEmptyJoin() {
		// Join where both subplans are empty.
		// Result is empty and entire plan is removed.

		// Empty requests
		final Var v1 = Var.alloc("x");
		final FederationMember fm = new TPFServerForTest();
		final TriplePattern tp = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp1 = new LogicalOpRequest<>( fm, false, new SPARQLRequestImpl(tp) );

		List<QueryPlanProperty> qpInfo = new ArrayList<>();
		qpInfo.add(QueryPlanProperty.cardinality(0, Quality.ACCURATE));
		qpInfo.add(QueryPlanProperty.maxCardinality(0, Quality.ACCURATE));
		qpInfo.add(QueryPlanProperty.minCardinality(0, Quality.ACCURATE));
		final LogicalPlan reqPlan1 = new LogicalPlanWithNullaryRootImpl(reqOp1, qpInfo);
		final LogicalPlan reqPlan2 = new LogicalPlanWithNullaryRootImpl(reqOp1, qpInfo);

		// Join at the top
		final LogicalPlan joinPlan = new LogicalPlanWithBinaryRootImpl( LogicalOpJoin.getInstance(), null, reqPlan1, reqPlan2);

		// Test
		final LogicalPlan result = new RemoveSubPlansWithEmptyResults().apply(joinPlan);

		// Check
		assertTrue( result instanceof LogicalPlanWithoutResult );
	}

	@Test
	public void removeEmptySubPlansLeftJoinWithEmptyRightBranch() {
		// Left join operator with non-empty left and empty right subplan.
		// Operator is removed and left subplan is returned.

		// Request with non-zero cardinality
		final Var v1 = Var.alloc("x");
		final FederationMember fm = new TPFServerForTest();
		final TriplePattern tp = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp1 = new LogicalOpRequest<>( fm, false, new SPARQLRequestImpl(tp) );
		final LogicalPlan reqPlan1 = new LogicalPlanWithNullaryRootImpl(reqOp1, null);
		final QueryPlanningInfo qpInfo1 = reqPlan1.getQueryPlanningInfo();
		qpInfo1.addProperty(QueryPlanProperty.cardinality(42, Quality.ACCURATE));
		qpInfo1.addProperty(QueryPlanProperty.maxCardinality(42, Quality.ACCURATE));
		qpInfo1.addProperty(QueryPlanProperty.minCardinality(42, Quality.ACCURATE));

		// Empty request
		final LogicalPlan reqPlan2 = new LogicalPlanWithNullaryRootImpl(reqOp1, null);
		final QueryPlanningInfo qpInfo2 = reqPlan2.getQueryPlanningInfo();
		qpInfo2.addProperty(QueryPlanProperty.cardinality(0, Quality.ACCURATE));
		qpInfo2.addProperty(QueryPlanProperty.maxCardinality(0, Quality.ACCURATE));
		qpInfo2.addProperty(QueryPlanProperty.minCardinality(0, Quality.ACCURATE));

		// Join at the top
		final LogicalPlan leftJoinPlan = new LogicalPlanWithBinaryRootImpl( LogicalOpLeftJoin.getInstance(), null, reqPlan1, reqPlan2);

		// Test
		final LogicalPlan result = new RemoveSubPlansWithEmptyResults().apply(leftJoinPlan);

		// Check
		assertTrue( result.getRootOperator() instanceof LogicalOpRequest );
		assertEquals( 0, result.numberOfSubPlans() );
	}

	@Test
	public void removeEmptySubPlansLeftJoinWithEmptyLeftBranch() {
		// Left join with empty left and non-empty right subplan.
		// Result is empty and entire plan is removed.

		// Request with non-zero cardinality
		final Var v1 = Var.alloc("x");
		final FederationMember fm = new TPFServerForTest();
		final TriplePattern tp = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp1 = new LogicalOpRequest<>( fm, false, new SPARQLRequestImpl(tp) );
		final LogicalPlan reqPlan1 = new LogicalPlanWithNullaryRootImpl(reqOp1, null);
		final QueryPlanningInfo qpInfo1 = reqPlan1.getQueryPlanningInfo();
		qpInfo1.addProperty(QueryPlanProperty.cardinality(42, Quality.ACCURATE));
		qpInfo1.addProperty(QueryPlanProperty.maxCardinality(42, Quality.ACCURATE));
		qpInfo1.addProperty(QueryPlanProperty.minCardinality(42, Quality.ACCURATE));

		// Empty request
		final LogicalPlan reqPlan2 = new LogicalPlanWithNullaryRootImpl(reqOp1, null);
		final QueryPlanningInfo qpInfo2 = reqPlan2.getQueryPlanningInfo();
		qpInfo2.addProperty(QueryPlanProperty.cardinality(0, Quality.ACCURATE));
		qpInfo2.addProperty(QueryPlanProperty.maxCardinality(0, Quality.ACCURATE));
		qpInfo2.addProperty(QueryPlanProperty.minCardinality(0, Quality.ACCURATE));

		// Join at the top
		final LogicalPlan leftJoinPlan = new LogicalPlanWithBinaryRootImpl( LogicalOpLeftJoin.getInstance(), null, reqPlan2, reqPlan1);

		// Test
		final LogicalPlan result = new RemoveSubPlansWithEmptyResults().apply(leftJoinPlan);

		// Check
		assertTrue( result instanceof LogicalPlanWithoutResult );
	}

	@Test
	public void removeEmptySubPlansRemoveEntirePlan() {
		// Unary operator (bind) applied to an empty subplan.
		// Emptiness propagates and entire plan is removed.

		// Empty request
		final Var v1 = Var.alloc("x");
		final FederationMember fm = new TPFServerForTest();
		final TriplePattern tp = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>( fm, false, new SPARQLRequestImpl(tp) );

		final LogicalPlan reqPlan = new LogicalPlanWithNullaryRootImpl(reqOp, null);
		final QueryPlanningInfo qpInfo = reqPlan.getQueryPlanningInfo();
		qpInfo.addProperty( QueryPlanProperty.cardinality(0, Quality.ACCURATE) );
		qpInfo.addProperty( QueryPlanProperty.maxCardinality(0, Quality.ACCURATE) );
		qpInfo.addProperty( QueryPlanProperty.minCardinality(0, Quality.ACCURATE) );

		// Bind above request
		final Expr bindExpr = NodeValue.makeInteger(42);
		final VarExprList bindExpressions = new VarExprList(v1, bindExpr);
		final LogicalOpBind bindOp = new LogicalOpBind(bindExpressions, false);
		final LogicalPlan bindPlan = new LogicalPlanWithUnaryRootImpl(bindOp, null, reqPlan);

		// Test
		final LogicalPlan result = new RemoveSubPlansWithEmptyResults().apply(bindPlan);

		// Check
		assertTrue( result instanceof LogicalPlanWithoutResult );
	}

	@Test
	public void removeEmptySubPlansKeepNonZeroRequest() {
		// Request with non-zero cardinality.
		// Plan remains unchanged.

		// Request with non-zero cardinality
		final Var v1 = Var.alloc("x");
		final FederationMember fm = new TPFServerForTest();
		final TriplePattern tp = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>( fm, false, new SPARQLRequestImpl(tp) );

		final LogicalPlan reqPlan = new LogicalPlanWithNullaryRootImpl(reqOp, null);
		final QueryPlanningInfo qpInfo = reqPlan.getQueryPlanningInfo();
		qpInfo.addProperty( QueryPlanProperty.cardinality(42, Quality.ACCURATE) );
		qpInfo.addProperty( QueryPlanProperty.maxCardinality(42, Quality.ACCURATE) );
		qpInfo.addProperty( QueryPlanProperty.minCardinality(42, Quality.ACCURATE) );

		// Test
		final LogicalPlan result = new RemoveSubPlansWithEmptyResults().apply(reqPlan);

		// Check
		assertTrue( result.getRootOperator() instanceof LogicalOpRequest );
		assertEquals( 42, result.getQueryPlanningInfo().getProperty(CARDINALITY).getValue() );
	}

}
