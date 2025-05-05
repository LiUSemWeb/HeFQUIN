package se.liu.ida.hefquin.engine.queryplan.executable.impl.pushbased;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutableOperator;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.NaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecPlanTask;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecPlanTaskInputException;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecPlanTaskInterruptionException;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

public class PushBasedExecPlanTaskForNaryOperator extends PushBasedExecPlanTaskBase
{
	protected final NaryExecutableOp op;
	protected final ExecPlanTask[] inputs;

	public PushBasedExecPlanTaskForNaryOperator( final NaryExecutableOp op,
	                                             final ExecPlanTask[] inputs,
	                                             final ExecutionContext execCxt ) {
		super(execCxt);

		assert op != null;
		assert inputs != null;
		assert inputs.length > 0;

		this.op = op;
		this.inputs = inputs;
	}

	@Override
	protected ExecutableOperator getExecOp() {
		return op;
	}

	@Override
	protected void produceOutput( final IntermediateResultElementSink sink )
			throws ExecOpExecutionException, ExecPlanTaskInputException, ExecPlanTaskInterruptionException {
		produceOutputByConsumingAllInputsInParallel(sink);
		//produceOutputByConsumingInputsOneAfterAnother(sink);
	}

	/**
	 * Consumes the complete i-th input first (and pushes that input to the
	 * operator {@link #op}), before moving on to the (i+1)-th input. Hence,
	 * this implementation does not consume the inputs in parallel. Instead,
	 * if one of the inputs requires a long time, no progress is made in
	 * parallel based on any of the other inputs.
	 */
	protected void produceOutputByConsumingInputsOneAfterAnother( final IntermediateResultElementSink sink )
			throws ExecOpExecutionException, ExecPlanTaskInputException, ExecPlanTaskInterruptionException {

		for ( int i = 0; i < inputs.length; i++ ) {
			boolean inputConsumed = false;
			while ( ! inputConsumed ) {
				final IntermediateResultBlock nextInputBlock = inputs[i].getNextIntermediateResultBlock();
				if ( nextInputBlock != null ) {
					for ( final SolutionMapping sm : nextInputBlock.getSolutionMappings() ) {
						op.processInputFromXthChild(i, sm, sink, execCxt);
					}
				}
				else {
					op.wrapUpForXthChild(i, sink, execCxt);
					inputConsumed = true;
				}
			}
		}
	}


	/**
	 * Consumes the complete i-th input first (and pushes that input to the
	 * operator {@link #op}), before moving on to the (i+1)-th input.
	 */
	protected void produceOutputByConsumingAllInputsInParallel( final IntermediateResultElementSink sink )
			throws ExecOpExecutionException, ExecPlanTaskInputException, ExecPlanTaskInterruptionException {

		final boolean[] inputConsumed = new boolean[inputs.length];
		for ( int i = 0; i < inputs.length; i++ ) { inputConsumed[i] = false; }

		int indexOfNextInputToWaitFor = 0;
		int numberOfInputsConsumed = 0;
		while ( numberOfInputsConsumed < inputs.length ) {
			// Before blindly asking any of the inputs to give us its next
			// IntermediateResultBlock (which may cause this thread to wait
			// if no such block is available at the moment), let's first ask
			// them if they currently have a block available. If so, request
			// the next block from the input that says it has a block available.
			boolean blockConsumed = false;
			for ( int i = 0; i < inputs.length; i++ ) {
				if ( ! inputConsumed[i] && inputs[i].hasNextIntermediateResultBlockAvailable() ) {
					// calling 'getNextIntermediateResultBlock()' should not cause this thread to wait
					final IntermediateResultBlock nextInputBlock = inputs[i].getNextIntermediateResultBlock();
					if ( nextInputBlock != null ) {
						for ( final SolutionMapping sm : nextInputBlock.getSolutionMappings() ) {
							op.processInputFromXthChild(i, sm, sink, execCxt);
						}
					}

					blockConsumed = true;
				}
			}

			if ( ! blockConsumed ) {
				// If none of the inputs had a block available at the moment,
				// we ask one of them to produce its next block, which may
				// cause this thread to wait until that next block has been
				// produced. To decide which of the inputs we ask (and, then,
				// wait for) we use a round robin approach. To this end, we
				// use the 'indexOfNextInputToWaitFor' pointer which we advance
				// each time we leave this code block here.

				// First, we have to make sure that 'indexOfNextInputToWaitFor'
				// points to an input that has not been consumed completely yet.
				while ( inputConsumed[indexOfNextInputToWaitFor] == true ) {
					indexOfNextInputToWaitFor = advanceIndexOfInput(indexOfNextInputToWaitFor);
				}

				// Now we ask that input to produce its next block, which may
				// cause this thread to wait.
				final IntermediateResultBlock nextInputBlock = inputs[indexOfNextInputToWaitFor].getNextIntermediateResultBlock();
				if ( nextInputBlock != null ) {
					for ( final SolutionMapping sm : nextInputBlock.getSolutionMappings() ) {
						op.processInputFromXthChild(indexOfNextInputToWaitFor, sm, sink, execCxt);
					}
				}
				else {
					op.wrapUpForXthChild(indexOfNextInputToWaitFor, sink, execCxt);
					inputConsumed[indexOfNextInputToWaitFor] = true;
					numberOfInputsConsumed++;
				}

				// Finally, we advance the 'indexOfNextInputToWaitFor' pointer
				// so that, next time we will have to wait, we will wait for
				// the next input (rather than always waiting for the same
				// input before moving on to the next input).
				indexOfNextInputToWaitFor = advanceIndexOfInput(indexOfNextInputToWaitFor);
			}
		}
	}

	/**
	 * Returns the given integer increased by one, unless such an
	 * increase results in an integer that is outside of the bounds
	 * of the {@link #inputs} array, in which case the function returns
	 * zero (effectively jumping back to the first index in the array).
	 */
	protected int advanceIndexOfInput( final int currentIndex ) {
		final int i = currentIndex + 1;
		return ( i < inputs.length ) ? i : 0;
	}
}
