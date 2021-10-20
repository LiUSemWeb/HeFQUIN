package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm;

import java.util.ArrayList;
import java.util.List;

public class PhysicalPlanWithCostUtils {

    public static PhysicalPlanWithCost findPlanWithLowestCost( final List<PhysicalPlanWithCost> plansWithCost ) {
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
        PhysicalPlanWithCost worstPlan = plansWithCost.get(0);
        double max = worstPlan.getWeight();

        for ( int i = 1; i < plansWithCost.size(); i++) {
            final PhysicalPlanWithCost p = plansWithCost.get(i);
            if ( p.getWeight() >= max ) {
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
            topKPlan = findPlanWithHighestCost(plans);
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
        double avgCost = calculateAvgCostOfPlans(plansWithCost);

        return calculateStDevCostOfPlans( plansWithCost, avgCost );
    }

    public static double calculateStDevCostOfPlans( final List<PhysicalPlanWithCost> plansWithCost, final double avgCost ) {
        double standardDeviation = 0.0;

        for( final PhysicalPlanWithCost plan: plansWithCost ) {
            standardDeviation += Math.pow(plan.getWeight() - avgCost, 2);
        }

        return Math.sqrt( standardDeviation/plansWithCost.size() );
    }

}
