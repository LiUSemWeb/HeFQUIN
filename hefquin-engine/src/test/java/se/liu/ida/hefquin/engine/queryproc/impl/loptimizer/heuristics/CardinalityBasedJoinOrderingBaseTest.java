package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.core.Var;
import org.junit.Test;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.engine.EngineTestBase;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanWithNaryRoot;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpFilter;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpLocalToGlobal;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithNaryRootImpl;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithNullaryRootImpl;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithUnaryRootImpl;
import se.liu.ida.hefquin.engine.queryproc.LogicalOptimizationException;
import se.liu.ida.hefquin.federation.TPFServer;
import se.liu.ida.hefquin.federation.access.TriplePatternRequest;

public class CardinalityBasedJoinOrderingBaseTest extends EngineTestBase
{
	@Test
	public void twoRequests1() throws LogicalOptimizationException {
		@SuppressWarnings("unchecked")
		final LogicalPlan joinPlan = createPlanWithJoinOverRequests(2, null, null);

		final int[] result1 = {1,2};
		final CardinalityBasedJoinOrderingBase h = new TestImpl(result1, null);

		final LogicalPlan resultPlan = h.apply(joinPlan);

		assertTrue( resultPlan.equals(joinPlan) );
	}

	@Test
	public void twoRequests2() throws LogicalOptimizationException {
		@SuppressWarnings("unchecked")
		final LogicalPlan joinPlan = createPlanWithJoinOverRequests(2, null, null);

		final int[] result1 = {2,1};
		final CardinalityBasedJoinOrderingBase h = new TestImpl(result1, null);

		final LogicalPlan resultPlan = h.apply(joinPlan);

		assertEquals( 2, resultPlan.numberOfSubPlans() );
		assertTrue( resultPlan.getSubPlan(0) == joinPlan.getSubPlan(1) ); // not just equal but indeed the same
		assertTrue( resultPlan.getSubPlan(1) == joinPlan.getSubPlan(0) ); // not just equal but indeed the same
	}

	@Test
	public void twoRequestsUnderFilter() throws LogicalOptimizationException {
		@SuppressWarnings("unchecked")
		final LogicalPlan joinPlan = createPlanWithJoinOverRequests(2, null, null);

		final LogicalOpFilter f = new LogicalOpFilter( Expr.NONE );
		final LogicalPlan filterPlan = new LogicalPlanWithUnaryRootImpl(f, joinPlan);

		final int[] result1 = {2,1};
		final CardinalityBasedJoinOrderingBase h = new TestImpl(result1, null);

		final LogicalPlan resultPlan = h.apply(filterPlan);

		assertEquals( 2, resultPlan.getSubPlan(0).numberOfSubPlans() );
		assertTrue( resultPlan.getSubPlan(0).getSubPlan(0) == joinPlan.getSubPlan(1) ); // not just equal but indeed the same
		assertTrue( resultPlan.getSubPlan(0).getSubPlan(1) == joinPlan.getSubPlan(0) ); // not just equal but indeed the same
	}

	@Test
	public void twoRequestsUnderL2G() throws LogicalOptimizationException {
		@SuppressWarnings("unchecked")
		final LogicalPlan joinPlan = createPlanWithJoinOverRequests(2, null, null);

		final LogicalOpLocalToGlobal l2g = new LogicalOpLocalToGlobal(null);
		final LogicalPlan l2gPlan = new LogicalPlanWithUnaryRootImpl(l2g, joinPlan);

		final int[] result1 = {2,1};
		final CardinalityBasedJoinOrderingBase h = new TestImpl(result1, null);

		final LogicalPlan resultPlan = h.apply(l2gPlan);

		assertEquals( 2, resultPlan.getSubPlan(0).numberOfSubPlans() );
		assertTrue( resultPlan.getSubPlan(0).getSubPlan(0) == joinPlan.getSubPlan(1) ); // not just equal but indeed the same
		assertTrue( resultPlan.getSubPlan(0).getSubPlan(1) == joinPlan.getSubPlan(0) ); // not just equal but indeed the same
	}

	@Test
	public void threeRequests1() throws LogicalOptimizationException {
		@SuppressWarnings("unchecked")
		final LogicalPlan joinPlan = createPlanWithJoinOverRequests(3,
				Collections.singleton(Var.alloc("x")),
				Collections.singleton(Var.alloc("x")),
				Collections.singleton(Var.alloc("x")) );

		final int[] result1 = {1,2,3};
		final CardinalityBasedJoinOrderingBase h = new TestImpl(result1, null);

		final LogicalPlan resultPlan = h.apply(joinPlan);

		assertTrue( resultPlan.equals(joinPlan) );
	}

