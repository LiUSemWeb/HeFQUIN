package se.liu.ida.hefquin.engine.queryproc.impl.compiler;

import java.util.LinkedList;

import se.liu.ida.hefquin.engine.queryplan.executable.BinaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutablePlan;
import se.liu.ida.hefquin.engine.queryplan.executable.NullaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.UnaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecPlanTask;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.TaskBasedExecutablePlanImpl;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;

public abstract class TaskBasedQueryPlanCompilerBase extends QueryPlanCompilerBase
{
	public TaskBasedQueryPlanCompilerBase( final QueryProcContext ctxt ) {
		super(ctxt);
	}

	@Override
	public ExecutablePlan compile( final PhysicalPlan qep ) {
		final ExecutionContext execCtxt = createExecContext();
		final LinkedList<ExecPlanTask> tasks = new LinkedList<>();
		createTasks(qep, tasks, 1, execCtxt);
		return new TaskBasedExecutablePlanImpl(tasks, execCtxt);
	}

	protected void createTasks( final PhysicalPlan qep,
	                            final LinkedList<ExecPlanTask> tasks,
	                            final int preferredOutputBlockSize,
	                            final ExecutionContext execCxt ) {
		final ExecPlanTask newTask;
		if ( qep.numberOfSubPlans() == 0 )
		{
			final NullaryExecutableOp execOp = (NullaryExecutableOp) qep.getRootOperator().createExecOp();
			newTask = createTaskForNullaryExecOp(execOp, execCxt, preferredOutputBlockSize);
		}
		else if ( qep.numberOfSubPlans() == 1 )
		{
			final PhysicalPlan subPlan = qep.getSubPlan(0);

			final UnaryExecutableOp execOp = (UnaryExecutableOp) qep.getRootOperator().createExecOp( subPlan.getExpectedVariables() );

			createTasks( subPlan, tasks, execOp.preferredInputBlockSize(), execCxt );
			final ExecPlanTask childTask = tasks.getFirst();

			newTask = createTaskForUnaryExecOp(execOp, childTask, execCxt, preferredOutputBlockSize);
		}
		else if ( qep.numberOfSubPlans() == 2 )
		{
			final PhysicalPlan subPlan1 = qep.getSubPlan(0);
			final PhysicalPlan subPlan2 = qep.getSubPlan(1);

			final BinaryExecutableOp execOp = (BinaryExecutableOp) qep.getRootOperator().createExecOp(
					subPlan1.getExpectedVariables(),
					subPlan2.getExpectedVariables() );

			createTasks( subPlan1, tasks, execOp.preferredInputBlockSizeFromChild1(), execCxt );
			final ExecPlanTask childTask1 = tasks.getFirst();

			createTasks( subPlan2, tasks, execOp.preferredInputBlockSizeFromChild2(), execCxt );
			final ExecPlanTask childTask2 = tasks.getFirst();

			newTask = createTaskForBinaryExecOp(execOp, childTask1, childTask2, execCxt, preferredOutputBlockSize);
		}
		else
		{
			throw new IllegalArgumentException();
		}

		tasks.addFirst(newTask);
	}

	protected abstract ExecPlanTask createTaskForNullaryExecOp( NullaryExecutableOp op,
	                                                            ExecutionContext execCxt,
	                                                            int preferredOutputBlockSize );

	protected abstract ExecPlanTask createTaskForUnaryExecOp( UnaryExecutableOp op,
	                                                          ExecPlanTask childTask,
	                                                          ExecutionContext execCxt,
	                                                          int preferredOutputBlockSize );

	protected abstract ExecPlanTask createTaskForBinaryExecOp( BinaryExecutableOp op,
	                                                           ExecPlanTask childTask1,
	                                                           ExecPlanTask childTask2,
	                                                           ExecutionContext execCxt,
	                                                           int preferredOutputBlockSize );
}
