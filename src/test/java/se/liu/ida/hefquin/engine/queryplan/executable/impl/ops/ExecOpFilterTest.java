package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.util.ExprUtils;
import org.junit.Test;

/*
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
*/
import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.impl.SolutionMappingImpl;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.GenericIntermediateResultBlockImpl;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.pullbased.TestUtils;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.pullbased.TestUtils.SolutionMappingForTests;

public class ExecOpFilterTest
{
	@Test
	public void filter_Numbers() {
		final FilterTestSink sink = new FilterTestSink();
		final GenericIntermediateResultBlockImpl resultBlock = new GenericIntermediateResultBlockImpl();
		final Expr lessThan10 = ExprUtils.parse("?x < 10");
		
		// Lacks support for binding, produces an error when processing.
		final SolutionMappingForTests mapping8 = new SolutionMappingForTests("?x -> 8");
		final SolutionMappingForTests mapping12 = new SolutionMappingForTests("?x -> 12");
		
		// Going to be looking more into how bindings are made, bindingbuilder, bindingfactory, etc.
		/*
		final SolutionMappingImpl mapping8 = new SolutionMappingImpl("?x -> 8");
		final SolutionMappingImpl mapping12 = new SolutionMappingImpl("?x -> 12");
		*/
		
		resultBlock.add(mapping8);
		resultBlock.add(mapping12);
		
		final ExecOpFilter filterLessThan10 = new ExecOpFilter(lessThan10);
		try {
			filterLessThan10.process(resultBlock, sink, TestUtils.createExecContextForTests());
		} catch (ExecOpExecutionException e) {
			e.printStackTrace();
		}
		/*
		assertThat(resultBlock.getSolutionMappings(), contains (
				hasProperty("string", is("?x -> 8"))
				));
			*/	
	}

	@Test
	public void test2_RenameTheseTestMethodsToSomethingThatDescribesTheirPurpose() {
		
	}

	protected static class FilterTestSink implements IntermediateResultElementSink
	{
		@Override
		public void send( final SolutionMapping element ) {
			// Do nothing;
		}
    }

}
