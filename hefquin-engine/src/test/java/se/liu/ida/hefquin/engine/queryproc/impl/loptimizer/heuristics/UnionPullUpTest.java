package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.Expr;
import org.junit.Test;

import se.liu.ida.hefquin.base.data.VocabularyMapping;
import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.impl.TriplePatternImpl;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.logical.NullaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpFilter;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpLocalToGlobal;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayUnion;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpUnion;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithNaryRootImpl;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithUnaryRootImpl;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.DataRetrievalInterface;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;

public class UnionPullUpTest
{
	@Test
	public void rewritePlanWithUnionRootAndUnionChild() {
		final LogicalPlan lp1 = new DummyLogicalPlan();
		final LogicalPlan lp2 = new DummyLogicalPlan();
		final LogicalPlan lp3 = new DummyLogicalPlan();
		final LogicalPlan lp4 = new DummyLogicalPlan();
		final LogicalPlan lp5 = new DummyLogicalPlan();

		final List<LogicalPlan> subPlans1 = new ArrayList<>();
		subPlans1.add(lp1);
		subPlans1.add(lp2);
		final LogicalPlan unionPlan1 = new LogicalPlanWithNaryRootImpl( LogicalOpMultiwayUnion.getInstance(), subPlans1 );

		final List<LogicalPlan> subPlans2 = new ArrayList<>();
		subPlans2.add(lp3);
		subPlans2.add(lp4);
		final LogicalPlan unionPlan2 = new LogicalPlanWithNaryRootImpl( LogicalOpMultiwayUnion.getInstance(), subPlans2 );

		final List<LogicalPlan> plansWithUnionRoot = new ArrayList<>();
		plansWithUnionRoot.add(unionPlan1);
		plansWithUnionRoot.add(unionPlan2);

		final List<LogicalPlan> plansWithNonUnionRoot = new ArrayList<>();
		plansWithNonUnionRoot.add(lp5);

		final LogicalPlan resultPlan = new UnionPullUp().rewritePlanWithUnionRootAndUnionChild(plansWithUnionRoot, plansWithNonUnionRoot);

		final LogicalOperator rootOfResultPlan = resultPlan.getRootOperator();
		assertTrue( rootOfResultPlan instanceof LogicalOpMultiwayUnion || rootOfResultPlan instanceof LogicalOpUnion );
		assertEquals( 5, resultPlan.numberOfSubPlans() );

		final Set<LogicalPlan> expectedSubPlans = new HashSet<>();
		expectedSubPlans.add(lp1);
		expectedSubPlans.add(lp2);
		expectedSubPlans.add(lp3);
		expectedSubPlans.add(lp4);
		expectedSubPlans.add(lp5);

		for ( int i = 0; i < 5; i++ ) {
			final LogicalPlan childOfResultPlan = resultPlan.getSubPlan(i);
			assertTrue( expectedSubPlans.contains(childOfResultPlan) );
		}
	}

