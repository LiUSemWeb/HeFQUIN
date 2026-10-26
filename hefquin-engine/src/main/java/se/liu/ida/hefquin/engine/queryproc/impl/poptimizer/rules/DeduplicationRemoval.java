package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.rules;

import se.liu.ida.hefquin.engine.queryplan.physical.DuplicateRelatedBehavior;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpDuplicateRemoval;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.PhysicalOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.RewritingRuleForPhysicalOptimization;

/**
 * A physical plan rewriting rule that removes redundant duplicate-removal
 * operators.
 *
 * <p>A {@link PhysicalOpDuplicateRemoval} is removed if its input plan
 * guarantees a duplicate-free result, as determined by the duplicate-related
 * behaviour of the operators in the plan.
 *
 * <p>The rule traverses the physical plan recursively to identify and remove
 * redundant duplicate-removal operators.
 */
public class DeduplicationRemoval implements RewritingRuleForPhysicalOptimization
{
	/**
	 * Applies this rewriting rule to the given physical plan.
	 *
	 * @param inputPlan the physical plan to rewrite
	 * @param ctxt the context for query processing
	 * @return the rewritten physical plan
	 * @throws PhysicalOptimizationException if an error occurs during physical
	 *         optimization
	 */
	@Override
	public PhysicalPlan apply( final PhysicalPlan inputPlan,
	                           final QueryProcContext ctxt )
			throws PhysicalOptimizationException
	{
		return apply(inputPlan);
	}

	/**
	 * Recursively removes redundant duplicate-removal operators from the given
	 * physical plan.
	 *
	 * <p>A duplicate-removal operator is removed if the plan beneath it
	 * guarantees a duplicate-free result. Otherwise, the operator is retained.
	 *
	 * @param inputPlan the physical plan to rewrite
	 * @return the rewritten physical plan
	 */
	public PhysicalPlan apply( final PhysicalPlan inputPlan ) {
		final int numberOfSubPlans = inputPlan.numberOfSubPlans();
		if ( numberOfSubPlans == 0 ) {
			return inputPlan;
		}

		final PhysicalOperator rootOp = inputPlan.getRootOperator();
		if ( rootOp instanceof PhysicalOpDuplicateRemoval ) {
			final PhysicalPlan subPlan = inputPlan.getSubPlan(0);
			if ( guaranteesDuplicateFreeResult(subPlan) ) {
				return subPlan;
			}
			else {
				return inputPlan;
			}
		}
		else {
			// For any other type of root operator, simply apply
			// this rewriting rule recursively to all of the subplans.
			final PhysicalPlan[] newSubPlans = new PhysicalPlan[numberOfSubPlans];
			boolean noChanges = true; // set to false if the heuristic changes any of the subplans
			for ( int i = 0; i < numberOfSubPlans; i++ ) {
				final PhysicalPlan oldSubPlan = inputPlan.getSubPlan(i);
				newSubPlans[i] = apply(oldSubPlan);
				if ( ! newSubPlans[i].isSamePlan(oldSubPlan) ) {
					noChanges = false;
				}
			}

			if ( noChanges )
				return inputPlan;

			return PhysicalPlanFactory.createPlan(rootOp, null, newSubPlans);
		}
	}

	/**
	 * Determines whether the given physical plan guarantees a duplicate-free
	 * result based on the duplicate-related behaviour of its operators.
	 *
	 * <p>An operator with {@link DuplicateRelatedBehavior#REMOVAL_GUARANTEED}
	 * guarantees a duplicate-free result. An operator with
	 * {@link DuplicateRelatedBehavior#NEUTRAL} guarantees a duplicate-free result
	 * if all its subplans do so. An operator with
	 * {@link DuplicateRelatedBehavior#DUPLICATES_POSSIBLE} does not guarantee a
	 * duplicate-free result.
	 *
	 * @param inputPlan the physical plan to examine
	 * @return {@code true} if the plan guarantees a duplicate-free result;
	 *         {@code false} otherwise
	 */
	protected boolean guaranteesDuplicateFreeResult( final PhysicalPlan inputPlan ) {
		final PhysicalOperator op = inputPlan.getRootOperator();

		switch ( op.getDuplicateRelatedBehavior() ) {
			case REMOVAL_GUARANTEED:
				return true;

			case NEUTRAL:
				final int numberOfSubPlans = inputPlan.numberOfSubPlans();
				for ( int i = 0; i < numberOfSubPlans; i++ ) {
					if ( ! guaranteesDuplicateFreeResult( inputPlan.getSubPlan(i) ) ) {
						return false;
					}
				}
				return true;

			case DUPLICATES_POSSIBLE:
				return false;
		}

		throw new IllegalStateException();
	}
}
