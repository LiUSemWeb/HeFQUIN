package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

public class ExecOpBinaryUnion implements BinaryExecutableOp{

	@Override
	public int preferredInputBlockSize() {
		// Since this algorithm processes the input solution mappings
		// sequentially (one at a time), an input block size of 1 may
		// reduce the response time of the overall execution process.
		return 1;
	}

	@Override
	public boolean requiresCompleteChild1InputFirst() {
		return false;
	}

	@Override
	public void processBlockFromChild1(final IntermediateResultBlock input, final IntermediateResultElementSink sink,
			final ExecutionContext execCxt) {
		for (final SolutionMapping sm : input.getSolutionMappings())
			sink.send(sm);
	}

	@Override
	public void wrapUpForChild1(IntermediateResultElementSink sink, ExecutionContext execCxt) {
		// nothing to be done here
	}

	@Override
	public void processBlockFromChild2(final IntermediateResultBlock input, final IntermediateResultElementSink sink,
			final ExecutionContext execCxt) {
		for (final SolutionMapping sm : input.getSolutionMappings())
			sink.send(sm);
	}

	@Override
	public void wrapUpForChild2(IntermediateResultElementSink sink, ExecutionContext execCxt) {
		// nothing to be done here
	}

}
