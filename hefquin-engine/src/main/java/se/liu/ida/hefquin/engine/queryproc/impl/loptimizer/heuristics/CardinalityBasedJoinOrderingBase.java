package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.base.utils.Pair;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanUtils;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayJoin;
import se.liu.ida.hefquin.engine.queryproc.LogicalOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.HeuristicForLogicalOptimization;

/**
 * Base class for heuristics that use cardinality estimates to decide on
 * a join order for the subplans of a multiway join or a binary join. The
 * heuristic is applied recursively to all joins within the given plan.
 *
 * The actual join ordering algorithm implemented in this class is a greedy
 * one. It first determines cardinality estimates for all the subplans of
 * the corresponding join. Given these estimates, the algorithm picks the
 * subplan with the lowest cardinality estimate to become the first plan
 * in the join order. Thereafter, the algorithm performs an iteration where
 * each step picks the next subplan for the join. This decision is based on
 * estimated join cardinalities. However, as candidates to be picked as the
 * next subplan, the algorithm considers only the subplans that have a join
 * variable with the subplans that have already been selected so far, which
 * is done to avoid cross products (unless a cross product is unavoidable).
 *
 * The actual cardinality estimation approach, both for the cardinalities
 * of the subplans and for the join cardinalities, may be different for each
 * subclass.
 */
public abstract class CardinalityBasedJoinOrderingBase implements HeuristicForLogicalOptimization
{
	@Override
    public LogicalPlan apply( final LogicalPlan inputPlan ) throws LogicalOptimizationException {
		final int numberOfSubPlans = inputPlan.numberOfSubPlans();
		if ( numberOfSubPlans == 0 ) {
			return inputPlan;
		}

		// recursively apply the join ordering to the subplans first
		final LogicalPlan[] newSubPlans = new LogicalPlan[numberOfSubPlans];
		boolean noChanges = true; // set to false if any of the subplans is changed
		for ( int i = 0; i < numberOfSubPlans; i++ ) {
			final LogicalPlan oldSubPlan = inputPlan.getSubPlan(i);
			newSubPlans[i] = apply(oldSubPlan);
			if ( ! newSubPlans[i].equals(oldSubPlan) ) {
				noChanges = false;
			}
		}

		// reorder the subplans if the root of the given plan is a join
		final LogicalOperator rootOp = inputPlan.getRootOperator();
		if ( rootOp instanceof LogicalOpJoin || rootOp instanceof LogicalOpMultiwayJoin ) {
			noChanges = false;
			reorder(newSubPlans);
		}

		if ( noChanges )
			return inputPlan;
		else
			return LogicalPlanUtils.createPlanWithSubPlans(rootOp, newSubPlans);
	}

	/**
	 * Changes the order of the subplans in the given array by using the
	 * greedy algorithm described above for this class.
	 */
	protected void reorder( final LogicalPlan[] plans ) throws LogicalOptimizationException {
		if ( plans.length < 2 ) return;

		// Determines cardinality estimates for all the subplans.
		final int[] cardinalities = estimateCardinalities(plans);

		// Pick the first subplan based on the cardinality estimates.
		final int idxOfFirstPlan = determineLowestValue(cardinalities);
		final LogicalPlan firstPlan = plans[idxOfFirstPlan];

		// If there are only two subplans, we do not need to execute the
		// iterative algorithm that follows below after the if block.
		if ( plans.length == 2 ) {
			if ( idxOfFirstPlan == 1 ) {
				final LogicalPlan secondPlan = plans[0];
				plans[0] = firstPlan;
				plans[1] = secondPlan;
			}
			return;
		}

		final AnnotatedLogicalPlan firstPlanA = new AnnotatedLogicalPlan( firstPlan,
		                                                                  cardinalities[idxOfFirstPlan] );

		// Create a list for collecting the subplans that can be joined with
		// the subplans selected so far.
		final List<AnnotatedLogicalPlan> nextCandidates = new ArrayList<>();

		// Create a list for collecting the remaining subplans (which do not
		// have variables in common with the subplans selected so far and,
		// thus, cannot reasonably be joined with them). We use a linked
		// list which makes it easy to simply pop a next element from it
		// when needed.
		final LinkedList<AnnotatedLogicalPlan> remainingPlans = new LinkedList<>();

		// Populate these two lists now.
		for ( int i = 0; i < plans.length; i++ ) {
			if ( i != idxOfFirstPlan ) {
				final AnnotatedLogicalPlan ithPlan = new AnnotatedLogicalPlan( plans[i],
				                                                               cardinalities[i] );

				if ( Collections.disjoint(ithPlan.potentialJoinVars, firstPlanA.potentialJoinVars) )
					remainingPlans.add(ithPlan);
				else
					nextCandidates.add(ithPlan);
			}
		}

		// Create an array for collecting the subplans selected so far, and
		// initialize it with the first subplan as selected above.
		final List<AnnotatedLogicalPlan> selectedPlans = new ArrayList<>(plans.length);
		selectedPlans.add(firstPlanA);

		// the estimated cardinality of the result of joining the results of the subplans selected so far  
		int joinCardOfSelectedPlans = cardinalities[idxOfFirstPlan];

		// Now we are ready to start an iteration in which each step selects a next subplan.
		for ( int i = 1; i < plans.length; i++ ) {
			// Determine the next subplan.
			final AnnotatedLogicalPlan nextPlan;
			final int joinCardWithNextPlan;
			if ( nextCandidates.isEmpty() ) {
				// If no more candidates are available that can be joined with
				// the subplans selected so far, resort to pick one of the
				// remaining subplans (which will result in a cross product).
				nextPlan = remainingPlans.pop();
				joinCardWithNextPlan = estimateJoinCardinality(selectedPlans,
				                                                joinCardOfSelectedPlans,
				                                                nextPlan);
			}
			else {
				final Pair<AnnotatedLogicalPlan, Integer> nextWithJoinCard =
						determineNextPlan(selectedPlans, joinCardOfSelectedPlans, nextCandidates);

				nextPlan = nextWithJoinCard.object1;
				nextCandidates.remove(nextPlan);

				joinCardWithNextPlan = nextWithJoinCard.object2;
			}

			// Add the next plan to the selected ones.
			selectedPlans.add(nextPlan);
			joinCardOfSelectedPlans = joinCardWithNextPlan;

			// Check whether any of the remaining subplans become candidates.
			if ( ! remainingPlans.isEmpty() ) {
				final List<AnnotatedLogicalPlan> tmp = new ArrayList<>( remainingPlans.size() );
				for ( final AnnotatedLogicalPlan p : remainingPlans ) {
					if ( ! Collections.disjoint(p.potentialJoinVars, nextPlan.potentialJoinVars) ) {
						tmp.add(p);
					}
				}

				for ( final AnnotatedLogicalPlan p : tmp ) {
					nextCandidates.add(p);
					remainingPlans.remove(p);
				}				
			}
		}

		// Finally, update the given array by using the new ordering.
		for ( int i = 0; i < plans.length; i++ ) {
			plans[i] = selectedPlans.get(i).plan;
		}
	}

