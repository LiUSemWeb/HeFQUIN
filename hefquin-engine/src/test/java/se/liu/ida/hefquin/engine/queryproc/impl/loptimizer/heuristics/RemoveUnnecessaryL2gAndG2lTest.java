package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_IsIRI;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.junit.Test;

import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.base.query.impl.TriplePatternImpl;
import se.liu.ida.hefquin.engine.EngineTestBase;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpFilter;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpLocalToGlobal;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGlobalToLocal;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithBinaryRootImpl;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithNullaryRootImpl;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithUnaryRootImpl;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.federation.access.impl.req.TriplePatternRequestImpl;

public class RemoveUnnecessaryL2gAndG2lTest extends EngineTestBase
{
	@Test
	public void apply_leafWithUnnecessaryL2G() {
		// Tests the case in which the heuristic is applied to a
		// plan with an unnecessary l2g operator. The l2g operator
		// is expected to be removed.

		// set up
		final TriplePattern tp = new TriplePatternImpl( NodeFactory.createVariable( "s" ),
		                                                NodeFactory.createURI( "http://ex.org/property" ),
		                                                NodeFactory.createVariable( "o" ) );

		final TriplePatternRequest req1 = new TriplePatternRequestImpl(tp);
		final FederationMember fm = new SPARQLEndpointForTest("http://ex.org");
		final LogicalOpRequest<?,?> reqOp1 = new LogicalOpRequest<>(fm, false, req1);
		final LogicalPlan leafPlan = new LogicalPlanWithNullaryRootImpl(reqOp1, null);

		final LogicalOpLocalToGlobal l2gOp = new LogicalOpLocalToGlobal(null, false);
		final LogicalPlan planWithL2g = new LogicalPlanWithUnaryRootImpl(l2gOp, null, leafPlan);

		// test
		final LogicalPlan result = new RemoveUnnecessaryL2gAndG2l().apply(planWithL2g);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpRequest );
		assertEquals( 0, result.numberOfSubPlans() );
	}

	@Test
	public void apply_leafWithUnnecessaryG2L() {
		// Tests the case in which the heuristic is applied to a
		// plan with an unnecessary g2l operator (for symmetry).
		// The g2l operator is expected to be removed.

		// set up
		final TriplePattern tp = new TriplePatternImpl( NodeFactory.createVariable( "s" ),
		                                                NodeFactory.createURI( "http://ex.org/property" ),
		                                                NodeFactory.createVariable( "o" ) );

		final TriplePatternRequest req1 = new TriplePatternRequestImpl(tp);
		final FederationMember fm = new SPARQLEndpointForTest("http://ex.org");
		final LogicalOpRequest<?,?> reqOp1 = new LogicalOpRequest<>(fm, false, req1);
		final LogicalPlan leafPlan = new LogicalPlanWithNullaryRootImpl(reqOp1, null);

		final LogicalOpGlobalToLocal g2lOp = new LogicalOpGlobalToLocal(null, false);
		final LogicalPlan planWithG2L = new LogicalPlanWithUnaryRootImpl(g2lOp, null, leafPlan);

		// test
		final LogicalPlan result = new RemoveUnnecessaryL2gAndG2l().apply(planWithG2L);

		// check
		assertFalse( result.getRootOperator() instanceof LogicalOpGlobalToLocal );
		assertEquals( 0, result.numberOfSubPlans() );
	}

	@Test
	public void apply_nestedL2GUnderFilter() {
		// Tests the case in which the heuristic is applied to a
		// plan with a necessary l2g operator nested under a filter operator.
		// The l2g operator is expected to not be removed.

		// set up
		final TriplePattern tp = new TriplePatternImpl( NodeFactory.createVariable( "s" ),
		                                                NodeFactory.createVariable( "p" ),
		                                                NodeFactory.createVariable( "o" ) );

		final TriplePatternRequest req1 = new TriplePatternRequestImpl(tp);
		final FederationMember fm = new SPARQLEndpointForTest("http://ex.org");
		final LogicalOpRequest<?,?> reqOp1 = new LogicalOpRequest<>(fm, false, req1);
		final LogicalPlan leafPlan = new LogicalPlanWithNullaryRootImpl(reqOp1, null);

		final LogicalOpLocalToGlobal l2gOp = new LogicalOpLocalToGlobal(null, false);
		final LogicalPlan planWithL2g = new LogicalPlanWithUnaryRootImpl(l2gOp, null, leafPlan);

		final Var v1 = Var.alloc("x");
		final Expr e = new E_IsIRI( new ExprVar(v1) );
		final LogicalOpFilter filterOp = new LogicalOpFilter(e, false);
		final LogicalPlan filterPlan = new LogicalPlanWithUnaryRootImpl(filterOp, null, planWithL2g);

		// test
		final LogicalPlan result = new RemoveUnnecessaryL2gAndG2l().apply(filterPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpFilter );
		assertEquals( 1, result.numberOfSubPlans() );

		final LogicalPlan subPlan = result.getSubPlan(0);
		assertTrue( subPlan.getRootOperator() instanceof LogicalOpLocalToGlobal );
		assertEquals( 1, subPlan.numberOfSubPlans() );

		final LogicalPlan childOfSubPlan = subPlan.getSubPlan(0);
		assertEquals( leafPlan, childOfSubPlan );
		assertEquals( 0, childOfSubPlan.numberOfSubPlans() );
	}

	@Test
	public void apply_binaryJoin() {
		// Tests the case in which the heuristic is applied to a binary
		// root plan with an unnecessary l2g operator as the root operator for
		// one of its subplans. The first subplan has an unnecessary l2g operator
		// and should be replaced by its child. The second subplan does not have an
		// l2g operator and should remain unchanged.

		// set up
		final FederationMember fm = new SPARQLEndpointForTest("http://ex.org");

		final TriplePattern tp1 = new TriplePatternImpl( NodeFactory.createVariable( "s" ),
		                                                 NodeFactory.createURI( "http://ex.org/property" ),
		                                                 NodeFactory.createVariable( "o" ) );
		final TriplePatternRequest req1 = new TriplePatternRequestImpl(tp1);
		final LogicalOpRequest<?,?> reqOp1 = new LogicalOpRequest<>(fm, false, req1);
		final LogicalPlan leafPlan1 = new LogicalPlanWithNullaryRootImpl(reqOp1, null);

		final LogicalOpLocalToGlobal l2gOp = new LogicalOpLocalToGlobal(null, false);
		final LogicalPlan planWithL2g = new LogicalPlanWithUnaryRootImpl(l2gOp, null, leafPlan1);

		final TriplePattern tp2 = new TriplePatternImpl( NodeFactory.createVariable( "s" ),
		                                                 NodeFactory.createVariable( "p" ),
		                                                 NodeFactory.createVariable( "o" ) );
		final TriplePatternRequest req2 = new TriplePatternRequestImpl(tp2);
		final LogicalOpRequest<?,?> reqOp2 = new LogicalOpRequest<>(fm, false, req2);
		final LogicalPlan leafPlan2 = new LogicalPlanWithNullaryRootImpl(reqOp2, null);

		final LogicalPlan binaryJoinPlan = new LogicalPlanWithBinaryRootImpl(
			LogicalOpJoin.getInstance(),
			null,
			planWithL2g,
			leafPlan2 );

		// test
		final LogicalPlan result = new RemoveUnnecessaryL2gAndG2l().apply(binaryJoinPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpJoin );
		assertEquals( 2, result.numberOfSubPlans() );

		final LogicalPlan firstSubPlan = result.getSubPlan(0);
		assertTrue( firstSubPlan.getRootOperator() instanceof LogicalOpRequest );
		assertEquals( 0, firstSubPlan.numberOfSubPlans() );

		final LogicalPlan secondSubPlan = result.getSubPlan(1);
		assertTrue( secondSubPlan.getRootOperator() instanceof LogicalOpRequest );
		assertEquals( 0, secondSubPlan.numberOfSubPlans() );

	}

	@Test
	public void extractTPs_leafRequest() {
		// Calls extractTPs with a leaf request. The extracted triple patterns
		// are expected to be unchanged from the original triple patterns.

		final TriplePattern tp = new TriplePatternImpl( NodeFactory.createVariable( "s" ),
		                                                NodeFactory.createVariable( "p" ),
		                                                NodeFactory.createVariable( "o" ) );

		final TriplePatternRequest req1 = new TriplePatternRequestImpl(tp);
		final FederationMember fm = new SPARQLEndpointForTest("http://ex.org");
		final LogicalOpRequest<?,?> reqOp1 = new LogicalOpRequest<>(fm, false, req1);
		final LogicalPlan plan = new LogicalPlanWithNullaryRootImpl(reqOp1, null);

		// test
		final Set<TriplePattern> result = RemoveUnnecessaryL2gAndG2l.extractTPs(plan);

		// check
		assertEquals( Set.of(tp), result );
	}

	@Test
	public void extractTPs_GPAddAboveRequestReturnsAllTPs() {
		// Calls extractTPs with a request under a GPAdd operator.
		// The extracted triple patterns are expected to be the combination
		// of the triple patterns of the request- and GPAdd plans.

		// set up
		final FederationMember fm = new SPARQLEndpointForTest("http://ex.org");

		final TriplePattern tp1 = new TriplePatternImpl( NodeFactory.createVariable("s"),
		                                                 NodeFactory.createVariable("p"),
		                                                 NodeFactory.createVariable("o"));
		final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>(fm, false, new TriplePatternRequestImpl(tp1));
		final LogicalPlan leafPlan = new LogicalPlanWithNullaryRootImpl(reqOp, null);

		final TriplePattern tp2 = new TriplePatternImpl( NodeFactory.createVariable("x"),
		                                                 NodeFactory.createVariable("y"),
		                                                 NodeFactory.createVariable("z"));
		final LogicalOpGPAdd gpAddOp = new LogicalOpGPAdd(fm, tp2, null, false);

		final LogicalPlan gpAddPlan = new LogicalPlanWithUnaryRootImpl(gpAddOp, null, leafPlan);

		// test
		final Set<TriplePattern> result = RemoveUnnecessaryL2gAndG2l.extractTPs(gpAddPlan);

		// check
		assertEquals( Set.of(tp1, tp2), result );
	}

	@Test
	public void extractTPs_requestUnderFilter() {
		// Calls extractTPs with a request under a filter operator.
		// The extracted triple patterns are expected to remain unchanged from
		// the original triple patterns because filter operators do not contain
		// triple patterns themselves.

		// set up
		final FederationMember fm = new SPARQLEndpointForTest("http://ex.org");
		final TriplePattern tp1 = new TriplePatternImpl( NodeFactory.createVariable("s"),
		                                                 NodeFactory.createVariable("p"),
		                                                 NodeFactory.createVariable("o"));
		final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>(fm, false, new TriplePatternRequestImpl(tp1));
		final LogicalPlan leafPlan = new LogicalPlanWithNullaryRootImpl(reqOp, null);

		final Var v1 = Var.alloc("x");
		final Expr e = new E_IsIRI( new ExprVar(v1) );
		final LogicalOpFilter filterOp = new LogicalOpFilter(e, false);

		final LogicalPlan filterPlan = new LogicalPlanWithUnaryRootImpl(filterOp, null, leafPlan);

		// test
		final Set<TriplePattern> result = RemoveUnnecessaryL2gAndG2l.extractTPs(filterPlan);

		// check
		assertEquals( Set.of(tp1), result );
	}

}