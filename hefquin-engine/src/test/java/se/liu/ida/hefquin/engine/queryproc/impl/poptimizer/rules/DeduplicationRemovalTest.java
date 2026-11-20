package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.rules;

import static org.junit.Assert.assertTrue;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_IsIRI;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.junit.Test;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.base.query.impl.TriplePatternImpl;
import se.liu.ida.hefquin.engine.EngineTestBase;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpFilter;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpFixedSolMap;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.physical.NullaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.UnaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpDuplicateRemoval;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpFilter;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpFixedSolMap;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.federation.access.impl.req.SPARQLRequestImpl;

public class DeduplicationRemovalTest extends EngineTestBase
{
	@Test
	public void removeDedupWhenInputGuaranteesDuplicateFreeResult() {
		// The deduplication operator is removed because its input
		// guarantees a duplicate-free result.

		// set up
		final Var v1 = Var.alloc("v1");
		final Var v2 = Var.alloc("v2");

		final Node x1 = NodeFactory.createURI("http://example.org/x1");
		final Node y1 = NodeFactory.createURI("http://example.org/y1");

		final SolutionMapping sm = SolutionMappingUtils.createSolutionMapping(v1, x1, v2, y1);

		final LogicalOpFixedSolMap logicalOpFixedSolMap = new LogicalOpFixedSolMap(sm);
		final NullaryPhysicalOp physicalOpFixedSolMap = PhysicalOpFixedSolMap.getFactory().create(logicalOpFixedSolMap);
		final PhysicalPlan physicalFixedSolMapPlan = PhysicalPlanFactory.createPlan(physicalOpFixedSolMap, (QueryPlanningInfo) null);

		final PhysicalOpDuplicateRemoval physicalOpDedup = PhysicalOpDuplicateRemoval.getInstance();
		final PhysicalPlan physicalDedupPlan = PhysicalPlanFactory.createPlan(physicalOpDedup, physicalFixedSolMapPlan);

		// test
		final PhysicalPlan result = new DeduplicationRemoval().apply(physicalDedupPlan);

		// check
		assertTrue( result.getRootOperator() instanceof PhysicalOpFixedSolMap );
	}

	@Test
	public void keepDedupWhenInputMayContainDuplicates() {
		// The deduplication operator is kept because its input may
		// contain duplicates.

		// set up
		final Var v1 = Var.alloc("v1");

		final TriplePattern tp1 = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>(
			new SPARQLEndpointForTest("http://exA.org"),
			false,
			new SPARQLRequestImpl(tp1) );
		final NullaryPhysicalOp physicalOpRequest = PhysicalOpRequest.getFactory().create(reqOp);
		final PhysicalPlan physicalRequestPlan = PhysicalPlanFactory.createPlan(physicalOpRequest, (QueryPlanningInfo) null);

		final PhysicalOpDuplicateRemoval physicalOpDedup = PhysicalOpDuplicateRemoval.getInstance();
		final PhysicalPlan requestPlan = PhysicalPlanFactory.createPlan(physicalOpDedup, physicalRequestPlan);

		// test
		final PhysicalPlan result = new DeduplicationRemoval().apply(requestPlan);

		// check
		assertTrue( result.isSamePlan(requestPlan) );
	}

	@Test
	public void removeDedupThroughNeutralOperator() {
		// The deduplication operator is removed because its input
		// guarantees a duplicate-free result, even though a neutral
		// operator occurs between them.

		// set up
		final Var v1 = Var.alloc("v1");
		final Var v2 = Var.alloc("v2");

		final Node x1 = NodeFactory.createURI("http://example.org/x1");
		final Node y1 = NodeFactory.createURI("http://example.org/y1");

		final SolutionMapping sm = SolutionMappingUtils.createSolutionMapping(v1, x1, v2, y1);

		final LogicalOpFixedSolMap logicalOpFixedSolMap = new LogicalOpFixedSolMap(sm);
		final NullaryPhysicalOp physicalOpFixedSolMap = PhysicalOpFixedSolMap.getFactory().create(logicalOpFixedSolMap);
		final PhysicalPlan physicalFixedSolMapPlan = PhysicalPlanFactory.createPlan(physicalOpFixedSolMap, (QueryPlanningInfo) null);

		final Expr e = new E_IsIRI( new ExprVar(v1) );
		final UnaryLogicalOp logicalOpFilter = new LogicalOpFilter(e, false);
		final UnaryPhysicalOp physicalOpFilter = PhysicalOpFilter.getFactory().create(logicalOpFilter);
		final PhysicalPlan physicalFilterPlan = PhysicalPlanFactory.createPlan(physicalOpFilter, physicalFixedSolMapPlan);

		final PhysicalOpDuplicateRemoval physicalOpDedup = PhysicalOpDuplicateRemoval.getInstance();
		final PhysicalPlan physicalDedupPlan = PhysicalPlanFactory.createPlan(physicalOpDedup, physicalFilterPlan);

		// test
		final PhysicalPlan result = new DeduplicationRemoval().apply(physicalDedupPlan);

		// check
		assertTrue( result.getRootOperator() instanceof PhysicalOpFilter );

		final PhysicalPlan subPlan = result.getSubPlan(0);
		assertTrue( subPlan.getRootOperator() instanceof PhysicalOpFixedSolMap );
	}

