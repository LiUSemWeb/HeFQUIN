package se.liu.ida.hefquin.engine.queryplan.executable.impl.pushbased;

import java.util.ArrayList;
import java.util.List;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.engine.queryplan.executable.BinaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutableOperator;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

public class PushBasedPlanThreadImplForBinaryOperator extends PushBasedPlanThreadImplBase
{
	protected final BinaryExecutableOp op;
	protected final PushBasedPlanThread input1;
	protected final PushBasedPlanThread input2;

	public PushBasedPlanThreadImplForBinaryOperator( final BinaryExecutableOp op,
	                                                 final PushBasedPlanThread input1,
	                                                 final PushBasedPlanThread input2,
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
			throws ExecOpExecutionException, ConsumingPushBasedPlanThreadException {
		if ( op.requiresCompleteChild1InputFirst() )
			produceOutputByConsumingInput1First(sink);
		else
			produceOutputByConsumingBothInputsInParallel(sink);
	}

	/**
	 * Consumes the complete child 1 input first (and pushes that input to the
	 * operator {@link #op}), before moving on to the input from child 2.
	 */
	protected void produceOutputByConsumingInput1First( final IntermediateResultElementSink sink )
			throws ExecOpExecutionException, ConsumingPushBasedPlanThreadException
	{
		final List<SolutionMapping> transferBuffer = new ArrayList<>();
		boolean input1Consumed = false;
		while ( ! input1Consumed ) {
			input1.transferAvailableOutput(transferBuffer);
			if ( ! transferBuffer.isEmpty() ) {
				op.processInputFromChild1(transferBuffer, sink, execCxt);
			}
			else {
				op.wrapUpForChild1(sink, execCxt);
				input1Consumed = true;
			}
		}

		boolean input2Consumed = false;
		while ( ! input2Consumed ) {
			input2.transferAvailableOutput(transferBuffer);
			if ( ! transferBuffer.isEmpty() ) {
				op.processInputFromChild2(transferBuffer, sink, execCxt);
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
			throws ExecOpExecutionException, ConsumingPushBasedPlanThreadException
	{
		final List<SolutionMapping> transferBuffer = new ArrayList<>();
		boolean nextWaitForInput1 = true; // flag to switch between waiting for input 1 versus input 2
		boolean input1ConsumedCompletely = false;
		boolean input2ConsumedCompletely = false;
		while ( ! input1ConsumedCompletely || ! input2ConsumedCompletely ) {
			// Before blindly asking any of the two inputs to give us the
			// solution mappings that it has currently available as output
			// (which may cause this thread to wait if no such output is
			// available at the moment), let's first ask them if they do
			// have some output readily available. If so, request this
			// output from the input that says it has output available.
			boolean someInputConsumed = false;
			if ( ! input1ConsumedCompletely && input1.hasMoreOutputAvailable() )
			{
				// calling 'transferAvailableOutput' should not cause this thread to wait
				input1.transferAvailableOutput(transferBuffer);
				if ( ! transferBuffer.isEmpty() ) {
					op.processInputFromChild1(transferBuffer, sink, execCxt);
				}

				someInputConsumed = true;
			}

			if ( ! input2ConsumedCompletely && input2.hasMoreOutputAvailable() )
			{
				// calling 'transferAvailableOutput' should not cause this thread to wait
				input2.transferAvailableOutput(transferBuffer);
				if ( ! transferBuffer.isEmpty() ) {
					op.processInputFromChild2(transferBuffer, sink, execCxt);
				}

				someInputConsumed = true;
			}

			if ( ! someInputConsumed ) {
				// If none of the two inputs had some output available at the
				// moment, we ask one of them to produce its next output,
				// which may cause this thread to wait until that next
				// output has been produced. To decide which of the two
				// inputs we ask (and, then, wait for) we use a round
				// robin approach (i.e., always switch between the two
				// inputs). To this end, we use the 'nextWaitForInput1'
				// flag: if that flag is true, we will next ask (and wait
				// for) input 1; if that flag is false, we will next ask
				// (and wait for) input 2.
				if  ( nextWaitForInput1 && ! input1ConsumedCompletely ) {
					// calling 'transferAvailableOutput' may cause this thread to wait
					input1.transferAvailableOutput(transferBuffer);
					if ( ! transferBuffer.isEmpty() ) {
						op.processInputFromChild1(transferBuffer, sink, execCxt);
					}
					else {
						op.wrapUpForChild1(sink, execCxt);
						input1ConsumedCompletely = true;
					}
				}
				else if ( ! input2ConsumedCompletely ) {
					// calling 'transferAvailableOutput)' may cause this thread to wait
					input2.transferAvailableOutput(transferBuffer);
					if ( ! transferBuffer.isEmpty() ) {
						op.processInputFromChild2(transferBuffer, sink, execCxt);
					}
					else {
						op.wrapUpForChild2(sink, execCxt);
						input2ConsumedCompletely = true;
					}
				}
				// flip the 'nextWaitForInput1' flag so that, next time we
				// have to wait, we will wait for the respective other input
				nextWaitForInput1 = ! nextWaitForInput1;
			}
		}
	}

}
