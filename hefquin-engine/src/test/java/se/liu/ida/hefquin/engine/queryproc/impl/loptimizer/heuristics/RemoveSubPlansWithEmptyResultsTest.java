package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static se.liu.ida.hefquin.engine.queryplan.info.QueryPlanProperty.CARDINALITY;

import org.apache.jena.sparql.core.Var;
import org.junit.Test;

import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.base.query.impl.TriplePatternImpl;
import se.liu.ida.hefquin.engine.EngineTestBase;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanProperty;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanProperty.Quality;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpUnion;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithNullaryRootImpl;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.impl.req.SPARQLRequestImpl;

public class RemoveSubPlansWithEmptyResultsTest extends EngineTestBase
{
	@Test
	public void removeEmptySubPlansRemoveUnion() {
		// union operator on top, left branch is empty and so it
		// is removed, leading to the union also being removed
		// because it only has a right branch

		// request that has cardinality 0 and quality accurate
		// can we set cardinality?
		final Var v1 = Var.alloc("x");
		final FederationMember fm = new TPFServerForTest();
		final TriplePattern tp = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>( fm, false, new SPARQLRequestImpl(tp) );

		final LogicalPlan reqPlan = new LogicalPlanWithNullaryRootImpl(reqOp, null);
		final QueryPlanningInfo qpInfo = reqPlan.getQueryPlanningInfo();
		qpInfo.addProperty( QueryPlanProperty.cardinality(0, Quality.ACCURATE) );
		qpInfo.addProperty( QueryPlanProperty.maxCardinality(0, Quality.ACCURATE) );
		qpInfo.addProperty( QueryPlanProperty.minCardinality(0, Quality.ACCURATE) );
		// does this above work?

		// bind which is the parent of this request,
		// nothing too fancy, we just dont want anything
		// that will change the cardinality

		// request with non-zero cardinality

		// union at the top

		// test
		assertEquals( 0, reqPlan.getQueryPlanningInfo().getProperty(CARDINALITY) );

		final LogicalPlan result = new RemoveSubPlansWithEmptyResults().apply(null);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpUnion );

		final LogicalPlan subResult = result.getSubPlan(0);
		assertTrue( subResult.getRootOperator() instanceof LogicalOpRequest );

		// somehow check that result doesnt have a second child, how do we do that
		assertEquals( 1, result.numberOfSubPlans() );
	}

	@Test
	public void removeEmptySubPlansKeepUnion() {
		// a similar case to the previous but the union does not get removed,
		// so a subsubplan would have to be removed but two kept



	}

}
