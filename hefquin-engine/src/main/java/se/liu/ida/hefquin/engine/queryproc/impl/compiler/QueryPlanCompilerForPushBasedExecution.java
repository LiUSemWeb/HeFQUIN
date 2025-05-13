package se.liu.ida.hefquin.engine.queryproc.impl.compiler;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.BinaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutablePlan;
import se.liu.ida.hefquin.engine.queryplan.executable.NaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.NullaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.UnaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.pushbased.PushBasedPlanThread;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.pushbased.PushBasedPlanThreadImplForBinaryOperator;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.pushbased.PushBasedPlanThreadImplForNaryOperator;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.pushbased.PushBasedPlanThreadImplForNullaryOperator;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.pushbased.PushBasedPlanThreadImplForUnaryOperator;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.pushbased.ConnectorForAdditionalConsumer;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.pushbased.PushBasedExecutablePlanImpl;
import se.liu.ida.hefquin.engine.queryplan.physical.BinaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.NaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.NullaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.UnaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;

public class QueryPlanCompilerForPushBasedExecution extends QueryPlanCompilerBase
{
	public QueryPlanCompilerForPushBasedExecution( final QueryProcContext ctxt ) {
		super(ctxt);
	}

	@Override
	public ExecutablePlan compile( final PhysicalPlan qep ) {
		final ExecutionContext execCtxt = createExecContext();
		final LinkedList<PushBasedPlanThread> threads = createThreads(qep, execCtxt);
		return new PushBasedExecutablePlanImpl(threads, execCtxt);
	}

	protected LinkedList<PushBasedPlanThread> createThreads( final PhysicalPlan qep,
	                                                         final ExecutionContext execCxt ) {
		final LinkedList<PushBasedPlanThread> threads = new LinkedList<>();
		createWorker().createThreads(qep, threads, execCxt);

		// remove all ConnectorForAdditionalConsumer from the list
		final LinkedList<PushBasedPlanThread> threads2 = new LinkedList<>();
		for ( final PushBasedPlanThread t : threads ) {
			if ( ! (t instanceof ConnectorForAdditionalConsumer) ) {
				threads2.add(t);
			}
		}

		return threads2;
	}

	protected Worker createWorker() {
		return new Worker();
	}

	// makes sure that, for sub-plans that are contained multiple times in the
	// given physical plan, we create only one PushBasedPlanThread, which then
	// provides its output solution mappings to multiple consumers
	protected class Worker
	{
		protected final Map<PhysicalPlan,PushBasedPlanThread> convertedSubPlans = new HashMap<>();

		public void createThreads( final PhysicalPlan qep,
		                           final LinkedList<PushBasedPlanThread> threads,
		                           final ExecutionContext execCxt ) {
			// Check whether we have seen the given QEP before (which would be
			// as a subplan somewhere else within the overall plan that we are
			// recursively converting here).
			if ( ! convertedSubPlans.containsKey(qep) ) {
				// If we have not seen the given QEP before, create the
				// relevant threads for it (which includes the threads
				// for its subplans, if any)
				final PushBasedPlanThread t = _createThreads(qep, threads, execCxt);
				convertedSubPlans.put(qep, t);
				threads.addFirst(t);
			}
			else {
				// If we have indeed seen the given QEP before, then reuse
				// the thread that we have already created for it, ...
				final PushBasedPlanThread existingThread = convertedSubPlans.get(qep);
				// ... by adding an extra connector to its output.
				final PushBasedPlanThread c = existingThread.addConnectorForAdditionalConsumer();
				threads.addFirst(c);
			}
		}

		protected PushBasedPlanThread _createThreads( final PhysicalPlan qep,
		                                     final LinkedList<PushBasedPlanThread> tasks,
		                                     final ExecutionContext execCxt ) {
			final PhysicalOperator pop = qep.getRootOperator();
			if ( pop instanceof NullaryPhysicalOp npop )
			{
				final NullaryExecutableOp execOp = npop.createExecOp(true);
				return createThread(execOp, execCxt);
			}
			else if ( pop instanceof UnaryPhysicalOp upop )
			{
				final PhysicalPlan subPlan = qep.getSubPlan(0);

				final UnaryExecutableOp execOp = upop.createExecOp( true, subPlan.getExpectedVariables() );

				createThreads(subPlan, tasks, execCxt);
				final PushBasedPlanThread childTask = tasks.getFirst();

				return createThread(execOp, childTask, execCxt);
			}
			else if ( pop instanceof BinaryPhysicalOp bpop )
			{
				final PhysicalPlan subPlan1 = qep.getSubPlan(0);
				final PhysicalPlan subPlan2 = qep.getSubPlan(1);

				final BinaryExecutableOp execOp = bpop.createExecOp(
						true,
						subPlan1.getExpectedVariables(),
						subPlan2.getExpectedVariables() );

				createThreads( subPlan1, tasks, execCxt );
				final PushBasedPlanThread childTask1 = tasks.getFirst();

				createThreads( subPlan2, tasks, execCxt );
				final PushBasedPlanThread childTask2 = tasks.getFirst();

				return createThread(execOp, childTask1, childTask2, execCxt);
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
					createThreads( qep.getSubPlan(i), tasks, execCxt );

					childTasks[i] = tasks.getFirst();
				}

				return createThread(execOp, childTasks, execCxt);
			}
			else
			{
				throw new IllegalArgumentException();
			}
		}

	} // end of helper class Worker


	protected PushBasedPlanThread createThread( final NullaryExecutableOp op,
	                                            final ExecutionContext execCxt ) {
		return new PushBasedPlanThreadImplForNullaryOperator(op, execCxt);
	}

	protected PushBasedPlanThread createThread( final UnaryExecutableOp op,
	                                            final PushBasedPlanThread input,
	                                            final ExecutionContext execCxt ) {
		return new PushBasedPlanThreadImplForUnaryOperator(op, input, execCxt);
	}

	protected PushBasedPlanThread createThread( final BinaryExecutableOp op,
	                                            final PushBasedPlanThread input1,
	                                            final PushBasedPlanThread input2,
	                                            final ExecutionContext execCxt ) {
		return new PushBasedPlanThreadImplForBinaryOperator(op, input1, input2, execCxt);
	}

	protected PushBasedPlanThread createThread( final NaryExecutableOp op,
	                                            final PushBasedPlanThread[] inputs,
	                                            final ExecutionContext execCxt ) {
		return new PushBasedPlanThreadImplForNaryOperator(op, inputs, execCxt);
	}

}
