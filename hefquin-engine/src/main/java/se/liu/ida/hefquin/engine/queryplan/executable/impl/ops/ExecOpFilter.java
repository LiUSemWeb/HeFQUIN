package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecutableOperatorStatsImpl;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

public class ExecOpFilter extends UnaryExecutableOpBaseWithoutBlocking
{
	private long numberOfOutputMappingsProduced = 0L;

	protected final ExprList filterExpressions;

	public ExecOpFilter( final ExprList filterExpressions,
	                     final boolean mayReduce,
	                     final boolean collectExceptions,
	                     final QueryPlanningInfo qpInfo ) {
		super(mayReduce, collectExceptions, qpInfo);

		assert filterExpressions != null;
		assert ! filterExpressions.isEmpty();

		this.filterExpressions = filterExpressions;
	}

	public ExecOpFilter( final Expr filterExpression,
	                     final boolean mayReduce,
	                     final boolean collectExceptions,
	                     final QueryPlanningInfo qpInfo ) {
		super(mayReduce, collectExceptions, qpInfo);

		assert filterExpression != null;

		this.filterExpressions = new ExprList(filterExpression);
	}

	@Override
	protected void _process( final SolutionMapping inputSolMap,
	                         final IntermediateResultElementSink sink,
	                         final ExecutionContext execCxt ) {
		// Check whether the given solution mapping satisfies each of the filter expressions
		if ( SolutionMappingUtils.checkSolutionMapping(inputSolMap, filterExpressions) == true ) {
			sink.send(inputSolMap);
			numberOfOutputMappingsProduced++;
		}
	}

	@Override
	protected void _process( final Iterator<SolutionMapping> inputSolMaps,
	                         final int maxBatchSize,
	                         final IntermediateResultElementSink sink,
	                         final ExecutionContext execCxt ) {
		// Iterate over the given input until either we find the first solution
		// mapping that satisfies all of the filter expressions of this operator
		// and, thus, can be passed on as an output solution mapping, or until
		// we have reached either the maximum batch size or the end of the
		// iterator.
		int cnt = 0;
		SolutionMapping firstOutput = null;
		while ( firstOutput == null && cnt < maxBatchSize && inputSolMaps.hasNext() ) {
			final SolutionMapping inputSolMap = inputSolMaps.next();
			cnt++;
			if ( SolutionMappingUtils.checkSolutionMapping(inputSolMap, filterExpressions) == true ) {
				firstOutput = inputSolMap;
			}
		}

		// Continue consuming the rest of the batch (if any). If we find
		// further solution mappings that can be passed on as output, we
		// collect them in an output list, with the earlier-found first
		// output solution mapping as the first list element. We create
		// the output list only in this case.
		List<SolutionMapping> allOutput = null;
		while ( cnt < maxBatchSize && inputSolMaps.hasNext() ) {
			final SolutionMapping inputSolMap = inputSolMaps.next();
			cnt++;
			if ( SolutionMappingUtils.checkSolutionMapping(inputSolMap, filterExpressions) == true ) {
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

}
