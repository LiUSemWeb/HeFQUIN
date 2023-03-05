package se.liu.ida.hefquin.engine.queryplan.executable.impl.pushbased;

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
	                                             final ExecutionContext execCxt,
	                                             final int minimumBlockSize ) {
		super(execCxt, minimumBlockSize);

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

		// Attention: the current implementation of this method simply consumes
		// and pushes the complete i-th child input first before moving on to
		// the (i+1)-th child. Hence, with this implementation we do not
		// actually benefit from the parallelization.

		for ( int i = 0; i < inputs.length; i++ ) {
			boolean inputConsumed = false;
			while ( ! inputConsumed ) {
				final IntermediateResultBlock nextInputBlock = inputs[i].getNextIntermediateResultBlock();
				if ( nextInputBlock != null ) {
					op.processBlockFromXthChild(i, nextInputBlock, sink, execCxt);
				}
				else {
					op.wrapUpForXthChild(i, sink, execCxt);
					inputConsumed = true;
				}
			}
		}
	}

}
