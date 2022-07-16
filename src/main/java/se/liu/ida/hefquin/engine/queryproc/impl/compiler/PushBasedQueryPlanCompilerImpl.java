package se.liu.ida.hefquin.engine.queryproc.impl.compiler;

import se.liu.ida.hefquin.engine.queryplan.executable.BinaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.NullaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.UnaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecPlanTask;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.pushbased.PushBasedExecPlanTaskForBinaryOperator;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.pushbased.PushBasedExecPlanTaskForNullaryOperator;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.pushbased.PushBasedExecPlanTaskForUnaryOperator;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;

public class PushBasedQueryPlanCompilerImpl extends TaskBasedQueryPlanCompilerBase
{
	public PushBasedQueryPlanCompilerImpl( final QueryProcContext ctxt ) {
		super(ctxt);
	}

	@Override
	protected ExecPlanTask createTaskForNullaryExecOp( final NullaryExecutableOp op,
	                                                   final ExecutionContext execCxt,
	                                                   final int preferredOutputBlockSize ) {
		return new PushBasedExecPlanTaskForNullaryOperator(op, execCxt, preferredOutputBlockSize);
	}

	@Override
	protected ExecPlanTask createTaskForUnaryExecOp( final UnaryExecutableOp op,
	                                                 final ExecPlanTask childTask,
	                                                 final ExecutionContext execCxt,
	                                                 final int preferredOutputBlockSize ) {
		return new PushBasedExecPlanTaskForUnaryOperator(op, childTask, execCxt, preferredOutputBlockSize);
	}

	@Override
	protected ExecPlanTask createTaskForBinaryExecOp( final BinaryExecutableOp op,
	                                                  final ExecPlanTask childTask1,
	                                                  final ExecPlanTask childTask2,
	                                                  final ExecutionContext execCxt,
	                                                  final int preferredOutputBlockSize ) {
		return new PushBasedExecPlanTaskForBinaryOperator(op, childTask1, childTask2, execCxt, preferredOutputBlockSize);
	}

}
