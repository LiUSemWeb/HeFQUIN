package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import static org.junit.Assert.assertEquals;
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
	public void test2_RenameTheseTestMethodsToSomethingThatDescribesTheirPurpose() {
		
	}
	
	protected void assertHasNext( final Iterator<SolutionMapping> it,
								  final int expectedIntforV1, final Var v1 )
	{
		assertTrue( it.hasNext() );
		
		final Binding b = it.next().asJenaBinding();
		assertEquals(1, b.size() );
		
		assertEquals( expectedIntforV1, b.get(v1).getLiteralValue() );
	}
}