	@Test
	public void rewritePlanWithJoinOverUnion1() {
		final LogicalPlan lp1 = new DummyLogicalPlan();
		final LogicalPlan lp2 = new DummyLogicalPlan();
		final LogicalPlan lp3 = new DummyLogicalPlan();
		final LogicalPlan lp4 = new DummyLogicalPlan();
		final LogicalPlan lp5 = new DummyLogicalPlan();

		final List<LogicalPlan> subPlans1 = new ArrayList<>();
		subPlans1.add(lp1);
		subPlans1.add(lp2);
		final LogicalPlan unionPlan1 = new LogicalPlanWithNaryRootImpl( LogicalOpMultiwayUnion.getInstance(), subPlans1 );

		final List<LogicalPlan> subPlans2 = new ArrayList<>();
		subPlans2.add(lp3);
		subPlans2.add(lp4);
		final LogicalPlan unionPlan2 = new LogicalPlanWithNaryRootImpl( LogicalOpMultiwayUnion.getInstance(), subPlans2 );

		final List<LogicalPlan> plansWithUnionRoot = new ArrayList<>();
		plansWithUnionRoot.add(unionPlan1);
		plansWithUnionRoot.add(unionPlan2);

		final List<LogicalPlan> plansWithNonUnionRoot = new ArrayList<>();
		plansWithNonUnionRoot.add(lp5);

		final LogicalPlan resultPlan = new UnionPullUp().rewritePlanWithJoinOverUnion(plansWithUnionRoot, plansWithNonUnionRoot);

		final LogicalOperator rootOfResultPlan = resultPlan.getRootOperator();
		assertTrue( rootOfResultPlan instanceof LogicalOpMultiwayUnion || rootOfResultPlan instanceof LogicalOpUnion );
		assertEquals( 4, resultPlan.numberOfSubPlans() );

		for ( int i = 0; i < 4; i++ ) {
			final LogicalPlan childOfResultPlan = resultPlan.getSubPlan(i);
			final LogicalOperator rootOfChild = childOfResultPlan.getRootOperator();

			assertTrue( rootOfChild instanceof LogicalOpMultiwayJoin || rootOfChild instanceof LogicalOpJoin );
			assertEquals( 3, childOfResultPlan.numberOfSubPlans() );

			boolean lp5Found = false;
			boolean lp1orlp2Found = false;
			boolean lp3orlp4Found = false;
			for ( int j = 0; j < 3; j++ ) {
				final LogicalPlan grandchildOfResultPlan = childOfResultPlan.getSubPlan(j);
				if ( grandchildOfResultPlan == lp5 ) // Don't use 'equals(..)' here!
					lp5Found = true;
				else if ( grandchildOfResultPlan == lp1 || grandchildOfResultPlan == lp2 )
					lp1orlp2Found = true;
				else if ( grandchildOfResultPlan == lp3 || grandchildOfResultPlan == lp4 )
					lp3orlp4Found = true;
			}

			assertTrue(lp5Found);
			assertTrue(lp1orlp2Found);
			assertTrue(lp3orlp4Found);
		}
	}

	@Test
	public void rewritePlanWithJoinOverUnion2() {
		final LogicalPlan lp1 = new DummyLogicalPlan();
		final LogicalPlan lp2 = new DummyLogicalPlan();
		final LogicalPlan lp3 = new DummyLogicalPlan();

		final List<LogicalPlan> subPlans = new ArrayList<>();
		subPlans.add(lp1);
		final LogicalPlan unionPlan = new LogicalPlanWithNaryRootImpl( LogicalOpMultiwayUnion.getInstance(), subPlans );

		final List<LogicalPlan> plansWithUnionRoot = new ArrayList<>();
		plansWithUnionRoot.add(unionPlan);

		final List<LogicalPlan> plansWithNonUnionRoot = new ArrayList<>();
		plansWithNonUnionRoot.add(lp2);
		plansWithNonUnionRoot.add(lp3);

		final LogicalPlan resultPlan = new UnionPullUp().rewritePlanWithJoinOverUnion(plansWithUnionRoot, plansWithNonUnionRoot);

		final LogicalOperator rootOfResultPlan = resultPlan.getRootOperator();
		assertTrue( rootOfResultPlan instanceof LogicalOpMultiwayUnion || rootOfResultPlan instanceof LogicalOpUnion );
		assertEquals( 1, resultPlan.numberOfSubPlans() );

		final LogicalPlan childOfResultPlan = resultPlan.getSubPlan(0);
		final LogicalOperator rootOfChild = childOfResultPlan.getRootOperator();

		assertTrue( rootOfChild instanceof LogicalOpMultiwayJoin || rootOfChild instanceof LogicalOpJoin );
		assertEquals( 3, childOfResultPlan.numberOfSubPlans() );

		boolean lp1Found = false;
		boolean lp2Found = false;
		boolean lp3Found = false;
		for ( int j = 0; j < 3; j++ ) {
			final LogicalPlan grandchildOfResultPlan = childOfResultPlan.getSubPlan(j);
			if ( grandchildOfResultPlan == lp1 ) // Don't use 'equals(..)' here!
				lp1Found = true;
			else if ( grandchildOfResultPlan == lp2 )
				lp2Found = true;
			else if ( grandchildOfResultPlan == lp3 )
				lp3Found = true;
		}

		assertTrue(lp1Found);
		assertTrue(lp2Found);
		assertTrue(lp3Found);
	}

