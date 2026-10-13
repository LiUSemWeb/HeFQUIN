package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.SortCondition;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.util.ExprUtils;
import org.junit.Test;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.CollectingIntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.iterbased.TestUtils;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContextExt;

public class ExecOpOrderByTest
{
	@Test
	public void orderByAscendingNumbers() throws ExecOpExecutionException {
		final CollectingIntermediateResultElementSink sink = new CollectingIntermediateResultElementSink();

		final Node value8 = NodeFactory.createLiteralDT("8", XSDDatatype.XSDinteger);
		final Node value9 = NodeFactory.createLiteralDT("9", XSDDatatype.XSDinteger);
		final Node value12 = NodeFactory.createLiteralDT("12", XSDDatatype.XSDinteger);
		final Var x = Var.alloc("x");

		final SolutionMapping sol8 = SolutionMappingUtils.createSolutionMapping(x, value8);
		final SolutionMapping sol9 = SolutionMappingUtils.createSolutionMapping(x, value9);
		final SolutionMapping sol12 = SolutionMappingUtils.createSolutionMapping(x, value12);

		final SortCondition sortCondition = new SortCondition(ExprUtils.parse("?x"), Query.ORDER_ASCENDING);

		final ExecOpOrderBy orderBy = new ExecOpOrderBy(List.of(sortCondition), false, false, null);

		final QueryProcContextExt ctx = TestUtils.createQueryProcContextForTests();

		orderBy.process(sol12, sink, ctx);
		orderBy.process(sol8, sink, ctx);
		orderBy.process(sol9, sink, ctx);

		orderBy.concludeExecution(sink, ctx);

		final Iterator<SolutionMapping> it = sink.getCollectedSolutionMappings().iterator();
		assertHasNext( it, 8, x );
		assertHasNext( it, 9, x );
		assertHasNext( it, 12, x );
		assertFalse( it.hasNext() );
	}

	@Test
	public void orderByDescendingNumbers() throws ExecOpExecutionException {
		final CollectingIntermediateResultElementSink sink = new CollectingIntermediateResultElementSink();

		final Node value8 = NodeFactory.createLiteralDT("8", XSDDatatype.XSDinteger);
		final Node value9 = NodeFactory.createLiteralDT("9", XSDDatatype.XSDinteger);
		final Node value12 = NodeFactory.createLiteralDT("12", XSDDatatype.XSDinteger);
		final Var x = Var.alloc("x");

		final SolutionMapping sol8 = SolutionMappingUtils.createSolutionMapping(x, value8);
		final SolutionMapping sol9 = SolutionMappingUtils.createSolutionMapping(x, value9);
		final SolutionMapping sol12 = SolutionMappingUtils.createSolutionMapping(x, value12);

		final SortCondition sortCondition = new SortCondition(ExprUtils.parse("?x"), Query.ORDER_DESCENDING);

		final ExecOpOrderBy orderBy = new ExecOpOrderBy(List.of(sortCondition), false, false, null);

		final QueryProcContextExt ctx = TestUtils.createQueryProcContextForTests();

		orderBy.process(sol8, sink, ctx);
		orderBy.process(sol12, sink, ctx);
		orderBy.process(sol9, sink, ctx);

		orderBy.concludeExecution(sink, ctx);

		final Iterator<SolutionMapping> it = sink.getCollectedSolutionMappings().iterator();
		assertHasNext( it, 12, x );
		assertHasNext( it, 9, x );
		assertHasNext( it, 8, x );
		assertFalse( it.hasNext() );
	}

