package se.liu.ida.hefquin.engine.queryplan.executable.impl.pushbased;

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
	                                               final ExecutionContext execCxt,
	                                               final int minimumBlockSize ) {
		super(execCxt, minimumBlockSize);

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

		// Attention: the current implementation of this method ignores
		// whether op.requiresCompleteChild1InputFirst() is true or
		// false but, instead, simply consumes and pushes the complete
		// child 1 input first (as would be required in case that the
		// aforementioned function returns true).

		boolean input1Consumed = false;
		while ( ! input1Consumed ) {
			final IntermediateResultBlock nextInputBlock = input1.getNextIntermediateResultBlock();
			if ( nextInputBlock != null ) {
				op.processBlockFromChild1(nextInputBlock, sink, execCxt);
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
				op.processBlockFromChild2(nextInputBlock, sink, execCxt);
			}
			else {
				op.wrapUpForChild2(sink, execCxt);
				input2Consumed = true;
			}
		}
	}

}
