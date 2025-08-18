package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.core.Var;
import org.junit.Test;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.engine.EngineTestBase;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanProperty;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanProperty.Quality;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanWithNaryRoot;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpFilter;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpLocalToGlobal;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithNaryRootImpl;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithNullaryRootImpl;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithUnaryRootImpl;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.CardinalityEstimator;
import se.liu.ida.hefquin.engine.queryproc.LogicalOptimizationException;
import se.liu.ida.hefquin.federation.TPFServer;
import se.liu.ida.hefquin.federation.access.TriplePatternRequest;

public class CardinalityBasedJoinOrderingBaseTest extends EngineTestBase
{
	@Test
	public void twoRequests1() throws LogicalOptimizationException {
		@SuppressWarnings("unchecked")
		final LogicalPlan joinPlan = createPlanWithJoinOverRequests(null, null);

		final Map<LogicalPlan,Integer> results = new HashMap<>();
		results.put( joinPlan, 999 ); // irrelevant for the test
		results.put( joinPlan.getSubPlan(0), 1 );
		results.put( joinPlan.getSubPlan(1), 2 );

		final CardinalityBasedJoinOrderingBase h = new TestImpl(results);

		final LogicalPlan resultPlan = h.apply(joinPlan);

		assertTrue( resultPlan.equals(joinPlan) );
	}

	@Test
	public void twoRequests2() throws LogicalOptimizationException {
		@SuppressWarnings("unchecked")
		final LogicalPlan joinPlan = createPlanWithJoinOverRequests(null, null);

		final Map<LogicalPlan,Integer> results = new HashMap<>();
		results.put( joinPlan, 999 ); // irrelevant for the test
		results.put( joinPlan.getSubPlan(0), 2 );
		results.put( joinPlan.getSubPlan(1), 1 );

		final CardinalityBasedJoinOrderingBase h = new TestImpl(results);

		final LogicalPlan resultPlan = h.apply(joinPlan);

		assertEquals( 2, resultPlan.numberOfSubPlans() );
		assertTrue( resultPlan.getSubPlan(0) == joinPlan.getSubPlan(1) ); // not just equal but indeed the same
		assertTrue( resultPlan.getSubPlan(1) == joinPlan.getSubPlan(0) ); // not just equal but indeed the same
	}

	@Test
	public void twoRequestsUnderFilter() throws LogicalOptimizationException {
		@SuppressWarnings("unchecked")
		final LogicalPlan joinPlan = createPlanWithJoinOverRequests(null, null);

		final LogicalOpFilter f = new LogicalOpFilter( Expr.NONE );
		final LogicalPlan filterPlan = new LogicalPlanWithUnaryRootImpl(f, joinPlan);

		final Map<LogicalPlan,Integer> results = new HashMap<>();
		results.put( filterPlan, 999 ); // irrelevant for the test
		results.put( joinPlan, 999 );   // irrelevant for the test
		results.put( joinPlan.getSubPlan(0), 2 );
		results.put( joinPlan.getSubPlan(1), 1 );

		final CardinalityBasedJoinOrderingBase h = new TestImpl(results);

		final LogicalPlan resultPlan = h.apply(filterPlan);

		assertEquals( 2, resultPlan.getSubPlan(0).numberOfSubPlans() );
		assertTrue( resultPlan.getSubPlan(0).getSubPlan(0) == joinPlan.getSubPlan(1) ); // not just equal but indeed the same
		assertTrue( resultPlan.getSubPlan(0).getSubPlan(1) == joinPlan.getSubPlan(0) ); // not just equal but indeed the same
	}

	@Test
	public void twoRequestsUnderL2G() throws LogicalOptimizationException {
		@SuppressWarnings("unchecked")
		final LogicalPlan joinPlan = createPlanWithJoinOverRequests(null, null);

		final LogicalOpLocalToGlobal l2g = new LogicalOpLocalToGlobal(null);
		final LogicalPlan l2gPlan = new LogicalPlanWithUnaryRootImpl(l2g, joinPlan);

		final Map<LogicalPlan,Integer> results = new HashMap<>();
		results.put( l2gPlan, 999 );  // irrelevant for the test
		results.put( joinPlan, 999 ); // irrelevant for the test
		results.put( joinPlan.getSubPlan(0), 2 );
		results.put( joinPlan.getSubPlan(1), 1 );

		final CardinalityBasedJoinOrderingBase h = new TestImpl(results);

		final LogicalPlan resultPlan = h.apply(l2gPlan);

		assertEquals( 2, resultPlan.getSubPlan(0).numberOfSubPlans() );
		assertTrue( resultPlan.getSubPlan(0).getSubPlan(0) == joinPlan.getSubPlan(1) ); // not just equal but indeed the same
		assertTrue( resultPlan.getSubPlan(0).getSubPlan(1) == joinPlan.getSubPlan(0) ); // not just equal but indeed the same
	}

