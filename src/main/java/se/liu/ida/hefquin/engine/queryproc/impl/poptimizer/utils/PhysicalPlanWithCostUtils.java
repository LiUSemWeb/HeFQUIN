package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.utils;

import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.PhysicalOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.CostEstimationException;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.CostModel;

import java.util.ArrayList;
import java.util.List;

public class PhysicalPlanWithCostUtils {
    
    public static PhysicalPlanWithCost annotatePlanWithCost( final CostModel cm, final PhysicalPlan plan ) throws PhysicalOptimizationException {
        final Double[] costs;
        try {
            costs = CostEstimationUtils.getEstimates(cm, plan);
        } catch ( final CostEstimationException e ) {
            throw new PhysicalOptimizationException( "Determining the cost for the plan caused an exception.", e.getCause() );
        }

        return new PhysicalPlanWithCost( plan, costs[0] );
    }

    public static List<PhysicalPlanWithCost> annotatePlansWithCost(final CostModel cost, final List<PhysicalPlan> plans ) throws PhysicalOptimizationException {
        final Double[] costs;
        try {
            costs = CostEstimationUtils.getEstimates( cost, plans );
        } catch ( final CostEstimationException e ) {
            throw new PhysicalOptimizationException( "Determining the cost for plans caused an exception.", e.getCause() );
        }

        final List<PhysicalPlanWithCost> plansWithCost = new ArrayList<>( );
        for ( int i = 0; i < plans.size(); i++ ) {
            plansWithCost.add( new PhysicalPlanWithCost( plans.get(i), costs[i] ) );
        }

        return plansWithCost;
    }

    public static <T> List<List<T>> slicePlans( final List<T> plans, final int batchSize ) {
        final List<List<T>> planBatches = new ArrayList<>();
        if (plans ==null || plans.size() == 0) {
            return planBatches;
        }

        int from = 0, to = 0, slicedItems = 0;
        while ( slicedItems < plans.size() ) {
            to = from + Math.min(batchSize, plans.size() - to);
            final List<T> slice = plans.subList(from, to);
            planBatches.add(slice);
            slicedItems += slice.size();
            from = to;
        }
        return planBatches;
    }

    public static PhysicalPlanWithCost findPlanWithLowestCost( final List<PhysicalPlanWithCost> plansWithCost ) {
        if ( plansWithCost.size() == 0 ) {
            throw new IllegalArgumentException( "Cannot find the plan with lowest cost from an empty set" );
        }

        PhysicalPlanWithCost bestPlan = plansWithCost.get(0);
        double min = bestPlan.getWeight();

        for ( int i = 1; i < plansWithCost.size(); i++) {
            final PhysicalPlanWithCost p = plansWithCost.get(i);
            if ( p.getWeight() <= min ) {
                min = p.getWeight();
                bestPlan = p;
            }
        }
        return bestPlan;
    }

    public static PhysicalPlanWithCost findPlanWithHighestCost( final List<PhysicalPlanWithCost> plansWithCost ) {
        if ( plansWithCost.size() == 0 ) {
            throw new IllegalArgumentException( "Cannot find the plan with highest cost from an empty set" );
        }

        PhysicalPlanWithCost worstPlan = plansWithCost.get(0);
        double max = worstPlan.getWeight();

        for ( int i = 1; i < plansWithCost.size(); i++) {
            final PhysicalPlanWithCost p = plansWithCost.get(i);
            if ( p.getWeight() > max ) {
                max = p.getWeight();
                worstPlan = p;
            }
        }
        return worstPlan;
    }

    public static PhysicalPlanWithCost findTopKPlanWithLowestCost( final List<PhysicalPlanWithCost> plansWithCost, final double p ) {
        final List<PhysicalPlanWithCost> plans = new ArrayList<>(plansWithCost);
        int k = Math.max(1, (int) ( plansWithCost.size() * p));

        PhysicalPlanWithCost topKPlan = findPlanWithLowestCost(plans);
        plans.remove(topKPlan);
        int i = 1;
        while( i < k && plans.size() != 0 ) {
            i++;
            topKPlan = findPlanWithLowestCost(plans);
            plans.remove(topKPlan);
        }

        return topKPlan;
    }

    public static double calculateAvgCostOfPlans( final List<PhysicalPlanWithCost> plansWithCost ) {
        double sum = 0;

        for ( final PhysicalPlanWithCost p : plansWithCost ) {
            sum += p.getWeight();
        }

        return sum/plansWithCost.size();
    }

    public static double calculateStDevCostOfPlans( final List<PhysicalPlanWithCost> plansWithCost ) {
        final double avgCost = calculateAvgCostOfPlans(plansWithCost);

        return calculateStDevCostOfPlans( plansWithCost, avgCost );
    }

    public static double calculateStDevCostOfPlans( final List<PhysicalPlanWithCost> plansWithCost, final double avgCost ) {
        double standardDeviation = 0.0;

        for( final PhysicalPlanWithCost plan: plansWithCost ) {
            standardDeviation += Math.pow( plan.getWeight() - avgCost, 2 );
        }

        return Math.sqrt( standardDeviation/plansWithCost.size() );
    }

    public static int countNrOfPlansWithBestCost( final List<PhysicalPlanWithCost> plansWithCost, final double bestCost ) {
        int count = 0;
        for( final PhysicalPlanWithCost plan: plansWithCost ) {
            if ( plan.getWeight() == bestCost ) {
                count += 1;
            }
        }

        return count;
    }

}
