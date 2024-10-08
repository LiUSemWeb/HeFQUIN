package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecutableOperatorStatsImpl;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

public class ExecOpBinaryUnion extends BinaryExecutableOpBase
{
	private long numberOfOutputMappingsProduced = 0L;
	
	public ExecOpBinaryUnion( final boolean collectExceptions ) {
		super(collectExceptions);
	}

	@Override
	public int preferredInputBlockSizeFromChild1() {
		// Since this algorithm processes the input solution mappings
		// sequentially (one at a time), an input block size of 1 may
		// reduce the response time of the overall execution process.
		return 1;
	}

	@Override
	public int preferredInputBlockSizeFromChild2() {
		// same rationale here
		return 1;
	}

	@Override
	public boolean requiresCompleteChild1InputFirst() {
		return false;
	}

	@Override
	protected void _processBlockFromChild1( final IntermediateResultBlock input,
	                                        final IntermediateResultElementSink sink,
	                                        final ExecutionContext execCxt ) {
		for ( final SolutionMapping sm : input.getSolutionMappings() ) {
			numberOfOutputMappingsProduced++;
			sink.send(sm);
		}
	}

	@Override
	protected void _wrapUpForChild1( final IntermediateResultElementSink sink,
	                                 final ExecutionContext execCxt ) {
		// nothing to be done here
	}

	@Override
	protected void _processBlockFromChild2( final IntermediateResultBlock input,
	                                        final IntermediateResultElementSink sink,
	                                        final ExecutionContext execCxt ) {
		for ( final SolutionMapping sm : input.getSolutionMappings() ) {
			numberOfOutputMappingsProduced++;
			sink.send(sm);
		}
	}

	@Override
	protected void _wrapUpForChild2( final IntermediateResultElementSink sink,
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
