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
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.GenericIntermediateResultBlockImpl;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.pullbased.TestUtils;

public class ExecOpFilterTest
{
	@Test
	public void filter_Numbers() {
		final FilterTestSink sink = new FilterTestSink();
		final GenericIntermediateResultBlockImpl resultBlock = new GenericIntermediateResultBlockImpl();
		final Expr lessThan10 = ExprUtils.parse("?x < 10");
		
		final Node value8 = NodeFactory.createLiteral("8", XSDDatatype.XSDinteger);
		final Node value12 = NodeFactory.createLiteral("8", XSDDatatype.XSDinteger);
		final Var x8 = Var.alloc("x");
		final Var x12 = Var.alloc("x");
		
		final SolutionMapping sol8 = SolutionMappingUtils.createSolutionMapping(x8, value8);
		final SolutionMapping sol12 = SolutionMappingUtils.createSolutionMapping(x12, value12);
		
		resultBlock.add(sol8);
		resultBlock.add(sol12);
		
		final ExecOpFilter filterLessThan10 = new ExecOpFilter(lessThan10);
		try {
			filterLessThan10.process(resultBlock, sink, TestUtils.createExecContextForTests());
		} catch (ExecOpExecutionException e) {
			e.printStackTrace();
		}

		final Iterator<SolutionMapping> it = resultBlock.getSolutionMappings().iterator();
		assertHasNext( it, 8, x8);
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

	protected static class FilterTestSink implements IntermediateResultElementSink
	{
		@Override
		public void send( final SolutionMapping element ) {
			// Do nothing;
		}
    }

}