	@Test
	public void threeRequests1() throws LogicalOptimizationException {
		@SuppressWarnings("unchecked")
		final LogicalPlan joinPlan = createPlanWithJoinOverRequests(
				Set.of( Var.alloc("x") ),
				Set.of( Var.alloc("x") ),
				Set.of( Var.alloc("x") ) );

		final Map<LogicalPlan,Integer> results = new HashMap<>();
		results.put( joinPlan, 999 ); // irrelevant for the test
		results.put( joinPlan.getSubPlan(0), 1 );
		results.put( joinPlan.getSubPlan(1), 2 );
		results.put( joinPlan.getSubPlan(2), 3 );

		final CardinalityBasedJoinOrderingBase h = new TestImpl(results);

		final LogicalPlan resultPlan = h.apply(joinPlan);

		assertTrue( resultPlan.equals(joinPlan) );
	}

	@Test
	public void threeRequests2() throws LogicalOptimizationException {
		@SuppressWarnings("unchecked")
		final LogicalPlan joinPlan = createPlanWithJoinOverRequests(
				Set.of( Var.alloc("x") ),
				Set.of( Var.alloc("x") ),
				Set.of( Var.alloc("x") ) );

		final Map<LogicalPlan,Integer> results = new HashMap<>();
		results.put( joinPlan, 999 ); // irrelevant for the test
		results.put( joinPlan.getSubPlan(0), 2 );
		results.put( joinPlan.getSubPlan(1), 1 );
		results.put( joinPlan.getSubPlan(2), 3 );

		final CardinalityBasedJoinOrderingBase h = new TestImpl(results);

		final LogicalPlan resultPlan = h.apply(joinPlan);

		assertEquals( 3, resultPlan.numberOfSubPlans() );
		assertTrue( resultPlan.getSubPlan(0) == joinPlan.getSubPlan(1) ); // not just equal but indeed the same
		assertTrue( resultPlan.getSubPlan(1) == joinPlan.getSubPlan(0) ); // not just equal but indeed the same
		assertTrue( resultPlan.getSubPlan(2) == joinPlan.getSubPlan(2) ); // not just equal but indeed the same
	}

	@Test
	public void threeRequests3() throws LogicalOptimizationException {
		@SuppressWarnings("unchecked")
		final LogicalPlan joinPlan = createPlanWithJoinOverRequests(
				Set.of( Var.alloc("y") ), // attention, no join variable!
				Set.of( Var.alloc("x") ),
				Set.of( Var.alloc("x") ) );

		final Map<LogicalPlan,Integer> results = new HashMap<>();
		results.put( joinPlan, 999 ); // irrelevant for the test
		results.put( joinPlan.getSubPlan(0), 2 );
		results.put( joinPlan.getSubPlan(1), 1 );
		results.put( joinPlan.getSubPlan(2), 3 );

		final CardinalityBasedJoinOrderingBase h = new TestImpl(results);

		final LogicalPlan resultPlan = h.apply(joinPlan);

		assertEquals( 3, resultPlan.numberOfSubPlans() );
		assertTrue( resultPlan.getSubPlan(0) == joinPlan.getSubPlan(1) ); // not just equal but indeed the same
		assertTrue( resultPlan.getSubPlan(1) == joinPlan.getSubPlan(2) ); // not just equal but indeed the same
		assertTrue( resultPlan.getSubPlan(2) == joinPlan.getSubPlan(0) ); // not just equal but indeed the same
	}

	// ----------- helpers ------------

	protected LogicalPlanWithNaryRoot createPlanWithJoinOverRequests( @SuppressWarnings("unchecked") final Set<Var> ... certainVars ) {
		final TPFServer srv = new TPFServerForTest();

		final LogicalPlan[] subPlans = new LogicalPlan[ certainVars.length ];
		for ( int i = 0; i < certainVars.length; i++ ) {
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

		public TestImpl( final Map<LogicalPlan,Integer> results ) {
			super( new MockCardinalityEstimator(results) );
		}

		@Override
		protected int estimateJoinCardinality( final List<LogicalPlan> selectedPlans,
		                                       final int joinCardOfSelectedPlans,
		                                       final LogicalPlan nextCandidate ) {
			return nextCandidate.getQueryPlanningInfo().getProperty(QueryPlanProperty.CARDINALITY).getValue();
		}
	}

	protected class MockCardinalityEstimator implements CardinalityEstimator {
		protected final Map<LogicalPlan,Integer> results;

		public MockCardinalityEstimator( final Map<LogicalPlan,Integer> results ) {
			this.results = results;
		}

		@Override
		public void addCardinalities( final LogicalPlan... plans ) {
			for ( int i = 0; i < plans.length; i++ ) {
				final LogicalPlan plan = plans[i];
				final Integer card = results.get(plan);

				if ( card == null )
					throw new IllegalArgumentException("The 'results' map has not been populated for all (sub)plans.");

				final QueryPlanningInfo qpInfo = plan.getQueryPlanningInfo();
				qpInfo.addProperty( QueryPlanProperty.cardinality(card, Quality.ACCURATE) );
				qpInfo.addProperty( QueryPlanProperty.maxCardinality(card, Quality.ESTIMATE_BASED_ON_ACCURATES) );
				qpInfo.addProperty( QueryPlanProperty.minCardinality(card, Quality.ESTIMATE_BASED_ON_ACCURATES) );

				if ( plan.numberOfSubPlans() > 0 ) {
					for ( int j = 0; j < plan.numberOfSubPlans(); j++ ) {
						addCardinalities( plan.getSubPlan(j) );
					}
				}
			}
		}

		@Override
		public void addCardinalities( final PhysicalPlan ... plans ) {
			throw new UnsupportedOperationException();
		}
	}

}
