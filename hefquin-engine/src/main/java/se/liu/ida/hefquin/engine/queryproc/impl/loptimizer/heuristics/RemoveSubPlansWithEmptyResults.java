package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics;

import static se.liu.ida.hefquin.engine.queryplan.info.QueryPlanProperty.CARDINALITY;

import java.util.ArrayList;
import java.util.List;

import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanProperty;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanProperty.Quality;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanUtils;
import se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.HeuristicForLogicalOptimization;

/**
 * Recursively removes subplans that are guaranteed to produce
 * an empty result. A subplan can only be removed if it has a cardinality
 * estimate of 0 with 'accurate' as its quality score.
 */
public class RemoveSubPlansWithEmptyResults implements HeuristicForLogicalOptimization
{

	@Override
	public LogicalPlan apply( final LogicalPlan inputPlan ) {
		final int numberOfSubPlans = inputPlan.numberOfSubPlans();
		// if ( numberOfSubPlans == 0 ) {
		// 	return inputPlan;
		// }

		// First, apply the heuristic recursively to all subplans.
		final List<LogicalPlan> rewrittenSubPlans = new ArrayList<>(numberOfSubPlans);

		for ( int i = 0; i < numberOfSubPlans; i++ ) {
			final LogicalPlan subPlan = inputPlan.getSubPlan(i);
			rewrittenSubPlans.add(apply(subPlan));
		}

		// Next, remove empty subplans
		final List<LogicalPlan> filteredSubs = new ArrayList<>();

		for ( final LogicalPlan sub : rewrittenSubPlans ) {
			final QueryPlanningInfo qpInfo = sub.getQueryPlanningInfo();
			final QueryPlanProperty crd = qpInfo.getProperty(CARDINALITY);

			boolean isEmpty = crd != null
			               && crd.getValue() == 0
			               && crd.getQuality() == Quality.ACCURATE;

			if ( ! isEmpty ) filteredSubs.add(sub);
		}

		// If all subplans are removed, the whole plan is empty
		if ( numberOfSubPlans > 0 && filteredSubs.isEmpty() )
			// return emptyPlan;

		// If subplans have been removed but the whole plan is not empty
		// we want to rebuild the plan.
		if ( filteredSubs.size() != numberOfSubPlans )
			return LogicalPlanUtils.createPlanWithSubPlans( inputPlan.getRootOperator(),
			                                                null,
			                                                filteredSubs );

		return inputPlan;
	}

}
