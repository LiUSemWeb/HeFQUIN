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
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMinus;
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
 * 0 with the quality score being {@link Quality#ACCURATE}. The subplans
 * are removed in a top-down manner (i.e., trying to remove bigger subplans
 * first).
 * <p>
 * The heuristic performs a recursive traversal of the logical plan and simplifies
 * the plan based on emptiness information:
 * <ul>
 *   <li>
 *   If the overall plan itself is provably empty, it is replaced by
 *   {@link LogicalPlanWithoutResult}.
 *   </li>
 *   <li>
 *   For union and multiway union operators, empty subplans are removed.
 *   If exactly one subplan remains, the union operator is removed and
 *   replaced by that subplan.
 *   </li>
 *   <li>
 *   For left join operators, if the right subplan is empty, the left
 *   join is removed and replaced by its left subplan.
 *   </li>
 *   <li>
 *   For all other operators, empty subplans are not removed individually.
 *   Instead, emptiness is propagated via cardinality estimates, allowing
 *   higher-level simplifications when possible.
 *   </li>
 * </ul>
 *
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
		// Ensure that all subplans have an up-to-date
		// cardinality estimate before rewriting.
		cardEst.addCardinalities(inputPlan);

		return _apply(inputPlan);
	}

	protected LogicalPlan _apply( final LogicalPlan inputPlan ) {
		// Top-down pruning step:
		// If the given plan is guaranteed to produce an empty result,
		// replace it immediately and do not recurse into its children.
		if ( isProvablyEmpty(inputPlan) )
			return LogicalPlanWithoutResult.getInstance();

		// Now consider the special case of a plan with a left join operator
		// or a minus operator as root and the second subplan
		// (i.e., the one that captures the optional part) is guaranteed
		// to produce an empty result. In this case, the left-join operator
		// and the second subplan can be removed; in other words, the given
		// plan can be replaced by the first subplan under the left-join
		// operator (i.e., the non-optional part).
		final LogicalOperator rootOp = inputPlan.getRootOperator();
		if (   (rootOp instanceof LogicalOpLeftJoin
		     || rootOp instanceof LogicalOpMinus)
		     && isProvablyEmpty(inputPlan.getSubPlan(1)) ) {
			return inputPlan.getSubPlan(0);
		}

		// Now we consider the case of a plan that does not have any subplan.
		// For such a plan, there is no rewriting possible/necessary.
		final int numberOfSubPlans = inputPlan.numberOfSubPlans();
		if ( numberOfSubPlans == 0 )
			return inputPlan;

		// For the other cases, we first collect rewritten versions of all
		// subplans of the given plan. During this step, we already skip
		// subplans that are guaranteed to produce the empty result (yet,
		// by the way the cardinality estimates have been propagated from
		// subplans to their parent plans, we would have such subplans to
		// be skipped only in case the root operator is a union).
		final List<LogicalPlan> rewrittenSubPlans = new ArrayList<>(numberOfSubPlans);
		for ( int i = 0; i < numberOfSubPlans; i++ ) {
			final LogicalPlan child = inputPlan.getSubPlan(i);

			// Skip recursion into subplans that are already known to be empty.
			if ( isProvablyEmpty(child) ) {
				continue;
			}

			rewrittenSubPlans.add( _apply(child) );
		}

		// Now consider the case in which the given plan has a union as its
		// root operator. In this case, we remove the subplans that are
		// guaranteed to produce the empty result (which we do implicitly
		// by using the rewritten subplans from the previous step, where
		// we already skipped the subplans to be removed). Moreover, if
		// if there is only one subplan left, we can replace the given
		// union plan by that subplan (i.e., removing the union operator
		// altogether).
		// If more than one subplan remains, the union structure is preserved
		// and the original query planning info can be retained because the
		// transformation only removes provably empty alternatives.
		if ( rootOp instanceof LogicalOpUnion || rootOp instanceof LogicalOpMultiwayUnion ) {
			if ( rewrittenSubPlans.size() == 1 )
				return rewrittenSubPlans.get(0); // remove union altogether

			if ( rewrittenSubPlans.size() > 1 )
				return LogicalPlanUtils.createPlanWithSubPlans(rootOp, inputPlan.getQueryPlanningInfo().getProperties(), rewrittenSubPlans);

			// We should never end up here.
			throw new IllegalStateException("All subplans of a union operator were removed, but the overall plan is not empty. This should not happen if cardinality estimates are correct.");
		}

		// Finally, for all remaining cases, we simply rebuild the given
		// plan by using the rewritten subplans, and preserving its root
		// operator and its query planning info properties.
		return LogicalPlanUtils.createPlanWithSubPlans( rootOp,
		                                                inputPlan.getQueryPlanningInfo().getProperties(),
		                                                rewrittenSubPlans );
	}

	/**
	 * Returns {@code true} if, according to the cardinality estimate that
	 * has been determined for the given plan, the plan is guaranteed to
	 * produce an empty result.
	 */
	protected boolean isProvablyEmpty ( final LogicalPlan plan ) {
		final QueryPlanningInfo info = plan.getQueryPlanningInfo();
		if ( info == null ) return false;

		final QueryPlanProperty crd = info.getProperty(CARDINALITY);
		return crd != null
		    && crd.getValue() == 0
		    && crd.getQuality() == Quality.ACCURATE;
	}
}
