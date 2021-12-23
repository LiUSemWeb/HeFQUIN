package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.CostEstimationException;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.CostModel;

import java.util.ArrayList;
import java.util.List;

public class PhysicalPlanWithCostUtils {
    
    public static PhysicalPlanWithCost annotatePlanWithCost( final CostModel cm, final PhysicalPlan plan ) throws QueryOptimizationException {
        final Double[] costs;
        try {
            costs = CostEstimationUtils.getEstimates(cm, plan);
        } catch ( final CostEstimationException e ) {
            throw new QueryOptimizationException( "Determining the cost for the plan caused an exception.", e.getCause() );
        }

        return new PhysicalPlanWithCost( plan, costs[0] );
    }

    public static List<PhysicalPlanWithCost> annotatePlansWithCost(final CostModel cost, final List<PhysicalPlan> plans ) throws QueryOptimizationException {
        final Double[] costs;
        try {
            costs = CostEstimationUtils.getEstimates( cost, plans );
        } catch ( final CostEstimationException e ) {
            throw new QueryOptimizationException( "Determining the cost for plans caused an exception.", e.getCause() );
        }

        final List<PhysicalPlanWithCost> plansWithCost = new ArrayList<>( );
        for ( int i = 0; i < plans.size(); i++ ) {
            plansWithCost.add( new PhysicalPlanWithCost( plans.get(i), costs[i] ) );
        }

        return plansWithCost;
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

    public static PhysicalPlanWithCost findTopKPlanWithLowestCost( final List<PhysicalPlanWithCost> plansWithCost, final int k ) {
        final List<PhysicalPlanWithCost> plans = new ArrayList<>(plansWithCost);

        PhysicalPlanWithCost topKPlan = findPlanWithLowestCost(plans);
        for( int i = 1; i < k; i++ ) {
            plans.remove(topKPlan);
            topKPlan = findPlanWithLowestCost(plans);
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

}
