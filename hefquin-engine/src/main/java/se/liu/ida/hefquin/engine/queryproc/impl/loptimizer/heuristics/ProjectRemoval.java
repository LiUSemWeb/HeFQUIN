package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics;

import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanUtils;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpProject;
import se.liu.ida.hefquin.engine.queryproc.LogicalOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;
import se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.HeuristicForLogicalOptimization;

/**
 * A heuristic that removes project operators for which the {@code mayReduce}
 * flag is not set.
 *
 * <p>Project push down may leave project operators in the plan after the
 * projection has been pushed as far down as possible. Such operators are
 * unnecessary when they do not contribute to duplicate reduction. Therefore,
 * this heuristic removes project operators with {@code mayReduce = false},
 * while retaining those with {@code mayReduce = true}.</p>
 */
public class ProjectRemoval implements HeuristicForLogicalOptimization
{
	@Override
	public LogicalPlan apply( final LogicalPlan inputPlan,
	                          final QueryProcContext ctxt )
			throws LogicalOptimizationException
	{
		return apply(inputPlan);
	}

	public LogicalPlan apply( final LogicalPlan inputPlan ) {
		final int numberOfSubPlans = inputPlan.numberOfSubPlans();
		if ( numberOfSubPlans == 0 ) {
			return inputPlan;
		}

		final LogicalOperator rootOp = inputPlan.getRootOperator();
		if ( rootOp instanceof LogicalOpProject op && !op.mayReduce() )
			return inputPlan.getSubPlan(0);

		final LogicalPlan[] newSubPlans = new LogicalPlan[numberOfSubPlans];
		boolean noChanges = true;
		for ( int i = 0; i < numberOfSubPlans; i++ ) {
			final LogicalPlan oldSubPlan = inputPlan.getSubPlan(i);
			newSubPlans[i] = apply(oldSubPlan);

			if ( ! newSubPlans[i].equals(oldSubPlan) ) {
				noChanges = false;
			}
		}

		if ( noChanges ) {
			return inputPlan;
		}

		return LogicalPlanUtils.createPlanWithSubPlans(rootOp, null, newSubPlans);
	}

	protected LogicalPlan removeProjectIfNecessary( final LogicalPlan plan ) {
		if ( plan.getRootOperator() instanceof LogicalOpProject op && ! op.mayReduce() )
			return plan.getSubPlan(0);

		return plan;
	}

}