	@Test
	public void rewritePlanWithUnaryRootAndUnionChild() {
		final LogicalPlan lp1 = new DummyLogicalPlan();
		final LogicalPlan lp2 = new DummyLogicalPlan();

		final List<LogicalPlan> subPlans = new ArrayList<>();
		subPlans.add(lp1);
		subPlans.add(lp2);
		final LogicalPlan unionPlan = new LogicalPlanWithNaryRootImpl( LogicalOpMultiwayUnion.getInstance(), subPlans );

		final UnaryLogicalOp rootOp = new LogicalOpFilter( Expr.NONE );

		final LogicalPlan resultPlan = new UnionPullUp().rewritePlanWithUnaryRootAndUnionChild(rootOp, unionPlan);

		final LogicalOperator rootOfResultPlan = resultPlan.getRootOperator();
		assertTrue( rootOfResultPlan instanceof LogicalOpMultiwayUnion || rootOfResultPlan instanceof LogicalOpUnion );
		assertEquals( 2, resultPlan.numberOfSubPlans() );

		boolean lp1Found = false;
		boolean lp2Found = false;

		for ( int i = 0; i < 2; i++ ) {
			final LogicalPlan childOfResultPlan = resultPlan.getSubPlan(i);
			final LogicalOperator rootOfChild = childOfResultPlan.getRootOperator();

			assertEquals( rootOp, rootOfChild );
			assertEquals( 1, childOfResultPlan.numberOfSubPlans() );

			final LogicalPlan grandchildOfResultPlan = childOfResultPlan.getSubPlan(0);
			if ( grandchildOfResultPlan == lp1 )  // Don't use 'equals(..)' here!
				lp1Found = true;
			else if ( grandchildOfResultPlan == lp2 )
				lp2Found = true;
		}

		assertTrue(lp1Found);
		assertTrue(lp2Found);
	}

	@Test
	public void apply_JoinOverUnion() {
		final LogicalPlan lp1 = new DummyLogicalPlan();
		final LogicalPlan lp2 = new DummyLogicalPlan();
		final LogicalPlan lp3 = new DummyLogicalPlan();

		final List<LogicalPlan> subPlans1 = new ArrayList<>();
		subPlans1.add(lp1);
		subPlans1.add(lp2);
		final LogicalPlan unionPlan = new LogicalPlanWithNaryRootImpl( LogicalOpMultiwayUnion.getInstance(), subPlans1 );

		final List<LogicalPlan> subPlans2 = new ArrayList<>();
		subPlans2.add(unionPlan);
		subPlans2.add(lp3);
		final LogicalPlan joinPlan = new LogicalPlanWithNaryRootImpl( LogicalOpMultiwayJoin.getInstance(), subPlans2 );

		final LogicalPlan resultPlan = new UnionPullUp().apply(joinPlan);

		final LogicalOperator rootOfResultPlan = resultPlan.getRootOperator();
		assertTrue( rootOfResultPlan instanceof LogicalOpMultiwayUnion || rootOfResultPlan instanceof LogicalOpUnion );
		assertEquals( 2, resultPlan.numberOfSubPlans() );

		boolean join13Found = false;
		boolean join23Found = false;

		for ( int i = 0; i < 2; i++ ) {
			final LogicalPlan childOfResultPlan = resultPlan.getSubPlan(i);
			final LogicalOperator rootOfChild = childOfResultPlan.getRootOperator();

			assertTrue( rootOfChild instanceof LogicalOpMultiwayJoin || rootOfChild instanceof LogicalOpJoin );
			assertEquals( 2, childOfResultPlan.numberOfSubPlans() );

			boolean lp1Found = false;
			boolean lp2Found = false;
			boolean lp3Found = false;

			for ( int j = 0; j < 2; j++ ) {
				final LogicalPlan grandchildOfResultPlan = childOfResultPlan.getSubPlan(j);
				if ( grandchildOfResultPlan == lp1 )
					lp1Found = true;
				else if ( grandchildOfResultPlan == lp2 )
					lp2Found = true;
				else if ( grandchildOfResultPlan == lp3 )
					lp3Found = true;
			}

			assertTrue(lp3Found);

			if ( lp1Found )
				join13Found = true;
			else if ( lp2Found )
				join23Found = true;
		}

		assertTrue(join13Found);
		assertTrue(join23Found);
		
	}

