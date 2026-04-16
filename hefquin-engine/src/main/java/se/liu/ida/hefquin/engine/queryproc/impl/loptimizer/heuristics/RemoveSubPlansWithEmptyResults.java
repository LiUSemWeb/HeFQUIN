package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics;

import static se.liu.ida.hefquin.engine.queryplan.info.QueryPlanProperty.CARDINALITY;

import java.util.ArrayList;
import java.util.List;

import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanProperty;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanProperty.Quality;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanUtils;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpLeftJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayUnion;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpUnion;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithoutResult;
import se.liu.ida.hefquin.engine.queryproc.CardinalityEstimator;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;
import se.liu.ida.hefquin.engine.queryproc.impl.cardinality.RequestBasedCardinalityEstimator;
import se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.HeuristicForLogicalOptimization;

/**
 * Removes every subplan that is guaranteed to produce an empty result
 * according to the cardinality estimate that is determined for it. In
 * particular, a subplan is removed if it has a cardinality estimate of
 * 0 with the quality score being ACCURATE. The subplans are removed in
 * a top-down manner (i.e., trying to remove bigger subplans first).
 * <p>
 * The heuristic performs a recursive traversal of the logical plan and simplifies
 * the plan based on emptiness information:
 * <ul>
 * <li>If the overall plan itself is provably empty, it is replaced by
 *   {@link LogicalPlanWithoutResult}.</li>
 * <li>For union and multiway union operators, empty subplans are removed.
 *   If exactly one subplan remains, the union operator is removed and
 *   replaced by that subplan.</li>
 * <li>For left join operators, if the right subplan is empty, the left join is
 *   removed and replaced by its left subplan.</li>
 * <li>For all other operators, empty subplans are not removed individually.
 * Instead, emptiness is propagated via cardinality estimates, allowing
 * higher-level simplifications when possible.</li>
 * </ul>
 * The heuristic relies on precomputed cardinality estimates provided by
 * the {@link CardinalityEstimator}.
 */
public class RemoveSubPlansWithEmptyResults implements HeuristicForLogicalOptimization
{
	protected final CardinalityEstimator cardEst;

	public RemoveSubPlansWithEmptyResults( final QueryProcContext ctxt ) {
		this( new RequestBasedCardinalityEstimator( ctxt.getFederationAccessMgr() ) );
	}

	public RemoveSubPlansWithEmptyResults( final CardinalityEstimator cardEst ) {
		assert cardEst != null;
		this.cardEst = cardEst;
	}

	@Override
	public LogicalPlan apply( final LogicalPlan inputPlan ) {
		// Ensure all subplans have up-to-date cardinality estimates before rewriting.
		cardEst.addCardinalities(inputPlan);

		return _apply(inputPlan);
	}

	protected LogicalPlan _apply( final LogicalPlan inputPlan ) {
		// Top-down pruning step:
		// If the current subplan is guaranteed to produce an empty result,
		// replace it immediately and do not recurse into its children.
		if ( isProvablyEmpty(inputPlan) )
			return LogicalPlanWithoutResult.getInstance();

		// Process the current node first; only then recurse into its subplans if needed.
		final LogicalOperator op = inputPlan.getRootOperator();
		final int numberOfSubPlans = inputPlan.numberOfSubPlans();
		final List<LogicalPlan> rewrittenSubPlans = new ArrayList<>(numberOfSubPlans);

		// For left join operators, empty right collapses to left.
		if ( op instanceof LogicalOpLeftJoin ) {
			if ( isProvablyEmpty(inputPlan.getSubPlan(1)) )
				return inputPlan.getSubPlan(0); // if the right subplan was pruned, return the left subplan
		}

		for ( int i = 0; i < numberOfSubPlans; i++ ) {
			LogicalPlan child = inputPlan.getSubPlan(i);

			// Skip recursion into subplans that are already known to be empty.
			if ( isProvablyEmpty(child) ) {
				continue;
			}

			rewrittenSubPlans.add(_apply(child));
		}

		// For union operators, remove empty subplans and collapse if only one remains.
		if ( op instanceof LogicalOpUnion || op instanceof LogicalOpMultiwayUnion ) {
			if ( rewrittenSubPlans.size() == 1 )
				return rewrittenSubPlans.get(0);
			else if ( rewrittenSubPlans.size() > 1 ){
				return LogicalPlanUtils.createPlanWithSubPlans( op, null, rewrittenSubPlans );
			}
			else
				throw new IllegalStateException("All subplans of a union operator were removed, but the overall plan is not empty. This should not happen if cardinality estimates are correct.");
		}


		// Rebuild the plan with rewritten subplans, preserving operator and properties.
		return LogicalPlanUtils.createPlanWithSubPlans( op,
		                                                inputPlan.getQueryPlanningInfo().getProperties(),
		                                                rewrittenSubPlans );
	}

	protected boolean isProvablyEmpty ( final LogicalPlan plan ) {
		// Returns true if the plan is guaranteed to produce an empty result
		// according to the cardinality estimate that is determined for it.
		final QueryPlanningInfo info = plan.getQueryPlanningInfo();
		if (info == null) return false;

		final QueryPlanProperty crd = info.getProperty(CARDINALITY);
		return crd != null
			&& crd.getValue() == 0
			&& crd.getQuality() == Quality.ACCURATE;
	}
}
