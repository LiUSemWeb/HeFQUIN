package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.util.ExprUtils;

import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

public class ExecOpFilter implements UnaryExecutableOp
{
	protected final Expr filterExpression;

	public ExecOpFilter( final Expr filterExpression ) {
		assert filterExpression != null;
		this.filterExpression = filterExpression;
	}

	@Override
	public int preferredInputBlockSize() {
		return 1;
	}

	@Override
	public void process( final IntermediateResultBlock input,
	                     final IntermediateResultElementSink sink,
	                     final ExecutionContext execCxt ) throws ExecOpExecutionException {
		// TODO Auto-generated method stub

		// The idea of what you should implement here is as follows: For every
		// solution mapping in the given input, input.getSolutionMappings(), check
		// whether it satisfies the filter expression (filterExpression); if it
		// does, push it to the output sink, sink.send( ... ), else simply move
		// on to the next input solution mapping.
		// To do the check, call ExprUtils.eval(filterExpression, b), where b is the
		// Binding object that you can get from the SolutionMapping. I assume (please
		// verify) that eval function may return either NodeValue.TRUE or NodeValue.FALSE,
		// where NodeValue.TRUE would mean that the solution mapping can be pushed to the
		// output sink.
		// Another useful function is ExprUtils.parse(String) which parses a string with
		// a filter expression as written directly in SPARQL into an Expr object. This
		// will come handy when writing unit tests for this class here.
	}

	@Override
	public void concludeExecution( final IntermediateResultElementSink sink,
	                               final ExecutionContext execCxt ) {
		// nothing to be done here
	}

}
