package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.util.ExprUtils;
import org.junit.Test;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.iterbased.TestUtils;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.CollectingIntermediateResultElementSink;

public class ExecOpFilterTest
{
	@Test
	public void filter_Numbers() throws ExecOpExecutionException {
		final CollectingIntermediateResultElementSink sink = new CollectingIntermediateResultElementSink();
		final Expr lessThan10 = ExprUtils.parse("?x < 10");
		
		final Node value8 = NodeFactory.createLiteral("8", XSDDatatype.XSDinteger);
		final Node value9 = NodeFactory.createLiteral("9", XSDDatatype.XSDinteger);
		final Node value12 = NodeFactory.createLiteral("12", XSDDatatype.XSDinteger);
		final Var x = Var.alloc("x");
		
		final SolutionMapping sol8 = SolutionMappingUtils.createSolutionMapping(x, value8);
		final SolutionMapping sol9 = SolutionMappingUtils.createSolutionMapping(x, value9);
		final SolutionMapping sol12 = SolutionMappingUtils.createSolutionMapping(x, value12);

		final ExecOpFilter filterLessThan10 = new ExecOpFilter(lessThan10, false);
		final ExecutionContext ctx = TestUtils.createExecContextForTests();
		filterLessThan10.process(sol8, sink, ctx);
		filterLessThan10.process(sol12, sink, ctx);  // 12 is processed before 9. This should not pass the filter. 9 should be after 8.
		filterLessThan10.process(sol9, sink, ctx);

		final Iterator<SolutionMapping> it = sink.getCollectedSolutionMappings().iterator();
		assertHasNext( it, 8, x);
		assertHasNext( it, 9, x); // See that 9 made it and 12 did not, as 9 < 10 is true whereas 12 < 10 is false.
	}

	@Test
	public void filter_Unbound() throws ExecOpExecutionException {
		final CollectingIntermediateResultElementSink sink = new CollectingIntermediateResultElementSink();
		final Expr lessThan10 = ExprUtils.parse("?x < 10");
		
		final Node value8 = NodeFactory.createLiteral("8", XSDDatatype.XSDinteger);
		final Node value9 = NodeFactory.createLiteral("9", XSDDatatype.XSDinteger);
		final Var x = Var.alloc("x");
		final Var y = Var.alloc("y");
		
		final SolutionMapping sol8 = SolutionMappingUtils.createSolutionMapping(x, value8);
		final SolutionMapping sol9 = SolutionMappingUtils.createSolutionMapping(y, value9);

		final ExecOpFilter filterLessThan10 = new ExecOpFilter(lessThan10, false);
		final ExecutionContext ctx = TestUtils.createExecContextForTests();
		filterLessThan10.process(sol8, sink, ctx);
		filterLessThan10.process(sol9, sink, ctx);

		final Iterator<SolutionMapping> it = sink.getCollectedSolutionMappings().iterator();
		assertHasNext( it, 8, x);
		assertFalse( it.hasNext() ); // Despite 9 being less than 10, there shouldn't be anything more because there is no x, only y.
	}

