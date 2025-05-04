package se.liu.ida.hefquin.engine.queryplan.executable.impl.pullbased;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutableOperator;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.engine.queryplan.executable.UnaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecPlanTask;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecPlanTaskInputException;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecPlanTaskInterruptionException;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.MaterializingIntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

public class PullBasedExecPlanTaskForUnaryOperator extends PullBasedExecPlanTaskBase
{
	protected final MaterializingIntermediateResultElementSink sink = new MaterializingIntermediateResultElementSink();

	protected final UnaryExecutableOp op;
	protected final ExecPlanTask input;

	public PullBasedExecPlanTaskForUnaryOperator( final UnaryExecutableOp op,
	                              final ExecPlanTask input,
	                              final ExecutionContext execCxt,
	                              final int minimumBlockSize ) {
		super(execCxt, minimumBlockSize);

		assert op != null;
		assert input != null;

		this.op = op;
		this.input = input;
	}

	@Override
	protected ExecutableOperator getExecOp() {
		return op;
	}

	@Override
	protected IntermediateResultBlock produceNextIntermediateResultBlock()
			throws ExecOpExecutionException, ExecPlanTaskInputException, ExecPlanTaskInterruptionException {

		// Do not try to produce more solution mappings for the current
		// output result block if we already have a sufficient number of
		// solution mappings (i.e., we already have achieved the minimum
		// block size). Additionally, we also need to stop if it has
		// become clear that there won't be any more solution mappings.

		boolean lastInputBlockConsumed = false;
		while ( sink.getSizeOfCurrentResultBlock() < preferredMinimumBlockSize && ! lastInputBlockConsumed ) {
			// consume next input result block
			final IntermediateResultBlock nextInputBlock = input.getNextIntermediateResultBlock();
			if ( nextInputBlock != null ) {
				for ( final SolutionMapping sm : nextInputBlock.getSolutionMappings() )
					op.process(sm, sink, execCxt);
			}
			else {
				op.concludeExecution(sink, execCxt);
				lastInputBlockConsumed = true;
			}
		}

		final IntermediateResultBlock nextOutputBlock = sink.getMaterializedResultBlock();
		return lastInputBlockConsumed ? new LastIntermediateResultBlock(nextOutputBlock) : nextOutputBlock;
	}

}
