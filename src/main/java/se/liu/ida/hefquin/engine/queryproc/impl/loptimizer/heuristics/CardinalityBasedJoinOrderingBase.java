package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanUtils;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayJoin;
import se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.HeuristicForLogicalOptimization;
import se.liu.ida.hefquin.engine.utils.Pair;

public abstract class CardinalityBasedJoinOrderingBase implements HeuristicForLogicalOptimization
{
	@Override
    public LogicalPlan apply( final LogicalPlan inputPlan ) {
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

	protected void reorder( final LogicalPlan[] plans ) {
		if ( plans.length < 2 ) return;

		final int[] cardinalities = determineCardinalities(plans); 
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

		final AnnotatedLogicalPlan firstPlanA = new AnnotatedLogicalPlan(firstPlan,
		                                                                 firstPlan.getExpectedVariables().getCertainVariables(),
		                                                                 cardinalities[idxOfFirstPlan]);

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
				final AnnotatedLogicalPlan ithPlan = new AnnotatedLogicalPlan(
						plans[i],
						plans[i].getExpectedVariables().getCertainVariables(),
						cardinalities[i]);

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
				joinCardWithNextPlan = determineJoinCardinality(selectedPlans,
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
	 * Assumes that nextCandidates is nonempty.
	 */
	protected Pair<AnnotatedLogicalPlan, Integer> determineNextPlan( final List<AnnotatedLogicalPlan> selectedPlans,
	                                                                 final int joinCardOfSelectedPlans,
	                                                                 final List<AnnotatedLogicalPlan> nextCandidates ) {
		final Iterator<AnnotatedLogicalPlan> it = nextCandidates.iterator();

		AnnotatedLogicalPlan bestCandidate = it.next();
		int joinCardWithBestCandidate = determineJoinCardinality(selectedPlans,
		                                                         joinCardOfSelectedPlans,
		                                                         bestCandidate);

		while ( it.hasNext() ) {
			final AnnotatedLogicalPlan nextCandidate = it.next();
			final int joinCardWithNextCandidate = determineJoinCardinality(selectedPlans,
			                                                               joinCardOfSelectedPlans,
			                                                               nextCandidate);
			if ( joinCardWithBestCandidate > joinCardWithNextCandidate ) {
				bestCandidate = nextCandidate;
				joinCardWithBestCandidate = joinCardWithNextCandidate;
			}
		}

		return new Pair<>(bestCandidate, joinCardWithBestCandidate);
	}

	protected abstract int[] determineCardinalities( final LogicalPlan[] plans );

	protected abstract int determineJoinCardinality( List<AnnotatedLogicalPlan> selectedPlans,
	                                                 int joinCardOfSelectedPlans,
	                                                 AnnotatedLogicalPlan nextCandidate );


	protected static class AnnotatedLogicalPlan {
		public final LogicalPlan plan;
		public final Set<Var> potentialJoinVars;
		public final int cardinality;

		public AnnotatedLogicalPlan( final LogicalPlan plan, final Set<Var> potentialJoinVars, final int card ) {
			this.plan = plan;
			this.potentialJoinVars = potentialJoinVars;
			this.cardinality = card;
		}
	}

}