	@Test
	public void filter_Dates() throws ExecOpExecutionException {
		final CollectingIntermediateResultElementSink sink = new CollectingIntermediateResultElementSink();
		final Expr after2019 = ExprUtils.parse("?x > \"2019-12-31\"^^xsd:date");

		final Node date2020 = NodeFactory.createLiteral("2020-10-20", XSDDatatype.XSDdate);
		final Node date2019 = NodeFactory.createLiteral("2019-10-20", XSDDatatype.XSDdate);
		final Node date2021 = NodeFactory.createLiteral("2021-02-01", XSDDatatype.XSDdate);
		final Node dateNewYearsEve = NodeFactory.createLiteral("2019-12-31", XSDDatatype.XSDdate);
		final Node dateNewYearsDay = NodeFactory.createLiteral("2020-01-01", XSDDatatype.XSDdate);
		final Var x = Var.alloc("x");

		final SolutionMapping sol2020 = SolutionMappingUtils.createSolutionMapping(x, date2020);
		final SolutionMapping sol2019 = SolutionMappingUtils.createSolutionMapping(x, date2019);
		final SolutionMapping sol2021 = SolutionMappingUtils.createSolutionMapping(x, date2021);
		final SolutionMapping solNYE = SolutionMappingUtils.createSolutionMapping(x, dateNewYearsEve);
		final SolutionMapping solNYD = SolutionMappingUtils.createSolutionMapping(x, dateNewYearsDay);

		final ExecOpFilter filterAfter2019 = new ExecOpFilter(after2019, false);
		final ExecutionContext ctx = TestUtils.createExecContextForTests();
		filterAfter2019.process(sol2020, sink, ctx);
		filterAfter2019.process(sol2019, sink, ctx);
		filterAfter2019.process(sol2021, sink, ctx);
		filterAfter2019.process(solNYE, sink, ctx);
		filterAfter2019.process(solNYD, sink, ctx);

		final Iterator<SolutionMapping> it = sink.getCollectedSolutionMappings().iterator();
		assertHasNext( it, "2020-10-20", x);
		assertHasNext( it, "2021-02-01", x);
		assertHasNext( it, "2020-01-01", x);
	}

	@Test
	public void twoFilterExpressions_fourSeparateSolMaps() throws ExecOpExecutionException {
		final ExprList exprs = new ExprList();
		exprs.add( ExprUtils.parse("?x < 13") );
		exprs.add( ExprUtils.parse("?x > 8") );

		final Node value8  = NodeFactory.createLiteral("8",  XSDDatatype.XSDinteger);
		final Node value9  = NodeFactory.createLiteral("9",  XSDDatatype.XSDinteger);
		final Node value12 = NodeFactory.createLiteral("12", XSDDatatype.XSDinteger);
		final Node value15 = NodeFactory.createLiteral("15", XSDDatatype.XSDinteger);
		final Var x = Var.alloc("x");

		final SolutionMapping sm1 = SolutionMappingUtils.createSolutionMapping(x, value8);
		final SolutionMapping sm2 = SolutionMappingUtils.createSolutionMapping(x, value9);
		final SolutionMapping sm3 = SolutionMappingUtils.createSolutionMapping(x, value12);
		final SolutionMapping sm4 = SolutionMappingUtils.createSolutionMapping(x, value15);

		final ExecOpFilter filterOp = new ExecOpFilter(exprs, false);

		final CollectingIntermediateResultElementSink sink = new CollectingIntermediateResultElementSink();		
		final ExecutionContext ctx = TestUtils.createExecContextForTests();

		filterOp.process(sm1, sink, ctx);
		filterOp.process(sm2, sink, ctx);
		filterOp.process(sm3, sink, ctx);
		filterOp.process(sm4, sink, ctx);

		final Iterator<SolutionMapping> it = sink.getCollectedSolutionMappings().iterator();

		assertTrue( it.hasNext() );
		assertEquals( value9, it.next().asJenaBinding().get(x) );

		assertTrue( it.hasNext() );
		assertEquals( value12, it.next().asJenaBinding().get(x) );

		assertFalse( it.hasNext() );
	}

