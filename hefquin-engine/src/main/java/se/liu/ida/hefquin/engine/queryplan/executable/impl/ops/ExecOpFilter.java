package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.VariableNotBoundException;
import org.apache.jena.sparql.util.ExprUtils;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecutableOperatorStatsImpl;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

public class ExecOpFilter extends UnaryExecutableOpBase
{
	private long numberOfOutputMappingsProduced = 0L;

	protected final ExprList filterExpressions;

	public ExecOpFilter( final ExprList filterExpressions, final boolean collectExceptions ) {
		super(collectExceptions);

		assert filterExpressions != null;
		assert ! filterExpressions.isEmpty();

		this.filterExpressions = filterExpressions;
	}

	public ExecOpFilter( final Expr filterExpression, final boolean collectExceptions ) {
		super(collectExceptions);

		assert filterExpression != null;

		this.filterExpressions = new ExprList(filterExpression);
	}

	@Override
	protected void _process( final SolutionMapping inputSolMap,
	                         final IntermediateResultElementSink sink,
	                         final ExecutionContext execCxt ) {
		// Check whether the given solution mapping satisfies each of the filter expressions
		final Binding sm = inputSolMap.asJenaBinding();
		for ( final Expr e : filterExpressions.getList() ) {
			final NodeValue evaluationResult;
			try {
				evaluationResult = ExprUtils.eval(e, sm);
			}
			catch ( final VariableNotBoundException ex ) {
				// If evaluating the filter expression based on the given
				// solution mapping results in this error, then this solution
				// mapping does not satisfy the filter condition.
				return;
			}

			if( evaluationResult.equals(NodeValue.FALSE) ) {
				return;
			}
			else if ( ! evaluationResult.equals(NodeValue.TRUE) ) {
				throw new IllegalArgumentException("The result of the eval is neither TRUE nor FALSE!");
			}
		}

		sink.send(inputSolMap);
		numberOfOutputMappingsProduced++;
	}

	@Override
	protected void _concludeExecution( final IntermediateResultElementSink sink,
	                                   final ExecutionContext execCxt ) {
		// nothing to be done here
	}

	@Override
	public void resetStats() {
		super.resetStats();
		numberOfOutputMappingsProduced = 0L;
	}

	@Override
	protected ExecutableOperatorStatsImpl createStats() {
		final ExecutableOperatorStatsImpl s = super.createStats();
		s.put( "numberOfOutputMappingsProduced",  Long.valueOf(numberOfOutputMappingsProduced) );
		return s;
	}

}
