package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.util.ExprUtils;
import org.junit.Test;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.GenericIntermediateResultBlockImpl;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.MaterializingIntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.pullbased.TestUtils;

public class ExecOpFilterTest
{
	@Test
	public void filter_Numbers() {
		final MaterializingIntermediateResultElementSink sink = new MaterializingIntermediateResultElementSink();
		final GenericIntermediateResultBlockImpl resultBlock = new GenericIntermediateResultBlockImpl();
		final Expr lessThan10 = ExprUtils.parse("?x < 10");
		
		final Node value8 = NodeFactory.createLiteral("8", XSDDatatype.XSDinteger);
		final Node value9 = NodeFactory.createLiteral("9", XSDDatatype.XSDinteger);
		final Node value12 = NodeFactory.createLiteral("12", XSDDatatype.XSDinteger);
		final Var x = Var.alloc("x");
		
		final SolutionMapping sol8 = SolutionMappingUtils.createSolutionMapping(x, value8);
		final SolutionMapping sol9 = SolutionMappingUtils.createSolutionMapping(x, value9);
		final SolutionMapping sol12 = SolutionMappingUtils.createSolutionMapping(x, value12);
		
		resultBlock.add(sol8);
		resultBlock.add(sol12); // 12 is added before 9. This should not pass the filter. 9 should be after 8.
		resultBlock.add(sol9);
		
		final ExecOpFilter filterLessThan10 = new ExecOpFilter(lessThan10);
		try {
			filterLessThan10.process(resultBlock, sink, TestUtils.createExecContextForTests());
		} catch (ExecOpExecutionException e) {
			e.printStackTrace();
		}

		final Iterator<SolutionMapping> it = sink.getMaterializedIntermediateResult().iterator();
		assertHasNext( it, 8, x);
		assertHasNext( it, 9, x); // See that 9 made it and 12 did not, as 9 < 10 is true whereas 12 < 10 is false.
	}
	@Test
	public void filter_Unbound() {
		final MaterializingIntermediateResultElementSink sink = new MaterializingIntermediateResultElementSink();
		final GenericIntermediateResultBlockImpl resultBlock = new GenericIntermediateResultBlockImpl();
		final Expr lessThan10 = ExprUtils.parse("?x < 10");
		
		final Node value8 = NodeFactory.createLiteral("8", XSDDatatype.XSDinteger);
		final Node value9 = NodeFactory.createLiteral("9", XSDDatatype.XSDinteger);
		final Var x = Var.alloc("x");
		final Var y = Var.alloc("y");
		
		final SolutionMapping sol8 = SolutionMappingUtils.createSolutionMapping(x, value8);
		final SolutionMapping sol9 = SolutionMappingUtils.createSolutionMapping(y, value9);
		
		resultBlock.add(sol8);
		resultBlock.add(sol9);
		
		final ExecOpFilter filterLessThan10 = new ExecOpFilter(lessThan10);
		try {
			filterLessThan10.process(resultBlock, sink, TestUtils.createExecContextForTests());
		} catch (ExecOpExecutionException e) {
			e.printStackTrace();
		}

		final Iterator<SolutionMapping> it = sink.getMaterializedIntermediateResult().iterator();
		assertHasNext( it, 8, x);
		assertFalse( it.hasNext() ); // Despite 9 being less than 10, there shouldn't be anything more because there is no x, only y.
	}

	@Test
	public void filter_Dates() {
		final MaterializingIntermediateResultElementSink sink = new MaterializingIntermediateResultElementSink();
		final GenericIntermediateResultBlockImpl resultBlock = new GenericIntermediateResultBlockImpl();
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

		resultBlock.add(sol2020);
		resultBlock.add(sol2019);
		resultBlock.add(sol2021);
		resultBlock.add(solNYE);
		resultBlock.add(solNYD);

		final ExecOpFilter filterAfter2019= new ExecOpFilter(after2019);
		try {
			filterAfter2019.process(resultBlock, sink, TestUtils.createExecContextForTests());
		} catch (ExecOpExecutionException e) {
			e.printStackTrace();
		}

		final Iterator<SolutionMapping> it = sink.getMaterializedIntermediateResult().iterator();
		assertHasNext( it, "2020-10-20", x);
		assertHasNext( it, "2021-02-01", x);
		assertHasNext( it, "2020-01-01", x);
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
