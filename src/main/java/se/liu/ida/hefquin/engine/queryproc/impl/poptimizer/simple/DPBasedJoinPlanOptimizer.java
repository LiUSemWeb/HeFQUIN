package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.simple;

import se.liu.ida.hefquin.engine.queryplan.logical.impl.*;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.*;
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
                    final List<PhysicalPlan> candidatePlans = new ArrayList<>();

                    // Split the current set of subplans into two subsets, and create candidate plans with join for each of the combinations.
                    final List<Pair<List<PhysicalPlan>, List<PhysicalPlan>>> candidatePairs = splitIntoSubSets(plans);
                    for ( final Pair<List<PhysicalPlan>, List<PhysicalPlan>> p: candidatePairs ) {
                        final PhysicalPlan plan_left = optPlan.get( p.object1 );
                        final PhysicalPlan plan_right = optPlan.get( p.object2 );

                        if( plan_left == null || plan_right == null ) {
                            throw new IllegalStateException( "No query plan is recorded for the subsets." );
                        }

                        candidatePlans.add( PhysicalPlanFactory.createPlanWithJoin( plan_left,  plan_right) );
                        candidatePlans.addAll( createPlanWithUnaryOp(plan_left, plan_right) );
                    }

                    // Prune: only the best candidate plan is retained in optPlan.
                    // TODO: Move the cost annotation out of this for-loop. For all plans of the same size, invoke the cost function once.
                    final List<PhysicalPlanWithCost> candidatesWithCost = PhysicalPlanWithCostUtils.annotatePlansWithCost(ctxt.getCostModel(), candidatePlans);
                    final PhysicalPlan planWithLowestCost = PhysicalPlanWithCostUtils.findPlanWithLowestCost(candidatesWithCost).getPlan();
                    optPlan.add( plans, planWithLowestCost );
                }
            }
            return optPlan.get( subplans );
        }

    }

    public abstract <T> List<Pair<List<T>, List<T>>> splitIntoSubSets( final List<T> superset );

    /**
     * In cases in which there is a union with requests or single request under right input (the first subquery of right input),
     * this function turns the requests into xxAdd operators with the previous join arguments as
     * subplans. Then use the rewritten subquery as input for the remaining subPlans of right input.
     **/
    public List<PhysicalPlan> createPlanWithUnaryOp( final PhysicalPlan left, final PhysicalPlan right ) {
        final List<PhysicalPlan> candidatePlans = new ArrayList<>();
        final PhysicalOperator rootOp = right.getRootOperator();
        if ( rootOp instanceof PhysicalOpRequest ) {
            return PhysicalPlanFactory.enumeratePlansWithUnaryOpFromReq((PhysicalOpRequest<?, ?>) rootOp, left );
        }
        if ( rootOp instanceof PhysicalOpRequestWithTranslation ) {
            return PhysicalPlanFactory.enumeratePlansWithUnaryOpFromReq( (PhysicalOpRequestWithTranslation<?,?>) rootOp, left );
        }

        if ( rootOp instanceof PhysicalOpBinaryUnion || rootOp instanceof PhysicalOpMultiwayUnion ){
            return enumeratePlansWithUnaryOpForUnionPlan( left, right );
        }

        if ( rootOp instanceof BasePhysicalOpBinaryJoin
                || rootOp instanceof BasePhysicalOpMultiwayJoin
                || rootOp instanceof BasePhysicalOpSingleInputJoin) {
            final int numberOfSubPlans = right.numberOfSubPlans();

            final List<List<PhysicalPlan>> rewrittenSubPlans = new ArrayList<>();
            for ( int i = 0; i < numberOfSubPlans; i++ ) {
                final PhysicalPlan subPlan = right.getSubPlan(i);
                if (i == 0) {
                    final List<PhysicalPlan> rewrittenSubPlan = createPlanWithUnaryOp( left, subPlan );
                    rewrittenSubPlans.add(rewrittenSubPlan);
                } else {
                    rewrittenSubPlans.add( Arrays.asList(subPlan) );
                }
            }

            final List<List<PhysicalPlan>> allPossibleCombSubPlans = getAllCombinations(rewrittenSubPlans);
            for (final List<PhysicalPlan> plans : allPossibleCombSubPlans) {
                candidatePlans.add(PhysicalPlanFactory.createPlan(rootOp, plans.toArray(new PhysicalPlan[0])));
            }

            return candidatePlans;
        }
        else
            throw new IllegalArgumentException("Unsupported type of subquery to apply UnaryOp (" + rootOp.getClass().getName() + ")");

    }

    protected List<PhysicalPlan> enumeratePlansWithUnaryOpForUnionPlan( final PhysicalPlan inputPlan, final PhysicalPlan unionPlan ) {
        final int numberOfSubPlansUnderUnion = unionPlan.numberOfSubPlans();
        final List<List<PhysicalPlan>> newSubPlansOfUnion = new ArrayList<>();

        for ( int i = 0; i < numberOfSubPlansUnderUnion; i++ ) {
            final PhysicalPlan oldSubPlan = unionPlan.getSubPlan(i);
            final List<PhysicalPlan> newSubPlans = new ArrayList<>();

            final PhysicalOperator oldSubPlanRootOp = oldSubPlan.getRootOperator();
            if ( oldSubPlanRootOp instanceof PhysicalOpRequest ) {
                final PhysicalOpRequest<?,?> reqOp = (PhysicalOpRequest<?,?>) oldSubPlanRootOp;
                newSubPlans.addAll( PhysicalPlanFactory.enumeratePlansWithUnaryOpFromReq( reqOp, inputPlan ) );
            }
            else if ( oldSubPlanRootOp instanceof PhysicalOpFilter
                    && oldSubPlan.getSubPlan(0).getRootOperator() instanceof PhysicalOpRequest ) {
                final PhysicalOpFilter filterOp = (PhysicalOpFilter) oldSubPlanRootOp;
                final PhysicalOpRequest<?,?> reqOp = (PhysicalOpRequest<?,?>) oldSubPlan.getSubPlan(0).getRootOperator();

                final List<PhysicalPlan> addOpPlans = PhysicalPlanFactory.enumeratePlansWithUnaryOpFromReq(reqOp, inputPlan );
                for ( final PhysicalPlan addOpPlan : addOpPlans ) {
                    newSubPlans.add( PhysicalPlanFactory.createPlan(filterOp, addOpPlan) );
                }
            }
            else {
                newSubPlans.add( PhysicalPlanFactory.createPlanWithJoin( inputPlan,  oldSubPlan) );
            }

            newSubPlansOfUnion.add( newSubPlans );
        }

        final List<List<PhysicalPlan>> allPossibleUnionSubPlans = getAllCombinations( newSubPlansOfUnion );
        final List<PhysicalPlan> output = new ArrayList<>();
        for ( List<PhysicalPlan> newUnionSubPlans : allPossibleUnionSubPlans ) {
            output.add( PhysicalPlanFactory.createPlan(LogicalOpMultiwayUnion.getInstance(), newUnionSubPlans) );
        }
        return output;
    }

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

    /**
     * Enumerate all combinations by picking one element from each sublist
     */
    protected static <T> List<List<T>> getAllCombinations( final List<List<T>> plans ) {
        final List<List<T>> combinations = new ArrayList<>();
        final int[] indices = new int[plans.size()];

        while (true) {
            final List<T> combination = new ArrayList<>();
            for ( int i = 0; i < indices.length; i++ ) {
                combination.add( plans.get(i).get(indices[i]) );
            }
            combinations.add(combination);

            // increment indices
            int j = indices.length - 1;
            while ( j >= 0 && indices[j] == plans.get(j).size() - 1 ) {
                indices[j] = 0;
                j--;
            }
            if (j < 0) {
                break;
            }
            indices[j]++;
        }

        return combinations;
    }

}
