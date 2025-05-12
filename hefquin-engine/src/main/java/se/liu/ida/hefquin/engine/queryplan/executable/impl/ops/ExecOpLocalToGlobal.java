package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.ArrayList;
import java.util.Iterator;
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
	protected int _process( final Iterator<SolutionMapping> it,
	                        final IntermediateResultElementSink sink,
	                        final ExecutionContext execCxt ) {
		if ( ! it.hasNext() ) {
			return 0;
		}

		final SolutionMapping firstInputSolMap = it.next();
		final Set<SolutionMapping> output4first = vm.translateSolutionMapping(firstInputSolMap);
		if ( ! it.hasNext() ) {
			sink.send(output4first);
			numberOfOutputMappingsProduced += output4first.size();
			return 1;
		}

		final List<SolutionMapping> allOutput = new ArrayList<>();
		allOutput.addAll(output4first);
		int cnt = 1;
		while ( it.hasNext() ) {
			cnt++;
			final SolutionMapping nextInputSolMap = it.next();
			allOutput.addAll( vm.translateSolutionMapping(nextInputSolMap) );
		}

		sink.send(allOutput);
		numberOfOutputMappingsProduced += allOutput.size();
		return cnt;
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
