package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.simple;

import java.util.*;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.base.utils.Pair;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.*;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanUtils;
import se.liu.ida.hefquin.engine.queryproc.PhysicalOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.CostModel;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.utils.PhysicalPlanWithCost;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.utils.PhysicalPlanWithCostUtils;

public abstract class DPBasedJoinPlanOptimizer extends JoinPlanOptimizerBase
{
	protected final CostModel costModel;

	public DPBasedJoinPlanOptimizer( final CostModel costModel ) {
		assert costModel != null;
		this.costModel = costModel;
	}

	@Override
	protected EnumerationAlgorithm initializeEnumerationAlgorithm( final List<PhysicalPlan> subplans ) {
		return new DynamicProgrammingOptimizerImpl(subplans);
	}


	protected class DynamicProgrammingOptimizerImpl implements EnumerationAlgorithm
	{
		protected final List<PhysicalPlan> subplans;

		public DynamicProgrammingOptimizerImpl( final List<PhysicalPlan> subplans ) {
			this.subplans = subplans;
		}

		@Override
		public PhysicalPlan getResultingPlan() throws PhysicalOptimizationException {
			// Create a data structure that will be used to store
			// the optimal plan for each subset of the (sub)plans.
			final OptimalPlansPerStage optmPlansPerStage = new OptimalPlansPerStage();

			for ( final PhysicalPlan plan: subplans ) {
				optmPlansPerStage.add( new ArrayList<>( Arrays.asList(plan) ), plan );
			}

			for ( int num = 2; num < subplans.size()+1; num++ ) {
				// Get all subsets of size num of the set of subplans.
				final List<List<PhysicalPlan>> subsets = getAllSubSets(subplans, num);

				final boolean atLeastOneFound = determineOptimalCandidatesAtStageN( subsets,
				                                                                    optmPlansPerStage,
				                                                                    true ); // ignoreCartesianProductJoins
				if ( ! atLeastOneFound ) {
					determineOptimalCandidatesAtStageN( subsets, optmPlansPerStage, false );
				}
			}

			return optmPlansPerStage.get( subplans );
		}

	}

	protected abstract <T> List<Pair<List<T>, List<T>>> splitIntoSubSets( final List<T> superset );

	/**
	 * This method returns all subsets (with the given size) of the given superset.
	 */
	protected static <T> List<List<T>> getAllSubSets( final List<T> superset, final int n ) {
		if ( n < 1 || n > superset.size() ) {
			throw new IllegalArgumentException("Does not support to get subsets with less than one element or containing more than the total number of elements in the superset (length of subset: " + n + ").");
		}

		final List<List<T>> result = new ArrayList<>();

		// If the request is for subsets of the same size as the superset
		// itself, then the superset is the only such subset.
		if ( n == superset.size() ) {
			result.add( superset );
			return result;
		}

		final List<List<T>> tempList = new ArrayList<>();
		tempList.add( new ArrayList<>() );

		for ( T element : superset ) {
			final int size = tempList.size();
			for ( int j = 0; j < size; j++ ) {
				// Stop adding more elements to the subset if its size is more than n.
				if( tempList.get(j).size() >= n ){
					continue;
				}

				// Create a copy of the current subsets and extend this copy by adding an another element.
				final List<T> clone = new ArrayList<>( tempList.get(j) );
				clone.add( element );
				tempList.add(clone);

				// Add the subset to the result if its size is n.
				if(  clone.size() == n ) {
					result.add(clone);
				}
			}
		}

		return result;
	}