	@Test
	public void apply_UnionOverUnion() {
		final LogicalPlan lp1 = new DummyLogicalPlan();
		final LogicalPlan lp2 = new DummyLogicalPlan();
		final LogicalPlan lp3 = new DummyLogicalPlan();

		final List<LogicalPlan> subPlans1 = new ArrayList<>();
		subPlans1.add(lp1);
		subPlans1.add(lp2);
		final LogicalPlan unionPlan1 = new LogicalPlanWithNaryRootImpl( LogicalOpMultiwayUnion.getInstance(), subPlans1 );

		final List<LogicalPlan> subPlans2 = new ArrayList<>();
		subPlans2.add(unionPlan1);
		subPlans2.add(lp3);
		final LogicalPlan unionPlan2 = new LogicalPlanWithNaryRootImpl( LogicalOpMultiwayUnion.getInstance(), subPlans2 );

		final LogicalPlan resultPlan = new UnionPullUp().apply(unionPlan2);

		final LogicalOperator rootOfResultPlan = resultPlan.getRootOperator();
		assertTrue( rootOfResultPlan instanceof LogicalOpMultiwayUnion || rootOfResultPlan instanceof LogicalOpUnion );
		assertEquals( 3, resultPlan.numberOfSubPlans() );

		boolean lp1Found = false;
		boolean lp2Found = false;
		boolean lp3Found = false;

		for ( int i = 0; i < 3; i++ ) {
			final LogicalPlan childOfResultPlan = resultPlan.getSubPlan(i);

			if ( childOfResultPlan == lp1 ) // don't use 'equals()' here!
				lp1Found = true;
			if ( childOfResultPlan == lp2 ) // don't use 'equals()' here!
				lp2Found = true;
			if ( childOfResultPlan == lp3 ) // don't use 'equals()' here!
				lp3Found = true;
		}

		assertTrue(lp1Found);
		assertTrue(lp2Found);
		assertTrue(lp3Found);
	}

	@Test
	public void apply_UnionOverJoinOverUnion() {
		final LogicalPlan lp1 = new DummyLogicalPlan();
		final LogicalPlan lp2 = new DummyLogicalPlan();
		final LogicalPlan lp3 = new DummyLogicalPlan();
		final LogicalPlan lp4 = new DummyLogicalPlan();

		final List<LogicalPlan> subPlans1 = new ArrayList<>();
		subPlans1.add(lp1);
		subPlans1.add(lp2);
		final LogicalPlan unionPlan1 = new LogicalPlanWithNaryRootImpl( LogicalOpMultiwayUnion.getInstance(), subPlans1 );

		final List<LogicalPlan> subPlans2 = new ArrayList<>();
		subPlans2.add(unionPlan1);
		subPlans2.add(lp3);
		final LogicalPlan joinPlan = new LogicalPlanWithNaryRootImpl( LogicalOpMultiwayJoin.getInstance(), subPlans2 );

		final List<LogicalPlan> subPlans3 = new ArrayList<>();
		subPlans3.add(joinPlan);
		subPlans3.add(lp4);
		final LogicalPlan unionPlan2 = new LogicalPlanWithNaryRootImpl( LogicalOpMultiwayUnion.getInstance(), subPlans3 );

		final LogicalPlan resultPlan = new UnionPullUp().apply(unionPlan2);

		final LogicalOperator rootOfResultPlan = resultPlan.getRootOperator();
		assertTrue( rootOfResultPlan instanceof LogicalOpMultiwayUnion || rootOfResultPlan instanceof LogicalOpUnion );
		assertEquals( 3, resultPlan.numberOfSubPlans() );

		boolean join13Found = false;
		boolean join23Found = false;
		boolean lp4Found = false;

		for ( int i = 0; i < 3; i++ ) {
			final LogicalPlan childOfResultPlan = resultPlan.getSubPlan(i);

			if ( childOfResultPlan == lp4 ) { // don't use 'equals()' here!
				lp4Found = true;
			}
			else {
				final LogicalOperator rootOfChild = childOfResultPlan.getRootOperator();

				assertTrue( rootOfChild instanceof LogicalOpMultiwayJoin || rootOfChild instanceof LogicalOpJoin );
				assertEquals( 2, childOfResultPlan.numberOfSubPlans() );

				boolean lp1Found = false;
				boolean lp2Found = false;
				boolean lp3Found = false;

				for ( int j = 0; j < 2; j++ ) {
					final LogicalPlan grandchildOfResultPlan = childOfResultPlan.getSubPlan(j);
					if ( grandchildOfResultPlan == lp1 )
						lp1Found = true;
					else if ( grandchildOfResultPlan == lp2 )
						lp2Found = true;
					else if ( grandchildOfResultPlan == lp3 )
						lp3Found = true;
				}

				assertTrue(lp3Found);

				if ( lp1Found )
					join13Found = true;
				else if ( lp2Found )
					join23Found = true;
			}
		}

		assertTrue(lp4Found);
		assertTrue(join13Found);
		assertTrue(join23Found);
	}

