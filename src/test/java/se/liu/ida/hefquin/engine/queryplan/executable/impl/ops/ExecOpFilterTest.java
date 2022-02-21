package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.util.ExprUtils;
import org.junit.Test;

/*
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
*/
import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.impl.SolutionMappingImpl;
import se.liu.ida.hefquin.engine.data.utils.SolutionMappingUtils;
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
		/*
		final SolutionMappingForTests mapping8 = new SolutionMappingForTests("?x -> 8");
		final SolutionMappingForTests mapping12 = new SolutionMappingForTests("?x -> 12");
		*/
		
		// Going to be looking more into how bindings are made, bindingbuilder, bindingfactory, etc.
		/*
		final SolutionMappingImpl mapping8 = new SolutionMappingImpl("?x -> 8");
		final SolutionMappingImpl mapping12 = new SolutionMappingImpl("?x -> 12");
		*/

		final Var var1 = Var.alloc("v1");
		final Var var2 = Var.alloc("v2");
		final Var var3 = Var.alloc("v3");
		final Node uri1 = NodeFactory.createURI("http://example.org/uri1");
		final Node uri2 = NodeFactory.createURI("http://example.org/uri2");
		final Node uri3 = NodeFactory.createURI("http://example.org/uri3");
		
		final SolutionMapping sol1 = SolutionMappingUtils.createSolutionMapping(var1, uri1, var2, uri2);
		final SolutionMapping sol2 = SolutionMappingUtils.createSolutionMapping(var3, uri3);
		
		//resultBlock.add(mapping8);
		//resultBlock.add(mapping12);
		
		resultBlock.add(sol1);
		resultBlock.add(sol2);
		
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