	/**
	 * Determines the smallest of the values in the given array and,
	 * then, returns the position of this value in the given array.
	 */
	protected int determineLowestValue( final int[] values ) {
		int idxOfLowestValue = 0;
		int lowestValue = values[0];
		for ( int i = 1; i < values.length; i++ ) {
			if ( values[i] < lowestValue ) {
				lowestValue = values[i];
				idxOfLowestValue = i;
			}
		}

		return idxOfLowestValue;
	}

	/**
	 * Returns the plan from the given list of next candidates that has the
	 * lowest join cardinality with a plan consisting of the given selected
	 * plans, and that join cardinality will be returned as well.
	 *
	 * The implementation of this function assumes that the given list of
	 * next candidates is nonempty.
	 */
	protected Pair<AnnotatedLogicalPlan, Integer> determineNextPlan( final List<AnnotatedLogicalPlan> selectedPlans,
	                                                                 final int joinCardOfSelectedPlans,
	                                                                 final List<AnnotatedLogicalPlan> nextCandidates ) throws LogicalOptimizationException {
		final Iterator<AnnotatedLogicalPlan> it = nextCandidates.iterator();

		// Initially, assumes that the first of the given candidates is the best one.
		AnnotatedLogicalPlan bestCandidate = it.next();
		int joinCardWithBestCandidate = estimateJoinCardinality(selectedPlans,
		                                                         joinCardOfSelectedPlans,
		                                                         bestCandidate);

		// Iterate of the rest of the given candidates in order to find the best one.
		while ( it.hasNext() ) {
			final AnnotatedLogicalPlan nextCandidate = it.next();
			final int joinCardWithNextCandidate = estimateJoinCardinality(selectedPlans,
			                                                               joinCardOfSelectedPlans,
			                                                               nextCandidate);
			if ( joinCardWithBestCandidate > joinCardWithNextCandidate ) {
				bestCandidate = nextCandidate;
				joinCardWithBestCandidate = joinCardWithNextCandidate;
			}
		}

		return new Pair<>(bestCandidate, joinCardWithBestCandidate);
	}

	/**
	 * Implementations of this function determine or estimate the cardinality
	 * of each of the given plans. That is, the returned array contains as many
	 * entries as there are plans in the given array such that the i-th entry
	 * in the returned array is the cardinality of the i-th plan.
	 */
	protected abstract int[] estimateCardinalities( final LogicalPlan[] plans ) throws LogicalOptimizationException;

	/**
	 * Implementations of this function estimate the cardinality of the join
	 * between the result of joining the given selected plans and the result
	 * of the given next candidate. Implementations that require the individual
	 * cardinality estimates for each of the plans can get these estimates by
	 * accessing {@link AnnotatedLogicalPlan#cardinality}. The estimated join
	 * cardinality of joining the given selected plans is also passed to this
	 * function, in case it is needed by some implementation.
	 */
	protected abstract int estimateJoinCardinality( List<AnnotatedLogicalPlan> selectedPlans,
	                                                int joinCardOfSelectedPlans,
	                                                AnnotatedLogicalPlan nextCandidate ) throws LogicalOptimizationException;


	/**
	 * A help class that wraps a {@link LogicalPlan} together with some
	 * information about this plan that is relevant for the algorithm of
	 * the main class ({@link CardinalityBasedJoinOrderingBase} and that
	 * may be relevant for implementations of the abstract functions.
	 */
	protected static class AnnotatedLogicalPlan {
		/**
		 * The wrapped {@link LogicalPlan}.
		 */
		public final LogicalPlan plan;

		/**
		 * The set of certain variables of {@link #plan}; shortcut for
		 * calling {@link ExpectedVariables#getCertainVariables()}) on
		 * {@link LogicalPlan#getExpectedVariables()}.
		 */
		public final Set<Var> potentialJoinVars;

		/**
		 * Result cardinality as has been estimated for {@link #plan}.
		 */
		public final int cardinality;

		public AnnotatedLogicalPlan( final LogicalPlan plan, final int card ) {
			this.plan = plan;
			this.potentialJoinVars = plan.getExpectedVariables().getCertainVariables();
			this.cardinality = card;
		}
	}

}