	@Test
	public void apply_GPAddOverGPAddOverUnion() {
		final LogicalPlan lp1 = new DummyLogicalPlan();
		final LogicalPlan lp2 = new DummyLogicalPlan();

		final List<LogicalPlan> subPlans = new ArrayList<>();
		subPlans.add(lp1);
		subPlans.add(lp2);
		final LogicalPlan unionPlan = new LogicalPlanWithNaryRootImpl( LogicalOpMultiwayUnion.getInstance(), subPlans );

		final LogicalOpGPAdd gpAdd1 = new LogicalOpGPAdd( new DummyFederationMember(), new DummyTriplePattern() );
		final LogicalPlan gpAddPlan1 = new LogicalPlanWithUnaryRootImpl(gpAdd1, unionPlan);

		final LogicalOpGPAdd gpAdd2 = new LogicalOpGPAdd( new DummyFederationMember(), new DummyTriplePattern() );
		final LogicalPlan gpAddPlan2 = new LogicalPlanWithUnaryRootImpl(gpAdd2, gpAddPlan1);

		final LogicalPlan resultPlan = new UnionPullUp().apply(gpAddPlan2);

		final LogicalOperator rootOfResultPlan = resultPlan.getRootOperator();
		assertTrue( rootOfResultPlan instanceof LogicalOpMultiwayUnion || rootOfResultPlan instanceof LogicalOpUnion );
		assertEquals( 2, resultPlan.numberOfSubPlans() );

		final LogicalPlan child1 = resultPlan.getSubPlan(0);
		final LogicalPlan child2 = resultPlan.getSubPlan(1);

		assertEquals( 1, child1.numberOfSubPlans() );
		assertEquals( 1, child2.numberOfSubPlans() );

		final LogicalOperator rootOfChild1 = child1.getRootOperator();
		final LogicalOperator rootOfChild2 = child2.getRootOperator();

		assertTrue( rootOfChild1 == gpAdd2 );
		assertTrue( rootOfChild2 == gpAdd2 );

		final LogicalPlan grandchild1 = child1.getSubPlan(0);
		final LogicalPlan grandchild2 = child2.getSubPlan(0);

		assertEquals( 1, grandchild1.numberOfSubPlans() );
		assertEquals( 1, grandchild2.numberOfSubPlans() );

		final LogicalOperator rootOfGrandchild1 = grandchild1.getRootOperator();
		final LogicalOperator rootOfGrandchild2 = grandchild2.getRootOperator();

		assertTrue( rootOfGrandchild1 == gpAdd1 );
		assertTrue( rootOfGrandchild2 == gpAdd1 );

		final LogicalPlan grandgrandchild1 = grandchild1.getSubPlan(0);
		final LogicalPlan grandgrandchild2 = grandchild2.getSubPlan(0);

		boolean lp1Found = ( grandgrandchild1 == lp1 || grandgrandchild2 == lp1 );
		boolean lp2Found = ( grandgrandchild1 == lp2 || grandgrandchild2 == lp2 );

		assertTrue(lp1Found);
		assertTrue(lp2Found);
	}

