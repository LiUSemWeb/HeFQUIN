package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.simple;

import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.*;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.PhysicalOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.CostModel;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.utils.PhysicalPlanWithCost;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.utils.PhysicalPlanWithCostUtils;
import se.liu.ida.hefquin.engine.utils.Pair;

import java.util.*;

public abstract class DPBasedJoinPlanOptimizer extends JoinPlanOptimizerBase {

    protected final CostModel costModel;

    public DPBasedJoinPlanOptimizer( final CostModel costModel ) {
        assert costModel != null;

        this.costModel = costModel;
    }

    @Override
    protected EnumerationAlgorithm initializeEnumerationAlgorithm( final List<PhysicalPlan> subplans ) {
        return new DynamicProgrammingOptimizerImpl(subplans);
    }

    protected class DynamicProgrammingOptimizerImpl implements EnumerationAlgorithm {
        protected final List<PhysicalPlan> subplans;

        public DynamicProgrammingOptimizerImpl( final List<PhysicalPlan> subplans ) {
            this.subplans = subplans;
        }

        @Override
        public PhysicalPlan getResultingPlan() throws PhysicalOptimizationException {

            // Create a data structure that will be used to store the optimal plan for each subset of (sub)plans.
            final DataStructureForStoringPlansOfSubsets optPlan = new DataStructureForStoringPlansOfSubsets();

            for ( final PhysicalPlan plan: subplans ){
                optPlan.add( new ArrayList<>( Arrays.asList(plan) ), plan );
            }

            for ( int num = 2; num < subplans.size()+1; num ++ ){
                // Get all subsets with size num.
                final List<List<PhysicalPlan>> subsets = getSubSet(subplans, num);
                for( final List<PhysicalPlan> plans : subsets ){
                    // Split the current set of subplans into two subsets, and create candidate plans with join for each of the combinations.
                    final List<Pair<List<PhysicalPlan>, List<PhysicalPlan>>> candidatePairs = splitIntoSubSets(plans);
                    final List<PhysicalPlan> candidatePlans = new ArrayList<>();
                    for ( final Pair<List<PhysicalPlan>, List<PhysicalPlan>> p: candidatePairs ) {
                        final PhysicalPlan plan_left = optPlan.get( p.object1 );
                        final PhysicalPlan plan_right = optPlan.get( p.object2 );

                        if( plan_left == null || plan_right == null ) {
                            throw new IllegalStateException( "No query plan is recorded for the subsets." );
                        }

                        candidatePlans.add( PhysicalPlanFactory.createPlanWithJoin( plan_left,  plan_right) );
                        final PhysicalOperator rightRootOp = plan_right.getRootOperator();
                        if ( rightRootOp instanceof PhysicalOpRequest ) {
                            candidatePlans.addAll( PhysicalPlanFactory.enumeratePlansWithUnaryOpFromReq((PhysicalOpRequest<?, ?>) rightRootOp, plan_left ) );
                        }
                        if ( rightRootOp instanceof PhysicalOpBinaryUnion || rightRootOp instanceof PhysicalOpMultiwayUnion ){
                            if ( PhysicalPlanFactory.checkUnaryOpApplicableToUnionPlan(plan_right) ) {
                                candidatePlans.add( PhysicalPlanFactory.createPlanWithUnaryOpForUnionPlan(plan_left, plan_right) );
                            }
                        }
                    }

                    // Prune: only the best candidate plan is retained in optPlan.
                    // TODO: Move the cost annotation out of this for-loop. For all plans of the same size, invoke the cost function once.
                    final List<PhysicalPlanWithCost> candidatesWithCost = PhysicalPlanWithCostUtils.annotatePlansWithCost(costModel, candidatePlans);
                    final PhysicalPlan planWithLowestCost = PhysicalPlanWithCostUtils.findPlanWithLowestCost(candidatesWithCost).getPlan();
                    optPlan.add( plans, planWithLowestCost );
                }
            }
            return optPlan.get( subplans );
        }

    }

    public abstract <T> List<Pair<List<T>, List<T>>> splitIntoSubSets( final List<T> superset );

    /**
     * This method returns all subsets (with the given size) of the given superset.
     */
    protected static <T> List<List<T>> getSubSet( final List<T> superset, final int n ){
        final List<List<T>> result = new ArrayList<>();
        if ( n < 1 || n > superset.size() ) {
            throw new IllegalArgumentException("Does not support to get subsets with less than one element or containing more than the total number of elements in the superset (length of subset: " + n + ").");
        }
        else if ( n == superset.size() ) {
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

                // Create a copy of the current subsets, then update the copy by adding an another element.
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
