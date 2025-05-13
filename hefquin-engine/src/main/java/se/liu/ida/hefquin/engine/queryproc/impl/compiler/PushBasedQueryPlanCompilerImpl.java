package se.liu.ida.hefquin.engine.queryproc.impl.compiler;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import se.liu.ida.hefquin.engine.queryplan.executable.BinaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.NaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.NullaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.UnaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.pushbased.ConnectorForAdditionalConsumer;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.pushbased.PushBasedPlanThread;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.pushbased.PushBasedPlanThreadImplForBinaryOperator;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.pushbased.PushBasedPlanThreadImplForNaryOperator;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.pushbased.PushBasedPlanThreadImplForNullaryOperator;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.pushbased.PushBasedPlanThreadImplForUnaryOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;

public class PushBasedQueryPlanCompilerImpl extends TaskBasedQueryPlanCompilerBase
{
	public PushBasedQueryPlanCompilerImpl( final QueryProcContext ctxt ) {
		super(ctxt);
	}

	@Override
	protected PushBasedPlanThread createTaskForNullaryExecOp( final NullaryExecutableOp op,
	                                                            final ExecutionContext execCxt ) {
		return new PushBasedPlanThreadImplForNullaryOperator(op, execCxt);
	}

	@Override
	protected PushBasedPlanThread createTaskForUnaryExecOp( final UnaryExecutableOp op,
	                                                          final PushBasedPlanThread childTask,
	                                                          final ExecutionContext execCxt ) {
		return new PushBasedPlanThreadImplForUnaryOperator(op, childTask, execCxt);
	}

	@Override
	protected PushBasedPlanThread createTaskForBinaryExecOp( final BinaryExecutableOp op,
	                                                           final PushBasedPlanThread childTask1,
	                                                           final PushBasedPlanThread childTask2,
	                                                           final ExecutionContext execCxt ) {
		return new PushBasedPlanThreadImplForBinaryOperator(op, childTask1, childTask2, execCxt);
	}

	@Override
	protected PushBasedPlanThread createTaskForNaryExecOp( final NaryExecutableOp op,
	                                                final PushBasedPlanThread[] childTasks,
	                                                final ExecutionContext execCxt ) {
		return new PushBasedPlanThreadImplForNaryOperator(op, childTasks, execCxt);
	}

	@Override
	protected LinkedList<PushBasedPlanThread> createTasks( final PhysicalPlan qep,
	                                                final ExecutionContext execCxt ) {
		final LinkedList<PushBasedPlanThread> tasks = super.createTasks(qep, execCxt);

		// remove all ConnectorForAdditionalConsumer from the list
		final LinkedList<PushBasedPlanThread> tasks2 = new LinkedList<>();
		for ( final PushBasedPlanThread t : tasks ) {
			if ( ! (t instanceof ConnectorForAdditionalConsumer) ) {
				tasks2.add(t);
			}
		}

		return tasks2;
	}

	@Override
	protected Worker createWorker() {
		return new Worker();
	}

	// makes sure that, for sub-plans that are contained multiple times in the
	// given physical plan, we create only one ExecPlanTask, which then pushes
	// its solution mappings to multiple consuming tasks
	protected class Worker extends TaskBasedQueryPlanCompilerBase.Worker
	{
		protected final Map<PhysicalPlan,PushBasedPlanThread> convertedSubPlans = new HashMap<>();

		@Override
		public void createTasks( final PhysicalPlan qep,
		                         final LinkedList<PushBasedPlanThread> tasks,
		                         final ExecutionContext execCxt ) {
			final PushBasedPlanThread newTask;
			final PushBasedPlanThread probe = convertedSubPlans.get(qep);
			if ( probe != null ) {
				newTask = probe.addConnectorForAdditionalConsumer();
			}
			else {
				newTask = _createTasks(qep, tasks, execCxt);
				convertedSubPlans.put(qep, newTask);
			}

			tasks.addFirst(newTask);
		}
	} // end of helper class Worker

}
