package se.liu.ida.hefquin.engine.queryplan.executable.impl.pushbased;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.engine.queryplan.executable.BinaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutableOperator;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecPlanTask;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecPlanTaskInputException;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecPlanTaskInterruptionException;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

public class PushBasedExecPlanTaskForBinaryOperator extends PushBasedExecPlanTaskBase
{
	protected final BinaryExecutableOp op;
	protected final ExecPlanTask input1;
	protected final ExecPlanTask input2;

	public PushBasedExecPlanTaskForBinaryOperator( final BinaryExecutableOp op,
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
	protected void produceOutput( final IntermediateResultElementSink sink )
			throws ExecOpExecutionException, ExecPlanTaskInputException, ExecPlanTaskInterruptionException {
		if ( op.requiresCompleteChild1InputFirst() )
			produceOutputByConsumingInput1First(sink);
		else
			//produceOutputByConsumingBothInputsInParallel(sink);
			produceOutputByConsumingInput1First(sink);
	}

	/**
	 * Consumes the complete child 1 input first (and pushes that input to the
	 * operator {@link #op}), before moving on to the input from child 2.
	 */
	protected void produceOutputByConsumingInput1First( final IntermediateResultElementSink sink )
			throws ExecOpExecutionException, ExecPlanTaskInputException, ExecPlanTaskInterruptionException {

		boolean input1Consumed = false;
		while ( ! input1Consumed ) {
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

		boolean input2Consumed = false;
		while ( ! input2Consumed ) {
			final IntermediateResultBlock nextInputBlock = input2.getNextIntermediateResultBlock();
			if ( nextInputBlock != null ) {
				for ( final SolutionMapping sm : nextInputBlock.getSolutionMappings() ) {
					op.processInputFromChild2(sm, sink, execCxt);
				}
			}
			else {
				op.wrapUpForChild2(sink, execCxt);
				input2Consumed = true;
			}
		}
	}

	/**
	 * Aims to consume both inputs in parallel.
	 */
	protected void produceOutputByConsumingBothInputsInParallel( final IntermediateResultElementSink sink )
			throws ExecOpExecutionException, ExecPlanTaskInputException, ExecPlanTaskInterruptionException {

		boolean nextWaitForInput1 = true; // flag to switch between waiting for input 1 versus input 2
		boolean input1Consumed = false;
		boolean input2Consumed = false;
		while ( ! input1Consumed || ! input2Consumed ) {
			// Before blindly asking any of the two inputs to give us its next
			// IntermediateResultBlock (which may cause this thread to wait if
			// no such block is available at the moment), let's first ask them
			// if they currently have a block available. If so, request the next
			// block from the input that says it has a block available.
			boolean blockConsumed = false;
			if ( ! input1Consumed && input1.hasNextIntermediateResultBlockAvailable() )
			{
				// calling 'getNextIntermediateResultBlock()' should not cause this thread to wait
				final IntermediateResultBlock nextInputBlock = input1.getNextIntermediateResultBlock();
				if ( nextInputBlock != null ) {
					for ( final SolutionMapping sm : nextInputBlock.getSolutionMappings() ) {
						op.processInputFromChild1(sm, sink, execCxt);
					}
				}

				blockConsumed = true;
			}

			if ( ! input2Consumed && input2.hasNextIntermediateResultBlockAvailable() )
			{
				// calling 'getNextIntermediateResultBlock()' should not cause this thread to wait
				final IntermediateResultBlock nextInputBlock = input2.getNextIntermediateResultBlock();
				if ( nextInputBlock != null ) {
					for ( final SolutionMapping sm : nextInputBlock.getSolutionMappings() ) {
						op.processInputFromChild2(sm, sink, execCxt);
					}
				}

				blockConsumed = true;
			}

			if ( ! blockConsumed ) {
				// If none of the two inputs had a block available at the
				// moment, we ask one of them to produce its next block,
				// which may cause this thread to wait until that next
				// block has been produced. To decide which of the two
				// inputs we ask (and, then, wait for) we use a round
				// robin approach (i.e., always switch between the two
				// inputs). To this end, we use the 'nextWaitForInput1'
				// flag: if that flag is true, we will next ask (and wait
				// for) input 1; if that flag is false, we will next ask
				// (and wait for) input 2.
				if  ( nextWaitForInput1 && ! input1Consumed ) {
					// calling 'getNextIntermediateResultBlock()' may cause this thread to wait
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
				else if ( ! input2Consumed ) {
					// calling 'getNextIntermediateResultBlock()' may cause this thread to wait
					final IntermediateResultBlock nextInputBlock = input2.getNextIntermediateResultBlock();
					if ( nextInputBlock != null ) {
						for ( final SolutionMapping sm : nextInputBlock.getSolutionMappings() ) {
							op.processInputFromChild2(sm, sink, execCxt);
						}
					}
					else {
						op.wrapUpForChild2(sink, execCxt);
						input2Consumed = true;
					}
				}
				// flip the 'nextWaitForInput1' flag so that, next time we
				// have to wait, we will wait for the respective other input
				nextWaitForInput1 = ! nextWaitForInput1;
			}
		}
	}

}
