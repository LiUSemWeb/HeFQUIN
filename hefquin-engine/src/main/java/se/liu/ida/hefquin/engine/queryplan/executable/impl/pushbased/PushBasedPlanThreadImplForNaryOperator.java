package se.liu.ida.hefquin.engine.queryplan.executable.impl.pushbased;

import java.util.ArrayList;
import java.util.List;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutableOperator;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.NaryExecutableOp;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

public class PushBasedPlanThreadImplForNaryOperator extends PushBasedPlanThreadImplBase
{
	protected final NaryExecutableOp op;
	protected final PushBasedPlanThread[] inputs;

	public PushBasedPlanThreadImplForNaryOperator( final NaryExecutableOp op,
	                                               final PushBasedPlanThread[] inputs,
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
			throws ExecOpExecutionException, ConsumingPushBasedPlanThreadException {
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
			throws ExecOpExecutionException, ConsumingPushBasedPlanThreadException
	{
		final List<SolutionMapping> transferBuffer = new ArrayList<>();
		for ( int i = 0; i < inputs.length; i++ ) {
			boolean inputConsumed = false;
			while ( ! inputConsumed ) {
				inputs[i].transferAvailableOutput(transferBuffer);
				if ( ! transferBuffer.isEmpty() ) {
					op.processInputFromXthChild(i, transferBuffer, sink, execCxt);
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
			throws ExecOpExecutionException, ConsumingPushBasedPlanThreadException
	{
		final List<SolutionMapping> transferBuffer = new ArrayList<>();
		final boolean[] inputConsumedCompletely = new boolean[inputs.length];
		for ( int i = 0; i < inputs.length; i++ ) { inputConsumedCompletely[i] = false; }

		int indexOfNextInputToWaitFor = 0;
		int numberOfInputsConsumed = 0;
		while ( numberOfInputsConsumed < inputs.length ) {
			// Before blindly asking any of the inputs to give us the
			// solution mappings that it has currently available as output
			// (which may cause this thread to wait if no such output is
			// available at the moment), let's first ask them if they do
			// have some output readily available. If so, request this
			// output from the input that says it has output available.
			boolean someInputConsumed = false;
			for ( int i = 0; i < inputs.length; i++ ) {
				if ( ! inputConsumedCompletely[i] && inputs[i].hasMoreOutputAvailable() ) {
					// calling 'transferAvailableOutput' should not cause this thread to wait
					inputs[i].transferAvailableOutput(transferBuffer);
					if ( ! transferBuffer.isEmpty() ) {
						op.processInputFromXthChild(i, transferBuffer, sink, execCxt);
					}

					someInputConsumed = true;
				}
			}

			if ( ! someInputConsumed ) {
				// If none of the inputs had some output available at the
				// moment, we ask one of them to produce its next output,
				// which may cause this thread to wait until that next
				// output has been produced.
				// To decide which of the inputs we ask (and, then, wait
				// for) we use a round robin approach. To this end, we use
				// the 'indexOfNextInputToWaitFor' pointer which we advance
				// each time we leave this code block here.

				// First, we have to make sure that 'indexOfNextInputToWaitFor'
				// points to an input that has not been consumed completely yet.
				while ( inputConsumedCompletely[indexOfNextInputToWaitFor] == true ) {
					indexOfNextInputToWaitFor = advanceIndexOfInput(indexOfNextInputToWaitFor);
				}

				// Now we ask that input to produce its next output, which may
				// cause this thread to wait.
				inputs[indexOfNextInputToWaitFor].transferAvailableOutput(transferBuffer);
				if ( ! transferBuffer.isEmpty() ) {
					op.processInputFromXthChild(indexOfNextInputToWaitFor, transferBuffer, sink, execCxt);
				}
				else {
					op.wrapUpForXthChild(indexOfNextInputToWaitFor, sink, execCxt);
					inputConsumedCompletely[indexOfNextInputToWaitFor] = true;
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