	@Test
	public void keepDedupThroughNeutralOperator() {
		// The deduplication operator is kept because its input may
		// contain duplicates, even though a neutral operator occurs
		// between them.

		// set up
		final Var v1 = Var.alloc("v1");

		final TriplePattern tp1 = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>(
			new SPARQLEndpointForTest("http://exA.org"),
			false,
			new SPARQLRequestImpl(tp1) );
		final NullaryPhysicalOp physicalOpRequest = PhysicalOpRequest.getFactory().create(reqOp);
		final PhysicalPlan physicalRequestPlan = PhysicalPlanFactory.createPlan(physicalOpRequest, (QueryPlanningInfo) null);

		final Expr e = new E_IsIRI( new ExprVar(v1) );
		final UnaryLogicalOp logicalOpFilter = new LogicalOpFilter(e, false);
		final UnaryPhysicalOp physicalOpFilter = PhysicalOpFilter.getFactory().create(logicalOpFilter);
		final PhysicalPlan physicalFilterPlan = PhysicalPlanFactory.createPlan(physicalOpFilter, physicalRequestPlan);

		final PhysicalOpDuplicateRemoval physicalOpDedup = PhysicalOpDuplicateRemoval.getInstance();
		final PhysicalPlan physicalDedupPlan = PhysicalPlanFactory.createPlan(physicalOpDedup, physicalFilterPlan);

		// test
		final PhysicalPlan result = new DeduplicationRemoval().apply(physicalDedupPlan);

		// check
		assertTrue( result.isSamePlan(physicalDedupPlan) );
	}

	@Test
	public void removeDedupInSubPlan() {
		// The deduplication operator is removed from a subplan even
		// when the root operator is not a deduplication operator.

		// set up
		final Var v1 = Var.alloc("v1");
		final Var v2 = Var.alloc("v2");

		final Node x1 = NodeFactory.createURI("http://example.org/x1");
		final Node y1 = NodeFactory.createURI("http://example.org/y1");

		final SolutionMapping sm = SolutionMappingUtils.createSolutionMapping(v1, x1, v2, y1);

		final LogicalOpFixedSolMap logicalOpFixedSolMap = new LogicalOpFixedSolMap(sm);
		final NullaryPhysicalOp physicalOpFixedSolMap = PhysicalOpFixedSolMap.getFactory().create(logicalOpFixedSolMap);
		final PhysicalPlan physicalFixedSolMapPlan = PhysicalPlanFactory.createPlan(physicalOpFixedSolMap, (QueryPlanningInfo) null);

		final PhysicalOpDuplicateRemoval physicalOpDedup = PhysicalOpDuplicateRemoval.getInstance();
		final PhysicalPlan physicalDedupPlan = PhysicalPlanFactory.createPlan(physicalOpDedup, physicalFixedSolMapPlan);

		final Expr e = new E_IsIRI( new ExprVar(v1) );
		final UnaryLogicalOp logicalOpFilter = new LogicalOpFilter(e, false);
		final UnaryPhysicalOp physicalOpFilter = PhysicalOpFilter.getFactory().create(logicalOpFilter);
		final PhysicalPlan physicalFilterPlan = PhysicalPlanFactory.createPlan(physicalOpFilter, physicalDedupPlan);

		// test
		final PhysicalPlan result = new DeduplicationRemoval().apply(physicalFilterPlan);

		// check
		assertTrue( result.getRootOperator() instanceof PhysicalOpFilter );

		final PhysicalPlan subPlan = result.getSubPlan(0);
		assertTrue( subPlan.getRootOperator() instanceof PhysicalOpFixedSolMap );
	}
}