	@Test
	public void twoFilterExpressions_twoSetsOfSolMaps() throws ExecOpExecutionException {
		// The input solution mappings in this test are the same as in
		// 'twoFilterExpressions_fourSeparateSolMaps' but now they are
		// send to the filter operator as two lists, each list with two
		// of the solution mappings.
		final ExprList exprs = new ExprList();
		exprs.add( ExprUtils.parse("?x < 13") );
		exprs.add( ExprUtils.parse("?x > 8") );

		final Node value8  = NodeFactory.createLiteral("8",  XSDDatatype.XSDinteger);
		final Node value9  = NodeFactory.createLiteral("9",  XSDDatatype.XSDinteger);
		final Node value12 = NodeFactory.createLiteral("12", XSDDatatype.XSDinteger);
		final Node value15 = NodeFactory.createLiteral("15", XSDDatatype.XSDinteger);
		final Var x = Var.alloc("x");

		final List<SolutionMapping> input1 = new ArrayList<>(2);
		input1.add( SolutionMappingUtils.createSolutionMapping(x, value8) );
		input1.add( SolutionMappingUtils.createSolutionMapping(x, value9) );
		final List<SolutionMapping> input2 = new ArrayList<>(2);
		input2.add( SolutionMappingUtils.createSolutionMapping(x, value12) );
		input2.add( SolutionMappingUtils.createSolutionMapping(x, value15) );

		final ExecOpFilter filterOp = new ExecOpFilter(exprs, false);

		final CollectingIntermediateResultElementSink sink = new CollectingIntermediateResultElementSink();		
		final ExecutionContext ctx = TestUtils.createExecContextForTests();

		filterOp.process(input1, sink, ctx);
		filterOp.process(input2, sink, ctx);

		final Iterator<SolutionMapping> it = sink.getCollectedSolutionMappings().iterator();

		assertTrue( it.hasNext() );
		assertEquals( value9, it.next().asJenaBinding().get(x) );

		assertTrue( it.hasNext() );
		assertEquals( value12, it.next().asJenaBinding().get(x) );

		assertFalse( it.hasNext() );
	}

	@Test
	public void twoFilterExpressions_oneSetOfSolMaps() throws ExecOpExecutionException {
		// The input solution mappings in this test are the same as in
		// 'twoFilterExpressions_fourSeparateSolMaps' but now they are
		// send to the filter operator as two lists, each list with two
		// of the solution mappings.
		final ExprList exprs = new ExprList();
		exprs.add( ExprUtils.parse("?x < 13") );
		exprs.add( ExprUtils.parse("?x > 8") );

		final Node value8  = NodeFactory.createLiteral("8",  XSDDatatype.XSDinteger);
		final Node value9  = NodeFactory.createLiteral("9",  XSDDatatype.XSDinteger);
		final Node value12 = NodeFactory.createLiteral("12", XSDDatatype.XSDinteger);
		final Node value15 = NodeFactory.createLiteral("15", XSDDatatype.XSDinteger);
		final Var x = Var.alloc("x");

		final List<SolutionMapping> input = new ArrayList<>(2);
		input.add( SolutionMappingUtils.createSolutionMapping(x, value8) );
		input.add( SolutionMappingUtils.createSolutionMapping(x, value9) );
		input.add( SolutionMappingUtils.createSolutionMapping(x, value12) );
		input.add( SolutionMappingUtils.createSolutionMapping(x, value15) );

		final ExecOpFilter filterOp = new ExecOpFilter(exprs, false);

		final CollectingIntermediateResultElementSink sink = new CollectingIntermediateResultElementSink();		
		final ExecutionContext ctx = TestUtils.createExecContextForTests();

		filterOp.process(input, sink, ctx);

		final Iterator<SolutionMapping> it = sink.getCollectedSolutionMappings().iterator();

		assertTrue( it.hasNext() );
		assertEquals( value9, it.next().asJenaBinding().get(x) );

		assertTrue( it.hasNext() );
		assertEquals( value12, it.next().asJenaBinding().get(x) );

		assertFalse( it.hasNext() );
	}



	protected void assertHasNext( final Iterator<SolutionMapping> it,
	                              final int expectedIntforV1,
	                              final Var v1 )
	{
		assertTrue( it.hasNext() );
		
		final Binding b = it.next().asJenaBinding();
		assertEquals(1, b.size() );
		
		assertEquals( expectedIntforV1, b.get(v1).getLiteralValue() );
	}

	protected void assertHasNext( final Iterator<SolutionMapping> it,
	                              final String expectedStrforV1,
	                              final Var v1 )
	{
		assertTrue( it.hasNext() );
		
		final Binding b = it.next().asJenaBinding();
		assertEquals(1, b.size() );
		
		assertEquals( expectedStrforV1, b.get(v1).getLiteralLexicalForm() );
	}

}
