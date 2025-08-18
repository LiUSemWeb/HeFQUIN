package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.base.utils.Pair;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanProperty;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanProperty.Quality;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanUtils;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayJoin;
import se.liu.ida.hefquin.engine.queryproc.CardinalityEstimator;
import se.liu.ida.hefquin.engine.queryproc.LogicalOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;
import se.liu.ida.hefquin.engine.queryproc.impl.cardinality.RequestBasedCardinalityEstimator;
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
// TODO: change the implementation of this class such that it does not rely
// on the (abstract) 'estimateJoinCardinality' function but, instead, uses
// the CardinalityEstimator for this functionality; CardinalityEstimator
// may have to be extended for this purpose. Also, instead of creating the
// CardinalityEstimator here, it should be provided via the QueryProcContext.
public abstract class CardinalityBasedJoinOrderingBase implements HeuristicForLogicalOptimization
{
	protected final CardinalityEstimator cardEst;

	public CardinalityBasedJoinOrderingBase( final QueryProcContext ctxt ) {
		this( new RequestBasedCardinalityEstimator( ctxt.getFederationAccessMgr() ) );
	}

	public CardinalityBasedJoinOrderingBase( final CardinalityEstimator cardEst ) {
		assert cardEst != null;
		this.cardEst = cardEst;
	}

	@Override
	public LogicalPlan apply( final LogicalPlan inputPlan )
			throws LogicalOptimizationException
	{
		// If the given plan does not have any subplans, there is nothing to do.
		if ( inputPlan.numberOfSubPlans() == 0 ) return inputPlan;

		// If the given plan does not have any LogicalOpJoin or LogicalOpMultiwayJoin, no need to do anything.
		if( ! containsJoinOp(inputPlan) ) return inputPlan;

		// As a first step, we make sure that all subplans within the given
		// plan are annotated with their respective cardinality estimates.
		cardEst.addCardinalities(inputPlan);

		// Now we apply the join ordering for all joins within the given
		// plan, for which we use a recursive function.
		return _apply(inputPlan);
	}

	protected LogicalPlan _apply( final LogicalPlan inputPlan )
		throws LogicalOptimizationException
	{
		final int numberOfSubPlans = inputPlan.numberOfSubPlans();

		// recursively apply the join ordering to the subplans first
		final LogicalPlan[] newSubPlans = new LogicalPlan[numberOfSubPlans];
		boolean noChanges = true; // set to false if any of the subplans is changed
		for ( int i = 0; i < numberOfSubPlans; i++ ) {
			final LogicalPlan oldSubPlan = inputPlan.getSubPlan(i);
			newSubPlans[i] = _apply(oldSubPlan);
			if ( ! newSubPlans[i].equals(oldSubPlan) ) {
				noChanges = false;
			}
		}

		// reorder the subplans if the root of the given plan is a join
		final LogicalOperator rootOp = inputPlan.getRootOperator();
		if ( rootOp instanceof LogicalOpJoin || rootOp instanceof LogicalOpMultiwayJoin ) {
// TODO: reorder(..) should return a boolean that indicates whether
// the order has indeed changed, and this can then be used here to
// set noChanges to false if there was a change
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

		// Pick the first subplan based on the cardinality estimates.
		final int idxOfFirstPlan = determineIdxOfSmallestCardinality(plans);
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

		final Set<Var> potentialJoinVars = new HashSet<>( firstPlan.getExpectedVariables().getCertainVariables() );

		// Create a list for collecting the subplans that can be joined with
		// the subplans selected so far.
		final List<LogicalPlan> nextCandidates = new ArrayList<>();

		// Create a list for collecting the remaining subplans (which do not
		// have variables in common with the subplans selected so far and,
		// thus, cannot reasonably be joined with them). We use a linked
		// list which makes it easy to simply pop a next element from it
		// when needed.
		final LinkedList<LogicalPlan> remainingPlans = new LinkedList<>();

		// Populate these two lists now.
		for ( int i = 0; i < plans.length; i++ ) {
			if ( i != idxOfFirstPlan ) {
				final LogicalPlan ithPlan = plans[i];
				final Set<Var> ithCertainVars = ithPlan.getExpectedVariables().getCertainVariables();

				if ( Collections.disjoint(ithCertainVars, potentialJoinVars) )
					remainingPlans.add(ithPlan);
				else
					nextCandidates.add(ithPlan);
			}
		}

		// Create an array for collecting the subplans selected so far, and
		// initialize it with the first subplan as selected above.
		final List<LogicalPlan> selectedPlans = new ArrayList<>(plans.length);
		selectedPlans.add(firstPlan);

		// the estimated cardinality of the result of joining the results of the subplans selected so far  
		int joinCardOfSelectedPlans = firstPlan.getQueryPlanningInfo().getProperty( QueryPlanProperty.CARDINALITY ).getValue();

		// Now we are ready to start an iteration in which each step
		// selects a next subplan.
		while ( ! nextCandidates.isEmpty() || ! remainingPlans.isEmpty() ) {
			// Determine the next subplan.
			final LogicalPlan nextPlan;
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
				final Pair<LogicalPlan, Integer> nextWithJoinCard =
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
				final List<LogicalPlan> tmp = new ArrayList<>( remainingPlans.size() );
				for ( final LogicalPlan p : remainingPlans ) {
					if ( ! Collections.disjoint(p.getExpectedVariables().getCertainVariables(),
					                            nextPlan.getExpectedVariables().getCertainVariables()) ) {
						tmp.add(p);
					}
				}

				for ( final LogicalPlan p : tmp ) {
					nextCandidates.add(p);
					remainingPlans.remove(p);
				}
			}
		}

		// Finally, update the given array by using the new ordering.
		for ( int i = 0; i < plans.length; i++ ) {
			plans[i] = selectedPlans.get(i);
		}
	}

