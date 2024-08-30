package se.liu.ida.hefquin.engine.queryplan.executable.impl.pullbased;

import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutableOperator;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.engine.queryplan.executable.NullaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.MaterializingIntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

public class PullBasedExecPlanTaskForNullaryOperator extends PullBasedExecPlanTaskBase
{
	protected final NullaryExecutableOp op;

	public PullBasedExecPlanTaskForNullaryOperator( final NullaryExecutableOp op,
	                                final ExecutionContext execCxt,
	                                final int minimumBlockSize ) {
		super(execCxt, minimumBlockSize);

		assert op != null;
		this.op = op;
	}

	@Override
	protected ExecutableOperator getExecOp() {
		return op;
	}

	@Override
	protected IntermediateResultBlock produceNextIntermediateResultBlock() throws ExecOpExecutionException {
		final MaterializingIntermediateResultElementSink sink = new MaterializingIntermediateResultElementSink();
		op.execute(sink, execCxt);
		return new LastIntermediateResultBlock( sink.getMaterializedResultBlock() );
	}
}
