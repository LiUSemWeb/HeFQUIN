package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecutableOperatorStatsImpl;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

/**
 * To be used for PROJECT clauses. This operator restricts each input solution
 * mapping to a given set of variables by removing bindings for all other variables.
 * For every input solution mapping, exactly one output solution mapping is produced.
 */
public class ExecOpProject extends UnaryExecutableOpBaseWithoutBlocking
{
	private long numberOfOutputMappingsProduced = 0L;

	protected final Set<Var> variables;

	public ExecOpProject( final Set<Var> variables,
	                      final boolean collectExceptions,
	                      final QueryPlanningInfo qpInfo ) {
		super(collectExceptions, qpInfo);

		assert variables != null;
		assert ! variables.isEmpty();

		this.variables = variables;
	}

	@Override
	protected void _process( final SolutionMapping inputSolMap,
	                         final IntermediateResultElementSink sink,
	                         final ExecutionContext execCxt )
			throws ExecOpExecutionException {
		final SolutionMapping outputSolMap = SolutionMappingUtils.restrict(inputSolMap, variables);
		sink.send(outputSolMap);
		numberOfOutputMappingsProduced++;
	}

	@Override
	protected void _process( final Iterator<SolutionMapping> inputSolMaps,
	                         final int maxBatchSize,
	                         final IntermediateResultElementSink sink,
	                         final ExecutionContext execCxt )
			throws ExecOpExecutionException {
		final List<SolutionMapping> output = new ArrayList<>();

		// Produce the output solution mappings
		// and populate the list with them.
		int cnt = 0;
		while ( cnt < maxBatchSize && inputSolMaps.hasNext() ) {
			cnt++;
			final SolutionMapping inputSolMap = inputSolMaps.next();
			final SolutionMapping outputSolMap = SolutionMappingUtils.restrict(inputSolMap, variables);
			output.add(outputSolMap);
		}

		numberOfOutputMappingsProduced += output.size();
		sink.send(output);
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