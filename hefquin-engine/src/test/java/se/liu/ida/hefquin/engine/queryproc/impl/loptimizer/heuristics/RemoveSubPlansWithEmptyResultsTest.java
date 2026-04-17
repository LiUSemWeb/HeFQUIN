package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static se.liu.ida.hefquin.engine.queryplan.info.QueryPlanProperty.CARDINALITY;

import java.util.concurrent.ExecutorService;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;
import org.junit.Ignore;
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
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalToPhysicalOpConverter;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalToPhysicalPlanConverter;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.federation.catalog.FederationCatalog;

public class RemoveSubPlansWithEmptyResultsTest extends EngineTestBase
{
	@Test
	public void removeUnionKeepSubPlan() {
		// Union operator with one empty and one non-empty branch.
		// Empty branch is removed and union collapses to the remaining subplan.

		// Empty request
		final Var v1 = Var.alloc("x");
		final FederationMember fm = new TPFServerForTest();
		final TriplePattern tp = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>( fm, false, new SPARQLRequestImpl(tp) );

		final LogicalPlan leftReqPlan = new LogicalPlanWithNullaryRootImpl(reqOp, null);
		addCardEstimate( 0, leftReqPlan.getQueryPlanningInfo() );

		// Bind operator above request
		final Expr bindExpr = NodeValue.makeInteger(42);
		final VarExprList bindExpressions = new VarExprList(v1, bindExpr);
		final LogicalOpBind bindOp = new LogicalOpBind(bindExpressions, false);
		final LogicalPlan bindPlan = new LogicalPlanWithUnaryRootImpl(bindOp, null, leftReqPlan);

		// Request with non-zero cardinality
		final LogicalPlan rightReqPlan = new LogicalPlanWithNullaryRootImpl(reqOp, null);
		addCardEstimate( 42, rightReqPlan.getQueryPlanningInfo() );

		// Union at the top
		final LogicalPlan unionPlan = new LogicalPlanWithBinaryRootImpl( LogicalOpUnion.getInstance(), null, bindPlan, rightReqPlan);

		// Test
		final LogicalPlan result = new RemoveSubPlansWithEmptyResults(new TestQueryProcContext()).apply(unionPlan);

		// Check
		assertTrue( result.getRootOperator() instanceof LogicalOpRequest );
		assertEquals( 0, result.numberOfSubPlans() );
		assertEquals( 42, result.getQueryPlanningInfo().getProperty(CARDINALITY).getValue() );
	}

	@Test
	public void keepUnion() {
		// Union operator with one non-empty branch and
		// one subplan with cardinality 0 but quality not accurate.
		// Empty branch is removed and union collapses to the remaining subplan.

		// Empty request with quality not accurate
		final Var v1 = Var.alloc("x");
		final FederationMember fm = new TPFServerForTest();
		final TriplePattern tp = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>( fm, false, new SPARQLRequestImpl(tp) );

		final LogicalPlan leftReqPlan = new LogicalPlanWithNullaryRootImpl(reqOp, null);
		final QueryPlanningInfo qpInfo = leftReqPlan.getQueryPlanningInfo();
		qpInfo.addProperty( QueryPlanProperty.cardinality(0, Quality.DIRECT_ESTIMATE) );
		qpInfo.addProperty( QueryPlanProperty.maxCardinality(0, Quality.DIRECT_ESTIMATE) );
		qpInfo.addProperty( QueryPlanProperty.minCardinality(0, Quality.DIRECT_ESTIMATE) );

		// Bind operator above request
		final Expr bindExpr = NodeValue.makeInteger(42);
		final VarExprList bindExpressions = new VarExprList(v1, bindExpr);
		final LogicalOpBind bindOp = new LogicalOpBind(bindExpressions, false);
		final LogicalPlan bindPlan = new LogicalPlanWithUnaryRootImpl(bindOp, null, leftReqPlan);

		// Request with non-zero cardinality
		final LogicalPlan rightReqPlan = new LogicalPlanWithNullaryRootImpl(reqOp, null);
		addCardEstimate( 42, rightReqPlan.getQueryPlanningInfo() );

		// Union at the top
		final LogicalPlan unionPlan = new LogicalPlanWithBinaryRootImpl( LogicalOpUnion.getInstance(), null, bindPlan, rightReqPlan);

		// Test
		final LogicalPlan result = new RemoveSubPlansWithEmptyResults(new TestQueryProcContext()).apply(unionPlan);

		// Check
		assertTrue( result.getRootOperator() instanceof LogicalOpUnion );
		assertEquals( 2, result.numberOfSubPlans() );

		final LogicalPlan left = result.getSubPlan(0);
		assertTrue( left.getRootOperator() instanceof LogicalOpBind );
		final LogicalPlan bindSubPlan = left.getSubPlan(0);
		assertTrue( bindSubPlan.getRootOperator() instanceof LogicalOpRequest );
		assertEquals( 0, bindSubPlan.getQueryPlanningInfo().getProperty(CARDINALITY).getValue() );
		assertEquals( Quality.DIRECT_ESTIMATE, bindSubPlan.getQueryPlanningInfo().getProperty(CARDINALITY).getQuality() );

		final LogicalPlan right = result.getSubPlan(1);
		assertTrue( right.getRootOperator() instanceof LogicalOpRequest );
		assertEquals( 42, right.getQueryPlanningInfo().getProperty(CARDINALITY).getValue() );
	}