	@Test
	public void apply_L2GOverGPAddOverGPAddOverUnion() {
		final LogicalPlan lp1 = new DummyLogicalPlan();
		final LogicalPlan lp2 = new DummyLogicalPlan();

		final List<LogicalPlan> subPlans = new ArrayList<>();
		subPlans.add(lp1);
		subPlans.add(lp2);
		final LogicalPlan unionPlan = new LogicalPlanWithNaryRootImpl( LogicalOpMultiwayUnion.getInstance(), subPlans );

		final LogicalOpGPAdd gpAdd1 = new LogicalOpGPAdd( new DummyFederationMember(), new DummyTriplePattern() );
		final LogicalPlan gpAddPlan1 = new LogicalPlanWithUnaryRootImpl(gpAdd1, unionPlan);

		final LogicalOpGPAdd gpAdd2 = new LogicalOpGPAdd( new DummyFederationMember(), new DummyTriplePattern() );
		final LogicalPlan gpAddPlan2 = new LogicalPlanWithUnaryRootImpl(gpAdd2, gpAddPlan1);

		final LogicalOpLocalToGlobal l2g = new LogicalOpLocalToGlobal(null);
		final LogicalPlan l2gPlan = new LogicalPlanWithUnaryRootImpl(l2g, gpAddPlan2);

		final LogicalPlan resultPlan = new UnionPullUp().apply(l2gPlan);

		final LogicalOperator rootOfResultPlan = resultPlan.getRootOperator();
		assertTrue( rootOfResultPlan instanceof LogicalOpMultiwayUnion || rootOfResultPlan instanceof LogicalOpUnion );
		assertEquals( 2, resultPlan.numberOfSubPlans() );

		final LogicalPlan child1 = resultPlan.getSubPlan(0);
		final LogicalPlan child2 = resultPlan.getSubPlan(1);

		assertEquals( 1, child1.numberOfSubPlans() );
		assertEquals( 1, child2.numberOfSubPlans() );

		final LogicalOperator rootOfChild1 = child1.getRootOperator();
		final LogicalOperator rootOfChild2 = child2.getRootOperator();

		assertEquals( l2g, rootOfChild1 );
		assertEquals( l2g, rootOfChild2 );

		final LogicalPlan grandchild1 = child1.getSubPlan(0);
		final LogicalPlan grandchild2 = child2.getSubPlan(0);

		assertEquals( 1, grandchild1.numberOfSubPlans() );
		assertEquals( 1, grandchild2.numberOfSubPlans() );

		final LogicalOperator rootOfGrandchild1 = grandchild1.getRootOperator();
		final LogicalOperator rootOfGrandchild2 = grandchild2.getRootOperator();

		assertTrue( rootOfGrandchild1 == gpAdd2 );
		assertTrue( rootOfGrandchild2 == gpAdd2 );

		final LogicalPlan grandgrandchild1 = grandchild1.getSubPlan(0);
		final LogicalPlan grandgrandchild2 = grandchild2.getSubPlan(0);

		assertEquals( 1, grandgrandchild1.numberOfSubPlans() );
		assertEquals( 1, grandgrandchild2.numberOfSubPlans() );

		final LogicalOperator rootOfGrandgrandchild1 = grandgrandchild1.getRootOperator();
		final LogicalOperator rootOfGrandgrandchild2 = grandgrandchild2.getRootOperator();

		assertTrue( rootOfGrandgrandchild1 == gpAdd1 );
		assertTrue( rootOfGrandgrandchild2 == gpAdd1 );

		final LogicalPlan grandgrandgrandchild1 = grandgrandchild1.getSubPlan(0);
		final LogicalPlan grandgrandgrandchild2 = grandgrandchild2.getSubPlan(0);

		boolean lp1Found = ( grandgrandgrandchild1 == lp1 || grandgrandgrandchild2 == lp1 );
		boolean lp2Found = ( grandgrandgrandchild1 == lp2 || grandgrandgrandchild2 == lp2 );

		assertTrue(lp1Found);
		assertTrue(lp2Found);
	}


	// ---- helpers -----

	protected static class DummyLogicalPlan implements LogicalPlan {
		final protected LogicalOperator rootOp = new DummyLogicalOp();
		@Override public LogicalOperator getRootOperator() { return rootOp; }
		@Override public ExpectedVariables getExpectedVariables() { throw new UnsupportedOperationException(); }
		@Override public int numberOfSubPlans() { return 0; }
		@Override public LogicalPlan getSubPlan(int i) { throw new UnsupportedOperationException(); }
	}

	protected static class DummyLogicalOp implements NullaryLogicalOp {
		@Override public void visit(LogicalPlanVisitor visitor) { throw new UnsupportedOperationException(); }
		@Override public ExpectedVariables getExpectedVariables(ExpectedVariables... inputVars) { throw new UnsupportedOperationException(); }
		@Override public int getID() { throw new UnsupportedOperationException(); }
	}

	protected static class DummyFederationMember implements FederationMember {
		@Override public DataRetrievalInterface getInterface() { return new DummyDataRetrievalInterface(); }
		@Override public VocabularyMapping getVocabularyMapping() { throw new UnsupportedOperationException(); }
	}

	protected static class DummyDataRetrievalInterface implements DataRetrievalInterface {
		@Override public boolean supportsTriplePatternRequests() { return true; }
		@Override public boolean supportsBGPRequests() { throw new UnsupportedOperationException(); }
		@Override public boolean supportsSPARQLPatternRequests() { throw new UnsupportedOperationException(); }
		@Override public boolean supportsRequest(DataRetrievalRequest req) { throw new UnsupportedOperationException(); }
		@Override public int getID() { throw new UnsupportedOperationException(); }
	}

	protected static class DummyTriplePattern extends TriplePatternImpl {
		public DummyTriplePattern() { super(Node.ANY, Node.ANY, Node.ANY); }
	}

}
