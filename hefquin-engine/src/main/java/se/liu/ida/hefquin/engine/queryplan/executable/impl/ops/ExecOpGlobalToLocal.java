package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.Set;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.VocabularyMapping;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecutableOperatorStatsImpl;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

public class ExecOpGlobalToLocal extends UnaryExecutableOpBase
{
	private long numberOfOutputMappingsProduced = 0L;

	protected final VocabularyMapping vm;

	public ExecOpGlobalToLocal( final VocabularyMapping vm, final boolean collectExceptions ) {
		super(collectExceptions);

		assert vm != null;
		this.vm = vm;
	}

	@Override
	protected void _process( final SolutionMapping inputSolMap,
	                         final IntermediateResultElementSink sink,
	                         final ExecutionContext execCxt ) {
		final Set<SolutionMapping> output = vm.translateSolutionMappingFromGlobal(inputSolMap);
		for ( final SolutionMapping sm : output ) {
			sink.send(sm);
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
