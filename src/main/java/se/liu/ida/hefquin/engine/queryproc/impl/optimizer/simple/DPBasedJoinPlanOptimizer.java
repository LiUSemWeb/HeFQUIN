package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.simple;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.QueryOptimizationContext;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils.PhysicalPlanWithCost;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils.PhysicalPlanWithCostUtils;
import se.liu.ida.hefquin.engine.utils.Pair;

import java.util.*;

public class DPBasedJoinPlanOptimizer extends JoinPlanOptimizerBase {

    protected final QueryOptimizationContext ctxt;

    public DPBasedJoinPlanOptimizer( final QueryOptimizationContext ctxt ) {
        assert ctxt != null;

        this.ctxt = ctxt;
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
        public PhysicalPlan getResultingPlan() throws QueryOptimizationException {

            // Create a map that will be used to store the optimal plan for each subset of (sub)plans.
            final Map<List<PhysicalPlan>, PhysicalPlan> optPlan = new HashMap<>();
            for ( final PhysicalPlan plan: subplans ){
                optPlan.put( new ArrayList<>(), plan );
            }

            for ( int num = 2; num < subplans.size(); num ++ ){
                // Get all subsets with size num.
                final List<List<PhysicalPlan>> subsets = getSubSet(subplans, num);

                for( final List<PhysicalPlan> plans : subsets ){
                    final List<PhysicalPlan> candidatePlans = new ArrayList<>();

                    // Split the current set of subplans into two subsets, and create candidate plans with join for each of the combinations.
                    final List<Pair<List<PhysicalPlan>, List<PhysicalPlan>>> candidatePairs = splitIntoSubSets(plans);
                    for ( Pair p: candidatePairs ){
                        final PhysicalPlan newPlan = PhysicalPlanFactory.createPlanWithJoin( optPlan.get(p.object1), optPlan.get(p.object2) );
                        candidatePlans.add(newPlan);
                    }

                    // Prune: only the best candidate plan is retained in optPlan.
                    final List<PhysicalPlanWithCost> candidatesWithCost = PhysicalPlanWithCostUtils.annotatePlansWithCost(ctxt.getCostModel(), candidatePlans);
                    final PhysicalPlan planWithLowestCost = PhysicalPlanWithCostUtils.findPlanWithLowestCost(candidatesWithCost).getPlan();
                    optPlan.put( plans, planWithLowestCost );
                }
            }
            return optPlan.get( subplans );
        }

    }

    // This method returns all subsets (with the given size) of the given superset.
    public static <T> List<List<T>> getSubSet( final List<T> superset, final int n ){
        final List<List<T>> result = new ArrayList<>();
        if( n==0 ){
            result.add( new ArrayList<>() );
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

    // Split a superset into two subsets. This method returns all possible combinations of subsets.
    public static <T> List<Pair<List<T>, List<T>>> splitIntoSubSets( final List<T> superset ){
        final List<List<T>> left = new ArrayList<>();
        final List<List<T>> right = new ArrayList<>();
        final List< Pair<List<T>, List<T>> > result = new ArrayList<>();

        left.add( new ArrayList<>() );
        right.add( new ArrayList<>(superset) );

        for ( T element : superset ) {
            final int leftSize = left.size();
            for ( int j = 0; j < leftSize; j++ ){
                final List<T> leftClone = new ArrayList<>( left.get(j) );
                leftClone.add( element );
                left.add( leftClone );

                final List<T> rightClone = new ArrayList<>( right.get(j) );
                rightClone.remove( element );
                right.add( rightClone );

                if ( leftClone.size() != 0 && rightClone.size() != 0 )
                    result.add( new Pair( leftClone, rightClone ) );

            }
        }
        return result;
    }

}
