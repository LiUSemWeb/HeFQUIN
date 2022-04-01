package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.Iterator;

import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
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
	protected final ExprList filterExpressions;

	public ExecOpFilter( final ExprList filterExpressions ) {
		assert filterExpressions != null;
		assert ! filterExpressions.isEmpty();

		this.filterExpressions = filterExpressions;
	}

	public ExecOpFilter( final Expr filterExpression ) {
		assert filterExpression != null;

		this.filterExpressions = new ExprList(filterExpression);
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
			// Check whether it satisfies each of the filter expressions
			boolean satisfies = true; // assume yes
			final Iterator<Expr> it = filterExpressions.iterator();
			final Binding sm = solution.asJenaBinding();
			while ( satisfies && it.hasNext() ) {
				final Expr e = it.next();
				try {
					final NodeValue evaluationResult = ExprUtils.eval(e, sm);
					if( evaluationResult.equals(NodeValue.FALSE) ) {
						satisfies = false;
					} else if ( ! evaluationResult.equals(NodeValue.TRUE) ) {
						throw new ExecOpExecutionException("The result of the eval is neither TRUE nor FALSE!", this);
					}
				} catch ( final VariableNotBoundException ex ) {
					// If evaluating the filter expression based on the current
					// solution mapping results in this error, then this solution
					// mapping does not satisfy the filter condition.
					satisfies = false;
				}
			}

			if ( satisfies == true ) {
				sink.send(solution);
			}
		}
	}

	@Override
	protected void _concludeExecution( final IntermediateResultElementSink sink,
	                                   final ExecutionContext execCxt ) {
		// nothing to be done here
	}

}
