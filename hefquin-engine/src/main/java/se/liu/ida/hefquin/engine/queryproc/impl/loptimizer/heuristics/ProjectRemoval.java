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
 * <p>
 * This heuristic is meant to be applied after {@link ProjectPushDown} which
 * aims to push projections as far down in the plan as possible and, in this
 * process, may leave several project operators all over the plan.
 * Such operators are unnecessary when they do not contribute to duplicate reduction.
 * Therefore, this heuristic removes project operators with {@code mayReduce = false},
 * while retaining those with {@code mayReduce = true}.
 * <p>
 * More background: Pushing down project operators is useful for two reasons:
 * <ol>
 * <li>If a projection ends up being pushed down all the way into a request operator,
 *      this request operator retrieves less data.</li>
 *      <li> If the query uses {@code SELECT DISTINCT}, duplicate removal through our
 *      {@code mayReduce} flags may become more effective in some cases if there are
 *      project operators in the plan that project away unnecessary variables early.</li>
 * </ol>
 * Notice that, in the context of the first of these reasons, project operators have no
 * use once projection push down has been completed; they are more like an intermediate
 * artifacts of the project push down process. Yet, keeping them can be useful for the
 * second of the reasons mentioned above. However, if the query does not use DISTINCT,
 * then the second reason is irrelevant; in this case, the project operators should be
 * removed after project push down has been completed.
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

		final LogicalPlan[] newSubPlans = new LogicalPlan[numberOfSubPlans];
		boolean noChanges = true;
		for ( int i = 0; i < numberOfSubPlans; i++ ) {
			final LogicalPlan oldSubPlan = inputPlan.getSubPlan(i);
			newSubPlans[i] = apply(oldSubPlan);

			if ( ! newSubPlans[i].equals(oldSubPlan) ) {
				noChanges = false;
			}
		}

		final LogicalPlan newPlan;
		final LogicalOperator rootOp = inputPlan.getRootOperator();

		if ( rootOp instanceof LogicalOpProject op && !op.mayReduce() )
			return newSubPlans[0];

		if ( noChanges ) {
			newPlan = inputPlan;
		}
		else {
			newPlan = LogicalPlanUtils.createPlanWithSubPlans(
					rootOp,
					null,
					newSubPlans);
		}

		return removeProjectIfNecessary(newPlan);
	}

	protected LogicalPlan removeProjectIfNecessary( final LogicalPlan plan ) {
		if ( plan.getRootOperator() instanceof LogicalOpProject op && ! op.mayReduce() )
			return plan.getSubPlan(0);

		return plan;
	}

}
