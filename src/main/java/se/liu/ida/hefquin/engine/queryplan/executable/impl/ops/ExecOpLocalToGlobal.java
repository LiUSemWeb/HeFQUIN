package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.VocabularyMapping;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

public class ExecOpLocalToGlobal extends UnaryExecutableOpBase{

	protected final VocabularyMapping vocabularyMapping;
	
	public ExecOpLocalToGlobal( final VocabularyMapping mapping ) {
		assert mapping != null;
		this.vocabularyMapping = mapping;
	}
	
	@Override
	public int preferredInputBlockSize() {
		return 1;
	}

	@Override
	protected void _process(IntermediateResultBlock input, IntermediateResultElementSink sink, ExecutionContext execCxt)
			throws ExecOpExecutionException {
		for (final SolutionMapping solution : input.getSolutionMappings() ) {
			for (final SolutionMapping sm : vocabularyMapping.translateSolutionMapping( solution )) {
				sink.send(sm);
			}
		}	
	}

	@Override
	protected void _concludeExecution(IntermediateResultElementSink sink, ExecutionContext execCxt)
			throws ExecOpExecutionException {
		// TODO Auto-generated method stub
		
	}
	
}