	@Test
	public void threeRequests2() throws LogicalOptimizationException {
		@SuppressWarnings("unchecked")
		final LogicalPlan joinPlan = createPlanWithJoinOverRequests(3,
				Collections.singleton(Var.alloc("x")),
				Collections.singleton(Var.alloc("x")),
				Collections.singleton(Var.alloc("x")) );

		final int[] result1 = {2,1,3};
		final CardinalityBasedJoinOrderingBase h = new TestImpl(result1, null);

		final LogicalPlan resultPlan = h.apply(joinPlan);

		assertEquals( 3, resultPlan.numberOfSubPlans() );
		assertTrue( resultPlan.getSubPlan(0) == joinPlan.getSubPlan(1) ); // not just equal but indeed the same
		assertTrue( resultPlan.getSubPlan(1) == joinPlan.getSubPlan(0) ); // not just equal but indeed the same
		assertTrue( resultPlan.getSubPlan(2) == joinPlan.getSubPlan(2) ); // not just equal but indeed the same
	}

	@Test
	public void threeRequests3() throws LogicalOptimizationException {
		@SuppressWarnings("unchecked")
		final LogicalPlan joinPlan = createPlanWithJoinOverRequests(3,
				Collections.singleton(Var.alloc("y")), // attention, no join variable!
				Collections.singleton(Var.alloc("x")),
				Collections.singleton(Var.alloc("x")) );

		final int[] result1 = {2,1,3};
		final CardinalityBasedJoinOrderingBase h = new TestImpl(result1, null);

		final LogicalPlan resultPlan = h.apply(joinPlan);

		assertEquals( 3, resultPlan.numberOfSubPlans() );
		assertTrue( resultPlan.getSubPlan(0) == joinPlan.getSubPlan(1) ); // not just equal but indeed the same
		assertTrue( resultPlan.getSubPlan(1) == joinPlan.getSubPlan(2) ); // not just equal but indeed the same
		assertTrue( resultPlan.getSubPlan(2) == joinPlan.getSubPlan(0) ); // not just equal but indeed the same
	}

	// ----------- helpers ------------

	protected LogicalPlanWithNaryRoot createPlanWithJoinOverRequests( final int numberOfRequestsToBeCreated,
	                                                                  @SuppressWarnings("unchecked") final Set<Var> ... certainVars ) {
		final TPFServer srv = new TPFServerForTest();

		final LogicalPlan[] subPlans = new LogicalPlan[numberOfRequestsToBeCreated];
		for ( int i = 0; i < numberOfRequestsToBeCreated; i++ ) {
			final Set<Var> cv = certainVars[i];
			final ExpectedVariables ev = new ExpectedVariables() {
				@Override public Set<Var> getCertainVariables() { return cv; }
				@Override public Set<Var> getPossibleVariables() { throw new UnsupportedOperationException(); }
			};

			final TriplePatternRequest req = new TriplePatternRequest() {
				@Override public ExpectedVariables getExpectedVariables() { return ev; }
				@Override public TriplePattern getQueryPattern() { throw new UnsupportedOperationException(); }
			};

			subPlans[i] = new LogicalPlanWithNullaryRootImpl( new LogicalOpRequest<>(srv, req) );
		}

		return new LogicalPlanWithNaryRootImpl( LogicalOpMultiwayJoin.getInstance(), subPlans );
	}

	protected class TestImpl extends CardinalityBasedJoinOrderingBase {
		protected final int[] result1;
		protected final Map<AnnotatedLogicalPlan,Integer> results2;

		public TestImpl( final int[] result1, final Map<AnnotatedLogicalPlan,Integer> results2 ) {
			this.result1 = result1;
			this.results2 = results2;
		}

		@Override
		protected int[] estimateCardinalities( final LogicalPlan[] plans ) {
			return result1;
		}

		@Override
		protected int estimateJoinCardinality( final List<AnnotatedLogicalPlan> selectedPlans,
		                                       final int joinCardOfSelectedPlans,
		                                       final AnnotatedLogicalPlan nextCandidate ) {
			if ( results2 != null )
				return results2.get(nextCandidate);
			else
				return nextCandidate.cardinality;
		}
	}

}
