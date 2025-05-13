package se.liu.ida.hefquin.engine.queryplan.executable.impl.pushbased;

import java.util.ArrayList;
import java.util.List;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutableOperator;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.UnaryExecutableOp;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

public class PushBasedPlanThreadImplForUnaryOperator extends PushBasedPlanThreadImplBase
{
	protected final UnaryExecutableOp op;
	protected final PushBasedPlanThread input;

	public PushBasedPlanThreadImplForUnaryOperator( final UnaryExecutableOp op,
	                                                final PushBasedPlanThread input,
	                                                final ExecutionContext execCxt ) {
		super(execCxt);

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
	protected void produceOutput( final IntermediateResultElementSink sink )
			throws ExecOpExecutionException, ConsumingPushBasedPlanThreadException
	{
		final List<SolutionMapping> transferBuffer = new ArrayList<>();
		boolean inputConsumed = false;
		while ( ! inputConsumed ) {
			input.transferAvailableOutput(transferBuffer);
			if ( ! transferBuffer.isEmpty() ) {
				op.process(transferBuffer, sink, execCxt);
			}
			else {
				op.concludeExecution(sink, execCxt);
				inputConsumed = true;
			}
		}
	}

}
