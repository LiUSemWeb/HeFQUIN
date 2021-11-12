package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.simple;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.access.BGPRequest;
import se.liu.ida.hefquin.engine.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalOpUtils;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.QueryOptimizationContext;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.rules.IdentifyTypeOfRequestUsedForReq;
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

            // Create a data structure that will be used to store the optimal plan for each subset of (sub)plans.
            final DataStructureForStoringPlansOfSubsets optPlan = new DataStructureForStoringPlansOfSubsets();

            for ( final PhysicalPlan plan: subplans ){
                optPlan.add( new ArrayList<>(), plan );
            }

            for ( int num = 2; num < subplans.size(); num ++ ){
                // Get all subsets with size num.
                final List<List<PhysicalPlan>> subsets = getSubSet(subplans, num);

                for( final List<PhysicalPlan> plans : subsets ){
                    final List<PhysicalPlan> candidatePlans = new ArrayList<>();

                    // Split the current set of subplans into two subsets, and create candidate plans with join for each of the combinations.
                    final List<Pair<List<PhysicalPlan>, List<PhysicalPlan>>> candidatePairs = splitIntoSubSets(plans);
                    for ( final Pair p: candidatePairs ) {
                        final PhysicalPlan plan_left = optPlan.get((List<PhysicalPlan>) p.object1);
                        final PhysicalPlan plan_right = optPlan.get((List<PhysicalPlan>) p.object2);

                        candidatePlans.add( PhysicalPlanFactory.createPlanWithJoin( plan_left,  plan_right) );

                        if ( IdentifyTypeOfRequestUsedForReq.isBGPRequest( plan_left.getRootOperator() ) ){
                            final LogicalOpBGPAdd newRoot = LogicalOpUtils.createBGPAddLopFromReq((BGPRequest) plan_left.getRootOperator());

                            candidatePlans.add( PhysicalPlanFactory.createPlanWithIndexNLJ( newRoot, plan_right ) );
                            candidatePlans.add( PhysicalPlanFactory.createPlanWithBindJoinFILTER( newRoot, plan_right ) );
                            candidatePlans.add( PhysicalPlanFactory.createPlanWithBindJoinUNION( newRoot, plan_right ) );
                            candidatePlans.add( PhysicalPlanFactory.createPlanWithBindJoinVALUES( newRoot, plan_right ) );
                        }
                        else if ( IdentifyTypeOfRequestUsedForReq.isTriplePatternRequest( plan_left.getRootOperator() ) ){
                            final LogicalOpTPAdd newRoot = LogicalOpUtils.createTPAddLopFromReq((TriplePatternRequest) plan_left.getRootOperator());
                            candidatePlans.add( PhysicalPlanFactory.createPlanWithIndexNLJ( newRoot, plan_right ) );

                            final FederationMember fm = ( (LogicalOpRequest<?, ?>)plan_left.getRootOperator() ).getFederationMember();
                            if ( fm instanceof SPARQLEndpoint ){
                                candidatePlans.add( PhysicalPlanFactory.createPlanWithBindJoinFILTER( newRoot, plan_right ) );
                                candidatePlans.add( PhysicalPlanFactory.createPlanWithBindJoinUNION( newRoot, plan_right ) );
                                candidatePlans.add( PhysicalPlanFactory.createPlanWithBindJoinVALUES( newRoot, plan_right ) );
                            }
                        }
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
