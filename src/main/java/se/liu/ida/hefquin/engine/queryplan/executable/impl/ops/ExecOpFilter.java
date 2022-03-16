package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.VariableNotBoundException;
import org.apache.jena.sparql.util.ExprUtils;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

public class ExecOpFilter extends UnaryExecutableOpBase
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
	protected void _process( final IntermediateResultBlock input,
	                         final IntermediateResultElementSink sink,
	                         final ExecutionContext execCxt ) throws ExecOpExecutionException {

		// For every solution mapping in the input...
		for( final SolutionMapping solution : input.getSolutionMappings() ) {
			//Check whether it satisfies the filter expression
			try {
				final NodeValue evaluationResult = ExprUtils.eval(filterExpression, solution.asJenaBinding());
				if( evaluationResult.equals(NodeValue.TRUE) ) {
					sink.send(solution);
				} else if ( ! evaluationResult.equals(NodeValue.FALSE) ) {
					throw new ExecOpExecutionException("The result of the eval is neither TRUE nor FALSE!", null);
				}
			} catch ( final VariableNotBoundException e ) {
				// The current solution mapping does not satisfy the filter condition (because
				// evaluating the filter expression based on this solution mapping resulted in
				// this error). Therefore this solution mapping is not in the output of this operator
				// and, thus, must not be sent to the sink. Hence, we do not have to do anything
				// here in this catch block.
			}
		}
	}

	@Override
	protected void _concludeExecution( final IntermediateResultElementSink sink,
	                                   final ExecutionContext execCxt ) {
		// nothing to be done here
	}

}
