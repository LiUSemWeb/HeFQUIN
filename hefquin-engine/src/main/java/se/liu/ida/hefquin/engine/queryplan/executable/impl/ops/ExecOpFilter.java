package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
		if ( checkSolutionMapping(inputSolMap) == true ) {
			sink.send(inputSolMap);
			numberOfOutputMappingsProduced++;
		}
	}

	@Override
	protected void _process( final List<SolutionMapping> inputSolMaps,
	                         final IntermediateResultElementSink sink,
	                         final ExecutionContext execCxt ) {
		// Iterate over the given list until either we find the first solution
		// mapping that satisfies all of the filter expressions of this operator
		// and, thus, can be passed on as an output solution mapping, or until
		// the end of the list.
		final Iterator<SolutionMapping> it = inputSolMaps.iterator();
		SolutionMapping firstOutput = null;
		while ( it.hasNext() && firstOutput == null ) {
			final SolutionMapping inputSolMap = it.next();
			if ( checkSolutionMapping(inputSolMap) == true ) {
				firstOutput = inputSolMap;
			}
		}

		// Continue consuming the rest of the list (if any). If we find
		// further solution mappings that can be passed on as output, we
		// collect them in an output list, with the earlier-found first
		// output solution mapping as the first list element. We create
		// the output list only in this case.
		List<SolutionMapping> allOutput = null;
		while ( it.hasNext() ) {
			final SolutionMapping inputSolMap = it.next();
			if ( checkSolutionMapping(inputSolMap) == true ) {
				// We found another output solution mapping. Check whether
				// we have already created the list; if not, create it now
				// and add earlier-found first output solution mapping to it.
				if ( allOutput == null ) {
					allOutput = new ArrayList<>();
					allOutput.add(firstOutput);
				}

				// And add the now-found output solution mapping to the list.
				allOutput.add(inputSolMap);
			}
		}

		// Send the produced output solution mappings to the given sink.
		if ( allOutput != null ) {
			sink.send(allOutput);
			numberOfOutputMappingsProduced += allOutput.size();
		}
		else if ( firstOutput != null ) {
			sink.send(firstOutput);
			numberOfOutputMappingsProduced++;
		}
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

	/**
	 * Returns true if the given solution mapping satisfies all of the filter
	 * expressions of this operator and, thus, can be passed on to the output.
	 */
	protected boolean checkSolutionMapping( final SolutionMapping inputSolMap ) {
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
				return false;
			}

			if( evaluationResult.equals(NodeValue.FALSE) ) {
				return false;
			}
			else if ( ! evaluationResult.equals(NodeValue.TRUE) ) {
				throw new IllegalArgumentException("The result of the eval is neither TRUE nor FALSE!");
			}
		}

		return true;
	}

}
