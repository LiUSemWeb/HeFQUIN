package se.liu.ida.hefquin.engine.queryproc.impl.compiler;

import se.liu.ida.hefquin.engine.queryplan.executable.BinaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.NaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.NullaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.UnaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecPlanTask;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.pullbased.PullBasedExecPlanTaskForBinaryOperator;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.pullbased.PullBasedExecPlanTaskForNullaryOperator;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.pullbased.PullBasedExecPlanTaskForUnaryOperator;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;

public class PullBasedQueryPlanCompilerImpl extends TaskBasedQueryPlanCompilerBase
{
	public PullBasedQueryPlanCompilerImpl( final QueryProcContext ctxt ) {
		super(ctxt);
	}

	@Override
	protected ExecPlanTask createTaskForNullaryExecOp( final NullaryExecutableOp op,
	                                                   final ExecutionContext execCxt,
	                                                   final int preferredOutputBlockSize ) {
		return new PullBasedExecPlanTaskForNullaryOperator(op, execCxt, preferredOutputBlockSize);
	}

	@Override
	protected ExecPlanTask createTaskForUnaryExecOp( final UnaryExecutableOp op,
	                                                 final ExecPlanTask childTask,
	                                                 final ExecutionContext execCxt,
	                                                 final int preferredOutputBlockSize ) {
		return new PullBasedExecPlanTaskForUnaryOperator(op, childTask, execCxt, preferredOutputBlockSize);
	}

	@Override
	protected ExecPlanTask createTaskForBinaryExecOp( final BinaryExecutableOp op,
	                                                  final ExecPlanTask childTask1,
	                                                  final ExecPlanTask childTask2,
	                                                  final ExecutionContext execCxt,
	                                                  final int preferredOutputBlockSize ) {
		return new PullBasedExecPlanTaskForBinaryOperator(op, childTask1, childTask2, execCxt, preferredOutputBlockSize);
	}

	@Override
	protected ExecPlanTask createTaskForNaryExecOp( final NaryExecutableOp op,
	                                                final ExecPlanTask[] childTasks,
	                                                final ExecutionContext execCxt,
	                                                final int preferredOutputBlockSize ) {
		throw new UnsupportedOperationException();
	}

}