	@Test
	public void orderUsingMultipleConditions() throws ExecOpExecutionException {
		final CollectingIntermediateResultElementSink sink = new CollectingIntermediateResultElementSink();

		final Var category = Var.alloc("category");
		final Var price = Var.alloc("price");

		final Node categoryA = NodeFactory.createLiteralString("A");
		final Node categoryB = NodeFactory.createLiteralString("B");

		final Node price10 = NodeFactory.createLiteralDT("10", XSDDatatype.XSDinteger);
		final Node price20 = NodeFactory.createLiteralDT("20", XSDDatatype.XSDinteger);
		final Node price30 = NodeFactory.createLiteralDT("30", XSDDatatype.XSDinteger);

		final SolutionMapping smB10 = SolutionMappingUtils.createSolutionMapping(
				category, categoryB, price, price10);
		final SolutionMapping smA20 = SolutionMappingUtils.createSolutionMapping(
				category, categoryA, price, price20);
		final SolutionMapping smB30 = SolutionMappingUtils.createSolutionMapping(
				category, categoryB, price, price30);
		final SolutionMapping smA10 = SolutionMappingUtils.createSolutionMapping(
				category, categoryA, price, price10);

		final List<SortCondition> sortConditions = List.of(
				new SortCondition(ExprUtils.parse("?category"), Query.ORDER_ASCENDING),
				new SortCondition(ExprUtils.parse("?price"), Query.ORDER_DESCENDING)
		);

		final ExecOpOrderBy orderBy = new ExecOpOrderBy(sortConditions, false, false, null);

		final QueryProcContextExt ctx = TestUtils.createQueryProcContextForTests();

		orderBy.process(smB10, sink, ctx);
		orderBy.process(smA20, sink, ctx);
		orderBy.process(smB30, sink, ctx);
		orderBy.process(smA10, sink, ctx);

		orderBy.concludeExecution(sink, ctx);

		final Iterator<SolutionMapping> it = sink.getCollectedSolutionMappings().iterator();

		assertEquals( smA20, it.next() );
		assertEquals( smA10, it.next() );
		assertEquals( smB30, it.next() );
		assertEquals( smB10, it.next() );

		assertFalse( it.hasNext() );
	}

	@Test
	public void doesNotProduceOutputBeforeAllInputIsReceived() throws ExecOpExecutionException {
		final CollectingIntermediateResultElementSink sink = new CollectingIntermediateResultElementSink();

		final Var x = Var.alloc("x");

		final Node value8 = NodeFactory.createLiteralDT("8", XSDDatatype.XSDinteger);
		final Node value12 = NodeFactory.createLiteralDT("12", XSDDatatype.XSDinteger);

		final SolutionMapping sm8 = SolutionMappingUtils.createSolutionMapping(x, value8);
		final SolutionMapping sm12 = SolutionMappingUtils.createSolutionMapping(x, value12);

		final SortCondition sortCondition = new SortCondition(ExprUtils.parse("?x"), Query.ORDER_ASCENDING);

		final ExecOpOrderBy orderBy = new ExecOpOrderBy(List.of(sortCondition), false, false, null);

		final QueryProcContextExt ctx = TestUtils.createQueryProcContextForTests();

		orderBy.process(sm12, sink, ctx);
		assertFalse( sink.getCollectedSolutionMappings().iterator().hasNext() );

		orderBy.process(sm8, sink, ctx);
		assertFalse( sink.getCollectedSolutionMappings().iterator().hasNext() );

		orderBy.concludeExecution(sink, ctx);

		final Iterator<SolutionMapping> it = sink.getCollectedSolutionMappings().iterator();

		assertHasNext( it, 8, x );
		assertHasNext( it, 12, x );
		assertFalse( it.hasNext() );
	}

	@Test
	public void preservesAllSolutionMappingsIncludingDuplicates() throws ExecOpExecutionException {
		final CollectingIntermediateResultElementSink sink = new CollectingIntermediateResultElementSink();

		final Var x = Var.alloc("x");

		final Node value8 = NodeFactory.createLiteralDT("8", XSDDatatype.XSDinteger);
		final Node value12 = NodeFactory.createLiteralDT("12", XSDDatatype.XSDinteger);

		final SolutionMapping sm8 = SolutionMappingUtils.createSolutionMapping(x, value8);
		final SolutionMapping sm12 = SolutionMappingUtils.createSolutionMapping(x, value12);

		final SortCondition sortCondition = new SortCondition(ExprUtils.parse("?x"), Query.ORDER_ASCENDING);

		final ExecOpOrderBy orderBy = new ExecOpOrderBy(List.of(sortCondition), false, false, null);

		final QueryProcContextExt ctx = TestUtils.createQueryProcContextForTests();

		orderBy.process(sm8, sink, ctx);
		orderBy.process(sm12, sink, ctx);
		orderBy.process(sm8, sink, ctx);

		orderBy.concludeExecution(sink, ctx);

		final Iterator<SolutionMapping> it = sink.getCollectedSolutionMappings().iterator();

		assertHasNext( it, 8, x );
		assertHasNext( it, 8, x );
		assertHasNext( it, 12, x );
		assertFalse( it.hasNext() );
	}



	protected void assertHasNext( final Iterator<SolutionMapping> it,
	                              final int expectedIntforV1,
	                              final Var v1 )
	{
		assertTrue( it.hasNext() );

		final Binding b = it.next().asJenaBinding();
		assertEquals( 1, b.size() );

		assertEquals( expectedIntforV1, b.get(v1).getLiteralValue() );
	}

}
