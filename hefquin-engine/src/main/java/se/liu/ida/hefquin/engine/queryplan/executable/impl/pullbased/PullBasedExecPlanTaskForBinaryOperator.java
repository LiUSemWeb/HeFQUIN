package se.liu.ida.hefquin.engine.queryplan.executable.impl.pullbased;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.engine.queryplan.executable.BinaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutableOperator;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecPlanTask;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecPlanTaskInputException;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecPlanTaskInterruptionException;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.MaterializingIntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

public class PullBasedExecPlanTaskForBinaryOperator extends PullBasedExecPlanTaskBase
{
	protected final MaterializingIntermediateResultElementSink sink = new MaterializingIntermediateResultElementSink();

	protected final BinaryExecutableOp op;
	protected final ExecPlanTask input1;
	protected final ExecPlanTask input2;

	protected boolean input1Consumed = false;

	public PullBasedExecPlanTaskForBinaryOperator( final BinaryExecutableOp op,
	                               final ExecPlanTask input1,
	                               final ExecPlanTask input2,
	                               final ExecutionContext execCxt ) {
		super(execCxt);

		assert op != null;
		assert input1 != null;
		assert input2 != null;

		this.op = op;
		this.input1 = input1;
		this.input2 = input2;
	}

	@Override
	protected ExecutableOperator getExecOp() {
		return op;
	}

	@Override
	protected IntermediateResultBlock produceNextIntermediateResultBlock()
			throws ExecOpExecutionException, ExecPlanTaskInputException, ExecPlanTaskInterruptionException {

		// Attention: the current implementation of this method ignores
		// whether op.requiresCompleteChild1InputFirst() is true or
		// false but, instead, simply consumes and pushes the complete
		// child 1 input first (as would be required in case that the
		// aforementioned function returns true).

		// Do not try to produce more solution mappings for the current
		// output result block if we already have a sufficient number of
		// solution mappings (i.e., we already have achieved the minimum
		// block size). Additionally, we also need to stop if it has
		// become clear that there won't be any more solution mappings.

		boolean lastInputBlockConsumed = false;
		while ( sink.getSizeOfCurrentResultBlock() < outputBlockSize && ! lastInputBlockConsumed ) {
			// consume next input result block from input 1 (if still needed)
			if ( ! input1Consumed ) {
				final IntermediateResultBlock nextInputBlock = input1.getNextIntermediateResultBlock();
				if ( nextInputBlock != null ) {
					for ( final SolutionMapping sm : nextInputBlock.getSolutionMappings() ) {
						op.processInputFromChild1(sm, sink, execCxt);
					}
				}
				else {
					op.wrapUpForChild1(sink, execCxt);
					input1Consumed = true;
				}
			}
			else {
				final IntermediateResultBlock nextInputBlock = input2.getNextIntermediateResultBlock();
				if ( nextInputBlock != null ) {
					for ( final SolutionMapping sm : nextInputBlock.getSolutionMappings() ) {
						op.processInputFromChild2(sm, sink, execCxt);
					}
				}
				else {
					op.wrapUpForChild2(sink, execCxt);
					lastInputBlockConsumed = true;
				}
			}
		}

		final IntermediateResultBlock nextOutputBlock = sink.getMaterializedResultBlock();
		return lastInputBlockConsumed ? new LastIntermediateResultBlock(nextOutputBlock) : nextOutputBlock;
	}

}