	/**
	 * For each of the sets of plans in 'subsets', determines the best possible
	 * join plan and adds this best plan to 'optPlansPerStage'.
	 *
	 * @param subsets is the set of all sets of plans to be considered at this
	 *          stage, where all these sets are assumed to be of the same size
	 *
	 * @param optPlansPerStage contains the optimal join plans for every proper
	 *          subset of every set of plans in 'subsets', these optimal join
	 *          plans were determined in the earlier stages of the DP algorithm
	 *
	 * @param ignoreCartesianProductJoins is given as true if this function has
	 *          to ignore any joins that would produce cartesian products (i.e.,
	 *          joins between subplans that don't share any variable)
	 * 
	 * @return true if at least one possible join plan was added; the only case
	 *         in which this may be false is if 'ignoreCartesianProductJoins' is
	 *         true and none of the sets of plans in 'subsets' can be split into
	 *         two disjoint subsets that share a variable.
	 */
	protected boolean determineOptimalCandidatesAtStageN( final List<List<PhysicalPlan>> subsets,
	                                                      final OptimalPlansPerStage optPlansPerStage,
	                                                      final boolean ignoreCartesianProductJoins )
			throws PhysicalOptimizationException
	{
		boolean atLeastOnePlanFound = false;
		for ( final List<PhysicalPlan> plans : subsets ) {
			// Create all possible pairs of two disjoint subsets of the given set of subplans.
			final List<Pair<List<PhysicalPlan>, List<PhysicalPlan>>> pairs = splitIntoSubSets(plans);

			// For such pair of subsets, create candidate join plans.
			final List<PhysicalPlan> candidatePlans = new ArrayList<>();
			for ( final Pair<List<PhysicalPlan>, List<PhysicalPlan>> pair : pairs ) {
				// Get the optimal plan for each of the two subsets
				// in the current pair, as was computed in an earlier
				// iteration.
				final PhysicalPlan optmLeft  = optPlansPerStage.get( pair.object1 );
				final PhysicalPlan optmRight = optPlansPerStage.get( pair.object2 );

				// If we don't have an optimal plan for any of the two
				// subsets of the current pair, then ignore this pair.
				// (I am not sure at the moment, how such a case may
				// occur.  --Olaf)
				if( optmLeft == null || optmRight == null ) {
					continue;
				}

				if ( ignoreCartesianProductJoins ) {
					final Set<Var> joinVars = PhysicalPlanUtils.intersectionOfAllVariables(optmLeft, optmRight);
					if ( joinVars.isEmpty() ) {
						// Since the current two optimal plans share no
						// variables, the join between these two plans is
						// a cartesian product join, which we ignore in the
						// current invocation of this function.
						continue;
					}
				}

				candidatePlans.add( PhysicalPlanFactory.createPlanWithJoin(optmLeft,optmRight) );

				final PhysicalOperator rightRootOp = optmRight.getRootOperator();
				if ( rightRootOp instanceof PhysicalOpRequest ) {
					final PhysicalOpRequest<?,?> _rightRootOp = (PhysicalOpRequest<?,?>) rightRootOp;
					final List<PhysicalPlan> plansWithUnaryRoot = PhysicalPlanFactory.enumeratePlansWithUnaryOpFromReq(_rightRootOp, optmLeft);
					candidatePlans.addAll(plansWithUnaryRoot);
				}
				else if (    rightRootOp instanceof PhysicalOpBinaryUnion
				          || rightRootOp instanceof PhysicalOpMultiwayUnion ) {
					if ( PhysicalPlanFactory.checkUnaryOpApplicableToUnionPlan(optmRight) ) {
						final PhysicalPlan planWithUnariesUnderUnion = PhysicalPlanFactory.createPlanWithUnaryOpForUnionPlan(optmLeft, optmRight, null);
						candidatePlans.add(planWithUnariesUnderUnion);
					}
				}
			}

			if ( candidatePlans.size() > 0 ) {
				atLeastOnePlanFound = true;

				// Prune: we only need to keep the best plan for the current subset of subplans
				// TODO: Move the cost annotation out of this for-loop. For all plans of the same size, invoke the cost function once.
				final List<PhysicalPlanWithCost> candidatesWithCost = PhysicalPlanWithCostUtils.annotatePlansWithCost(costModel, candidatePlans);
				final PhysicalPlanWithCost planWithLowestCost = PhysicalPlanWithCostUtils.findPlanWithLowestCost(candidatesWithCost);
				optPlansPerStage.add( plans, planWithLowestCost.getPlan() );
			}
		}

		return atLeastOnePlanFound;
	}


	/*
	 * Data structure for storing the optimal plan for each subset of the plans.
	 */
	protected static class OptimalPlansPerStage
	{
		protected Map< Integer, Map<List<PhysicalPlan>, PhysicalPlan> > map = new HashMap<>();

		public void add( final List<PhysicalPlan> subsets, final PhysicalPlan plan ) {
			final int size = subsets.size();
			final Map<List<PhysicalPlan>, PhysicalPlan> mapValue = map.get(size);

			if ( mapValue == null) {
				final Map<List<PhysicalPlan>, PhysicalPlan> mapTemp = new HashMap<>();
				mapTemp.put(subsets, plan);
				map.put(size, mapTemp);
			}
			else
				mapValue.put(subsets, plan);
		}

		public PhysicalPlan get( final List<PhysicalPlan> subsets ) {
			final Map<List<PhysicalPlan>, PhysicalPlan> mapValue = map.get( subsets.size() );
			return ( mapValue == null) ? null : mapValue.get(subsets);
		}
	}

}
