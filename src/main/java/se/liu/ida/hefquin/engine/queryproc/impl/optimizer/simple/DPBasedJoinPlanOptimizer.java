package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.simple;

import org.apache.commons.lang3.ArrayUtils;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.CostModel;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils.CostEstimationUtils;

import java.util.*;

public class DPBasedJoinPlanOptimizer extends JoinPlanOptimizerBase{

    protected final CostModel costModel;

    public DPBasedJoinPlanOptimizer( final CostModel costModel ) {
        assert costModel != null;
        this.costModel= costModel;
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
            final Map<PhysicalPlan[], PhysicalPlan> optPlan = new HashMap<>();
            for ( final PhysicalPlan plan: subplans ){
                final PhysicalPlan[] subset = {plan};
                optPlan.put( subset, plan);
            }

            for ( int nums = 2; nums < subplans.size(); nums ++ ){
                final List<PhysicalPlan[]> subsets = processSubsets(nums);

                for( final PhysicalPlan[] plans : subsets ){

                    final List<PhysicalPlan> candidatePlans = new ArrayList<>();
                    for ( int index = 0 ; index < plans.length; index ++ ) {
                        final PhysicalPlan[] remainingPlans = ArrayUtils.remove( plans, index );
                        final PhysicalPlan newPlan = PhysicalPlanFactory.createPlanWithJoin( plans[index], (PhysicalPlan) optPlan.get(remainingPlans));
                        candidatePlans.add(newPlan);
                    }

                    // prune
                    final Double[] costs = CostEstimationUtils.getEstimates(costModel, candidatePlans);

                    int indexOfBestPlan = 0;
                    for ( int t = 1; t < candidatePlans.size(); ++t ) {
                        if ( costs[indexOfBestPlan] > costs[t] ) {
                            indexOfBestPlan = t;
                        }
                    }
                    optPlan.put( plans, candidatePlans.get(indexOfBestPlan) );
                }
            }
            return (PhysicalPlan) optPlan.get(subplans);
        }

        protected List<PhysicalPlan[]> processSubsets( final int nums ) {
            final List<PhysicalPlan[]> subsets = new ArrayList<>();
            final PhysicalPlan[] subset = new PhysicalPlan[nums];

            for ( int index = 0; index < subplans.size()-nums; index ++ ) {
                subsets.add( processLargerSubsets( subset, 0, index ) );
            }
            return subsets;
        }

        protected PhysicalPlan[] processLargerSubsets( final PhysicalPlan[] subset, final int subsetSize, final int nextIndex ) {
            if  ( subsetSize < subset.length) {
                subset[subsetSize] = subplans.get(nextIndex);
                processLargerSubsets( subset, subsetSize + 1, nextIndex + 1 );
            }
            return subset;
        }

    }

}
