package se.liu.ida.hefquin.engine.queryproc.impl.compiler;

import java.util.LinkedList;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.BinaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutablePlan;
import se.liu.ida.hefquin.engine.queryplan.executable.NaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.NullaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.UnaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecPlanTask;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.TaskBasedExecutablePlanImpl;
import se.liu.ida.hefquin.engine.queryplan.physical.BinaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.NaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.NullaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.UnaryPhysicalOp;
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
		final LinkedList<ExecPlanTask> tasks = createTasks(qep, execCtxt);
		return new TaskBasedExecutablePlanImpl(tasks, execCtxt);
	}

	protected LinkedList<ExecPlanTask> createTasks( final PhysicalPlan qep,
	                                                final ExecutionContext execCxt ) {
		final int preferredOutputBlockSize = 1;
		final LinkedList<ExecPlanTask> tasks = new LinkedList<>();
		createWorker().createTasks(qep, tasks, preferredOutputBlockSize, execCxt);
		return tasks;
	}

	protected Worker createWorker() {
		return new Worker();
	}

	protected class Worker
	{
		public void createTasks( final PhysicalPlan qep,
		                         final LinkedList<ExecPlanTask> tasks,
		                         final int preferredOutputBlockSize,
		                         final ExecutionContext execCxt ) {
			final ExecPlanTask newTask = _createTasks(qep, tasks, preferredOutputBlockSize, execCxt);
			tasks.addFirst(newTask);
		}

		protected ExecPlanTask _createTasks( final PhysicalPlan qep,
		                                     final LinkedList<ExecPlanTask> tasks,
		                                     final int preferredOutputBlockSize,
		                                     final ExecutionContext execCxt ) {
			final PhysicalOperator pop = qep.getRootOperator();
			if ( pop instanceof NullaryPhysicalOp npop )
			{
				final NullaryExecutableOp execOp = npop.createExecOp(true);
				return createTaskForNullaryExecOp(execOp, execCxt, preferredOutputBlockSize);
			}
			else if ( pop instanceof UnaryPhysicalOp upop )
			{
				final PhysicalPlan subPlan = qep.getSubPlan(0);

				final UnaryExecutableOp execOp = upop.createExecOp( true, subPlan.getExpectedVariables() );

				createTasks( subPlan, tasks, execOp.preferredInputBlockSize(), execCxt );
				final ExecPlanTask childTask = tasks.getFirst();

				return createTaskForUnaryExecOp(execOp, childTask, execCxt, preferredOutputBlockSize);
			}
			else if ( pop instanceof BinaryPhysicalOp bpop )
			{
				final PhysicalPlan subPlan1 = qep.getSubPlan(0);
				final PhysicalPlan subPlan2 = qep.getSubPlan(1);

				final BinaryExecutableOp execOp = bpop.createExecOp(
						true,
						subPlan1.getExpectedVariables(),
						subPlan2.getExpectedVariables() );

				createTasks( subPlan1, tasks, execOp.preferredInputBlockSizeFromChild1(), execCxt );
				final ExecPlanTask childTask1 = tasks.getFirst();

				createTasks( subPlan2, tasks, execOp.preferredInputBlockSizeFromChild2(), execCxt );
				final ExecPlanTask childTask2 = tasks.getFirst();

				return createTaskForBinaryExecOp(execOp, childTask1, childTask2, execCxt, preferredOutputBlockSize);
			}
			else if ( pop instanceof NaryPhysicalOp npop )
			{
				final ExpectedVariables[] expVars = new ExpectedVariables[ qep.numberOfSubPlans() ];
				for ( int i = 0; i < expVars.length; i++ ) {
					expVars[i] = qep.getSubPlan(i).getExpectedVariables();
				}

				final NaryExecutableOp execOp = npop.createExecOp(true, expVars);

				final ExecPlanTask[] childTasks = new ExecPlanTask[ qep.numberOfSubPlans() ];
				for ( int i = 0; i < childTasks.length; i++ ) {
					createTasks( qep.getSubPlan(i),
					             tasks,
					             execOp.preferredInputBlockSizeFromChilden(),
					             execCxt );

					childTasks[i] = tasks.getFirst();
				}

				return createTaskForNaryExecOp(execOp, childTasks, execCxt, preferredOutputBlockSize);
			}
			else
			{
				throw new IllegalArgumentException();
			}
		}

	} // end of helper class Worker

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

	protected abstract ExecPlanTask createTaskForNaryExecOp( NaryExecutableOp op,
	                                                         ExecPlanTask[] childTasks,
	                                                         ExecutionContext execCxt,
	                                                         int preferredOutputBlockSize );
}
