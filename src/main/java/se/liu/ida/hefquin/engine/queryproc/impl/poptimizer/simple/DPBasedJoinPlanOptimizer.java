package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.simple;

import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.*;
import se.liu.ida.hefquin.engine.queryplan.utils.ExpectedVariablesUtils;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.PhysicalOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.QueryOptimizationContext;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.utils.PhysicalPlanWithCost;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.utils.PhysicalPlanWithCostUtils;
import se.liu.ida.hefquin.engine.utils.Pair;

import java.util.*;

public abstract class DPBasedJoinPlanOptimizer extends JoinPlanOptimizerBase {

    protected final QueryOptimizationContext ctxt;

    public DPBasedJoinPlanOptimizer( final QueryOptimizationContext ctxt ) {
        assert ctxt != null;

        this.ctxt = ctxt;
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
            final DataStructureForStoringPlansOfSubsets optPlan = new DataStructureForStoringPlansOfSubsets();

            for ( final PhysicalPlan plan: subplans ){
                optPlan.add( new ArrayList<>( Arrays.asList(plan) ), plan );
            }

            boolean existsConnectedSubsetInSizeNum = true;
            for ( int num = 2; num < subplans.size()+1; num++ ) {
                // Get all subsets of size num of the set of subplans.
                final List<List<PhysicalPlan>> subsets = getAllSubSets(subplans, num);

                int countCandidatesWithSizeNum = 0;
                for( final List<PhysicalPlan> plans : subsets ){
                    // Split the current set of subplans into two (disjoint)
                    // subsets, in all possible ways; then, ...
                    final List<Pair<List<PhysicalPlan>, List<PhysicalPlan>>> pairs = splitIntoSubSets(plans);

                    // ... for such pair of subsets, create candidate join plans.
                    final List<PhysicalPlan> candidatePlans = new ArrayList<>();
                    for ( final Pair<List<PhysicalPlan>, List<PhysicalPlan>> pair : pairs ) {
                        // Get the optimal plan for each of the two subsets
                        // in the current pair, as was computed in an earlier
                        // iteration.
                        final PhysicalPlan plan_left = optPlan.get( pair.object1 );
                        final PhysicalPlan plan_right = optPlan.get( pair.object2 );

                        // If we don't have an optimal plan for any of the two
                        // subsets of the current pair, then ignore this pair.
                        // (I am not sure at the moment, how such a case may
                        // occur.  --Olaf)
                        if( plan_left == null || plan_right == null ) {
                            continue;
                        }

                        // 'existsConnectedSubsetInSizeNum' can be false when there exist independent parts in all subsets of size num (exist sub-queries that cannot be joined with any other sub-queries)
                        if ( existsConnectedSubsetInSizeNum ) {
                            // Only measure cost for queries that contain join variables
                            if ( ExpectedVariablesUtils.intersectionOfAllVariables(plan_left, plan_right).isEmpty() ) {
                                continue;
                            }
                        }

                        candidatePlans.add( PhysicalPlanFactory.createPlanWithJoin( plan_left,  plan_right) );
                        final PhysicalOperator rightRootOp = plan_right.getRootOperator();
                        if ( rightRootOp instanceof PhysicalOpRequest ) {
                            candidatePlans.addAll( PhysicalPlanFactory.enumeratePlansWithUnaryOpFromReq((PhysicalOpRequest<?, ?>) rightRootOp, plan_left ) );
                        }
                        if ( rightRootOp instanceof PhysicalOpRequestWithTranslation ) {
                            candidatePlans.addAll( PhysicalPlanFactory.enumeratePlansWithUnaryOpFromReq( (PhysicalOpRequestWithTranslation<?,?>) rightRootOp, plan_left ) );
                        }
                        if ( rightRootOp instanceof PhysicalOpBinaryUnion || rightRootOp instanceof PhysicalOpMultiwayUnion ){
                            if ( PhysicalPlanFactory.checkUnaryOpApplicableToUnionPlan(plan_right) ) {
                                candidatePlans.add( PhysicalPlanFactory.createPlanWithUnaryOpForUnionPlan(plan_left, plan_right) );
                            }
                        }
                    }

                    countCandidatesWithSizeNum += candidatePlans.size();
                    if ( candidatePlans.size() != 0 ) {
                        // Prune: only the best candidate plan is retained in optPlan.
                        // TODO: Move the cost annotation out of this for-loop. For all plans of the same size, invoke the cost function once.
                        final List<PhysicalPlanWithCost> candidatesWithCost = PhysicalPlanWithCostUtils.annotatePlansWithCost(ctxt.getCostModel(), candidatePlans);
                        final PhysicalPlan planWithLowestCost = PhysicalPlanWithCostUtils.findPlanWithLowestCost(candidatesWithCost).getPlan();
                        optPlan.add(plans, planWithLowestCost);
                    }
                }

                if( countCandidatesWithSizeNum == 0 ) {
                    // There exist independent parts for any subset (of size num). For a complete query plan, recreate candidate plans without considering join variables
                    existsConnectedSubsetInSizeNum = false;
                    num --;
                }
                else {
                    existsConnectedSubsetInSizeNum = true;
                }
            }
            return optPlan.get( subplans );
        }

    }

    public abstract <T> List<Pair<List<T>, List<T>>> splitIntoSubSets( final List<T> superset );

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
            for ( int j = 0; j < size; j++ ){
                // Stop adding more elements to the subset if its size is more than n.
                if( tempList.get(j).size() >= n ){
                    continue;
                }

                // Create a copy of the current subsets and extend this copy by adding an another element.
                final List<T> clone = new ArrayList<>( tempList.get(j) );
                clone.add( element );
                tempList.add(clone);

                // Add the subset to the result if its size is n.
                if( clone.size() == n ){
                    result.add(clone);
                }
            }
        }

        return result;
    }

}
