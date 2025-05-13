package se.liu.ida.hefquin.engine.queryplan.executable.impl.pushbased;

import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutableOperator;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.NullaryExecutableOp;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

public class PushBasedPlanThreadImplForNullaryOperator extends PushBasedPlanThreadImplBase
{
	protected final NullaryExecutableOp op;

	public PushBasedPlanThreadImplForNullaryOperator( final NullaryExecutableOp op,
	                                                  final ExecutionContext execCxt ) {
		super(execCxt);

		assert op != null;
		this.op = op;
	}

	@Override
	protected ExecutableOperator getExecOp() {
		return op;
	}

	@Override
	protected void produceOutput( final IntermediateResultElementSink sink )
			throws ExecOpExecutionException
	{
		op.execute(sink, execCxt);
	}

}
