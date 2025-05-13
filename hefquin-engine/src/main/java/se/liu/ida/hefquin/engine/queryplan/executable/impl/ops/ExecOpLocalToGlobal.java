package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.VocabularyMapping;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecutableOperatorStatsImpl;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

public class ExecOpLocalToGlobal extends UnaryExecutableOpBase
{
	private long numberOfOutputMappingsProduced = 0L;

	protected final VocabularyMapping vm;

	public ExecOpLocalToGlobal( final VocabularyMapping vm, final boolean collectExceptions ) {
		super(collectExceptions);

		assert vm != null;
		this.vm = vm;
	}

	@Override
	protected void _process( final SolutionMapping inputSolMap,
	                         final IntermediateResultElementSink sink,
	                         final ExecutionContext execCxt ) {
		final Set<SolutionMapping> output = vm.translateSolutionMapping(inputSolMap);
		numberOfOutputMappingsProduced += output.size();
		sink.send(output);
	}

	@Override
	protected void _process( final List<SolutionMapping> inputSolMaps,
	                         final IntermediateResultElementSink sink,
	                         final ExecutionContext execCxt ) {
		if ( inputSolMaps.size() == 1 ) {
			// If we have only a single input solution mapping, there will
			// be only a single set of output solution mappings. In contrast
			// to the general case (below), we don't need to create a list to
			// collect the output.
			final SolutionMapping inputSolMap = inputSolMaps.get(0);
			final Set<SolutionMapping> output = vm.translateSolutionMapping(inputSolMap);
			sink.send(output);
			numberOfOutputMappingsProduced += output.size();
		}
		else if ( inputSolMaps.size() > 1 ) {
			// If we have multiple input solution mappings, create
			// a list to collect the output solution mappings.
			final List<SolutionMapping> output = new ArrayList<>();

			// Produce the output solution mappings and populate the
			// list with them.
			for ( final SolutionMapping inputSolMap : inputSolMaps ) {
				output.addAll( vm.translateSolutionMapping(inputSolMap) );
			}

			sink.send(output);
			numberOfOutputMappingsProduced += output.size();
		}
		// no else case here - nothing to do if inputSolMaps is empty
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
