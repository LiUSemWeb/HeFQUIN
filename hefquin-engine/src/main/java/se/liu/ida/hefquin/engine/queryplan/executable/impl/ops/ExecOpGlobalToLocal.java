package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.VocabularyMapping;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecutableOperatorStatsImpl;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

public class ExecOpGlobalToLocal extends UnaryExecutableOpBaseWithoutBlocking
{
	private long numberOfOutputMappingsProduced = 0L;

	protected final VocabularyMapping vm;

	public ExecOpGlobalToLocal( final VocabularyMapping vm,
	                            final boolean collectExceptions,
	                            final QueryPlanningInfo qpInfo ) {
		super(collectExceptions, qpInfo);

		assert vm != null;
		this.vm = vm;
	}

	@Override
	protected void _process( final SolutionMapping inputSolMap,
	                         final IntermediateResultElementSink sink,
	                         final ExecutionContext execCxt ) {
		final Set<SolutionMapping> output = vm.translateSolutionMappingFromGlobal(inputSolMap);
		numberOfOutputMappingsProduced += output.size();
		sink.send(output);
	}

	@Override
	protected void _process( final Iterator<SolutionMapping> inputSolMaps,
	                         final int maxBatchSize,
	                         final IntermediateResultElementSink sink,
	                         final ExecutionContext execCxt ) {
		final List<SolutionMapping> output = new ArrayList<>();

		// Produce the output solution mappings
		// and populate the list with them.
		int cnt = 0;
		while ( cnt < maxBatchSize && inputSolMaps.hasNext() ) {
			cnt++;
			final SolutionMapping inputSolMap = inputSolMaps.next();
			output.addAll( vm.translateSolutionMappingFromGlobal(inputSolMap) );
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
