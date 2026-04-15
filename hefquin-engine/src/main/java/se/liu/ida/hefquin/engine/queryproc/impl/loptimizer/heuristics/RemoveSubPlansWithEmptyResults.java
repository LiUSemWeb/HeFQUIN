package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics;

import static se.liu.ida.hefquin.engine.queryplan.info.QueryPlanProperty.CARDINALITY;

import java.util.ArrayList;
import java.util.List;

import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanProperty;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanProperty.Quality;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanUtils;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpLeftJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayUnion;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpUnion;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithoutResult;
import se.liu.ida.hefquin.engine.queryproc.impl.cardinality.CardinalityEstimationWorkerImpl;
import se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.HeuristicForLogicalOptimization;

/**
 * Recursively removes subplans that are guaranteed to produce empty results.
 * A subplan is considered empty only if it has a cardinality estimate of 0
 * with ACCURATE quality.
 *
 * The heuristic first ensures that all subplans are annotated with
 * cardinality estimates. It then traverses the plan bottom-up and removes
 * subplans that are guaranteed to be empty.
 *
 * For union and multiway union operators, empty subplans are removed. If all
 * subplans are removed, the entire plan is replaced with a
 * {@link LogicalPlanWithoutResult}. If exactly one subplan remains, the union
 * operator is removed and replaced by that subplan.
 *
 * For left join operators: if the left subplan is empty, the entire result is empty.
 * If the right subplan is empty, the left join is removed and replaced by its left subplan.
 *
 * For all other operators, empty subplans are not removed individually.
 * Instead, emptiness is propagated via cardinality estimates, meaning that
 * if an operator is guaranteed to produce an empty result, the entire plan
 * rooted at that operator is replaced with a {@link LogicalPlanWithoutResult}.
 */
public class RemoveSubPlansWithEmptyResults implements HeuristicForLogicalOptimization
{
	protected final CardinalityEstimationWorkerImpl cardEst = new CardinalityEstimationWorkerImpl();

	@Override
	public LogicalPlan apply( final LogicalPlan inputPlan ) {
		// Ensure all subplans have up-to-date cardinality estimates before rewriting.
		cardEst.addCardinalities(inputPlan);

		// If the entire plan is guaranteed empty, replace it immediately.
		final QueryPlanningInfo qpInfo = inputPlan.getQueryPlanningInfo();
		if (qpInfo != null) {
			final QueryPlanProperty crd = qpInfo.getProperty(CARDINALITY);
			if ( crd != null
			  && crd.getValue() == 0
			  && crd.getQuality() == Quality.ACCURATE ) {
				return LogicalPlanWithoutResult.getInstance();
			}
		}

		// Recursively rewrite all subplans before handling the current operator.
		final int numberOfSubPlans = inputPlan.numberOfSubPlans();
		final List<LogicalPlan> rewrittenSubPlans = new ArrayList<>(numberOfSubPlans);

		for ( int i = 0; i < numberOfSubPlans; i++ ) {
			rewrittenSubPlans.add(apply(inputPlan.getSubPlan(i)));
		}

		// For union operators, remove empty subplans and collapse if only one remains.
		if ( inputPlan.getRootOperator() instanceof LogicalOpUnion
		  || inputPlan.getRootOperator() instanceof LogicalOpMultiwayUnion ) {
			final List<LogicalPlan> filteredSubs = new ArrayList<>();

			for ( final LogicalPlan sub : rewrittenSubPlans ) {
				if ( ! isEmpty(sub) ) filteredSubs.add(sub);
			}

			if ( filteredSubs.size() != numberOfSubPlans ) {
				if ( filteredSubs.size() == 1 )
					return filteredSubs.get(0);
				else {
					return LogicalPlanUtils.createPlanWithSubPlans( inputPlan.getRootOperator(),
					null,
					filteredSubs );
				}
			}
		}

		// For left join operators, empty left yields empty result,
		// empty right collapses to left.
		if ( inputPlan.getRootOperator() instanceof LogicalOpLeftJoin ) {
			final LogicalPlan left = rewrittenSubPlans.get(0);
			final LogicalPlan right = rewrittenSubPlans.get(1);

			if ( isEmpty(left) ) return LogicalPlanWithoutResult.getInstance();

			if ( isEmpty(right) ) return left;
		}

		// Rebuild the plan with rewritten subplans, preserving operator and properties.
		return LogicalPlanUtils.createPlanWithSubPlans( inputPlan.getRootOperator(),
		                                                inputPlan.getQueryPlanningInfo().getProperties(),
		                                                rewrittenSubPlans );
	}

	protected boolean isEmpty ( final LogicalPlan plan ) {
		// Returns true if the plan is guaranteed empty
		// (either explicit or via accurate cardinality 0).
		if ( plan instanceof LogicalPlanWithoutResult ) return true;

		QueryPlanningInfo info = plan.getQueryPlanningInfo();
		if (info == null) return false;

		QueryPlanProperty crd = info.getProperty(CARDINALITY);
		return crd != null
			&& crd.getValue() == 0
			&& crd.getQuality() == Quality.ACCURATE;
	}
}
