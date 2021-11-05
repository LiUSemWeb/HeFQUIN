package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.DynamicProgramming;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.QueryOptimizationContext;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm.PhysicalPlanWithCost;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm.PhysicalPlanWithCostUtils;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.simple.JoinPlanOptimizerBase;

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

            final Map<List<PhysicalPlan>, PhysicalPlan> optPlan = new HashMap<>();
            for ( final PhysicalPlan plan: subplans ){
                optPlan.put( new ArrayList<>(), plan );
            }

            for ( int num = 2; num < subplans.size(); num ++ ){
                // Get all subsets with size num.
                final List<List<PhysicalPlan>> subsets = DPBasedOptimizerHelper.getSubSet(subplans, num);

                for( final List<PhysicalPlan> plans : subsets ){
                    final List<PhysicalPlan> candidatePlans = new ArrayList<>();

                    // Split the current set of subplans into two subsets, and create candidate plans with join for each of the combinations.
                    DPBasedOptimizerHelper.splitIntoSubSets(plans).forEach(l->{
                        final PhysicalPlan newPlan = PhysicalPlanFactory.createPlanWithJoin( optPlan.get(l.get(0)), optPlan.get(l.get(1)));
                        candidatePlans.add(newPlan);
                    });

                    // Prune: only the best candidate plan is retained in optPlan.
                    final List<PhysicalPlanWithCost> candidatesWithCost = PhysicalPlanWithCostUtils.annotatePlansWithCost(ctxt.getCostModel(), candidatePlans);
                    final PhysicalPlan planWithLowestCost = PhysicalPlanWithCostUtils.findPlanWithLowestCost(candidatesWithCost).getPlan();
                    optPlan.put( plans, planWithLowestCost );
                }
            }
            return optPlan.get( subplans );
        }

    }

}
