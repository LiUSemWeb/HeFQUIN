package se.liu.ida.hefquin.engine.queryproc.impl.compiler;

import java.util.LinkedList;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.BinaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutablePlan;
import se.liu.ida.hefquin.engine.queryplan.executable.NaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.NullaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.UnaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.pushbased.PushBasedPlanThread;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.pushbased.PushBasedExecutablePlanImpl;
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
		final LinkedList<PushBasedPlanThread> tasks = createTasks(qep, execCtxt);
		return new PushBasedExecutablePlanImpl(tasks, execCtxt);
	}

	protected LinkedList<PushBasedPlanThread> createTasks( final PhysicalPlan qep,
	                                                final ExecutionContext execCxt ) {
		final LinkedList<PushBasedPlanThread> tasks = new LinkedList<>();
		createWorker().createTasks(qep, tasks, execCxt);
		return tasks;
	}

	protected Worker createWorker() {
		return new Worker();
	}

	protected class Worker
	{
		public void createTasks( final PhysicalPlan qep,
		                         final LinkedList<PushBasedPlanThread> tasks,
		                         final ExecutionContext execCxt ) {
			final PushBasedPlanThread newTask = _createTasks(qep, tasks, execCxt);
			tasks.addFirst(newTask);
		}

		protected PushBasedPlanThread _createTasks( final PhysicalPlan qep,
		                                     final LinkedList<PushBasedPlanThread> tasks,
		                                     final ExecutionContext execCxt ) {
			final PhysicalOperator pop = qep.getRootOperator();
			if ( pop instanceof NullaryPhysicalOp npop )
			{
				final NullaryExecutableOp execOp = npop.createExecOp(true);
				return createTaskForNullaryExecOp(execOp, execCxt);
			}
			else if ( pop instanceof UnaryPhysicalOp upop )
			{
				final PhysicalPlan subPlan = qep.getSubPlan(0);

				final UnaryExecutableOp execOp = upop.createExecOp( true, subPlan.getExpectedVariables() );

				createTasks(subPlan, tasks, execCxt);
				final PushBasedPlanThread childTask = tasks.getFirst();

				return createTaskForUnaryExecOp(execOp, childTask, execCxt);
			}
			else if ( pop instanceof BinaryPhysicalOp bpop )
			{
				final PhysicalPlan subPlan1 = qep.getSubPlan(0);
				final PhysicalPlan subPlan2 = qep.getSubPlan(1);

				final BinaryExecutableOp execOp = bpop.createExecOp(
						true,
						subPlan1.getExpectedVariables(),
						subPlan2.getExpectedVariables() );

				createTasks( subPlan1, tasks, execCxt );
				final PushBasedPlanThread childTask1 = tasks.getFirst();

				createTasks( subPlan2, tasks, execCxt );
				final PushBasedPlanThread childTask2 = tasks.getFirst();

				return createTaskForBinaryExecOp(execOp, childTask1, childTask2, execCxt);
			}
			else if ( pop instanceof NaryPhysicalOp npop )
			{
				final ExpectedVariables[] expVars = new ExpectedVariables[ qep.numberOfSubPlans() ];
				for ( int i = 0; i < expVars.length; i++ ) {
					expVars[i] = qep.getSubPlan(i).getExpectedVariables();
				}

				final NaryExecutableOp execOp = npop.createExecOp(true, expVars);

				final PushBasedPlanThread[] childTasks = new PushBasedPlanThread[ qep.numberOfSubPlans() ];
				for ( int i = 0; i < childTasks.length; i++ ) {
					createTasks( qep.getSubPlan(i), tasks, execCxt );

					childTasks[i] = tasks.getFirst();
				}

				return createTaskForNaryExecOp(execOp, childTasks, execCxt);
			}
			else
			{
				throw new IllegalArgumentException();
			}
		}

	} // end of helper class Worker

	protected abstract PushBasedPlanThread createTaskForNullaryExecOp( NullaryExecutableOp op,
	                                                            ExecutionContext execCxt );

	protected abstract PushBasedPlanThread createTaskForUnaryExecOp( UnaryExecutableOp op,
	                                                          PushBasedPlanThread childTask,
	                                                          ExecutionContext execCxt );

	protected abstract PushBasedPlanThread createTaskForBinaryExecOp( BinaryExecutableOp op,
	                                                           PushBasedPlanThread childTask1,
	                                                           PushBasedPlanThread childTask2,
	                                                           ExecutionContext execCxt );

	protected abstract PushBasedPlanThread createTaskForNaryExecOp( NaryExecutableOp op,
	                                                         PushBasedPlanThread[] childTasks,
	                                                         ExecutionContext execCxt );
}