	@Test
	public void keepUnionKeepTwoSubPlans() {
		// Multiway union operator with one empty and two non-empty branches.
		// Empty branch is removed but union remains with the two non-empty subplans.

		// Request that has cardinality 0 and quality accurate
		final Var v1 = Var.alloc("x");
		final FederationMember fm = new TPFServerForTest();
		final TriplePattern tp = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp1 = new LogicalOpRequest<>( fm, false, new SPARQLRequestImpl(tp) );

		// Empty request
		final LogicalPlan reqPlan1 = new LogicalPlanWithNullaryRootImpl(reqOp1, null);
		addCardEstimate( 0, reqPlan1.getQueryPlanningInfo() );

		// Request with non-zero cardinality
		final LogicalPlan reqPlan2 = new LogicalPlanWithNullaryRootImpl(reqOp1, null);
		addCardEstimate( 42, reqPlan2.getQueryPlanningInfo() );

		// Bind above request
		final Expr bindExpr = NodeValue.makeInteger(42);
		final VarExprList bindExpressions = new VarExprList(v1, bindExpr);
		final LogicalOpBind bindOp = new LogicalOpBind(bindExpressions, false);
		final LogicalPlan bindPlan = new LogicalPlanWithUnaryRootImpl(bindOp, null, reqPlan2);

		// Union at the top
		final LogicalPlan multiwayUnionPlan = new LogicalPlanWithNaryRootImpl( LogicalOpMultiwayUnion.getInstance(), null, reqPlan1, reqPlan2, bindPlan);

		// Test
		final LogicalPlan result = new RemoveSubPlansWithEmptyResults(new TestQueryProcContext()).apply(multiwayUnionPlan);

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
	public void removeWholeUnion() {
		// Union where all branches are empty.
		// Entire plan is replaced with an empty plan (LogicalPlanWithoutResult).

		// Empty requests
		final Var v1 = Var.alloc("x");
		final FederationMember fm = new TPFServerForTest();
		final TriplePattern tp = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp1 = new LogicalOpRequest<>( fm, false, new SPARQLRequestImpl(tp) );

		final LogicalPlan reqPlan1 = new LogicalPlanWithNullaryRootImpl(reqOp1, null);
		addCardEstimate( 0, reqPlan1.getQueryPlanningInfo() );
		final LogicalPlan reqPlan2 = new LogicalPlanWithNullaryRootImpl(reqOp1, null);
		addCardEstimate( 0, reqPlan2.getQueryPlanningInfo() );

		// Union at the top
		final LogicalPlan unionPlan = new LogicalPlanWithBinaryRootImpl( LogicalOpUnion.getInstance(), null, reqPlan1, reqPlan2);

		// Test
		final LogicalPlan result = new RemoveSubPlansWithEmptyResults(new TestQueryProcContext()).apply(unionPlan);

		// Check
		assertTrue( result instanceof LogicalPlanWithoutResult );
	}

	@Test
	public void nestedUnionNonEmptyInner() {
		// Nested union operator where an inner union contains an empty branch.
		// Inner union collapses and outer union keeps the remaining non-empty branches.

		// Request with non-zero cardinality
		final Var v1 = Var.alloc("x");
		final FederationMember fm = new TPFServerForTest();
		final TriplePattern tp = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>( fm, false, new SPARQLRequestImpl(tp) );

		final LogicalPlan nonZeroReqPlan = new LogicalPlanWithNullaryRootImpl(reqOp, null );
		addCardEstimate( 42, nonZeroReqPlan.getQueryPlanningInfo() );

		// Empty request
		final LogicalPlan emptyPlan = new LogicalPlanWithNullaryRootImpl(reqOp, null);
		addCardEstimate( 0, emptyPlan.getQueryPlanningInfo() );

		// Inner union with one empty and one non-empty branch
		final LogicalPlan childUnionPlan = new LogicalPlanWithBinaryRootImpl( LogicalOpUnion.getInstance(), null, emptyPlan, nonZeroReqPlan);

		// Union at the top
		final LogicalPlan parentUnionPlan = new LogicalPlanWithBinaryRootImpl( LogicalOpUnion.getInstance(), null, childUnionPlan, nonZeroReqPlan);

		// Test
		final LogicalPlan result = new RemoveSubPlansWithEmptyResults(new TestQueryProcContext()).apply(parentUnionPlan);

		// Check
		assertTrue( result.getRootOperator() instanceof LogicalOpUnion );
		assertEquals( 2, result.numberOfSubPlans() );

		final LogicalPlan left = result.getSubPlan(0);
		assertTrue( left.getRootOperator() instanceof LogicalOpRequest );

		final LogicalPlan right = result.getSubPlan(1);
		assertTrue( right.getRootOperator() instanceof LogicalOpRequest );
	}

	@Test
	public void nestedUnionEmptyInner() {
		// Nested union operator where an inner union contains two empty branches
		// Inner union is removed and outer union is collapses to the remaining non-empty branch.

		// Request with non-zero cardinality
		final Var v1 = Var.alloc("x");
		final FederationMember fm = new TPFServerForTest();
		final TriplePattern tp = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>( fm, false, new SPARQLRequestImpl(tp) );

		final LogicalPlan nonZeroReqPlan = new LogicalPlanWithNullaryRootImpl(reqOp, null );
		addCardEstimate( 42, nonZeroReqPlan.getQueryPlanningInfo() );

		// Empty request
		final LogicalPlan emptyPlan = new LogicalPlanWithNullaryRootImpl(reqOp, null);
		addCardEstimate( 0, emptyPlan.getQueryPlanningInfo() );

		// Inner union with two empty branches
		final LogicalPlan childUnionPlan = new LogicalPlanWithBinaryRootImpl( LogicalOpUnion.getInstance(), null, emptyPlan, emptyPlan);

		// Union at the top
		final LogicalPlan parentUnionPlan = new LogicalPlanWithBinaryRootImpl( LogicalOpUnion.getInstance(), null, childUnionPlan, nonZeroReqPlan);

		// Test
		final LogicalPlan result = new RemoveSubPlansWithEmptyResults(new TestQueryProcContext()).apply(parentUnionPlan);

		// Check
		assertTrue( result.getRootOperator() instanceof LogicalOpRequest );
		assertEquals( 0, result.numberOfSubPlans() );
	}

	@Test
	public void removeJoin() {
		// Join operator with one empty and one non-empty subplan.
		// Result is empty and entire plan is removed.

		// Empty request
		final Var v1 = Var.alloc("x");
		final FederationMember fm = new TPFServerForTest();
		final TriplePattern tp = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>( fm, false, new SPARQLRequestImpl(tp) );

		final LogicalPlan leftReqPlan = new LogicalPlanWithNullaryRootImpl(reqOp, null);
		addCardEstimate( 0, leftReqPlan.getQueryPlanningInfo() );

		// Request with non-zero cardinality
		final LogicalPlan rightReqPlan = new LogicalPlanWithNullaryRootImpl(reqOp, null);
		addCardEstimate( 42, rightReqPlan.getQueryPlanningInfo() );

		// Join at the top
		final LogicalPlan joinPlan = LogicalPlanUtils.createPlanWithBinaryJoin(
				false,
				leftReqPlan,
				rightReqPlan,
				null );

		// Test
		final LogicalPlan result = new RemoveSubPlansWithEmptyResults(new TestQueryProcContext()).apply(joinPlan);

		// Check
		assertTrue( result instanceof LogicalPlanWithoutResult );
	}

	@Test
	public void removeEmptyJoin() {
		// Join where both subplans are empty.
		// Result is empty and entire plan is removed.

		// Empty requests
		final Var v1 = Var.alloc("x");
		final FederationMember fm = new TPFServerForTest();
		final TriplePattern tp = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp1 = new LogicalOpRequest<>( fm, false, new SPARQLRequestImpl(tp) );

		final LogicalPlan reqPlan1 = new LogicalPlanWithNullaryRootImpl(reqOp1, null);
		addCardEstimate( 0, reqPlan1.getQueryPlanningInfo() );
		final LogicalPlan reqPlan2 = new LogicalPlanWithNullaryRootImpl(reqOp1, null);
		addCardEstimate( 0, reqPlan2.getQueryPlanningInfo() );

		// Join at the top
		final LogicalPlan joinPlan = new LogicalPlanWithBinaryRootImpl( LogicalOpJoin.getInstance(), null, reqPlan1, reqPlan2);

		// Test
		final LogicalPlan result = new RemoveSubPlansWithEmptyResults(new TestQueryProcContext()).apply(joinPlan);

		// Check
		assertTrue( result instanceof LogicalPlanWithoutResult );
	}

	@Test
	public void leftJoinWithEmptyRightBranch() {
		// Left join operator with non-empty left and empty right subplan.
		// Operator is removed and left subplan is returned.

		// Request with non-zero cardinality
		final Var v1 = Var.alloc("x");
		final FederationMember fm = new TPFServerForTest();
		final TriplePattern tp = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp1 = new LogicalOpRequest<>( fm, false, new SPARQLRequestImpl(tp) );
		final LogicalPlan reqPlan1 = new LogicalPlanWithNullaryRootImpl(reqOp1, null);
		addCardEstimate( 42, reqPlan1.getQueryPlanningInfo() );

		// Empty request
		final LogicalPlan reqPlan2 = new LogicalPlanWithNullaryRootImpl(reqOp1, null);
		addCardEstimate( 0, reqPlan2.getQueryPlanningInfo() );

		// Join at the top
		final LogicalPlan leftJoinPlan = new LogicalPlanWithBinaryRootImpl( LogicalOpLeftJoin.getInstance(), null, reqPlan1, reqPlan2);

		// Test
		final LogicalPlan result = new RemoveSubPlansWithEmptyResults(new TestQueryProcContext()).apply(leftJoinPlan);

		// Check
		assertTrue( result.getRootOperator() instanceof LogicalOpRequest );
		assertEquals( 0, result.numberOfSubPlans() );
	}

	@Ignore("This test currently fails because the cardinality estimator degrades the cardinality quality (TODO #585).")
	public void leftJoinWithEmptyLeftBranch() {
		// Left join with empty left and non-empty right subplan.
		// Result is empty and entire plan is removed.

		// Request with non-zero cardinality
		final Var v1 = Var.alloc("x");
		final FederationMember fm = new TPFServerForTest();
		final TriplePattern tp = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp1 = new LogicalOpRequest<>( fm, false, new SPARQLRequestImpl(tp) );
		final LogicalPlan reqPlan1 = new LogicalPlanWithNullaryRootImpl(reqOp1, null);
		addCardEstimate( 42, reqPlan1.getQueryPlanningInfo() );

		// Empty request
		final LogicalPlan reqPlan2 = new LogicalPlanWithNullaryRootImpl(reqOp1, null);
		addCardEstimate( 0, reqPlan2.getQueryPlanningInfo() );

		// Join at the top
		final LogicalPlan leftJoinPlan = new LogicalPlanWithBinaryRootImpl( LogicalOpLeftJoin.getInstance(), null, reqPlan2, reqPlan1);

		// Test
		final LogicalPlan result = new RemoveSubPlansWithEmptyResults(new TestQueryProcContext()).apply(leftJoinPlan);

		// Check
		assertTrue( result instanceof LogicalPlanWithoutResult );
	}

	@Test
	public void removeEntirePlan() {
		// Unary operator (bind) applied to an empty subplan.
		// Emptiness propagates and entire plan is removed.

		// Empty request
		final Var v1 = Var.alloc("x");
		final FederationMember fm = new TPFServerForTest();
		final TriplePattern tp = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>( fm, false, new SPARQLRequestImpl(tp) );

		final LogicalPlan reqPlan = new LogicalPlanWithNullaryRootImpl(reqOp, null);
		addCardEstimate( 0, reqPlan.getQueryPlanningInfo() );

		// Bind above request
		final Expr bindExpr = NodeValue.makeInteger(42);
		final VarExprList bindExpressions = new VarExprList(v1, bindExpr);
		final LogicalOpBind bindOp = new LogicalOpBind(bindExpressions, false);
		final LogicalPlan bindPlan = new LogicalPlanWithUnaryRootImpl(bindOp, null, reqPlan);

		// Test
		final LogicalPlan result = new RemoveSubPlansWithEmptyResults(new TestQueryProcContext()).apply(bindPlan);

		// Check
		assertTrue( result instanceof LogicalPlanWithoutResult );
	}

	@Test
	public void keepEntirePlan() {
		// Unary operator (bind) applied to a subplan with
		// cardinality 0 but quality not accurate.
		// Emptiness propagates and entire plan is removed.

		// Empty request with quality not accurate
		final Var v1 = Var.alloc("x");
		final FederationMember fm = new TPFServerForTest();
		final TriplePattern tp = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>( fm, false, new SPARQLRequestImpl(tp) );

		final LogicalPlan reqPlan = new LogicalPlanWithNullaryRootImpl(reqOp, null);
		final QueryPlanningInfo qpInfo = reqPlan.getQueryPlanningInfo();
		qpInfo.addProperty( QueryPlanProperty.cardinality(0, Quality.DIRECT_ESTIMATE) );
		qpInfo.addProperty( QueryPlanProperty.maxCardinality(0, Quality.DIRECT_ESTIMATE) );
		qpInfo.addProperty( QueryPlanProperty.minCardinality(0, Quality.DIRECT_ESTIMATE) );

		// Bind above request
		final Expr bindExpr = NodeValue.makeInteger(42);
		final VarExprList bindExpressions = new VarExprList(v1, bindExpr);
		final LogicalOpBind bindOp = new LogicalOpBind(bindExpressions, false);
		final LogicalPlan bindPlan = new LogicalPlanWithUnaryRootImpl(bindOp, null, reqPlan);

		// Test
		final LogicalPlan result = new RemoveSubPlansWithEmptyResults(new TestQueryProcContext()).apply(bindPlan);

		// Check
		assertTrue( result.getRootOperator() instanceof LogicalOpBind );

		assertTrue( result.getSubPlan(0).getRootOperator() instanceof LogicalOpRequest );
		assertEquals( 0, result.getSubPlan(0).getQueryPlanningInfo().getProperty(CARDINALITY).getValue() );
		assertEquals( Quality.DIRECT_ESTIMATE, result.getSubPlan(0).getQueryPlanningInfo().getProperty(CARDINALITY).getQuality() );
	}

	@Test
	public void keepNonZeroRequest() {
		// Request with non-zero cardinality.
		// Plan remains unchanged.

		// Request with non-zero cardinality
		final Var v1 = Var.alloc("x");
		final FederationMember fm = new TPFServerForTest();
		final TriplePattern tp = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>( fm, false, new SPARQLRequestImpl(tp) );

		final LogicalPlan reqPlan = new LogicalPlanWithNullaryRootImpl(reqOp, null);
		addCardEstimate( 42, reqPlan.getQueryPlanningInfo() );

		// Test
		final LogicalPlan result = new RemoveSubPlansWithEmptyResults(new TestQueryProcContext()).apply(reqPlan);

		// Check
		assertTrue( result.getRootOperator() instanceof LogicalOpRequest );
		assertEquals( 42, result.getQueryPlanningInfo().getProperty(CARDINALITY).getValue() );
	}

	// ----------- helpers ------------

	protected void addCardEstimate( final int cardinality, final QueryPlanningInfo qpInfo ) {
		qpInfo.addProperty( QueryPlanProperty.cardinality(cardinality, Quality.ACCURATE) );
		qpInfo.addProperty( QueryPlanProperty.maxCardinality(cardinality, Quality.ACCURATE) );
		qpInfo.addProperty( QueryPlanProperty.minCardinality(cardinality, Quality.ACCURATE) );
	}

	protected class TestQueryProcContext implements QueryProcContext {
		protected final FederationAccessManager fedAccessMgr = new FederationAccessManagerForTest();

		@Override
		public FederationAccessManager getFederationAccessMgr() {
			return fedAccessMgr;
		}

		@Override
		public FederationCatalog getFederationCatalog() {
			throw new UnsupportedOperationException();
		}

		@Override
		public LogicalToPhysicalPlanConverter getLogicalToPhysicalPlanConverter() {
			throw new UnsupportedOperationException();
		}

		@Override
		public LogicalToPhysicalOpConverter getLogicalToPhysicalOpConverter() {
			throw new UnsupportedOperationException();
		}

		@Override
		public ExecutorService getExecutorServiceForPlanTasks() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isExperimentRun() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean skipExecution() {
			throw new UnsupportedOperationException();
		}
	}

}