	/**
	 * Compares the given plans in terms of their estimated cardinalities and,
	 * for the plan that has the smallest estimated cardinality, returns the
	 * index of this plan within the given list.
	 */
	protected int determineIdxOfSmallestCardinality( final LogicalPlan[] plans ) {
		// TODO: The implementation here simply considers the estimated
		// cardinalities, and uses the quality scores associated with
		// these estimates only as tie breaker. It may be smarter to
		// take the quality scores also into account for cases in which
		// the estimated cardinalities of two plans are close (and small).
		int idxOfSmallestSeenCardinality = 0;
		int smallestSeenCardinality = Integer.MAX_VALUE;
		Quality qtyOfSmallestSeenCardinality = Quality.PURE_GUESS;

		for ( int i = 0; i < plans.length; i++ ) {
			final QueryPlanningInfo qpInfo = plans[i].getQueryPlanningInfo();
			final QueryPlanProperty crd = qpInfo.getProperty( QueryPlanProperty.CARDINALITY );
			if ( crd.getValue() < smallestSeenCardinality ) {
				smallestSeenCardinality = crd.getValue();
				qtyOfSmallestSeenCardinality = crd.getQuality();
				idxOfSmallestSeenCardinality = i;
			}
			if (    crd.getValue() == smallestSeenCardinality 
			     && crd.getQuality().higherThan(qtyOfSmallestSeenCardinality) ) {
				smallestSeenCardinality = crd.getValue();
				qtyOfSmallestSeenCardinality = crd.getQuality();
				idxOfSmallestSeenCardinality = i;
			}
		}

		return idxOfSmallestSeenCardinality;
	}

	/**
	 * Returns the plan from the given list of next candidates that has the
	 * lowest join cardinality with a plan consisting of the given selected
	 * plans, and that join cardinality will be returned as well.
	 *
	 * The implementation of this function assumes that the given list of
	 * next candidates is nonempty.
	 */
	protected Pair<LogicalPlan, Integer> determineNextPlan( final List<LogicalPlan> selectedPlans,
	                                                        final int joinCardOfSelectedPlans,
	                                                        final List<LogicalPlan> nextCandidates ) {
		final Iterator<LogicalPlan> it = nextCandidates.iterator();

		// Initially, assumes that the first of the given candidates is the best one.
		LogicalPlan bestCandidate = it.next();
		int joinCardWithBestCandidate = estimateJoinCardinality(selectedPlans,
		                                                        joinCardOfSelectedPlans,
		                                                        bestCandidate);

		// Iterate over the rest of the given candidates to find the best one.
		while ( it.hasNext() ) {
			final LogicalPlan nextCandidate = it.next();
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
	 * Implementations of this function estimate the cardinality of the join
	 * between the result of joining the given selected plans and the result
	 * of the given next candidate. Implementations that require the individual
	 * cardinality estimates for each of the plans can get these estimates by
	 * accessing {@link LogicalPlan#getQueryPlanningInfo()}. The estimated join
	 * cardinality of joining the given selected plans is also passed to this
	 * function, in case it is needed by some implementation.
	 */
	protected abstract int estimateJoinCardinality( List<LogicalPlan> selectedPlans,
	                                                int joinCardOfSelectedPlans,
	                                                LogicalPlan nextCandidate );

	/**
	 * Recursively checks whether the given logical plan contains any join
	 * operators (LogicalOpJoin or LogicalOpMultiwayJoin).
	 *
	 * <p>
	 * This method inspects the root operator of the given plan and, if it is
	 * neither a {@link LogicalOpJoin} nor a {@link LogicalOpMultiwayJoin}, it
	 * traverses all subplans to perform the same check.
	 * </p>
	 *
	 * @param logicalPlan the logical plan to inspect
	 * @return {@code true} if the plan contains at least one {@link LogicalOpJoin}
	 *         or {@link LogicalOpMultiwayJoin} operator, {@code false} otherwise
	 */
	protected static boolean containsJoinOp( final LogicalPlan logicalPlan ) {
		final LogicalOperator op = logicalPlan.getRootOperator();

		// Check if this op is a join
		if ( op instanceof LogicalOpJoin || op instanceof LogicalOpMultiwayJoin ) {
			return true;
		}

		// Recursively check subplans
		final int numChildren = logicalPlan.numberOfSubPlans();
		for ( int i = 0; i < numChildren; i++ ) {
			if ( containsJoinOp( logicalPlan.getSubPlan(i) ) ) {
				return true;
			}
		}
		return false;
    }
}
