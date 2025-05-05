package se.liu.ida.hefquin.engine.queryplan.executable.impl.pushbased;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutableOperator;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.UnaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecPlanTask;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecPlanTaskInputException;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecPlanTaskInterruptionException;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

public class PushBasedExecPlanTaskForUnaryOperator extends PushBasedExecPlanTaskBase
{
	protected final UnaryExecutableOp op;
	protected final ExecPlanTask input;

	public PushBasedExecPlanTaskForUnaryOperator( final UnaryExecutableOp op,
	                                              final ExecPlanTask input,
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
			throws ExecOpExecutionException, ExecPlanTaskInputException, ExecPlanTaskInterruptionException {

		boolean lastInputBlockConsumed = false;
		while ( ! lastInputBlockConsumed ) {
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
	}

}
